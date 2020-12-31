package arisia.schedule

import java.time.Instant
import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

import arisia.db.DBService
import arisia.models.{Schedule, ProgramItem}
import arisia.timer.{TimerService, TimeService}
import arisia.util.Done
import play.api.{Configuration, Logging}
import doobie._
import doobie.implicits._

import scala.annotation.tailrec
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
  config: Configuration
)(
  implicit ec: ExecutionContext
) extends ScheduleQueueService with Logging
{
  lazy val queueCheckInterval = config.get[FiniteDuration]("arisia.schedule.check.interval")

  // On a regular basis, check whether we need to start/stop Zoom sessions
  timerService.register("Schedule Queue Service", queueCheckInterval)(checkQueues)

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
  private def checkQueues(now: Instant): Unit = {
    _scheduleQueue.get().headOption match {
      // The item at the front of the queue needs to be started:
      case Some(item) if (item.zoomStart.get.toLong < now.toEpochMilli) => {
        // Start this item:
        startProgramItem(item)
        // Drop it from the head of the queue:
        _scheduleQueue.getAndUpdate(_.tail)
        // On to the next
        // TODO: once we're confident it's all working right, mark this as @tailrec
        checkQueues(now)
      }
      // We're done:
      case _ => setScheduleCursor(now.toEpochMilli)
    }
  }

  private def startProgramItem(item: ProgramItem): Unit = {
    logger.info(s"Starting Program Item ${item.title.getOrElse("UNNAMED")}")
    // TODO: look up the zoom room in the DB
    // TODO: create the meeting, using the appropriate userid
    // TODO: record the running meeting in the active_program_item table
    // TODO: add the item to the Running Items Queue, for shutdown
    // TODO: add the item to Currently Running Items, for quick access when people try to join
  }
}
