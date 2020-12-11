package arisia.schedule

import java.util.concurrent.atomic.AtomicReference

import arisia.db.DBService
import arisia.models.Schedule
import doobie._
import doobie.implicits._
import play.api.libs.ws.WSClient

import scala.concurrent.{Future, ExecutionContext}

trait ScheduleService {
  /**
   * Returns the currently-cached Schedule.
   */
  def currentSchedule(): Schedule

  /**
   * Go out to Zambia and fetch the current version of the Schedule.
   */
  def refresh(): Unit
}

class ScheduleServiceImpl(
  dbService: DBService,
  ws: WSClient
)(
  implicit ec: ExecutionContext
) extends ScheduleService {

  case class ScheduleCache(jsonp: String) {
    lazy val parsed = Schedule.parseKonOpas(jsonp)
  }

  // The currently-cached schedule, to serve out whenever the frontend needs it.
  // The initial value is just there so that we have something valid while we wait to load the initial value
  // from the DB
  val _theSchedule: AtomicReference[ScheduleCache] =
    new AtomicReference(ScheduleCache("var program = []; var people = []"))

  final val scheduleRowName: String = "scheduleJsonp"

  val loadInitialScheduleQuery: ConnectionIO[String] =
    sql"SELECT value from text_files WHERE name = '$scheduleRowName'"
    .query[String]
    .unique

  // At boot time, load the last-known version of the schedule:
  dbService.run(loadInitialScheduleQuery).map { jsonp =>
    println(s"======> Schedule loaded from DB")
    _theSchedule.set(ScheduleCache(jsonp))
  }

  // TODO: on the clock timer, grab the schedule JSONP from a configurable URL, initially pointed at the FakeZambiaController

  /**
   * Go out to Zambia, and fetch the current version of the schedule.
   *
   * This is called by the TimerService periodically.
   */
  def refresh(): Unit = {
    // TODO: make the URL configurable so that we can actually point it to Zambia. But until we have
    // that, it can just be hardcoded.
    ws.url("http://localhost:9000/test/fakseschedule")
      .get()
      .map { response =>
        val jsonp = response.body
        if (jsonp == _theSchedule.get.jsonp) {
          // TODO: replace all of these ======> printlns with proper logback logging:
          println(s"======> Refresh -- schedule hasn't changed")
        } else {
          println(s"======> Refreshed the schedule from Zambia")
          // TODO: don't actually set the cache until we validate that the jsonp validates:
          _theSchedule.set(ScheduleCache(jsonp))
        }
      }
  }

//  private def fetchSchedule(): Future[Schedule] = {
//    val jsonp: String = Resource.getAsString("konopastest.jsonp")
//    _theSchedule.set(Schedule.parseKonOpas(jsonp))
//    Future.successful(_theSchedule.get)
//  }

  def currentSchedule(): Schedule = {
    _theSchedule.get.parsed
  }
}
