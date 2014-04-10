package engine

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.modules.reactivemongo.ReactiveMongoPlugin
import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollectionProducer
import reactivemongo.bson._

import play.api.Play.current

import models.Player

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

  implicit val playerHandler = Macros.handler[Player]

}
