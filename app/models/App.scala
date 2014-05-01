package models

case class App(
  key: String,
  name: Option[String],
  url: Option[String],
  token: String
) {
  def id = key
}
