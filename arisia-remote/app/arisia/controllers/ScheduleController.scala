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
  def getSchedule(): EssentialAction = Action.async { implicit request =>
    scheduleService.currentSchedule().map { schedule =>
      val json = Json.toJson(schedule)
      Ok(json).as(JSON)
    }.recover {
      case th: Throwable => {
        // TODO: log the error!!!
        ServiceUnavailable("""{"success":"false","message":"We are currently unable to load the Schedule; please try again soon!"}""")
      }
    }
  }
}
