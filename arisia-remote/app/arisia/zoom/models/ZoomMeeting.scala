package arisia.zoom.models

import play.api.libs.json.{Format, Json}

/**
 * The information we get back from Zoom when we create a Meeting.
 *
 * This is not the full data structure: we are only bothering to deserialize fields that we might possibly
 * care about.
 */
case class ZoomMeeting(
  id: Long,
  topic: String,
  created_at: String,
  start_url: String,
  join_url: String,
  password: String
)
object ZoomMeeting {
  implicit val fmt: Format[ZoomMeeting] = Json.format
}

object ZoomMeetingType {
  final val Instant = 1
  final val Scheduled = 2
  final val RecurringUnfixed = 3
  final val RecurringFixed = 8
}
