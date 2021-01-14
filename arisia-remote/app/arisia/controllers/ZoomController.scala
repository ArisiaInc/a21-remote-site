package arisia.controllers

import arisia.admin.RoomService
import arisia.auth.LoginService
import arisia.models.{ProgramItemId, LoginUser, ZoomRoom}
import arisia.schedule.{ScheduleService, ScheduleQueueService}
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
  scheduleQueueService: ScheduleQueueService,
  val loginService: LoginService
)(
  implicit val ec: ExecutionContext
) extends BaseController
  with AdminControllerFuncs
  with UserFuncs
  with I18nSupport
  with Logging
{

  // TODO: get rid of this. There isn't much harm to it, but we won't need it once integration is really working,
  // and there is no point leaving extra entry points around for potential mischief.
  def test(meetingId: Long): EssentialAction = Action.async { implicit request =>
    ???
//    zoomService.checkMeeting(meetingId).map { _ =>
//      Ok("Got it!")
//    }
  }

  private def getItemId(rawItemStr: String): ProgramItemId = {
    val itemStr =
      if (rawItemStr.endsWith("-prep"))
        rawItemStr.dropRight(5)
      else
        rawItemStr
    ProgramItemId(itemStr)
  }

  def enterItemBase(rawItemStr: String)(lookupUrl: (LoginUser, ProgramItemId) => Option[String]): EssentialAction = Action { implicit request =>
    val itemId = getItemId(rawItemStr)

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

  val restartForm = Form(
    single(
      "itemId" -> nonEmptyText
    )
  )

  def showRestart(): EssentialAction = adminsOnly("Show Restart") { info =>
    implicit val request = info.request

    Ok(arisia.views.html.restartMeeting(restartForm.fill("")))
  }

  def restartProgramItem(): EssentialAction = adminsOnlyAsync("restart meeting") { info =>
    implicit val request = info.request
    val rawItemStr = restartForm.bindFromRequest().get
    val itemId = getItemId(rawItemStr)

    scheduleQueueService.getRunningMeeting(itemId) match {
      case Some(meeting) => {
        zoomService.isMeetingRunning(meeting.id).flatMap { running =>
          if (running) {
            // TODO: think about this. If the meeting is running and somebody tries to use this API, should
            // we stomp the existing meeting? It could conceivably happen in case of a takeover. We do have
            // a fallback in this case, though: the Zoom Admin goes in and manually stomps it, then comes
            // back here.
            Future.successful(BadRequest("That meeting is running fine already"))
          } else {
            // Okay, we have a meeting that is supposed to be running, but has stopped:
            scheduleQueueService.restartMeeting(itemId).map { _ =>
              Redirect("/admin")
            }
          }
        }
      }
      case None => Future.successful(BadRequest("That item isn't currently running!"))
    }
  }

  def isRoomOpen(name: String): EssentialAction = withLoggedInUser { userRequest =>
    roomService.getManualRoom(name) match {
      case Some(room) => {
        val meetingId = room.zoomId.toLong
        zoomService.isMeetingRunning(meetingId).map { running =>
          if (running) {
            Ok("""{"running":true}""")
          } else {
            Ok("""{"running":false}""")
          }
        }
      }
      case _ => Future.successful(BadRequest(""))
    }
  }

  def goToRoom(name: String): EssentialAction =  withLoggedInUser { userRequest =>
    roomService.getManualRoom(name) match {
      case Some(room) => {
        val meetingId = room.zoomId.toLong
        zoomService.getJoinUrl(meetingId).map { url =>
          Redirect(url)
        }
      }
      case _ => Future.successful(BadRequest(""))
    }
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
