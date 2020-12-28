package arisia.zoom

import arisia.zoom.models.{ZoomUser, ZoomMeeting, ZoomMeetingType}
import play.api.libs.json.{Format, Json}
import play.api.{Configuration, Logging}
import play.api.libs.ws._

import scala.concurrent.{Future, ExecutionContext}
import scala.util.Random

/**
 * Logical layer over the Zoom API.
 *
 * This doesn't try to impose any semantics -- it is just a relatively thin pass-through.
 */
trait ZoomService {
  def getUsers(): Future[List[ZoomUser]]

  def startMeeting(topic: String): Future[ZoomMeeting]
}

private[zoom] case class ZoomMeetingParams(
  topic: String,
  `type`: Int,
  password: String
)
object ZoomMeetingParams {
  implicit val fmt: Format[ZoomMeetingParams] = Json.format
}

class ZoomServiceImpl(
  config: Configuration,
  ws: WSClient,
  jwtService: JwtService
)(
  implicit ec: ExecutionContext
) extends ZoomService
  with Logging
{

  lazy val baseUrl: String = config.get[String]("arisia.zoom.api.baseUrl")
  lazy val zoomUserId: String = config.get[String]("arisia.zoom.api.userId")

  def urlWithJwt(url: String): WSRequest = {
    val jwt = jwtService.getJwtToken()
    ws.url(s"$baseUrl$url")
      .addHttpHeaders(
        ("Authorization", jwt)
      )
  }

  def getUsers(): Future[List[ZoomUser]] = {
    urlWithJwt("/users")
      .get()
      .map { response =>
        // TODO: remove the log, and make the response real.
        logger.info(s"The response from Zoom is $response, body ${response.body}")
        List.empty
      }
  }

  def startMeeting(topic: String): Future[ZoomMeeting] = {
    // Generate a random password for this meeting:
    val password = Random.alphanumeric.take(10).mkString
    val params = ZoomMeetingParams(
      topic,
      ZoomMeetingType.Instant,
      password
    )
    urlWithJwt(s"/users/$zoomUserId/meetings")
      .post(Json.toJson(params))
      .map { response =>
        // TODO: handle errors more correctly, and remove the logging here:
        logger.info(s"The response from Zoom is $response, body ${response.body}")
        Json.parse(response.body).as[ZoomMeeting]
      }
  }
}

class DisabledZoomService() extends ZoomService {
  def getUsers(): Future[List[ZoomUser]] = ???
  def startMeeting(topic: String): Future[ZoomMeeting] = ???
}
