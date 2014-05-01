package formats

import play.api.libs.json._
import reactivemongo.bson.BSONObjectID

import models.{ App, Log, Player }

object Logs {

  import formats.Apps._
  import formats.Players._

  def jsonLogWrites(players: Map[BSONObjectID, Player], apps: Map[String, App]) = new Writes[Log] {
    def writes(log: Log) = Json.obj(
      "_id" -> log.id,
      "player" -> players.get(log.playerId),
      "diff" -> log.diff,
      "oldPoints" -> log.oldPoints,
      "newPoints" -> log.newPoints,
      "msg" -> log.msg,
      "app" -> log.appId.flatMap(apps.get(_)),
      "date" -> log.date
    )
  }

}
