package engine

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollectionProducer
import reactivemongo.bson._

import play.api.Play.current

import models.{ Profile, Player }

object Players {

  def db = ReactiveMongoPlugin.db

  def collection = db("players")(BSONCollectionProducer)

  def empty: Future[Boolean] = {
    collection.find(BSONDocument()).one[BSONDocument].map(_.isEmpty)
  }

  def insert(player: Player): Future[Player] = {
    collection.insert(player).map(_ => player)
  }

  def top10: Future[Seq[Player]] = {
    collection.find(BSONDocument())
              .sort(BSONDocument("points" -> -1))
              .cursor[Player]
              .collect[Seq](upTo = 10)
  }

  def fromProfile(profile: Profile): Future[Player] = {
    collection.find(BSONDocument("profile.id" -> profile.id)).one[Player].flatMap {
      case Some(player) => updateProfile(player, profile)
      case None => insert(Player.create(profile))
    }
  }

  def updateProfile(player: Player, profile: Profile): Future[Player] = {
    collection.update(
      BSONDocument("_id" -> player.oid),
      BSONDocument("$set" -> BSONDocument("profile" -> profile))
    ).map(_ => player.copy(profile = profile))
  }

  implicit val profileHandler = Macros.handler[Profile]
  implicit val playerHandler = Macros.handler[Player]

}
