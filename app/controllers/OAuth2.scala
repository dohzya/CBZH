package controllers

import java.net.URLEncoder

import org.joda.time.DateTime

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util._

import play.api._
import play.api.mvc._
import play.api.libs.iteratee.Done
import play.api.Play.current
import play.api.libs.iteratee._

import play.api.libs.json._
import play.api.libs.functional.syntax._

import play.api.libs.ws._

import models.Player
import engine.Players

trait OAuth2 {
  this: Controller =>

  case class AuthenticatedRequest[A](request: Request[A], player: Player) extends WrappedRequest(request)

  implicit def me(implicit req: AuthenticatedRequest[_]) = req.player

  object Authenticated extends ActionBuilder[AuthenticatedRequest] {
    def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[SimpleResult]) = {
      OAuth2.TokenResponse.fromSession(request.session) match {
        case Some(token) =>
          OAuth2.getProfile(token).flatMap {
            case Success(profile) => profile.toPlayer.flatMap { player =>
              block(AuthenticatedRequest(request, player))
            }
            case Failure(OAuth2.OAuthErrorResult(errorResult)) => Future.successful { errorResult }
            case Failure(error) => Future.successful { InternalServerError("Internal Error") }
          }
        case None => Future.successful { Redirect(OAuth2.connectionURL) }
      }
    }
  }

}

object OAuth2 extends Controller {
  import play.api.data._
  import play.api.data.Forms._

  case class OAuth2Token(value: String)

  trait OauthError extends Throwable
  case class OAuthErrorJson(content: JsValue) extends OauthError
  case class OAuthErrorResult(content: SimpleResult) extends OauthError

  private def loadConf(key: String) = {
    current.configuration.getString(key).getOrElse {
      throw new java.lang.RuntimeException(s"Missing conf $key")
    }
  }
  lazy val AuthProviderX509CertUrl = loadConf("oauth2.authproviderx509certurl")
  lazy val AuthURI = loadConf("oauth2.authuri")
  lazy val ClientEmail = loadConf("oauth2.clientemail")
  lazy val ClientId = loadConf("oauth2.clientid")
  lazy val ClientSecret = loadConf("oauth2.clientsecret")
  lazy val ClientX509CertUrl = loadConf("oauth2.clientx509certurl")
  lazy val JavascriptOrigin = loadConf("oauth2.javascriptorigin")
  lazy val RedirectURI = loadConf("oauth2.redirecturi")
  lazy val TokenURI = loadConf("oauth2.tokenuri")

  val scopes = Seq(
    "https://www.googleapis.com/auth/userinfo.email",
    "https://www.googleapis.com/auth/userinfo.profile"
  )
  val scope = scopes.mkString(" ")

  def urlEncode(str: String) = URLEncoder.encode(str, "UTF-8")

  def connectionURL: String = connectionURL(Some("1"))
  def connectionURL(state: Option[String]): String = {
    AuthURI + "?" + Seq(
      "scope" -> Some(urlEncode(scope)),
      "state" -> state,
      "redirect_uri" -> Some(urlEncode(RedirectURI)),
      "response_type" -> Some("code"),
      "client_id" -> Some(ClientId)
    ).collect{ case (k,Some(v)) => k + "=" + v }.mkString("&")
  }
  def accessTokenBody(code: String) = {
    Map(
      "code" -> Some(code),
      "client_id" -> Some(ClientId),
      "client_secret" -> Some(ClientSecret),
      "redirect_uri" -> Some(RedirectURI),
      "grant_type" -> Some("authorization_code")
    ).collect{ case (k,Some(v)) => (k, Seq(v)) }
  }

  def login = Action { implicit request =>
    Redirect(connectionURL)
  }

  val logoutForm = Form(single("redirectURI" -> nonEmptyText))
  def logout = Action { implicit request =>
    val url = logoutForm.bindFromRequest.value.getOrElse(routes.Application.index.toString)
    Redirect(url).withNewSession
  }

