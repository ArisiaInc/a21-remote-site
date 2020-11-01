package arisia.models

import play.api.libs.json.{Format, Json}

case class LoginRequest(id: String, password: String)

object LoginRequest {
  implicit val fmt: Format[LoginRequest] = Json.format
}
