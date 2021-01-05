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
  val loginService: LoginService,
  zoomService: ZoomService,
  roomService: RoomService,
  duckService: DuckService
)(
  implicit val ec: ExecutionContext
) extends BaseController
  with AdminControllerFuncs
  with I18nSupport
  with Logging
{

  def home(): EssentialAction = adminsOnly { info =>
    Ok(arisia.views.html.adminHome(info.permissions))
  }

  val usernameForm = Form(
    mapping(
      "username" -> nonEmptyText
    )(LoginId.apply)(LoginId.unapply)
  )


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
      "requestingUrl" -> text
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
