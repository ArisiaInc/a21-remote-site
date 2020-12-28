package arisia.zoom

import play.api.Configuration
import play.api.libs.json._
import java.time.{Clock, Duration}

import pdi.jwt.JwtSession

import scala.concurrent.duration.FiniteDuration

/**
 * This little tool deals with creating the JWTs for Zoom.
 *
 * This also tells you whether Zoom integration is enabled locally. (Since it wraps the API Key and Secret, it knows
 * whether you've provided the necessary bits.)
 */
trait JwtService {
  /**
   * Is Zoom integration enabled on this site or not?
   */
  def zoomEnabled: Boolean

  /**
   * Fetches a short-lived, one-shot, JWT for talking to Zoom.
   *
   * IMPORTANT: for security reasons, this JWT doesn't last for long, so only use it for a single request. They
   * aren't so expensive to generate that we need to reuse them.
   */
  def getJwtToken(): String
}

class JwtServiceImpl(
  mainConfig: Configuration
) extends JwtService with DefaultWrites {

  implicit val clock = Clock.systemUTC()

  lazy val apiKey = mainConfig.get[String]("arisia.zoom.api.key")
  lazy val apiSecret = mainConfig.get[String]("arisia.zoom.api.secret")
  lazy val apiTimeout = mainConfig.get[Long]("arisia.zoom.api.timeout")

  /**
   * The JWT library uses the standard Play settings, but we really don't want that here: we want short-lived
   * JWTs, in contrast to the long-lived ones for user session cookies.
   */
  lazy val jwtConfig =
    Configuration(
      "play.http.secret.key" -> apiSecret,
      "play.http.session.privateKey" -> apiSecret,
      "play.http.session.maxAge" -> apiTimeout
    )

  implicit val conf = jwtConfig

  lazy val zoomEnabled: Boolean = apiKey.length > 0 && apiSecret.length > 0

  def getJwtToken(): String = {
    // Note that the expiration is milliseconds in config, but Zoom requires that we specify it in seconds:
    val expSeconds = clock.instant().plus(Duration.ofMillis(apiTimeout)).getEpochSecond
    // Conveniently, Zoom and JwtSession both default to HS256, so we can just use the default constructor:
    // The syntax here looks dumb because multiarg infix is being phased out in Scala, so the way this is
    // intended to be used now generates warnings:
    val session =
      JwtSession().++(("iss", apiKey), ("exp", expSeconds))
    "Bearer " + session.serialize
  }
}
