package arisia.controllers

import arisia.auth.LoginService
import arisia.models.{LoginRequest, LoginUser, LoginId}
import play.api.Configuration
import play.api.http._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{Future, ExecutionContext}

class LoginController (
  val controllerComponents: ControllerComponents,
  config: Configuration,
  loginService: LoginService
)(
  implicit ec: ExecutionContext
)
  extends BaseController
{
  import LoginController._

  lazy val earlyAccessOnly: Boolean = config.get[Boolean]("arisia.early.access.only")

  def me(): EssentialAction = Action { implicit request =>
    request.session.get(userKey) match {
      case Some(jsonStr) => Ok(jsonStr)
        // TODO: is there a more robust way to make sure that we hit all the fields here?
      case None => Ok("""{"id":null,"name":null}""")
    }
  }

  def login(): EssentialAction = Action.async(controllerComponents.parsers.tolerantJson[LoginRequest]) { implicit request =>
    val req = request.body
    loginService.checkLogin(req.id, req.password).flatMap {
      _ match {
        case Some(user) => {
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
              Unauthorized("""{"success":"false", "message":"Sorry - you aren't allowed into this site yet. Talk to Remote if you believe you should have access."}""")
            }
          }
        }
        case None => {
          Future.successful(Unauthorized("""{"success":"false", "message":"Sorry - that isn't a valid username and password"}"""))
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

  def loggedInUser()(implicit request: Request[AnyContent]): Option[LoginUser] = {
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
}
