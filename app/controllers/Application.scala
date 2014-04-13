package controllers

import scala.concurrent.ExecutionContext.Implicits.global

import play.api._
import play.api.mvc._

import engine.Players

object Application extends Controller with Authentication {

  def index = Authenticated.async { implicit req =>
    Players.top10.map { top10 =>
      Ok(views.html.index(top10))
    }
  }

}
