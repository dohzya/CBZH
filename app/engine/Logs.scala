package engine

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import org.joda.time._

import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollectionProducer
import reactivemongo.bson._

import play.api.Play.current
import play.api.Logger

import engine.Apps
import models.{ App, Log, Player }

object Logs {

  def db = ReactiveMongoPlugin.db

  def collection = db("logs")(BSONCollectionProducer)

  def insert(log: Log): Future[Log] = {
    collection.insert(log).map(_ => log)
  }

  def all: Future[Seq[Log]] = {
    collection.find(BSONDocument())
              .sort(BSONDocument("date" -> -1))
              .cursor[Log]
              .collect[Seq]()
  }

  def allWithPlayers: Future[(Seq[Log], Map[String, App], Map[BSONObjectID, Player])] = {
    all.flatMap { logs =>
      val appIds = logs.map(_.appId).flatten.distinct
      val playerIds = logs.map(_.playerId).distinct
      (
        Apps.findAllById(appIds) zip Players.findAllById(playerIds)
      ).map { case (apps, players) =>
        val appsMap = apps.map(a => a.id -> a).toMap
        val playersMap = players.map(p => p.oid -> p).toMap
        (logs, appsMap, playersMap)
      }
    }
  }

  implicit val logHandler = Macros.handler[Log]

}
