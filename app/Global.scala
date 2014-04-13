import scala.concurrent.ExecutionContext.Implicits.global

import play.api._

import models.{ Log, Player, Profile }
import engine.Players

object Global extends GlobalSettings {

  override def onStart(app: Application) {
   Players.empty.map { empty =>
      if (empty) {
        Seq(
          Profile(
            id = "fake1",
            email = "ast@zengularity.com",
            verifiedEmail = true,
            name = "Alexandre STANISLAWSKI",
            givenName = "Alexandre",
            familyName = "STANISLAWSKI",
            link = "",
            gender = ""
          ) -> 5235,
          Profile(
            id = "fake2",
            email = "jba@zengularity.com",
            verifiedEmail = true,
            name = "Jacques BECHERELLE",
            givenName = "Jacques",
            familyName = "BECHERELLE",
            link = "",
            gender = ""
          ) -> 4645,
          Profile(
            id = "fake3",
            email = "nla@zengularity.com",
            verifiedEmail = true,
            name = "Nathanaël LAMELIÈRE",
            givenName = "Nathanaël",
            familyName = "LAMELIÈRE",
            link = "",
            gender = ""
          ) -> 3568,
          Profile(
            id = "fake4",
            email = "gre@zengularity.com",
            verifiedEmail = true,
            name = "Gaëtan RENEDAUD",
            givenName = "Gaëtan",
            familyName = "RENEDAUD",
            link = "",
            gender = ""
          ) -> 3457,
          Profile(
            id = "fake5",
            email = "vbr@zengularity.com",
            verifiedEmail = true,
            name = "Valerian BARBOT",
            givenName = "Valerian",
            familyName = "BARBOT",
            link = "",
            gender = ""
          ) -> 2452,
          Profile(
            id = "fake6",
            email = "gge@zengularity.com",
            verifiedEmail = true,
            name = "Gwenaëlle GEORGET",
            givenName = "Gwenaëlle",
            familyName = "GEORGET",
            link = "",
            gender = ""
          ) -> 2314,
          Profile(
            id = "fake7",
            email = "evo@zengularity.com",
            verifiedEmail = true,
            name = "Étienne VALLETTE d'OSIA",
            givenName = "Étienne",
            familyName = "VALLETTE d'OSIA",
            link = "",
            gender = ""
          ) -> 346
        ).map { case (profile, points) =>
          Players.fromProfile(profile).map { player =>
            val log = Log.create(player, points, "did some awesome stuff!", None)
            Players.updatePoints(player, log)
          }
        }
      }
    }
  }

}
