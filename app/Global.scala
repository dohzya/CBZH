import scala.concurrent.ExecutionContext.Implicits.global

import play.api._

import models.{ Player, Profile }
import engine.Players

object Global extends GlobalSettings {

  override def onStart(app: Application) {
   Players.empty.map { empty =>
      if (empty) {
        Seq(
          Player.create(Profile(
            id = "",
            email = "ast@zengularity.com",
            verifiedEmail = true,
            name = "Alexandre STANISLAWSKI",
            givenName = "Alexandre",
            familyName = "STANISLAWSKI",
            link = "",
            gender = ""
          )).copy(points = 5235),
          Player.create(Profile(
            id = "",
            email = "jba@zengularity.com",
            verifiedEmail = true,
            name = "Jacques BECHERELLE",
            givenName = "Jacques",
            familyName = "BECHERELLE",
            link = "",
            gender = ""
          )).copy(points = 4645),
          Player.create(Profile(
            id = "",
            email = "nla@zengularity.com",
            verifiedEmail = true,
            name = "Nathanaël LAMELIÈRE",
            givenName = "Nathanaël",
            familyName = "LAMELIÈRE",
            link = "",
            gender = ""
          )).copy(points = 3568),
          Player.create(Profile(
            id = "",
            email = "gre@zengularity.com",
            verifiedEmail = true,
            name = "Gaëtan RENEDAUD",
            givenName = "Gaëtan",
            familyName = "RENEDAUD",
            link = "",
            gender = ""
          )).copy(points = 3457),
          Player.create(Profile(
            id = "",
            email = "vbr@zengularity.com",
            verifiedEmail = true,
            name = "Valerian BARBOT",
            givenName = "Valerian",
            familyName = "BARBOT",
            link = "",
            gender = ""
          )).copy(points = 2452),
          Player.create(Profile(
            id = "",
            email = "gge@zengularity.com",
            verifiedEmail = true,
            name = "Gwenaëlle GEORGET",
            givenName = "Gwenaëlle",
            familyName = "GEORGET",
            link = "",
            gender = ""
          )).copy(points = 2314),
          Player.create(Profile(
            id = "",
            email = "evo@zengularity.com",
            verifiedEmail = true,
            name = "Étienne VALLETTE d'OSIA",
            givenName = "Étienne",
            familyName = "VALLETTE d'OSIA",
            link = "",
            gender = ""
          )).copy(points = 346)
        ).map(Players.insert)
      }
    }
  }

}
