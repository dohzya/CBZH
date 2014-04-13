package controllers

import scala.concurrent.ExecutionContext.Implicits.global

import play.api._
import play.api.mvc._
import play.api.libs.json._

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

}
