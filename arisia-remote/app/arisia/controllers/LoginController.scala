package controllers

import arisia.auth.LoginService
import arisia.models.LoginRequest
import play.api.http._
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
    loginService.checkLogin(req.id, req.password).map { verified =>
      if (verified) {
        Ok("Login accepted").withSession(idKey -> req.id)
      } else {
        Unauthorized("Not a known login")
      }
    }
  }

  def logout(): EssentialAction = Action { implicit request =>
    Ok("Logged out").withNewSession
  }
}
