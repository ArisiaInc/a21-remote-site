package arisia.controllers

import arisia.GeneralModule
import arisia.auth.LoginService
import arisia.schedule.ScheduleService
import arisia.zoom.ZoomModule
import com.softwaremill.macwire._
import controllers.Assets
import play.api.Configuration
import play.api.http.HttpErrorHandler
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

trait ControllerModule extends GeneralModule with ZoomModule {
  implicit def executionContext: ExecutionContext
  def controllerComponents: ControllerComponents
  def assets: Assets
  def httpErrorHandler: HttpErrorHandler
  def configuration: Configuration

  lazy val adminController: AdminController = wire[AdminController]
  lazy val discordController: DiscordController = wire[DiscordController]
  lazy val duckController: DuckController = wire[DuckController]
  lazy val fileController: FileController = wire[FileController]
  lazy val frontendController: FrontendController = wire[FrontendController]
  lazy val loginController: LoginController = wire[LoginController]
  lazy val ribbonController: RibbonController = wire[RibbonController]
  lazy val scheduleController: ScheduleController = wire[ScheduleController]
  lazy val settingsController: SettingsController = wire[SettingsController]
  lazy val zoomController: ZoomController = wire[ZoomController]

  lazy val fakeZambiaController: FakeZambiaController = wire[FakeZambiaController]
  lazy val scheduleTestController: ScheduleTestController = wire[ScheduleTestController]
}
