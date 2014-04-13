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

import models.{ Log, Player }

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

  def allWithPlayers: Future[(Seq[Log], Map[BSONObjectID, Player])] = {
    all.flatMap { logs =>
      val playerIds = logs.map(_.playerId).distinct
      Players.findAllById(playerIds).map { players =>
        logs -> players.map(p => p.oid -> p).toMap
      }
    }
  }

  implicit val logHandler = Macros.handler[Log]

}
