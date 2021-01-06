package arisia.controllers

import arisia.models.LoginUser
import play.api.mvc.{Request, AnyContent, BaseController, EssentialAction, Result}

import scala.concurrent.Future

trait UserFuncs extends BaseController {

  case class UserRequest(user: LoginUser, request: Request[AnyContent])

  def withLoggedInUser(f: UserRequest => Future[Result]): EssentialAction = Action.async { implicit request =>
    LoginController.loggedInUser() match {
      case Some(user) => {
        val userRequest = UserRequest(user, request)
        f(userRequest)
      }
      case _ => Future.successful(Forbidden(s"""{"success":false, "message":"You're not logged in! Please log in and try again."}"""))
    }
  }

}
