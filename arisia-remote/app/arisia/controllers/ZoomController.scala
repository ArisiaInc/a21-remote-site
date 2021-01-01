package arisia.controllers

import arisia.models.{LoginUser, ProgramItemId}
import arisia.schedule.ScheduleService
import arisia.zoom.ZoomService
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}

import scala.concurrent.ExecutionContext

/**
 * This controller manages all external entry points that are Zoom-specific.
 */
class ZoomController(
  val controllerComponents: ControllerComponents,
  zoomService: ZoomService,
  scheduleService: ScheduleService
)(
  implicit ec: ExecutionContext
) extends BaseController {

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
}
