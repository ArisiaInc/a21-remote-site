package arisia.auth

import arisia.models.{LoginUser, LoginId, LoginName}

import scala.concurrent.Future

trait LoginService {
  /**
   * Given credentials, says whether they match a known login.
   */
  def checkLogin(id: String, password: String): Future[Option[LoginUser]]
}

class LoginServiceImpl extends LoginService {
  def checkLogin(id: String, password: String): Future[Option[LoginUser]] = {
    // TODO: make this real
    val result = if (id == "joe" && password == "volcano")
      Some(LoginUser(LoginId("joe"), LoginName("Joe Banks")))
    else
      None

    Future.successful(result)
  }
}
