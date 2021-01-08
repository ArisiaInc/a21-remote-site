package arisia

import akka.actor.ActorSystem
import arisia.admin.{RoomServiceImpl, RoomService, AdminServiceImpl, AdminService}
import arisia.auth.{CMService, CMServiceImpl, LoginService, LoginServiceImpl}
import arisia.db.{DBServiceImpl, DBService}
import arisia.discord.{DiscordService, DiscordServiceImpl}
import arisia.fun.{DuckServiceImpl, DuckService}
import arisia.general.{LifecycleServiceImpl, LifecycleService, SettingsService, SettingsServiceImpl}
import com.softwaremill.macwire.wire
import arisia.schedule.{ScheduleService, ScheduleQueueService, ScheduleServiceImpl, StarService, StarServiceImpl, ScheduleQueueServiceImpl}
import arisia.timer.{TimerService, TimeServiceImpl, TimerServiceImpl, TimeService, Ticker, TickerImpl}
import arisia.zoom.ZoomModule
import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

/**
 * This is the catch-all Module for instantiating services that don't really need a Module of their own.
 */
trait GeneralModule extends ZoomModule {
  implicit def executionContext: ExecutionContext
  def configuration: Configuration
  def wsClient: WSClient
  def actorSystem: ActorSystem

  lazy val adminService: AdminService = wire[AdminServiceImpl]
  lazy val cmService: CMService = wire[CMServiceImpl]
  lazy val dbService: DBService = wire[DBServiceImpl]
  lazy val discordService: DiscordService = wire[DiscordServiceImpl]
  lazy val duckService: DuckService = wire[DuckServiceImpl]
  lazy val lifecycleService: LifecycleService = wire[LifecycleServiceImpl]
  lazy val loginService: LoginService = wire[LoginServiceImpl]
  lazy val roomService: RoomService = wire[RoomServiceImpl]
  lazy val scheduleQueueService: ScheduleQueueService = wire[ScheduleQueueServiceImpl]
  lazy val scheduleService: ScheduleService = wire[ScheduleServiceImpl]
  lazy val settingsService: SettingsService = wire[SettingsServiceImpl]
  lazy val starService: StarService = wire[StarServiceImpl]
  lazy val ticker: Ticker = wire[TickerImpl]
  lazy val timeService: TimeService = wire[TimeServiceImpl]
  lazy val timerService: TimerService = wire[TimerServiceImpl]
}
