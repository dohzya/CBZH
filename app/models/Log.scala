package models

import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.Macros.Annotations.Key

case class Log(
  @Key("_id") oid: BSONObjectID,
  playerId: BSONObjectID,
  diff: Int,
  msg: String,
  by: Option[String],
  date: DateTime
)
object Log {
  def create(player: Player, diff: Int, msg: String, by: Option[String]) = {
    Log(
      oid = BSONObjectID.generate,
      playerId = player.oid,
      diff = diff,
      msg = msg,
      by = by,
      date = DateTime.now
    )
  }
}
