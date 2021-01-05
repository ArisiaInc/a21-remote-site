package arisia.controllers

import arisia.auth.LoginService
import arisia.models.{LoginUser, Permissions}
import play.api.mvc._

import scala.concurrent.{Future, ExecutionContext}

trait AdminControllerFuncs { self: BaseController =>
  def loginService: LoginService
  implicit def ec: ExecutionContext

  case class AdminInfo(request: Request[AnyContent], user: LoginUser, permissions: Permissions)

  /**
   * Standard wrapper -- the provided function will only be used iff the user is a logged-in Admin.
   *
   * All entry points in this Controller need to make use of this, to ensure consistency and safety.
   *
   * Note that this only ensures basic admin access; some functions may demand higher-level clearance.
   *
   * @param f function that actually does something.
   */
  def adminsOnlyAsync(f: AdminInfo => Future[Result]): EssentialAction = Action.async { implicit request =>
    LoginController.loggedInUser() match {
      case Some(user) => {
        loginService.getPermissions(user.id).flatMap { permissions =>
          if (permissions.admin) {
            // Okay, this is a person who is allowed to use the Admin UI
            val info = AdminInfo(request, user, permissions)
            f(info)
          } else {
            Future.successful(Forbidden(s"You aren't allowed to use the Admin interface"))
          }
        }
      }
      case _ => Future.successful(NotFound("You aren't logged in!"))
    }
  }

  /**
   * Synchronous version of adminsOnlyAsync(), for simpler functions.
   */
  def adminsOnly(f: AdminInfo => Result): EssentialAction = adminsOnlyAsync(info => Future.successful(f(info)))

  /**
   * Enhanced version of adminsOnlyAsync, for stuff that only super-admins can do.
   */
  def superAdminsOnlyAsync(f: AdminInfo => Future[Result]): EssentialAction = adminsOnlyAsync { info =>
    if (info.permissions.superAdmin) {
      f(info)
    } else {
      Future.successful(Forbidden("You need super-admin permission for this"))
    }
  }

  def superAdminsOnly(f: AdminInfo => Result): EssentialAction = superAdminsOnlyAsync(info => Future.successful(f(info)))

}