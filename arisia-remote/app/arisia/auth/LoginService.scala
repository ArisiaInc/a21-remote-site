package arisia.auth

import scala.concurrent.Future

trait LoginService {
  /**
   * Given credentials, says whether they match a known login.
   */
  def checkLogin(id: String, password: String): Future[Boolean]
}

class LoginServiceImpl extends LoginService {
  def checkLogin(id: String, password: String): Future[Boolean] = {
    // TODO: make this real
    val result = if (id == "joe" && password == "volcano")
      true
    else
      false

    Future.successful(result)
  }
}
