package arisia.schedule

import java.time.Instant
import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

import arisia.admin.RoomService
import arisia.db.DBService
import arisia.general.{LifecycleService, LifecycleItem}
import arisia.models.{ProgramItem, ZoomRoom, Schedule, ProgramItemId, ProgramItemLoc}
import arisia.timer.{TimerService, TimeService}
import arisia.util.Done
import arisia.zoom.ZoomService
import arisia.zoom.models.{ZoomMeeting, ZoomMeetingType}
import play.api.{Configuration, Logging}
import doobie._
import doobie.implicits._

import scala.annotation.tailrec
import scala.collection.SortedSet
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, ExecutionContext}

/**
 * This manages the Schedule Queue and Running Items Queue.
 */
trait ScheduleQueueService {
  def setSchedule(schedule: Schedule): Future[Done]

  def getRunningMeeting(id: ProgramItemId): Option[ZoomMeeting]

  def restartMeeting(id: ProgramItemId): Future[Done]
}

class ScheduleQueueServiceImpl(
  dbService: DBService,
  timerService: TimerService,
  time: TimeService,
  config: Configuration,
  roomService: RoomService,
  zoomService: ZoomService,
  val lifecycleService: LifecycleService
)(
  implicit ec: ExecutionContext
) extends ScheduleQueueService with LifecycleItem with Logging
{
  lazy val queueCheckInterval = config.get[FiniteDuration]("arisia.schedule.check.interval")

  case class RunningItem(endAt: Instant, itemId: ProgramItemId, meeting: ZoomMeeting)
  object RunningItem {
    implicit val runningItemOrdering: Ordering[RunningItem] = (x: RunningItem, y: RunningItem) => {
      val instantOrdering = implicitly[Ordering[Instant]]
      instantOrdering.compare(x.endAt, y.endAt)
    }
  }

  val lifecycleName = "ScheduleQueueService"
  lifecycleService.register(this)
  override def init() = {

    // On a regular basis, check whether we need to start/stop Zoom sessions
    timerService.register("Schedule Queue Service", queueCheckInterval)(checkQueues)

    dbService.run(
      sql"""
            SELECT end_at, program_item_id, zoom_meeting_id, host_url, attendee_url, webinar
              FROM active_program_items
           """
        .query[(Long, String, Long, String, String, Boolean)]
        .to[List]
    ).map { rawItems =>
      val items = rawItems.map { case (endAt, itemId, zoomId, hostUrl, attendeeUrl, isWebinar) =>
        RunningItem(
          Instant.ofEpochMilli(endAt),
          ProgramItemId(itemId),
          ZoomMeeting(zoomId, hostUrl, attendeeUrl, if (isWebinar) ZoomMeetingType.Webinar else ZoomMeetingType.Instant)
        )
      }
      _runningItemsQueue.set(SortedSet(items:_*))
      val itemMap = items.map(item => (item.itemId -> item)).toMap
      _currentlyRunningItems.set(itemMap)
      Done
    }
  }

  /**
   * The queue of Program Items yet to start.
   */
  val _scheduleQueue: AtomicReference[List[ProgramItem]] = new AtomicReference(List.empty)
  /**
   * The timestamp of when we most recently processed the queue.
   *
   * We use -1 to signify "not initialized". 0 means "has never been run".
   */
  val _scheduleCursor: AtomicLong = new AtomicLong(-1L)

  def fetchCursorFromDatabase(): Future[Done] = {
    dbService.run(
      sql"""SELECT value
              FROM text_files
             WHERE name = 'scheduleStrobeTime'"""
        .query[Long]
        .option
    ).map { cursorOpt =>
      val cursor: Long = cursorOpt.getOrElse(0)
      _scheduleCursor.set(cursor)
      Done
    }
  }

  def setScheduleCursor(now: Long): Future[Int] = {
    _scheduleCursor.set(now)
    dbService.run(
      sql"""INSERT INTO text_files
                 (name, value)
                 VALUES ('scheduleStrobeTime', ${now})
                 ON CONFLICT (name)
                 DO UPDATE SET value = ${now}"""
        .update
        .run
    )
  }

  val _schedule: AtomicReference[Schedule] = new AtomicReference(Schedule.empty)

  def setSchedule(schedule: Schedule): Future[Done] = {
    // Make sure that the schedule cursor is loaded before we try to compute the queue:
    logger.info(s"Setting the Schedule Queue...")
    _schedule.set(schedule)
    val dbFut =
      if (_scheduleCursor.get() == -1L) {
        fetchCursorFromDatabase()
      } else {
        Future.successful(Done)
      }
    dbFut.map { _ =>
      val queue =
        schedule
          .program
          // TODO: filter out non-Zoom items like YouTube videos, based on their tags
          // The Zoom info adheres to the prep session items, *not* the ordinary ones:
          // TODO: can we structure all of this in a more typeful way?
          .filter(_.prepFor.isDefined)
          .filter { item =>
            // Only bother with items whose time is after our last processing time:
            item.zoomStart.get.toLong > _scheduleCursor.get()
          }
          .filterNot { item =>
            // Just to be on the safe side, filter out anything that has already ended
            // (That is, the zoomEnd is less than now)
            item.zoomEnd.get.toLong < time.now().toEpochMilli
          }
          .sortBy(_.zoomStart.get.toLong)

      // TODO: in theory, we should check that there are no overlapping items in a given room

      _scheduleQueue.set(queue)
      logger.info(s"Schedule Queue updated -- ${queue.length} items remaining")
      Done
    }
  }

  /**
   * This is called periodically by the Timer. It checks whether there are Zoom meetings that we need to start.
   */
  private def checkScheduleQueue(now: Instant): Unit = {
    _scheduleQueue.get().headOption match {
      // The item at the front of the queue needs to be started:
      case Some(item) if (item.zoomStart.get.toLong < now.toEpochMilli) => {
        // Note that we intentially do *not* block subsequent items on this one, so that one failure doesn't
        // bring down the whole works. This is sad, but probably correct.
        // Drop it from the head of the queue:
        _scheduleQueue.getAndUpdate(_.tail)
        // Belt and suspenders check: does the database say that this meeting has already started?
        hasMeetingStarted(item.id).map { hasStarted =>
          if (hasStarted) {
            logger.warn(s"Not starting ${item.id.v} (${item.title}) because is seems to have already started.")
            Future.successful(())
          } else {
            // Start this item:
            startProgramItem(item)
          }

          // On to the next
          checkScheduleQueue(now)
        }
      }
      // We're done:
      case _ => setScheduleCursor(now.toEpochMilli)
    }
  }

  private def checkMeetingsToEnd(now: Instant): Unit = {
    _runningItemsQueue.get().headOption match {
      case Some(item) if (item.endAt.toEpochMilli < now.toEpochMilli) => {
        _runningItemsQueue.getAndUpdate(_.tail)
        endRunningItem(item)
        checkMeetingsToEnd(now)
      }
      case _ => // Nothing running
    }
  }

  private def checkQueues(now: Instant): Unit = {
    checkScheduleQueue(now)
    checkMeetingsToEnd(now)
  }

  private def startProgramItem(item: ProgramItem): Future[Done] = {
    val actualItemOpt = for {
      prepFor <- item.prepFor
      i <- _schedule.get().byItemId.get(prepFor)
    }
      yield i
    val actualItem = actualItemOpt.getOrElse(item)

    val title = actualItem.title.map(_.v).getOrElse("UNNAMED")
    logger.info(s"Starting Program Item $title")
    // TODO: logging for the failure cases here:
    for {
      roomOpt <- roomService.getRoomForZambia(item.loc.head)
      if (roomOpt.isDefined)
      room = roomOpt.get
      meetingEither <- zoomService.startMeeting(title, room.zoomId, room.isWebinar)
      if (meetingEither.isRight)
      Right(meeting) = meetingEither
      _ <- recordMeetingAsActive(item, meeting)
    }
      yield Done
  }

  private def endRunningItem(item: RunningItem, premature: Boolean = false): Future[Done] = {
    for {
      _ <- zoomService.endMeeting(item.meeting.id, item.meeting.isWebinar)
      _ = _currentlyRunningItems.getAndUpdate { items =>
            items - item.itemId
          }
      _ <- dbService.run(
        sql"""
              DELETE FROM active_program_items
               WHERE program_item_id = ${item.itemId.v}
             """
          .update
          .run
      )
      _ = _schedule.get().byItemId.get(item.itemId).foreach { programItem =>
        logger.info(s"Stopped Program Item ${programItem.title}")
      }
    }
      yield Done
  }

  // The Running Items Queue. Note that this is automatically sorted by the end time:
  val _runningItemsQueue: AtomicReference[SortedSet[RunningItem]] = new AtomicReference(SortedSet.empty)

  // The Currently Running Items Map, which we fetch meetings from when people want to enter them:
  val _currentlyRunningItems: AtomicReference[Map[ProgramItemId, RunningItem]] = new AtomicReference(Map.empty)

  def getRunningMeeting(id: ProgramItemId): Option[ZoomMeeting] = {
    _currentlyRunningItems.get.get(id).map(_.meeting)
  }

  def restartMeeting(id: ProgramItemId): Future[Done] = {
    _currentlyRunningItems.get().get(id) match {
      case Some(runningItem) => {
        for {
          _ <- endRunningItem(runningItem, true)
          _ = _runningItemsQueue.getAndUpdate {
            _.filterNot(_.itemId == id)
          }
          item = _schedule.get().program.find(_.id.v == s"$id-prep").get
          _ <- startProgramItem(item)
        }
          yield Done
      }
      // Someone seems to be confused:
      case _ => Future.successful(Done)
    }
  }

  private def hasMeetingStarted(itemId: ProgramItemId): Future[Boolean] = {
    dbService.run(
      sql"""
           SELECT program_item_id
             FROM active_program_items
            WHERE program_item_id = ${itemId.v}"""
        .query[String]
        .to[List]
    ).map { existingItems =>
      !(existingItems.isEmpty)
    }
  }

  private def recordMeetingAsActive(item: ProgramItem, meeting: ZoomMeeting): Future[Int] = {
    // Add the meeting to the Running Items Queue, so we know to shut it down:
    val endAt = item.zoomEnd.get
    // Note that what we record as running is the underlying item, not this prep item:
    val actualItemId = item.prepFor.get
    val runningItem = RunningItem(item.zoomEnd.get.t, actualItemId, meeting)
    _runningItemsQueue.accumulateAndGet(SortedSet(runningItem), _ ++ _)

    // Add the meeting to the Currently Running Items Map, so people can enter it:
    _currentlyRunningItems.accumulateAndGet(
      Map((actualItemId -> runningItem)),
      _ ++ _
    )

    // Now record it in the DB:
    dbService.run(
      sql"""
           INSERT INTO active_program_items
           (end_at, program_item_id, zoom_meeting_id, host_url, attendee_url, webinar)
           VALUES
           (${endAt.toLong}, ${actualItemId.v}, ${meeting.id}, ${meeting.start_url}, ${meeting.join_url}, ${meeting.isWebinar})"""
        .update
        .run
    )
  }

}
