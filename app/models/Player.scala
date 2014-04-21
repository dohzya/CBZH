package models

import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.Macros.Annotations.Key

case class Profile(
  id: String,
  email: String,
  verifiedEmail: Boolean,
  name: String,
  givenName: String,
  familyName: String,
  link: String,
  picture: Option[String] = None,
  gender: String,
  birthday: Option[String] = None,
  locale: Option[String] = None
)
case class Player(
  @Key("_id") oid: BSONObjectID,
  trigram: String,
  profile: Profile,
  points: Int
) {
  def id = oid.stringify
  def email = profile.email
  def name = profile.name
}
object Player {
  def create(profile: Profile) = {
    val rx = """^(...)@zen(exity|gularity).com$""".r
    val trigram = profile.email match {
      case rx(t, _) => t
      case _ => "???"
    }
    Player(
      oid = BSONObjectID.generate,
      trigram = trigram,
      profile = profile,
      points = 0
    )
  }
}
