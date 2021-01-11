package arisia.controllers

import arisia.auth.LoginService
import arisia.models.{LoginUser, Permissions}
import play.api.mvc._

import scala.concurrent.{Future, ExecutionContext}

trait AdminControllerFuncs { self: BaseController =>
  def loginService: LoginService
  implicit def ec: ExecutionContext

  case class AdminInfo[T](request: Request[T], user: LoginUser, permissions: Permissions)

  /**
   * Standard wrapper -- the provided function will only be used iff the user is a logged-in Admin.
   *
   * All entry points in this Controller need to make use of this, to ensure consistency and safety.
   *
   * Note that this only ensures basic admin access; some functions may demand higher-level clearance.
   *
   * @param f function that actually does something.
   */
  def adminsOnlyAsync[T](parser: BodyParser[T])(f: AdminInfo[T] => Future[Result]): EssentialAction = Action(parser).async { implicit request =>
    LoginController.loggedInUserBase[T]() match {
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
  def adminsOnlyAsync(f: AdminInfo[AnyContent] => Future[Result]): EssentialAction =
    adminsOnlyAsync(controllerComponents.parsers.anyContent)(f)

  /**
   * Synchronous version of adminsOnlyAsync(), for simpler functions.
   */
  def adminsOnly[T](parser: BodyParser[T])(f: AdminInfo[T] => Result): EssentialAction =
    adminsOnlyAsync[T](parser)(info => Future.successful(f(info)))
  def adminsOnly(f: AdminInfo[AnyContent] => Result): EssentialAction =
    adminsOnly(controllerComponents.parsers.anyContent)(f)

  /**
   * Enhanced version of adminsOnlyAsync, for stuff that only super-admins can do.
   */
  def superAdminsOnlyAsync[T](parser: BodyParser[T])(f: AdminInfo[T] => Future[Result]): EssentialAction =
    adminsOnlyAsync[T](parser) { info =>
      if (info.permissions.superAdmin) {
        f(info)
      } else {
        Future.successful(Forbidden("You need super-admin permission for this"))
      }
    }
  def superAdminsOnlyAsync(f: AdminInfo[AnyContent] => Future[Result]): EssentialAction =
    superAdminsOnlyAsync(controllerComponents.parsers.anyContent)(f)

  def superAdminsOnly[T](parser: BodyParser[T])(f: AdminInfo[T] => Result): EssentialAction =
    superAdminsOnlyAsync(parser)(info => Future.successful(f(info)))
  def superAdminsOnly(f: AdminInfo[AnyContent] => Result): EssentialAction =
    superAdminsOnly(controllerComponents.parsers.anyContent)(f)

}
