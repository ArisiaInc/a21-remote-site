package arisia.timer

import akka.actor.{Cancellable, ActorSystem}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

/**
 * This "ticks" on a regular basis. It is a thin abstraction over the Akka Scheduler, so that we can stub that out
 * with something deterministic in unit tests.
 */
trait Ticker {
  def scheduleAtFixedRate(initialDelay: FiniteDuration, interval: FiniteDuration)(runnable: Runnable): Cancellable
}

class TickerImpl(
  actorSystem: ActorSystem
)(
  implicit ec: ExecutionContext
) extends Ticker {
  def scheduleAtFixedRate(initialDelay: FiniteDuration, interval: FiniteDuration)(runnable: Runnable): Cancellable = {
    actorSystem.scheduler.scheduleAtFixedRate(initialDelay, interval)(runnable)
  }
}
