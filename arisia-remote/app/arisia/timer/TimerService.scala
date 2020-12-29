package arisia.timer

import akka.actor.Cancellable
import arisia.schedule.ScheduleService
import play.api.{Logging, Configuration}

import scala.concurrent.duration._

/**
 * This is the top-level "loop" for the system.
 *
 * For now, this is all horribly coupled. Really, this should be a registry, which other services register listeners
 * for. But let's not fart around with dependency-injection complications right now. If we have time, we can
 * refactor all of this better later. (Possibly with a more sophisticated DI solution.)
 */
trait TimerService {
  def init(): Unit
  def shutdown(): Unit
}

class TimerServiceImpl(
  ticker: Ticker,
  scheduleService: ScheduleService,
  config: Configuration
) extends TimerService with Logging {

  lazy val initialDelay = config.get[FiniteDuration]("arisia.timer.initial.delay")
  lazy val interval = config.get[FiniteDuration]("arisia.timer.interval")

  class Runner() extends Runnable {
    // This is called on every "tick":
    def run(): Unit = {
      logger.info("Tick: running events")
      scheduleService.refresh()
    }
  }

  var _cancellable: Option[Cancellable] = None

  def init(): Unit = {
    _cancellable = Some(ticker.scheduleAtFixedRate(initialDelay, interval)(new Runner()))
  }

  def shutdown(): Unit = {
    _cancellable.map(_.cancel())
  }
}