  val oauth2callbackForm = Form(tuple(
    "code" -> optional(text),
    "state" -> optional(text),
    "error" -> optional(text)
  ))
  def oauth2callback = Action.async { implicit request =>
    oauth2callbackForm.bindFromRequest.fold(
      err => Future.successful { BadRequest("invalid request") },
      {
        case (_, _, Some(error)) =>
          Logger("oauth2.callback").debug(s"Received error: $error")
          Future.successful {
            if (error == Some("access_denied")) Unauthorized("Access denied")
            else BadRequest(error)
          }
        case (Some(code), state, _) =>
          Logger("oauth2.callback").debug(s"Received code (code: $code, state: $state)")
            getToken(code).flatMap {
              case Success(token) =>
                Logger("oauth2.callback").debug(s"Token: $token")
                getProfile(token).flatMap {
                  case Success(profile) => profile.toPlayer.map { player =>
                    val target = routes.Application.index
                    Redirect(target).withSession(
                      "accessToken" -> token.accessToken.value,
                      "expiresIn" -> token.expiresIn.getMillis.toString
                    )
                  }
                  case Failure(OAuthErrorResult(errorResult)) => Action(errorResult)(request)
                  case Failure(error) =>
                    Logger("oauth2.callback").error("Received error", error)
                    Action(InternalServerError("Internal Error"))(request)
                }
              case Failure(_) =>
                if (state == "3") {
                  Action(InternalServerError("Can't authenticate you (sorry)"))(request)
                }
                else {
                  val rx = """^(\\d+)$""".r
                  Logger("oauth2.callback").warn("No token received")
                  val s = state match {
                    case rx(n) =>
                      val next = n.toInt + 1
                      Some(next.toString)
                    case _ => None
                  }
                  Future.successful { Redirect(connectionURL(s)).withNewSession }
                }
          }
        case other =>
          Logger("oauth2.callback").debug(s"Received bad request: $other")
          Future.successful { BadRequest("Bad parameters") }
      }
    )
  }

  case class TokenResponse(
    accessToken: OAuth2Token,
    expiresIn: DateTime//,
    //token_type: String,
    //id_token: Option[String]
  )
  object TokenResponse {
    def fromSession(implicit session: Session) = {
      for {
        accessToken <- session.get("accessToken")
        expiresIn <- session.get("expiresIn").map(_.toLong)
        if expiresIn > DateTime.now.getMillis
      } yield {
        TokenResponse(
          accessToken = OAuth2Token(accessToken),
          expiresIn = new DateTime(expiresIn)
        )
      }
    }
  }
  def useRefreshToken(token: String): Future[Try[TokenResponse]] = {
    Logger("oauth2.token").debug("POST" + Map("client_id" -> Seq(ClientId),
      "client_secret" -> Seq(ClientSecret),
      "refresh_token" -> Seq(token),
      "grant_type" -> Seq("refresh_token")))
    WS.url(TokenURI).post(Map(
      "client_id" -> Seq(ClientId),
      "client_secret" -> Seq(ClientSecret),
      "refresh_token" -> Seq(token),
      "grant_type" -> Seq("refresh_token")
    )).map(readToken)
  }
  def getToken(code: String): Future[Try[TokenResponse]] = {
    WS.url(TokenURI).post(accessTokenBody(code)).map(readToken)
  }
  def readToken(wsResponse: Response): Try[TokenResponse] = {
    implicit object TokenReads extends Reads[TokenResponse] {
      def reads(json: JsValue) = JsSuccess(TokenResponse(
        accessToken = OAuth2Token((json \ "access_token").as[String]),
        expiresIn = {
          val now = DateTime.now
          val TTL = (json \ "expires_in").as[Long]
          new DateTime(now.getMillis + TTL * 1000)
        }
      ))
    }
    val json = wsResponse.json
    Logger("oauth2.getToken").debug(s"Received: $json")
    if (wsResponse.status == 200 && (json \ "error").asOpt[String].isEmpty) {
      val tokenResp = json.as[TokenResponse]
      Logger("oauth2.getToken").debug(s"Received token: $tokenResp")
      Success(tokenResp)
    }
    else {
      Logger("oauth2.getToken").debug(s"Can't received token: ${wsResponse.body}")
      Failure(OAuthErrorResult(BadRequest("Token can't be retrieved")))
    }
  }

  case class Profile(
    id: String,
    email: String,
    verified_email: Boolean,
    name: String,
    given_name: String,
    family_name: String,
    link: String,
    picture: Option[String],
    gender: String,
    birthday: Option[String],
    locale: Option[String]
  ) {
    def toPlayer: Future[Player] = {
      Players.fromProfile(models.Profile(
        id = id,
        email = email,
        verifiedEmail = verified_email,
        name = name,
        givenName = given_name,
        familyName = family_name,
        link = link,
        picture = picture,
        gender = gender,
        birthday = birthday,
        locale = locale
      ))
    }
  }
  implicit val profileFormat = Json.format[Profile]
  def getProfile(token: TokenResponse): Future[Try[Profile]] = getProfile(token.accessToken)
  def getProfile(token: OAuth2Token): Future[Try[Profile]] = {
    WS.url(s"https://www.googleapis.com/oauth2/v1/userinfo?access_token=${token.value}").get().map { wsResponse =>
        val json = wsResponse.json
        if (wsResponse.status == 200) {
          Logger("oauth2.getProfile").debug(s"Received profile: $json")
          Success(json.as[Profile])
        }
        else {
          Logger("oauth2.getProfile").debug(s"Can't receive profile: $json")
          Failure(OAuthErrorJson(json))
        }
      }
  }

}
