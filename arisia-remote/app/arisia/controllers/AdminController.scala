package arisia.controllers

import arisia.auth.LoginService
import play.api.mvc._

import scala.concurrent.{Future, ExecutionContext}

class AdminController (
  val controllerComponents: ControllerComponents,
  loginService: LoginService
)(
  implicit ec: ExecutionContext
) extends BaseController
{
  def home(): EssentialAction = Action.async { implicit request =>
    LoginController.loggedInUser() match {
      case Some(user) => {
        loginService.getPermissions(user).map { permissions =>
          if (permissions.admin) {
            // Okay, this is a person who is allowed to use the Admin UI
            Ok(arisia.views.html.adminHome(permissions))
          } else {
            Forbidden(s"You aren't allowed to use the Admin interface")
          }
        }
      }
      case _ => Future.successful(NotFound("You aren't logged in!"))
    }
  }

  def manageAdmins(): EssentialAction = Action { implicit request =>
    Ok("TODO")
  }
}
