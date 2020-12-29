package arisia.controllers

import arisia.admin.AdminService
import arisia.auth.LoginService
import arisia.models.{LoginName, LoginUser, Permissions, LoginId}
import arisia.zoom.ZoomService
import play.api.Logging
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.http.Writeable
import play.api.i18n.I18nSupport
import play.api.libs.json.Json

import scala.concurrent.{Future, ExecutionContext}

class AdminController (
  val controllerComponents: ControllerComponents,
  adminService: AdminService,
  loginService: LoginService,
  zoomService: ZoomService
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

  def home(): EssentialAction = adminsOnly { info =>
    Ok(arisia.views.html.adminHome(info.permissions))
  }

  val usernameForm = Form(
    mapping(
      "username" -> nonEmptyText
    )(LoginId.apply)(LoginId.unapply)
  )

  /* ********************
   *
   * Other ad-hoc functions
   *
   */

  /**
   * For now, this is internal-only, for testing, and can only be accessed from Swagger.
   */
  def startMeeting(): EssentialAction = adminsOnlyAsync { info =>
    zoomService.startMeeting("Ad hoc meeting").map { errorOrMeeting =>
      errorOrMeeting match {
        case Right(meeting) => Ok(Json.toJson(meeting).toString())
        case Left(error) => InternalServerError(error)
      }
    }
  }

  def endMeeting(meetingIdStr: String): EssentialAction = adminsOnlyAsync { info =>
    meetingIdStr.toLongOption match {
      case Some(meetingId) => {
        zoomService.endMeeting(meetingId).map { _ =>
          Ok(s"Meeting $meetingId ended")
        }
      }
      case _ => {
        Future.successful(BadRequest(s"$meetingIdStr is not a valid meeting ID"))
      }
    }
  }

  /* ********************
   *
   * Generic Permission controller functions
   *
   * These are the template functions, which we reuse for the various specific permissions.
   *
   */

  private def showPermissionMembers[C: Writeable](
    getMembers: AdminService => Future[List[LoginId]],
    display: List[LoginId] => C
  )(implicit request: Request[AnyContent]) = {
    getMembers(adminService).map { members =>
      val sorted = members.sortBy(_.v)
      Ok(display(sorted))
    }
  }

  private def addPermission(
    addFunc: (AdminService, LoginId) => Future[Int],
    onSuccess: Request[AnyContent] => Future[Result],
    onError: Call
  ): AdminInfo => Future[Result] = { info =>
    implicit val request = info.request

    usernameForm.bindFromRequest().fold(
      formWithErrors => {
        // TODO: actually display the error!
        Future.successful(Redirect(onError))
      },
      loginName => {
        addFunc(adminService, loginName).flatMap(_ => onSuccess(request))
      }
    )
  }

  private def removePermission(
    idStr: String,
    removeFunc: (AdminService, LoginId) => Future[Int],
    whenFinished: Call
  ): AdminInfo => Future[Result] = { info =>
    val id = LoginId(idStr)
    val fut = for {
      targetPerms <- loginService.getPermissions(id)
      // Safety check: you can't remove privs from the super-admins:
      if (!targetPerms.superAdmin)
      _ <- removeFunc(adminService, id)
    }
      yield ()

    fut.map(_ => Redirect(whenFinished))
  }

  /* ******************
   *
   * Admin permission
   *
   */

  private def showManageAdmins()(implicit request: Request[AnyContent]) = {
    showPermissionMembers(
      _.getAdmins(),
      arisia.views.html.manageAdmins(_, usernameForm.fill(LoginId("")))
    )
  }

  def manageAdmins(): EssentialAction = superAdminsOnlyAsync { info =>
    implicit val request = info.request
    showManageAdmins()
  }

  def addAdmin(): EssentialAction = superAdminsOnlyAsync {
    addPermission(
      _.addAdmin(_),
      showManageAdmins()(_),
      routes.AdminController.manageAdmins()
    )
  }

  def removeAdmin(idStr: String): EssentialAction = superAdminsOnlyAsync {
    removePermission(idStr, _.removeAdmin(_), routes.AdminController.manageAdmins())
  }

  /* ******************
   *
   * Early access permission
   *
   */

  private def showManageEarlyAccess()(implicit request: Request[AnyContent]) =
    showPermissionMembers(
      _.getEarlyAccess(),
      arisia.views.html.manageEarlyAccess(_, usernameForm.fill(LoginId("")))
    )

  def manageEarlyAccess(): EssentialAction = adminsOnlyAsync { info =>
    implicit val request = info.request
    showManageEarlyAccess()
  }

  def addEarlyAccess(): EssentialAction = adminsOnlyAsync {
    addPermission(
      _.addEarlyAccess(_),
      showManageEarlyAccess()(_),
      routes.AdminController.manageEarlyAccess()
    )
  }

  def removeEarlyAccess(idStr: String): EssentialAction = adminsOnlyAsync {
    removePermission(idStr, _.removeEarlyAccess(_), routes.AdminController.manageEarlyAccess())
  }
}
