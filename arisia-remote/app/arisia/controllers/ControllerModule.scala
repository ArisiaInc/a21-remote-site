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

  lazy val loginController: LoginController = wire[LoginController]
  lazy val scheduleController: ScheduleController = wire[ScheduleController]
  lazy val frontendController: FrontendController = wire[FrontendController]
  lazy val adminController: AdminController = wire[AdminController]
  lazy val zoomController: ZoomController = wire[ZoomController]

  lazy val fakeZambiaController: FakeZambiaController = wire[FakeZambiaController]
}
