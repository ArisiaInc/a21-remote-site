package arisia.controllers

import arisia.admin.AdminService
import arisia.auth.LoginService
import arisia.models.{LoginName, LoginUser, Permissions, LoginId}
import play.api.Logging
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport

import scala.concurrent.{Future, ExecutionContext}

class AdminController (
  val controllerComponents: ControllerComponents,
  adminService: AdminService,
  loginService: LoginService
)(
  implicit ec: ExecutionContext
) extends BaseController
  with I18nSupport
  with Logging
{
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

  def superAdminsOnlyAsync(f: AdminInfo => Future[Result]): EssentialAction = adminsOnlyAsync { info =>
    if (info.permissions.superAdmin) {
      f(info)
    } else {
      Future.successful(Forbidden("You need super-admin permission for this"))
    }
  }

  def home(): EssentialAction = adminsOnly { info =>
    Ok(arisia.views.html.adminHome(info.permissions))
  }

  val usernameForm = Form(
    mapping(
      "username" -> nonEmptyText
    )(LoginId.apply)(LoginId.unapply)
  )

  private def showManageAdmins()(implicit request: Request[AnyContent]) = {
    adminService.getAdmins().map { admins =>
      val sorted = admins.sortBy(_.v)
      Ok(arisia.views.html.manageAdmins(sorted, usernameForm.fill(LoginId(""))))
    }
  }

  def manageAdmins(): EssentialAction = superAdminsOnlyAsync { info =>
    implicit val request = info.request
    showManageAdmins()
  }

  def addAdmin(): EssentialAction = superAdminsOnlyAsync { info =>
    implicit val request = info.request

    usernameForm.bindFromRequest().fold(
      formWithErrors => {
        // TODO: actually display the error!
        Future.successful(Redirect(routes.AdminController.manageAdmins()))
      },
      loginName => {
        adminService.addAdmin(loginName).flatMap(_ => showManageAdmins())
      }
    )
  }

  def removeAdmin(idStr: String): EssentialAction = superAdminsOnlyAsync { info =>
    val id = LoginId(idStr)
    val fut = for {
      targetPerms <- loginService.getPermissions(id)
      // Safety check: you can't remove admin privs from the super-admins:
      if (!targetPerms.superAdmin)
      _ <- adminService.removeAdmin(id)
    }
      yield ()

    fut.map(_ => Redirect(routes.AdminController.manageAdmins()))
  }
}
