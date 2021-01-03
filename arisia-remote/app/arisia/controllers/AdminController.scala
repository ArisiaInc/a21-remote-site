package arisia.controllers

import arisia.admin.{RoomService, AdminService}
import arisia.auth.LoginService
import arisia.fun.{Duck, DuckService}
import arisia.models.{LoginUser, LoginId, Permissions, ZoomRoom, LoginName}
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
  zoomService: ZoomService,
  roomService: RoomService,
  duckService: DuckService
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

  def superAdminsOnly(f: AdminInfo => Result): EssentialAction = superAdminsOnlyAsync(info => Future.successful(f(info)))

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
   * Zoom Room CRUD
   */

  val roomForm = Form(
    mapping(
      "id" -> number,
      "displayName" -> nonEmptyText,
      "zoomId" -> nonEmptyText,
      "zambiaName" -> nonEmptyText,
      "isManual" -> boolean,
      "isWebinar" -> boolean
    )(ZoomRoom.apply)(ZoomRoom.unapply)
  )

  def manageZoomRooms(): EssentialAction = superAdminsOnly { info =>
    implicit val request = info.request

    val rooms = roomService.getRooms()
    Ok(arisia.views.html.manageZoomRooms(rooms))
  }

  def createRoom(): EssentialAction = superAdminsOnly { info =>
    implicit val request = info.request

    Ok(arisia.views.html.editRoom(roomForm.fill(ZoomRoom.empty)))
  }
  def showEditRoom(id: Int): EssentialAction = superAdminsOnly { info =>
    implicit val request = info.request

    val rooms = roomService.getRooms()
    rooms.find(_.id == id) match {
      case Some(room) => Ok(arisia.views.html.editRoom(roomForm.fill(room)))
      case _ => BadRequest(s"$id isn't a known Room!")
    }
  }

  def roomModified(): EssentialAction = superAdminsOnlyAsync { info =>
    implicit val request = info.request

    roomForm.bindFromRequest().fold(
      formWithErrors => {
        // TODO: actually display the error!
        Future.successful(BadRequest(arisia.views.html.editRoom(formWithErrors)))
      },
      room => {
        val fut =
          if (room.id == 0) {
            roomService.addRoom(room)
          } else {
            roomService.editRoom(room)
          }

        fut.map { _ =>
          Redirect(arisia.controllers.routes.AdminController.manageZoomRooms())
        }
      }
    )
  }


  ///////////////////////////////
  //
  // Duck CRUD
  //
  // TODO: this is finally getting to be the straw that breaks the camel's back. We should lift up an
  // AdminControllerBase class, and put these little CRUDs in their own controllers.
  //

  val duckForm = Form(
    mapping(
      "id" -> number,
      "imageUrl" -> nonEmptyText,
      "altText" -> nonEmptyText,
      "link" -> nonEmptyText,
      "hint" -> optional(text),
      "requestingUrl" -> optional(text)
    )(Duck.apply)(Duck.unapply)
  )

  def manageDucks(): EssentialAction = adminsOnly { info =>
    implicit val request = info.request

    logger.info("In manageDucks")

    val ducks = duckService.getDucks()
    logger.info("Got the ducks")
    Ok(arisia.views.html.manageDucks(ducks))
  }

  def createDuck(): EssentialAction = adminsOnly { info =>
    implicit val request = info.request

    Ok(arisia.views.html.editDuck(duckForm.fill(Duck.empty)))
  }
  def showEditDuck(id: Int): EssentialAction = adminsOnly { info =>
    implicit val request = info.request

    val ducks = duckService.getDucks()
    ducks.find(_.id == id) match {
      case Some(duck) => Ok(arisia.views.html.editDuck(duckForm.fill(duck)))
      case _ => BadRequest(s"$id isn't a known Duck!")
    }
  }

  def duckModified(): EssentialAction = adminsOnlyAsync { info =>
    implicit val request = info.request

    duckForm.bindFromRequest().fold(
      formWithErrors => {
        // TODO: actually display the error!
        Future.successful(BadRequest(arisia.views.html.editDuck(formWithErrors)))
      },
      duck => {
        val fut =
          if (duck.id == 0) {
            duckService.addDuck(duck)
          } else {
            duckService.editDuck(duck)
          }

        fut.map { _ =>
          Redirect(arisia.controllers.routes.AdminController.manageDucks())
        }
      }
    )
  }


  /* ********************
   *
   * Other ad-hoc functions
   *
   */

  /**
   * For now, this is internal-only, for testing, and can only be accessed from Swagger.
   */
  def startMeeting(): EssentialAction = adminsOnlyAsync { info =>
    // The below code no longer works, since we need the user Id. For now, just commenting it out, since it
    // isn't being used:
    ???
//    zoomService.startMeeting("Ad hoc meeting").map { errorOrMeeting =>
//      errorOrMeeting match {
//        case Right(meeting) => Ok(Json.toJson(meeting).toString())
//        case Left(error) => InternalServerError(error)
//      }
//    }
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

  /* ******************
   *
   * Tech staff permission
   *
   */

  private def showManageTech()(implicit request: Request[AnyContent]) =
    showPermissionMembers(
      _.getTech(),
      arisia.views.html.manageTech(_, usernameForm.fill(LoginId("")))
    )

  def manageTech(): EssentialAction = adminsOnlyAsync { info =>
    implicit val request = info.request
    showManageTech()
  }

  def addTech(): EssentialAction = adminsOnlyAsync {
    addPermission(
      _.addTech(_),
      showManageTech()(_),
      routes.AdminController.manageTech()
    )
  }

  def removeTech(idStr: String): EssentialAction = adminsOnlyAsync {
    removePermission(idStr, _.removeTech(_), routes.AdminController.manageTech())
  }
}
