package arisia

import arisia.auth.{LoginService, LoginServiceImpl}
import arisia.db.{DBServiceImpl, DBService}
import com.softwaremill.macwire.wire
import arisia.schedule.{ScheduleServiceImpl, ScheduleService}
import play.api.Configuration

import scala.concurrent.ExecutionContext

/**
 * This is the catch-all Module for instantiating services that don't really need a Module of their own.
 */
trait GeneralModule {
  implicit def executionContext: ExecutionContext
  def configuration: Configuration

  lazy val scheduleService: ScheduleService = wire[ScheduleServiceImpl]
  lazy val loginService: LoginService = wire[LoginServiceImpl]
  lazy val dbService: DBService = wire[DBServiceImpl]
}
