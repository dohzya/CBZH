package engine

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.Play.current

import models.App

object Apps {

  // For now apps are in config file, but it's easy to put them in DB

  def checkToken(key: String, token: String): Future[Option[App]] = {
    findByKey(key).map {
      case res@Some(app) if app.token == token => res
      case _ => None
    }
  }

  def findByKey(key: String): Future[Option[App]] = {
    Future.successful {
      for {
        conf <- current.configuration.getConfig(s"app.$key")
        token <- conf.getString("token")
      } yield {
        val name = conf.getString("name")
        val url = conf.getString("url")
        App(key, name, url, token)
      }
    }
  }

  def findById(id: String): Future[Option[App]] = findByKey(id)

  def findAllById(ids: Seq[String]): Future[Seq[App]] = {
    Future.sequence(ids.distinct.map(findById)).map(_.flatten)
  }

}
