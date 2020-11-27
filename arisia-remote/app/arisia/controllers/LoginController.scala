package arisia.controllers

import arisia.auth.LoginService
import arisia.models.LoginRequest
import play.api.http._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{Future, ExecutionContext}

class LoginController (
  val controllerComponents: ControllerComponents,
  loginService: LoginService
)(
  implicit ec: ExecutionContext
)
  extends BaseController
{
  final val userKey = "user"

  def me(): EssentialAction = Action { implicit request =>
    request.session.get(userKey) match {
      case Some(jsonStr) => Ok(jsonStr)
        // TODO: is there a more robust way to make sure that we hit all the fields here?
      case None => Ok("""{"id":null,"name":null}""")
    }
  }

  def login(): EssentialAction = Action.async(controllerComponents.parsers.tolerantJson[LoginRequest]) { implicit request =>
    val req = request.body
    loginService.checkLogin(req.id, req.password).map {
      _ match {
        case Some(user) => {
          val json = Json.toJson(user)
          val jsStr = Json.stringify(json)
          Ok(jsStr).withSession(userKey -> jsStr).as(JSON)
        }
        case None => {
          Unauthorized("""{"success":"false", "message":"Put a real error message here"}""")
        }
      }
    }
  }

  def logout(): EssentialAction = Action { implicit request =>
    Ok(Json.obj("success" -> true)).withNewSession
  }
}
