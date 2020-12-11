package arisia.timer

import akka.actor.Cancellable
import arisia.schedule.ScheduleService

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
  scheduleService: ScheduleService
) extends TimerService {

  class Runner() extends Runnable {
    // This is called on every "tick":
    def run(): Unit = {
      scheduleService.refresh()
    }
  }

  var _cancellable: Option[Cancellable] = None

  def init(): Unit = {
    // TODO: make the durations configurable
    _cancellable = Some(ticker.scheduleAtFixedRate(10.seconds, 5.minutes)(new Runner()))
  }

  def shutdown(): Unit = {
    _cancellable.map(_.cancel())
  }
}
