package arisia.zoom

import arisia.zoom.models.ZoomUser
import play.api.{Configuration, Logging}
import play.api.libs.ws.WSClient

import scala.concurrent.{Future, ExecutionContext}

/**
 * Logical layer over the Zoom API.
 *
 * This doesn't try to impose any semantics -- it is just a relatively thin pass-through.
 */
trait ZoomService {
  def getUsers(): Future[List[ZoomUser]]
}

class ZoomServiceImpl(
  config: Configuration,
  ws: WSClient,
  jwtService: JwtService
)(
  implicit ec: ExecutionContext
) extends ZoomService with Logging {

  lazy val baseUrl: String = config.get[String]("arisia.zoom.api.baseUrl")

  def getUsers(): Future[List[ZoomUser]] = {
    val jwt = jwtService.getJwtToken()
    // TODO: remove
    logger.info(s"The JWT is $jwt")
    ws.url(s"$baseUrl/users")
      .addHttpHeaders(
        ("Authorization", jwt)
      )
      .get()
      .map { response =>
        // TODO: remove the log, and make the response real.
        logger.info(s"The response from Zoom is $response, body ${response.body}")
        List.empty
      }
  }
}

class DisabledZoomService() extends ZoomService {
  def getUsers(): Future[List[ZoomUser]] = ???
}
