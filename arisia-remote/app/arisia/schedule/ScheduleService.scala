package arisia.schedule

import java.util.concurrent.atomic.AtomicReference

import arisia.db.DBService
import arisia.models.Schedule
import doobie._
import doobie.implicits._
import play.api.Logging
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
) extends ScheduleService with Logging {

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
    sql"SELECT value from text_files WHERE name = $scheduleRowName"
    .query[String]
    .unique

  def updateScheduleStatement(scheduleJsonp: String): ConnectionIO[Int] =
    sql"UPDATE text_files set value = $scheduleJsonp where name = $scheduleRowName"
    .update
    .run

  // At boot time, load the last-known version of the schedule:
  dbService.run(loadInitialScheduleQuery).map { jsonp =>
    logger.info(s"Schedule loaded from DB -- size ${jsonp.length}")
    _theSchedule.set(ScheduleCache(jsonp))
  }

  /**
   * Go out to Zambia, and fetch the current version of the schedule.
   *
   * This is called by the TimerService periodically.
   */
  def refresh(): Unit = {
    // TODO: make the URL configurable so that we can actually point it to Zambia. But until we have
    // that, it can just be hardcoded.
    ws.url("http://localhost:9000/test/fakeschedule")
      .get()
      .map { response =>
        val jsonp = response.body
        if (jsonp == _theSchedule.get.jsonp) {
          logger.info(s"Refresh -- schedule hasn't changed")
        } else {
          logger.info(s"Refreshed the schedule from Zambia -- size ${jsonp.length}")
          // TODO: don't actually set the cache until we validate that the jsonp validates:
          val cacheable = ScheduleCache(jsonp)
          try {
            // For the moment, this can throw Exceptions.
            // TODO: make this pathway non-Exception-centric.
            val parsed = cacheable.parsed
            // Save it in the DB:
            dbService.run(updateScheduleStatement(jsonp)).map { _ =>
              logger.info(s"Saved new Schedule in the database")
              // Once that's done, cache it:
              _theSchedule.set(ScheduleCache(jsonp))
            }
          } catch {
            case ex: Exception => {
              logger.error(s"Unable to parse Schedule that we received from Zambia!")
            }
          }
        }
      }
  }

  def currentSchedule(): Schedule = {
    _theSchedule.get.parsed
  }
}
