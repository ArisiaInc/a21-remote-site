package arisia.schedule

import java.time.Instant
import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

import arisia.admin.RoomService
import arisia.db.DBService
import arisia.general.{LifecycleItem, LifecycleService}
import arisia.models.{ProgramItem, ZoomRoom, Schedule, ProgramItemId, ProgramItemLoc}
import arisia.timer.{TimerService, TimeService}
import arisia.util.Done
import arisia.zoom.ZoomService
import arisia.zoom.models.ZoomMeeting
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
}

class ScheduleQueueServiceImpl(
  dbService: DBService,
  timerService: TimerService,
  config: Configuration,
  roomService: RoomService,
  zoomService: ZoomService,
  val lifecycleService: LifecycleService
)(
  implicit ec: ExecutionContext
) extends ScheduleQueueService with LifecycleItem with Logging
{
  lazy val queueCheckInterval = config.get[FiniteDuration]("arisia.schedule.check.interval")

  val lifecycleName = "ScheduleQueueService"
  lifecycleService.register(this)
  override def init() = {

    // On a regular basis, check whether we need to start/stop Zoom sessions
    timerService.register("Schedule Queue Service", queueCheckInterval)(checkQueues)

    // TODO: at boot time, load the active_program_items table into the Running Items Queue, and shut down anything
    // that needs it

    Future.successful(Done)
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

  def setSchedule(schedule: Schedule): Future[Done] = {
    // Make sure that the schedule cursor is loaded before we try to compute the queue:
    logger.info(s"Setting the Schedule Queue...")
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
        // Start this item:
        startProgramItem(item)
        // Drop it from the head of the queue:
        _scheduleQueue.getAndUpdate(_.tail)
        // On to the next
        // TODO: once we're confident it's all working right, mark this as @tailrec
        checkScheduleQueue(now)
      }
      // We're done:
      case _ => setScheduleCursor(now.toEpochMilli)
    }
  }

  private def checkQueues(now: Instant): Unit = {
    checkScheduleQueue(now)
    // TODO: check the Running Items Queue
  }

  private def startProgramItem(item: ProgramItem): Future[Done] = {
    val title = item.title.map(_.v).getOrElse("UNNAMED")
    logger.info(s"Starting Program Item $title")
    // TODO: logging for the failure cases here:
    for {
      roomOpt <- roomService.getRoomForZambia(item.loc.head)
      if (roomOpt.isDefined)
      room = roomOpt.get
      meetingEither <- zoomService.startMeeting(title, room.zoomId)
      if (meetingEither.isRight)
      Right(meeting) = meetingEither
      _ <- recordMeetingAsActive(item, meeting)
    }
      yield Done
  }


  case class RunningItem(endAt: Instant, itemId: ProgramItemId, meeting: ZoomMeeting)
  object RunningItem {
    implicit val runningItemOrdering: Ordering[RunningItem] = (x: RunningItem, y: RunningItem) => {
      val instantOrdering = implicitly[Ordering[Instant]]
      instantOrdering.compare(x.endAt, y.endAt)
    }
  }

  // The Running Items Queue. Note that this is automatically sorted by the end time:
  val _runningItemsQueue: AtomicReference[SortedSet[RunningItem]] = new AtomicReference(SortedSet.empty)

  // The Currently Running Items Map, which we fetch meetings from when people want to enter them:
  val _currentlyRunningItems: AtomicReference[Map[ProgramItemId, RunningItem]] = new AtomicReference(Map.empty)

  private def recordMeetingAsActive(item: ProgramItem, meeting: ZoomMeeting): Future[Int] = {
    // Add the meeting to the Running Items Queue, so we know to shut it down:
    val endAt = item.zoomEnd.get
    val runningItem = RunningItem(item.zoomEnd.get.t, item.id, meeting)
    _runningItemsQueue.accumulateAndGet(SortedSet(runningItem), _ ++ _)

    // Add the meeting to the Currently Running Items Map, so people can enter it:
    _currentlyRunningItems.accumulateAndGet(
      Map((item.id -> runningItem)),
      _ ++ _
    )

    // Now record it in the DB:
    dbService.run(
      sql"""
           INSERT INTO active_program_items
           (end_at, program_item_id, zoom_meeting_id, host_url, attendee_url)
           VALUES
           (${endAt.toLong}, ${item.id.v}, ${meeting.id}, ${meeting.start_url}, ${meeting.join_url})"""
        .update
        .run
    )
  }

}
