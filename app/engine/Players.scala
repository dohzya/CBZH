package engine

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollectionProducer
import reactivemongo.bson._

import play.api.Play.current

import models.{ Log, Profile, Player }

object Players {

  def db = ReactiveMongoPlugin.db

  def collection = db("players")(BSONCollectionProducer)

  def empty: Future[Boolean] = {
    collection.find(BSONDocument()).one[BSONDocument].map(_.isEmpty)
  }

  private def insert(player: Player): Future[Player] = {
    for {
      _ <- collection.insert(player)
      _ <- Logs.insert(Log.create(player, 0, "entered in the game!", None))
    } yield player
  }

  def top10: Future[Seq[Player]] = {
    collection.find(BSONDocument())
              .sort(BSONDocument("points" -> -1))
              .cursor[Player]
              .collect[Seq](upTo = 10)
  }

  def findAllById(ids: Seq[BSONObjectID]): Future[Seq[Player]] = {
    collection.find(BSONDocument("_id" -> BSONDocument("$in" -> ids)))
              .cursor[Player]
              .collect[Seq]()
      .map { players =>
        play.api.Logger.debug(s"players($ids): $players")
        players
      }
  }

  def fromProfile(profile: Profile): Future[Player] = {
    collection.find(BSONDocument("profile.id" -> profile.id)).one[Player].flatMap {
      case Some(player) => updateProfile(player, profile)
      case None => insert(Player.create(profile))
    }
  }

  private def updateProfile(player: Player, profile: Profile): Future[Player] = {
    collection.update(
      BSONDocument("_id" -> player.oid),
      BSONDocument("$set" -> BSONDocument("profile" -> profile))
    ).map(_ => player.copy(profile = profile))
  }

  def updatePoints(player: Player, log: Log): Future[Player] = {
    collection.update(
      BSONDocument("_id" -> player.oid),
      BSONDocument("$inc" -> BSONDocument("points" -> log.diff))
    ).flatMap { _ =>
      Logs.insert(log).flatMap { _ =>
        collection.find(
          BSONDocument("_id" -> player.oid),
          BSONDocument("points" -> 1)
        ).one[BSONDocument].map { doc =>
          val points = doc.flatMap(_.getAs[Int]("points")).getOrElse {
            throw new java.lang.RuntimeException(s"Can't find player ${player.oid}'s ‘point’ field!")
          }
          player.copy(points = points)
        }
      }
    }
  }

  implicit val profileHandler = Macros.handler[Profile]
  implicit val playerHandler = Macros.handler[Player]

}
