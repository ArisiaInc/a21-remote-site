package arisia.schedule

import java.util.concurrent.atomic.AtomicReference

import arisia.models.{ProgramPerson, Schedule, ProgramItem}
import better.files.Resource
import play.api.libs.json.{Json, JsError, JsSuccess}

import scala.concurrent.Future

trait ScheduleService {
  /**
   * Returns the currently-cached Schedule.
   */
  def currentSchedule(): Future[Schedule]
}

class ScheduleServiceImpl()
  extends ScheduleService
{
  val _theSchedule: AtomicReference[Schedule] = new AtomicReference(Schedule.empty)

  private def fetchSchedule(): Future[Schedule] = {
    // TODO: replace this with something real, of course
    val jsonp: String = Resource.getAsString("konopastest.jsonp")
    _theSchedule.set(Schedule.parseKonOpas(jsonp))
    Future.successful(_theSchedule.get)
  }

  def currentSchedule(): Future[Schedule] = {
    Future.successful(_theSchedule.get)
  }
}
