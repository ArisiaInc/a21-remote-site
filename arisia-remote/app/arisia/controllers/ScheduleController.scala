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
    val schedule = scheduleService.currentSchedule()
    val json = Json.toJson(schedule)
    Ok(json).as(JSON)
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
