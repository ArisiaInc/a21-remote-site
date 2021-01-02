package arisia.controllers

import arisia.models.ProgramItemId
import arisia.schedule.{ScheduleService, StarService}
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}

import scala.concurrent.ExecutionContext

class ScheduleController(
  val controllerComponents: ControllerComponents,
  scheduleService: ScheduleService,
  starService: StarService
)(
  implicit ec: ExecutionContext
)
  extends BaseController with LoginControllerFuncs
{
  def getSchedule(): EssentialAction = Action { implicit request =>
    val userOpt = LoginController.loggedInUser()
    // Note: we explicitly assume that this fetch is synchronous and fast. It is up to the ScheduleService to
    // ensure that.
    val schedule = userOpt match {
      // Potential Zoom hosts see everything, including all prep sessions:
      case Some(user) if (user.zoomHost) => scheduleService.fullSchedule()
      // TODO: check if this person is a program participant; if so, give them a filtered version of the
      // full schedule:
      case _ => scheduleService.currentSchedule()
    }
    // The HTTP standard says that the hash should be in double-quotes in both directions:
    //   https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/ETag
    val hashStr = s""""${schedule.hash}""""
    request.headers.get("If-None-Match") match {
      case Some(prev) if (prev == hashStr) => NotModified
      case _ => Ok(schedule.json).as(JSON).withHeaders(("ETag", hashStr))
    }
  }

  def addStar(whichStr: String): EssentialAction = withLoggedInAsync { info =>
    starService.addStar(info.who.id, ProgramItemId(whichStr)).map { _ =>
      Ok("")
    }
  }

  def removeStar(whichStr: String): EssentialAction = withLoggedInAsync { info =>
    starService.removeStar(info.who.id, ProgramItemId(whichStr)).map { _ =>
      Ok("")
    }
  }

  def getStars(): EssentialAction = withLoggedInAsync { info =>
    starService.getStars(info.who.id).map { stars =>
      Ok(Json.toJson(stars).toString())
    }
  }
}
