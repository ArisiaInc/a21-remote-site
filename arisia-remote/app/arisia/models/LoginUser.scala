package arisia.models

import arisia.util.{StdString, StdStringUtils}
import play.api.libs.json.{Format, Json}

case class LoginId(v: String) extends StdString
object LoginId extends StdStringUtils(new LoginId(_))

case class LoginName(v: String) extends StdString
object LoginName extends StdStringUtils(new LoginName(_))

case class LoginUser(
  id: LoginId,
  name: LoginName
)
object LoginUser {
  implicit val fmt: Format[LoginUser] = Json.format
}
