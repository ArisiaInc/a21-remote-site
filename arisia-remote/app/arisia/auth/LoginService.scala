package arisia.auth

import scala.concurrent.duration._
import arisia.models.{LoginName, LoginUser, LoginId}
import play.api.libs.ws.WSClient

import scala.concurrent.{Future, ExecutionContext}

trait LoginService {
  /**
   * Given credentials, says whether they match a known login.
   */
  def checkLogin(id: String, password: String): Future[Option[LoginUser]]
}

class LoginServiceImpl(
  ws: WSClient
)(
  implicit ec: ExecutionContext
) extends LoginService {

  val badgeNamePrefix = """<input type=text name="registrant_fan_name_new" value=""""

  /**
   * Check the passed-in login credentials.
   *
   * Since Convention Master (CM) owns the concept of "this is a registered member", we're going to leverage that
   * for the Remote-site login. We're doing a very simple web-scrape here: just pass the given credentials directly
   * to CM's login page, and see if we get a success or error back. Conveniently for us, the next page includes the
   * member's badge name, so we pull that out at the same time.
   */
  def checkLogin(id: String, password: String): Future[Option[LoginUser]] = {
      // TODO: on principle, make this URL configurable
      ws.url("https://reg.arisia.org/kiosk/web_reg/index.php")
        // In case it gets antsy about XSS. Yes, we're lying to it, but it's our site, so it really isn't an issue:
      .addHttpHeaders("origin" -> "https://reg.arisia.org")
        // TODO: propagate the error more gracefully if this times out:
      .withRequestTimeout(10.seconds)
      .post(Map(
        "regUsername" -> Seq(id),
        "mode" -> Seq("loginUser"),
        "regPassword" -> Seq(password)
      )).map { response =>
        val body = response.body
        if (body.contains("Bad username or password")) {
          // CM says that it's wrong:
          None
        } else if (body.contains("Please Double-check your name information")) {
          // That indicates that CM thinks it's a legit username/password
          // Parse out the badgename. We're not even going to try and be cute about this
          // (parsing HTML is infamously dangerous) -- we're just going to count on the
          // precise format of the returned page:
          val badgeName = {
            val prefixPos = body.indexOf(badgeNamePrefix)
            if (prefixPos == -1) {
              id
            } else {
              val namePos = prefixPos + badgeNamePrefix.length
              val endPos = body.indexOf('"', namePos)
              val name = body.substring(namePos, endPos)
              if (name.length == 0)
                id
              else
                name
            }
          }

          Some(LoginUser(LoginId(id), LoginName(badgeName)))
        } else {
          // TODO: this is an unexpected result. Put in an alarm!
          None
        }
      }
  }
}
