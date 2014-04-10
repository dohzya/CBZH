package models

import reactivemongo.bson.BSONObjectID

case class Player(
  oid: BSONObjectID,
  trigram: String,
  name: String,
  points: Int
) {
  def id = oid.stringify
}
object Player {
  def create(
    trigram: String,
    name: String,
    points: Int
  ) = Player(
    oid = BSONObjectID.generate,
    trigram = trigram,
    name = name,
    points = points
  )
}
