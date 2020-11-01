package controllers

import play.api.mvc.{BaseController, ControllerComponents, EssentialAction}

import scala.concurrent.Future

class LoginController (val controllerComponents: ControllerComponents) extends BaseController {
  def login(): EssentialAction = Action.async { implicit request =>
    Future.successful(Ok(""))
  }
}
