package arisia.timer

import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

import akka.actor.Cancellable
import arisia.schedule.ScheduleService
import play.api.{Configuration, Logging}

import scala.concurrent.duration._

/**
 * This is the top-level "loop" for the system.
 *
 * For now, this is all horribly coupled. Really, this should be a registry, which other services register listeners
 * for. But let's not fart around with dependency-injection complications right now. If we have time, we can
 * refactor all of this better later. (Possibly with a more sophisticated DI solution.)
 */
trait TimerService {
  def register(name: String, interval: FiniteDuration)(cb: Instant => Unit): Unit

  def init(): Unit
  def shutdown(): Unit
}

class TimerServiceImpl(
  ticker: Ticker,
  time: TimeService,
  config: Configuration
) extends TimerService with Logging {

  lazy val initialDelay = config.get[FiniteDuration]("arisia.timer.initial.delay")
  lazy val interval = config.get[FiniteDuration]("arisia.timer.interval")

  case class TimerEntry(
    name: String,
    interval: FiniteDuration,
    cancellable: Option[Cancellable],
    cb: Instant => Unit
  ) extends Runnable {
    def run(): Unit = {
      logger.info(s"Tick: running $name")
      cb(time.now())
    }

    def start(): TimerEntry = {
      copy(
        cancellable = Some(ticker.scheduleAtFixedRate(initialDelay, interval)(this))
      )
    }

    def cancel(): Unit = cancellable.map(_.cancel())
  }

  val registry: AtomicReference[List[TimerEntry]] = new AtomicReference(List.empty)

  def register(name: String, interval: FiniteDuration)(cb: Instant => Unit): Unit = {
    registry.accumulateAndGet(
      List(TimerEntry(name, interval, None, cb).start()),
      _ ++ _
    )
  }

  def init(): Unit = {}

  def shutdown(): Unit = {
    registry.get().map(_.cancel())
  }
}
