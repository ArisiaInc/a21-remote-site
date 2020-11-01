package controllers

import com.softwaremill.macwire._
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

trait ControllerModule {
  implicit def executionContext: ExecutionContext
  def controllerComponents: ControllerComponents

  lazy val homeController: HomeController = wire[HomeController]
}
