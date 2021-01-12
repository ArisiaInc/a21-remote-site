package arisia.controllers

import arisia.auth.{LoginError, LoginService}
import arisia.fun.DuckService
import arisia.models.{LoginRequest, LoginUser, LoginId, BadgeNumber, LoginName}
import play.api.Configuration
import play.api.http._
import play.api.libs.json.{Json, JsValue, Format}
import play.api.mvc._

import scala.concurrent.{Future, ExecutionContext}

case class ProfileInfo(
  id: LoginId,
  name: LoginName,
  badgeNumber: BadgeNumber,
  zoomHost: Boolean,
  ducks: List[String]
)
object ProfileInfo {
  implicit val fmt: Format[ProfileInfo] = Json.format
}

class LoginController (
  val controllerComponents: ControllerComponents,
  config: Configuration,
  loginService: LoginService,
  duckService: DuckService
)(
  implicit ec: ExecutionContext
)
  extends BaseController
  with UserFuncs
{
  import LoginController._

  lazy val earlyAccessOnly: Boolean = config.get[Boolean]("arisia.early.access.only")

  def me(): EssentialAction = Action { implicit request =>
    request.session.get(userKey) match {
      case Some(jsonStr) => Ok(jsonStr)
      case None => Unauthorized("""{"id":null,"name":null}""")
    }
  }

  def getProfileInfoCore(user: LoginUser): Future[Result] = {
    duckService.getDucksFor(user.id).map { ducks =>
      val info = ProfileInfo(
        user.id,
        user.name,
        user.badgeNumber,
        user.zoomHost,
        ducks.map(_.toString)
      )
      Ok(Json.toJson(info).toString)
    }
  }

  def getProfileInfo(badgeNum: String): EssentialAction = withLoggedInUser { userRequest =>
    loginService.fetchUserInfo(BadgeNumber(badgeNum)).flatMap {
      _ match {
        case Some(user) => {
          getProfileInfoCore(user)
        }
        case _ =>
          Future.successful(NotFound(s"""{"success":"false", "message":"$badgeNum has not logged in to Virtual Arisia"}"""))
      }
    }
  }

  def getProfileInfoForMe(): EssentialAction = withLoggedInUser { userRequest =>
    getProfileInfoCore(userRequest.user)
  }

  def login(): EssentialAction = Action.async(controllerComponents.parsers.tolerantJson[LoginRequest]) { implicit request =>
    val req = request.body
    loginService.login(req.id, req.password).flatMap {
      _ match {
        case Right(user) => {
          loginService.getPermissions(user.id).map { permissions =>
            val allowed =
              if (earlyAccessOnly) {
                // We're in early-access mode, so the general public is *not* allowed in
                // For local dev environments, add your CM username to either arisia.allow.logins or
                // arisia.dev.admins in secrets.conf, to give yourself access:
                permissions.hasEarlyAccess
              } else {
                // The doors are open -- everyone can log in:
                true
              }

            if (allowed) {
              val json = Json.toJson(user)
              val jsStr = Json.stringify(json)
              Ok(jsStr).withSession(userKey -> jsStr).as(JSON)
            } else {
              val msg = LoginError.NotYet.value
              Unauthorized(s"""{"success":"false", "message":"$msg"}""")
            }
          }
        }
        case Left(error) => {
          Future.successful(Unauthorized(s"""{"success":"false", "message":"${error.value}"}"""))
        }
      }
    }
  }

  def logout(): EssentialAction = Action { implicit request =>
    Ok(Json.obj("success" -> true)).withNewSession
  }
}

object LoginController {
  final val userKey = "user"

  def loggedInUserBase[T]()(implicit request: Request[T]): Option[LoginUser] = {
    try {
      for {
        userJson <- request.session.get(userKey)
        jsValue = Json.parse(userJson)
        loginUser <- jsValue.asOpt[LoginUser]
      }
        yield loginUser
    } catch {
      case ex: Exception => None
    }
  }

  def loggedInUser()(implicit request: Request[AnyContent]): Option[LoginUser] =
    loggedInUserBase[AnyContent]()

  def loggedInUserJson()(implicit request: Request[JsValue]): Option[LoginUser] =
    loggedInUserBase[JsValue]()

}
