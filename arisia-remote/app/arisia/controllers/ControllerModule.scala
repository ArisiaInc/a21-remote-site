package controllers

import arisia.auth.LoginService
import com.softwaremill.macwire._
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

trait ControllerModule {
  implicit def executionContext: ExecutionContext
  def controllerComponents: ControllerComponents
  def loginService: LoginService

  lazy val homeController: HomeController = wire[HomeController]
  lazy val loginController: LoginController = wire[LoginController]
}
