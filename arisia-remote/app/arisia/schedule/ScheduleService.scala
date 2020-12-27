package arisia.schedule

import java.time.{LocalDateTime, ZoneId}
import java.util.concurrent.atomic.AtomicReference

import arisia.db.DBService
import arisia.models.{Schedule, ProgramItemTimestamp}
import doobie._
import doobie.implicits._
import play.api.Logging
import play.api.libs.json.Json
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

  /**
   * The timezone that the Zambia data is assuming.
   *
   * In principle, this should come from config -- if anybody uses this for other conventions, I would recommend
   * doing that.
   */
  lazy val arisiaZone: ZoneId = ZoneId.of("EST", ZoneId.SHORT_IDS)

  case class ScheduleCache(jsonStr: String) {
    lazy val parsed: Schedule = {
      // TODO: make this cope with failure!
      val originalSchedule = Json.parse(jsonStr).as[Schedule]
      // We are enhancing the Zambia data with pre-calculated timestamps for each program item, to ease the effort
      // on the frontend side:
      val itemsWithTimestamps = originalSchedule.program.map { item =>
        val instant = for {
          date <- item.date
          time <- item.time
          dateTime = LocalDateTime.of(date.d, time.t)
          zoned = dateTime.atZone(arisiaZone)
        }
          yield ProgramItemTimestamp(zoned.toInstant)
        item.copy(timestamp = instant)
      }
      originalSchedule.copy(program = itemsWithTimestamps)
    }
  }

  // The currently-cached schedule, to serve out whenever the frontend needs it.
  // The initial value is just there so that we have something valid while we wait to load the initial value
  // from the DB
  val _theSchedule: AtomicReference[ScheduleCache] =
    new AtomicReference(ScheduleCache("{\"program\":[], \"people\": []}"))

  final val scheduleRowName: String = "scheduleJson"

  val loadInitialScheduleQuery: ConnectionIO[String] =
    sql"SELECT value from text_files WHERE name = $scheduleRowName"
    .query[String]
    .unique

  def updateScheduleStatement(scheduleJson: String): ConnectionIO[Int] =
    sql"UPDATE text_files set value = $scheduleJson where name = $scheduleRowName"
    .update
    .run

  // At boot time, load the last-known version of the schedule:
  dbService.run(loadInitialScheduleQuery).map { json =>
    logger.info(s"Schedule loaded from DB -- size ${json.length}")
    _theSchedule.set(ScheduleCache(json))
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
        val json = response.body
        if (json == _theSchedule.get.jsonStr) {
          logger.info(s"Refresh -- schedule hasn't changed")
        } else {
          logger.info(s"Refreshed the schedule from Zambia -- size ${json.length}")
          // TODO: don't actually set the cache until we validate that the jsonp validates:
          val cacheable = ScheduleCache(json)
          try {
            // For the moment, this can throw Exceptions.
            // TODO: make this pathway non-Exception-centric.
            val parsed = cacheable.parsed
            // Save it in the DB:
            dbService.run(updateScheduleStatement(json)).map { _ =>
              logger.info(s"Saved new Schedule in the database")
              // Once that's done, cache it:
              _theSchedule.set(ScheduleCache(json))
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
