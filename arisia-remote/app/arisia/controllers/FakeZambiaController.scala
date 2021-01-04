package arisia.controllers

import java.time.{Period, LocalDate}

import arisia.models.{ProgramItemDate, Schedule}
import better.files.Resource
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}

import scala.concurrent.ExecutionContext

/**
 * This is a mockup of the Zambia interface that we expect to call. We use this during development so that we
 * can inject fake schedule data to test with.
 *
 * TODO: make this much more sophisticated -- generate the schedule to be *now*, so that we are supplying the rest of the
 * system with something that can be live-tested.
 */
class FakeZambiaController (
  val controllerComponents: ControllerComponents
)(
  implicit ec: ExecutionContext
)
  extends BaseController with Logging
{
  case class AdjustedSchedule(original: Schedule) {
    /**
     * We are going to adjust the schedule so that everything that happened on the Saturday of Arisia 2014 is "today".
     */
    val originalSaturday: LocalDate = LocalDate.of(2014, 1, 18)

    lazy val today: LocalDate = LocalDate.now()
    lazy val adjusted: Schedule = {
      // We are shifting everything by this period:
      val adjustment: Period = originalSaturday.until(today)
      logger.info(s"Adjusting the schedule forward by $adjustment")
      val adjustedItems = original.program.map { item =>
        item.copy(date = item.date.map(date => ProgramItemDate(date.d.plus(adjustment))))
      }
      original.copy(program = adjustedItems)
    }
    lazy val stringified = Json.toJson(adjusted).toString()
  }

  // EVIL: note that we're using plain old vars here in this test-only code. Please refrain from using them in
  // real code -- they risk all sorts of thread-unsafety problems that we are trying very hard to avoid. (It's only
  // okay here because it's test code that is only called every five minutes, so we can be a bit lazy.)
  var _originalSchedule: Option[String] = None
  var _adjustedSchedule: Option[AdjustedSchedule] = None

  def getSchedule(): EssentialAction = Action { implicit request =>
    val originalScheduleStr: String = _originalSchedule match {
      case Some(schedule) => schedule
      case None => {
        // Our test data is the 2014 schedule in JSONP format. Translate that into JSON, which is what we now
        // expect.
        // EVIL: please note that the following line is *not* okay in real code, because it blocks the current
        // thread. It's legit here only because this is test code that won't run in the real site. If you
        // need to do file IO for real, let's talk about ways to do that more appropriately. (There are better
        // options, they just require a bit more code.)
        val jsonp = Resource.getAsString("konopastest.jsonp")
        val schedule = Schedule.parseKonOpas(jsonp)
        // TODO: massage the timing of the schedule so that it is happening today
        // TODO: massage the rooms in the schedule to match our actual rooms, and reduce to fewer of them
        _originalSchedule = Some(Json.toJson(schedule).toString())
        _originalSchedule.get
      }
    }

    val s = _adjustedSchedule match {
      case Some(schedule) if schedule.today == LocalDate.now() => schedule
      case _ => {
        // Either we don't have any Schedule yet, or we've moved to a new day and need to readjust:
        logger.info(s"Readjusting the test schedule")
        val originalSchedule = Json.parse(originalScheduleStr).as[Schedule]
        val newSchedule = AdjustedSchedule(originalSchedule)
        _adjustedSchedule = Some(newSchedule)
        newSchedule
      }
    }

    Ok(s.stringified)
  }
}
