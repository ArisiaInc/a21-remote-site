package arisia.controllers

import arisia.models.{LoginId, LoginUser}
import play.api.mvc.{Request, AnyContent, BaseController, EssentialAction, Result}

import scala.concurrent.Future

/**
 * Mixin trait for controllers, to make it easy to define functions that are only available to logged-in users.
 */
trait LoginControllerFuncs extends BaseController {
  case class LoggedInRequest(who: LoginUser, request: Request[AnyContent])

  def withLoggedInAsync(f: LoggedInRequest => Future[Result]): EssentialAction = Action.async { implicit request =>
    LoginController.loggedInUser() match {
      case Some(user) => {
        f(LoggedInRequest(user, request))
      }
      case _ => Future.successful(Forbidden("You need to be logged in to do this"))
    }
  }
}
