package arisia.schedule

import java.time.{LocalDateTime, Instant, ZoneId, LocalTime}
import java.util.concurrent.atomic.AtomicReference

import scala.jdk.DurationConverters._
import scala.concurrent.duration._
import arisia.db.DBService
import arisia.general.{LifecycleService, LifecycleItem}
import arisia.models.{ProgramItemTime, LoginUser, ProgramItem, ProgramItemTimestamp, Schedule, ProgramItemId, ProgramItemTitle}
import arisia.timer.{TimerService, TimeService}
import arisia.util.Done
import doobie._
import doobie.implicits._
import play.api.{Configuration, Logging}
import play.api.libs.json.Json
import play.api.libs.ws.{WSCookie, WSClient}

import scala.concurrent.{Future, ExecutionContext}

trait ScheduleService {
  /**
   * Returns the currently-cached Schedule.
   */
  def currentSchedule(): Schedule

  /**
   * Iff this ProgramItem is running, and this person is allowed to enter it, return the URL to join.
   *
   * This will return None if the prep session is running, but this person isn't allowed to enter.
   *
   * Note that, since we require a LoginUser, we already know by type that this isn't anonymous.
   */
  def getAttendeeUrlFor(who: LoginUser, which: ProgramItemId): Option[String]
}

class ScheduleServiceImpl(
  timerService: TimerService,
  time: TimeService,
  dbService: DBService,
  ws: WSClient,
  config: Configuration,
  queueService: ScheduleQueueService,
  val lifecycleService: LifecycleService
)(
  implicit ec: ExecutionContext
) extends ScheduleService with LifecycleItem with Logging {

  lazy val schedulePrepStart = config.get[FiniteDuration]("arisia.schedule.prep.start")
  lazy val schedulePrepStop = config.get[FiniteDuration]("arisia.schedule.prep.end")
  lazy val prepMins: Long = schedulePrepStart.toMinutes
  // When, before the item's official start time, do we start letting people in the door?
  lazy val entryTime: FiniteDuration = schedulePrepStart - schedulePrepStop

  lazy val zambiaRefreshInterval = config.get[FiniteDuration]("arisia.zambia.refresh.interval")
  lazy val zambiaUrl = config.get[String]("arisia.zambia.url")
  lazy val zambiaLoginUrl = config.get[String]("arisia.zambia.loginUrl")
  lazy val zambiaBadgeId = config.get[String]("arisia.zambia.badgeId")
  lazy val zambiaPassword = config.get[String]("arisia.zambia.password")

  val lifecycleName = "ScheduleService"
  lifecycleService.register(this)
  override def init() = {
    // Start refreshing from Zambia on a regular basis
    timerService.register("Schedule Service", zambiaRefreshInterval)(refresh)

    // Load the last-known version of the schedule:
    dbService.run(loadInitialScheduleQuery).map { json =>
      logger.info(s"Schedule loaded from DB -- size ${json.length}")
      setSchedule(parseSchedule(json))
    }.map { _ => Done }
  }

  /**
   * The timezone that the Zambia data is assuming.
   *
   * In principle, this should come from config -- if anybody uses this for other conventions, I would recommend
   * doing that.
   */
  lazy val arisiaZone: ZoneId = ZoneId.of("EST", ZoneId.SHORT_IDS)

  def parseSchedule(jsonStr: String): Schedule = {
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

  val _scheduleJson: AtomicReference[String] = new AtomicReference("{\"program\":[], \"people\": []}")

  // The currently-cached schedule, to serve out whenever the frontend needs it.
  val _baseSchedule: AtomicReference[Schedule] =
    new AtomicReference(Schedule.empty)

  // An enhanced version of the schedule, with all of the prep sessions added.
  // This is computed based on the base schedule. It is used by the Schedule Queue for knowing when to start
  // and stop Zoom meetings, and we customize a view of this for people who have access to prep sessions.
  val _scheduleWithPrep: AtomicReference[Schedule] =
    new AtomicReference(Schedule.empty)

  def computeScheduleWithPrep(base: Schedule): Schedule = {
    val prepSessions =
      base.program
        // TODO: filter so this only happens for the Zoom rooms
        .map { item =>
          val prepTitle = item.title.map(t => ProgramItemTitle(s"Prep - ${t.v}"))
          val itemStart = item.when
          val prepStart = itemStart.minus(schedulePrepStart.toJava)
          val prepTime = item.time.map(time => ProgramItemTime(time.t.minus(schedulePrepStart.toJava)))
          val zoomEnd = itemStart.plus(item.duration.toJava)
          item.copy(
            id = ProgramItemId(item.id.v + "-prep"),
            title = prepTitle,
            prepFor = Some(item.id),
            time = prepTime,
            timestamp = Some(ProgramItemTimestamp(prepStart)),
            mins = Some(prepMins.toString),
            zoomStart = Some(ProgramItemTimestamp(prepStart)),
            zoomEnd = Some(ProgramItemTimestamp(zoomEnd))
          )
        }

    logger.info(s"Computed ${prepSessions.length} prep sessions")

    base.copy(
      program = base.program ++ prepSessions
    )
  }

  def setSchedule(cache: Schedule): Future[Done] = {
    _baseSchedule.set(cache)
    val withPrep = computeScheduleWithPrep(cache)
    _scheduleWithPrep.set(withPrep)
    queueService.setSchedule(withPrep)
  }

  final val scheduleRowName: String = "scheduleJson"

  val loadInitialScheduleQuery: ConnectionIO[String] =
    sql"SELECT value from text_files WHERE name = $scheduleRowName"
    .query[String]
    .unique

  def updateScheduleStatement(scheduleJson: String): ConnectionIO[Int] =
    sql"UPDATE text_files set value = $scheduleJson where name = $scheduleRowName"
    .update
    .run

  def logIntoZambia(): Future[Seq[WSCookie]] = {
    ws.url(zambiaLoginUrl)
      .withRequestTimeout(10.seconds)
      .post(Map(
        "badgeid" -> zambiaBadgeId,
        "passwd" -> zambiaPassword
      )).map { response =>
        response.cookies.toSeq
      }
  }

  /**
   * Go out to Zambia, and fetch the current version of the schedule.
   *
   * This is called by the TimerService periodically.
   */
  private def refresh(now: Instant): Unit = {
    // For the moment, we are logging in each time
    // TODO: cache the login cookies, and try using those instead of re-logging in each time. The current
    // approach works, but is a bit of extra labor for both Remote and Zambia.
    logIntoZambia().flatMap { cookies =>
      ws.url(zambiaUrl)
        .addCookies(cookies:_*)
        .get()
        .map { response =>
          val json = response.body
          if (json == _scheduleJson.get) {
            logger.info(s"Refresh -- schedule hasn't changed")
          } else {
            logger.info(s"Refreshed the schedule from Zambia -- size ${json.length}")
            try {
              // For the moment, this can throw Exceptions.
              // TODO: make this pathway non-Exception-centric.
              val schedule = parseSchedule(json)
              // Save it in the DB:
              dbService.run(updateScheduleStatement(json)).flatMap { _ =>
                logger.info(s"Saved new Schedule in the database")
                _scheduleJson.set(json)
                // Once that's done, cache it:
                setSchedule(schedule)
              }
            } catch {
              case ex: Exception => {
                logger.error(s"Unable to parse Schedule that we received from Zambia!")
                logger.error(response.body)
              }
            }
          }
        }
    }
  }

  def currentSchedule(): Schedule = {
    _baseSchedule.get
  }

  def getAttendeeUrlFor(who: LoginUser, which: ProgramItemId): Option[String] = {
    for {
      // Is this item real?
      item <- _baseSchedule.get.byItemId.get(which)
      // Is the meeting running?
      meeting <- queueService.getRunningMeeting(which)
      // Are they allowed to join yet?
      if (attendeeAllowedIn(who, item))
    }
      yield meeting.join_url
  }

  private def attendeeAllowedIn(who: LoginUser, item: ProgramItem): Boolean = {
    // If we have gotten to this point, it's a valid item and the meeting has started. Are they allowed in?
    if (time.now().isAfter(item.when.minus(entryTime.toJava))) {
      // Yes -- the doors are open
      true
    } else {
      // The doors aren't open yet -- are they allowed into the prep session?
      if (who.zoomHost) {
        // They're a potential Zoom host, so yes
        true
      } else if (item.people.exists(programPerson => programPerson.matches(who))) {
        // They're in the program item, so yes
        true
      } else {
        // Nope, the door is still closed to you
        false
      }
    }
  }
}
