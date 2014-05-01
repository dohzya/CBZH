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
            email = "hk1@zengularity.com",
            verifiedEmail = true,
            name = "Hacker 1",
            givenName = "Hacker",
            familyName = "1",
            link = "",
            gender = ""
          ) -> 3,
          Profile(
            id = "fake2",
            email = "hk2@zengularity.com",
            verifiedEmail = true,
            name = "Hacker 2",
            givenName = "Hacker",
            familyName = "2",
            link = "",
            gender = ""
          ) -> 2,
          Profile(
            id = "fake3",
            email = "hk3@zengularity.com",
            verifiedEmail = true,
            name = "Hacker 3",
            givenName = "Hacker",
            familyName = "3",
            link = "",
            gender = ""
          ) -> 4,
          Profile(
            id = "fake4",
            email = "hk4@zengularity.com",
            verifiedEmail = true,
            name = "Hacker 4",
            givenName = "Hacker",
            familyName = "4",
            link = "",
            gender = ""
          ) -> 2,
          Profile(
            id = "fake5",
            email = "hk5@zengularity.com",
            verifiedEmail = true,
            name = "Hacker 5",
            givenName = "Hacker",
            familyName = "5",
            link = "",
            gender = ""
          ) -> 1,
          Profile(
            id = "fake6",
            email = "hk6@zengularity.com",
            verifiedEmail = true,
            name = "Hacker 6",
            givenName = "Hacker",
            familyName = "6",
            link = "",
            gender = ""
          ) -> 2,
          Profile(
            id = "fake7",
            email = "hk7@zengularity.com",
            verifiedEmail = true,
            name = "Hacker 7",
            givenName = "Hacker",
            familyName = "VALLETTE d'7",
            link = "",
            gender = ""
          ) -> 3
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
