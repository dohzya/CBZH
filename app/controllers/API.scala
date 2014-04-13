package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.Play.current

import models.Log
import engine.Players

object API extends Controller {

  def getPoint(email: String) = Action.async { implicit req =>
    Players.findByEmail(email).map {
      case Some(player) => Ok(Json.obj(
        "email" -> email,
        "points" -> player.points
      ))
      case None => NotFound(Json.obj("error" -> s"Player $email not found"))
    }
  }

  def update(email: String, app: String, token: String) = Action.async(parse.json) { implicit req =>
    Play.configuration.getString(s"app.$app.token") match {
      case Some(`token`) =>
        val reader = ((__ \ "diff").read[Int] ~ (__ \ "msg").read[String]).tupled
        reader.reads(req.body).fold(
          err => Future.successful {BadRequest(Json.obj("error" -> "Bad request")) },
          { case (diff, msg) =>
            Players.findByEmail(email).flatMap {
              case Some(player) =>
                val log = Log.create(player, diff, msg, Some(app))
                Players.updatePoints(player, log).map { player =>
                  Ok(Json.obj(
                    "email" -> email,
                    "points" -> player.points
                  ))
                }
              case None => Future.successful {
                NotFound(Json.obj("error" -> s"Player $email not found"))
              }
            }
          }
        )
      case _ => Future.successful { NotFound(Json.obj("error" -> "Application now found")) }
    }
  }

}
