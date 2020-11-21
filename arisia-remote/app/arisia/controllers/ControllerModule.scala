package arisia.controllers

import arisia.auth.LoginService
import arisia.schedule.ScheduleService
import com.softwaremill.macwire._
import controllers.Assets
import play.api.Configuration
import play.api.http.HttpErrorHandler
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

trait ControllerModule {
  implicit def executionContext: ExecutionContext
  def controllerComponents: ControllerComponents
  def loginService: LoginService
  def scheduleService: ScheduleService
  def assets: Assets
  def httpErrorHandler: HttpErrorHandler
  def configuration: Configuration

  lazy val homeController: HomeController = wire[HomeController]
  lazy val loginController: LoginController = wire[LoginController]
  lazy val scheduleController: ScheduleController = wire[ScheduleController]
  lazy val frontendController: FrontendController = wire[FrontendController]
}
