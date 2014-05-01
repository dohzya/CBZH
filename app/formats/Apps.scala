package formats

import play.api.libs.json._

import models.App

object Apps {

  implicit val jsonAppWrites = new Writes[App] {
    def writes(app: App) = Json.obj(
      "id" -> app.id,
      "name" -> app.name,
      "url" -> app.url,
      "token" -> app.token
    )
  }

}
