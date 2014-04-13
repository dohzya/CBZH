package controllers

import scala.concurrent.ExecutionContext.Implicits.global

import play.api._
import play.api.mvc._

import engine.{ Logs, Players }

object Application extends Controller with OAuth2 {

  def index = Authenticated.async { implicit req =>
    Players.top10.map { top10 =>
      Ok(views.html.index(top10))
    }
  }

  def logs = Authenticated.async { implicit req =>
    Logs.allWithPlayers.map { case (logs, apps, players) =>
      Ok(views.html.logs(logs, apps, players))
    }
  }

}
