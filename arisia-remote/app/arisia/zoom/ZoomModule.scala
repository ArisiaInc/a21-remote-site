package arisia.zoom

import play.api.Configuration
import com.softwaremill.macwire.wire

import scala.concurrent.ExecutionContext
import play.api.libs.ws.WSClient

trait ZoomModule {
  implicit def executionContext: ExecutionContext
  def configuration: Configuration
  def wsClient: WSClient

  lazy val jwtService: JwtService = wire[JwtServiceImpl]
  lazy val zoomService: ZoomService = {
    if (jwtService.zoomEnabled)
      wire[ZoomServiceImpl]
    else
      wire[DisabledZoomService]
  }
}
