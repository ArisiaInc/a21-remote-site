package arisia.auth

import enumeratum.values._

sealed abstract class LoginError(val value: String) extends StringEnumEntry

object LoginError extends StringPlayEnum[LoginError] {
  // This username/password combination isn't found in CM
  case object NoLogin extends LoginError("An error has occurred during log in. Please check your username and password at https://reg.arisia.org. For help, please email registration@arisia.org")
  // This account doesn't have a membership for this year
  case object NoMembership extends LoginError("Oops! It appears you don’t have a convention membership for 2021. Please go to https://reg.arisia.org to register for Arisia 2021. Note that the deadline has passed for rollovers, but registration is still available.")
  // This person hasn't signed the current Code of Conduct
  case object NoCoC extends LoginError("It is mandatory that all participants at Arisia understand and sign the Code of Conduct. Please do so at https://reg.arisia.org. For help, please email registration@arisia.org")
  // This person isn't allowed into the site yet
  case object NotYet extends LoginError("Too soon! Virtual Arisia isn’t open yet, but will be soon! Please check the Schedule at https://www.arisia.org/Publications#Schedule.")
  // This person's password has been reset
  case object PasswordReset extends LoginError("Your password has expired. Please go to https://reg.arisia.org and follow the instructions to reset your password. If you have issues doing so, please email registration@arisia.org.")

  val values = findValues
}
