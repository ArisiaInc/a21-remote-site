package arisia

import akka.actor.ActorSystem
import arisia.admin.{AdminServiceImpl, AdminService}
import arisia.auth.{LoginService, LoginServiceImpl}
import arisia.db.{DBServiceImpl, DBService}
import com.softwaremill.macwire.wire
import arisia.schedule.{ScheduleService, ScheduleQueueService, ScheduleServiceImpl, StarService, StarServiceImpl, ScheduleQueueServiceImpl}
import arisia.timer.{TimerService, TimeServiceImpl, TimerServiceImpl, TimeService, Ticker, TickerImpl}
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

  lazy val adminService: AdminService = wire[AdminServiceImpl]
  lazy val dbService: DBService = wire[DBServiceImpl]
  lazy val loginService: LoginService = wire[LoginServiceImpl]
  lazy val scheduleQueueService: ScheduleQueueService = wire[ScheduleQueueServiceImpl]
  lazy val scheduleService: ScheduleService = wire[ScheduleServiceImpl]
  lazy val starService: StarService = wire[StarServiceImpl]
  lazy val ticker: Ticker = wire[TickerImpl]
  lazy val timeService: TimeService = wire[TimeServiceImpl]
  lazy val timerService: TimerService = wire[TimerServiceImpl]
}
