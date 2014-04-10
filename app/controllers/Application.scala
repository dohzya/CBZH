package controllers

import scala.concurrent.ExecutionContext.Implicits.global

import play.api._
import play.api.mvc._

import engine.Players

object Application extends Controller {

  def index = Action.async { req =>
    Players.top10.map { top10 =>
      Ok(views.html.index(top10))
    }
  }

}
