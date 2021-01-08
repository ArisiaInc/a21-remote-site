package arisia.auth

import enumeratum.values._

sealed abstract class LoginError(val value: String) extends StringEnumEntry

object LoginError extends StringPlayEnum[LoginError] {
  // This username/password combination isn't found in CM
  case object NoLogin extends LoginError("nologin")
  // This account doesn't have a membership for this year
  case object NoMembership extends LoginError("nomembership")
  // This person hasn't signed the current Code of Conduct
  case object NoCoC extends LoginError("nococ")
  // This person isn't allowed into the site yet
  case object NotYet extends LoginError("notyet")

  val values = findValues
}
