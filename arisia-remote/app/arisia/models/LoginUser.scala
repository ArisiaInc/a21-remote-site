package arisia.models

import arisia.auth.MembershipType
import arisia.util.{StdString, StdStringUtils}
import play.api.libs.json.{Format, Json, Writes}

case class LoginId(v: String) extends StdString {
  lazy val lower: String = v.toLowerCase
}
object LoginId extends StdStringUtils(new LoginId(_))

case class LoginName(v: String) extends StdString
object LoginName extends StdStringUtils(new LoginName(_))

case class BadgeNumber(v: String) extends StdString
object BadgeNumber extends StdStringUtils(new BadgeNumber(_))

case class LoginUser(
  id: LoginId,
  name: LoginName,
  badgeNumber: BadgeNumber,
  zoomHost: Boolean,
  membershipType: MembershipType
)
object LoginUser {
  implicit val fmt: Format[LoginUser] = Json.format
}
