package arisia.schedule

import java.util.concurrent.atomic.{AtomicLong, AtomicReference}

import arisia.db.DBService
import arisia.models.{Schedule, ProgramItem}
import arisia.timer.TimeService
import arisia.util.Done
import play.api.Logging
import doobie._
import doobie.implicits._

import scala.concurrent.{ExecutionContext, Future}

/**
 * This manages the Schedule Queue and Running Items Queue.
 */
trait ScheduleQueueService {
  def setSchedule(schedule: Schedule): Future[Done]
  def checkQueues(): Unit
}

class ScheduleQueueServiceImpl(
  dbService: DBService,
  time: TimeService
)(
  implicit ec: ExecutionContext
) extends ScheduleQueueService with Logging
{
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
          .filter { item =>
            // Only bother with items that have known times, and that time is after our last processing time:
            item.timestamp match {
              case Some(ts) if (ts.toLong > _scheduleCursor.get()) => true
              case _ => false
            }
          }
          // TODO: filter out non-Zoom items like YouTube videos, based on their tags
          .sortBy(_.timestamp.get.toLong)

      // TODO: in theory, we should check that there are no overlapping items in a given room

      _scheduleQueue.set(queue)
      logger.info(s"Schedule Queue updated -- ${queue.length} items remaining")
      Done
    }
  }

  /**
   * This is called periodically by the Timer. It checks whether there are Zoom meetings that we need to start.
   */
  def checkQueues(): Unit = {

  }
}
