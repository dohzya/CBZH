import scala.concurrent.ExecutionContext.Implicits.global

import play.api._

import models.Player
import engine.Players

object Global extends GlobalSettings {

  override def onStart(app: Application) {
   Players.empty.map { empty =>
      if (empty) {
        Seq(
          Player.create("ast", "Alexandre STANISLAWSKI", 5235),
          Player.create("jba", "Jacques BECHERELLE", 4645),
          Player.create("nla", "Nathanaël LAMELIÈRE", 3568),
          Player.create("gre", "Gaëtan RENEDAUD", 3457),
          Player.create("vbr", "Valerian BARBOT", 2452),
          Player.create("gge", "Gwenaëlle GEORGET", 2314),
          Player.create("evo", "Étienne VALLETTE d'OSIA", 346)
        ).map(Players.insert)
      }
    }
  }

}
