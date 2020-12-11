package arisia.controllers

import arisia.schedule.ScheduleService
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}

import scala.concurrent.ExecutionContext

class ScheduleController(
  val controllerComponents: ControllerComponents,
  scheduleService: ScheduleService
)(
  implicit ec: ExecutionContext
)
  extends BaseController
{
  def getSchedule(): EssentialAction = Action { implicit request =>
    val schedule = scheduleService.currentSchedule()
    val json = Json.toJson(schedule)
    Ok(json).as(JSON)
  }
}
