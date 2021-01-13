package arisia.controllers

import arisia.admin.RoomService
import arisia.auth.LoginService
import arisia.models.{ProgramItemId, LoginUser, ZoomRoom}
import arisia.schedule.ScheduleService
import arisia.zoom.ZoomService
import play.api.Logging
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
  with Logging
{

  // TODO: get rid of this. There isn't much harm to it, but we won't need it once integration is really working,
  // and there is no point leaving extra entry points around for potential mischief.
  def test(): EssentialAction = Action.async { implicit request =>
    zoomService.getUsers().map { _ =>
      Ok("Got it!")
    }
  }

  def enterItemBase(rawItemStr: String)(lookupUrl: (LoginUser, ProgramItemId) => Option[String]): EssentialAction = Action { implicit request =>
    val itemStr =
      if (rawItemStr.endsWith("-prep"))
        rawItemStr.dropRight(5)
      else
        rawItemStr
    val itemId = ProgramItemId(itemStr)

    val redirectOpt = for {
      // Only logged in users are allowed to join meetings:
      user <- LoginController.loggedInUser()
      attendeeUrl <- lookupUrl(user, itemId)
      // If we get here, they're allowed in:
    }
      yield Found(attendeeUrl)

    // TODO: what should we return if this fails? This is effectively a system error: it shouldn't be possible for them
    // to get into this state, but we should probably provide a better error.
    redirectOpt.getOrElse(NotFound(
      arisia.views.html.errorPage(
        "Meeting Not Running",
        "That isn't a meeting that is currently running and open. Please check the schedule. Sorry!"
      )))
  }

  def enterItem(rawItemStr: String): EssentialAction =
    enterItemBase(rawItemStr)(scheduleService.getAttendeeUrlFor)

  def enterItemAsHost(rawItemStr: String): EssentialAction =
    enterItemBase(rawItemStr)(scheduleService.getHostUrlFor)

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
      "discordName" -> nonEmptyText,
      "isManual" -> boolean,
      "isWebinar" -> boolean
    )(ZoomRoom.apply)(ZoomRoom.unapply)
  )

  def manageZoomRooms(): EssentialAction = superAdminsOnly("Manage Zoom Rooms") { info =>
    implicit val request = info.request

    val rooms = roomService.getRooms()
    Ok(arisia.views.html.manageZoomRooms(rooms))
  }

  def createRoom(): EssentialAction = superAdminsOnly("Show Create Room") { info =>
    implicit val request = info.request

    Ok(arisia.views.html.editRoom(roomForm.fill(ZoomRoom.empty)))
  }
  def showEditRoom(id: Int): EssentialAction = superAdminsOnly(s"Show Edit Room $id") { info =>
    implicit val request = info.request

    val rooms = roomService.getRooms()
    rooms.find(_.id == id) match {
      case Some(room) => Ok(arisia.views.html.editRoom(roomForm.fill(room)))
      case _ => BadRequest(s"$id isn't a known Room!")
    }
  }

  def roomModified(): EssentialAction = superAdminsOnlyAsync("Room Modified") { info =>
    implicit val request = info.request

    roomForm.bindFromRequest().fold(
      formWithErrors => {
        // TODO: actually display the error!
        Future.successful(BadRequest(arisia.views.html.editRoom(formWithErrors)))
      },
      room => {
        info.audit(s"Room ${room.id} (${room.zambiaName})")
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

  def removeRoom(id: Int): EssentialAction = adminsOnlyAsync(s"Remove Room $id") { info =>
    roomService.removeRoom(id).map { _ =>
      Redirect(arisia.controllers.routes.ZoomController.manageZoomRooms())
    }
  }

}
