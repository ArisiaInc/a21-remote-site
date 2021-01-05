package arisia.controllers

import arisia.admin.RoomService
import arisia.auth.LoginService
import arisia.models.{ProgramItemId, LoginUser, ZoomRoom}
import arisia.schedule.ScheduleService
import arisia.zoom.ZoomService
import play.api.data._
import play.api.data.Forms._
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}
import play.api.i18n.I18nSupport

import scala.concurrent.{Future, ExecutionContext}

/**
 * This controller manages all external entry points that are Zoom-specific.
 */
class ZoomController(
  val controllerComponents: ControllerComponents,
  zoomService: ZoomService,
  roomService: RoomService,
  scheduleService: ScheduleService,
  val loginService: LoginService
)(
  implicit val ec: ExecutionContext
) extends BaseController
  with AdminControllerFuncs
  with I18nSupport
{

  // TODO: get rid of this. There isn't much harm to it, but we won't need it once integration is really working,
  // and there is no point leaving extra entry points around for potential mischief.
  def test(): EssentialAction = Action.async { implicit request =>
    zoomService.getUsers().map { _ =>
      Ok("Got it!")
    }
  }

  def enterItem(itemStr: String): EssentialAction = Action { implicit request =>
    val redirectOpt = for {
      // Only logged in users are allowed to join meetings:
      user <- LoginController.loggedInUser()
      attendeeUrl <- scheduleService.getAttendeeUrlFor(user, ProgramItemId(itemStr))
      // If we get here, they're allowed in:
    }
      yield Found(attendeeUrl)

    // TODO: what should we return if this fails? This is effectively a system error: it shouldn't be possible for them
    // to get into this state, but we should probably provide a better error.
    redirectOpt.getOrElse(NotFound("""{"success":false, "message":"That isn't a currently-running panel"}"""))
  }

  def enterItemAsHost(itemStr: String): EssentialAction = Action { implicit request =>
    val redirectOpt = for {
      // Only logged in users are allowed to join meetings:
      user <- LoginController.loggedInUser()
      hostUrl <- scheduleService.getHostUrlFor(user, ProgramItemId(itemStr))
      // If we get here, they're allowed in:
    }
      yield Found(hostUrl)

    // TODO: what should we return if this fails? This is effectively a system error: it shouldn't be possible for them
    // to get into this state, but we should probably provide a better error.
    redirectOpt.getOrElse(NotFound("""{"success":false, "message":"That isn't a currently-running panel"}"""))
  }

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
          Redirect(arisia.controllers.routes.ZoomController.manageZoomRooms())
        }
      }
    )
  }

}
