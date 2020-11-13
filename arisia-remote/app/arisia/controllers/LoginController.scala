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
  final val idKey = "id"

  def login(): EssentialAction = Action.async(controllerComponents.parsers.tolerantJson[LoginRequest]) { implicit request =>
    val req = request.body
    loginService.checkLogin(req.id, req.password).map {
      _ match {
        case Some(user) => {
          val json = Json.toJson(user)
          Ok(Json.stringify(json)).withSession(idKey -> user.id.v).as(JSON)
        }
        case None => {
          Unauthorized("""{"success":"false", "message":"Put a real error message here"}""")
        }
      }
    }
  }

  def logout(): EssentialAction = Action { implicit request =>
    Ok("Logged out").withNewSession
  }
}
