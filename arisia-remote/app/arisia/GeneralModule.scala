package arisia

import akka.actor.ActorSystem
import arisia.auth.{LoginService, LoginServiceImpl}
import arisia.db.{DBServiceImpl, DBService}
import com.softwaremill.macwire.wire
import arisia.schedule.{ScheduleServiceImpl, ScheduleService}
import arisia.timer.{TimerServiceImpl, Ticker, TimerService, TickerImpl}
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

/**
 * This is the catch-all Module for instantiating services that don't really need a Module of their own.
 */
trait GeneralModule {
  implicit def executionContext: ExecutionContext
  def configuration: Configuration
  def wsClient: WSClient
  def actorSystem: ActorSystem

  lazy val scheduleService: ScheduleService = wire[ScheduleServiceImpl]
  lazy val loginService: LoginService = wire[LoginServiceImpl]
  lazy val dbService: DBService = wire[DBServiceImpl]
  lazy val ticker: Ticker = wire[TickerImpl]
  lazy val timerService: TimerService = wire[TimerServiceImpl]
}
