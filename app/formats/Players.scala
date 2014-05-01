package formats

import play.api.libs.json._
import reactivemongo.bson.BSONObjectID

import models.{ Player, Profile }

object Players {

  implicit val jsonProfileWrites = new Writes[Profile]  {
    def writes(profile: Profile) = Json.obj(
      "id" -> profile.id,
      "email" -> profile.email,
      "verifiedEmail" -> profile.verifiedEmail,
      "name" -> profile.name,
      "givenName" -> profile.givenName,
      "familyName" -> profile.familyName,
      "link" -> profile.link,
      "picture" -> profile.picture,
      "gender" -> profile.gender,
      "birthday" -> profile.birthday,
      "locale" -> profile.locale
    )
  }

  implicit val jsonPlayerWrites = new Writes[Player] {
    def writes(player: Player) = Json.obj(
      "_id" -> player.id,
      "name" -> player.name,
      "trigram" -> player.trigram,
      "profile" -> player.profile,
      "points" -> player.points
    )
  }

}
