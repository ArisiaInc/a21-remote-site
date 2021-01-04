package arisia.zoom.models

import play.api.libs.json.{Format, Json}

/**
 * The information we get back from Zoom when we create a Meeting.
 *
 * This is not the full data structure: we are only bothering to deserialize fields that we might possibly
 * care about. This is intentionally minimal, so that we can easily store this record in the DB and
 * restore it in memory.
 */
case class ZoomMeeting(
  id: Long,
  start_url: String,
  join_url: String
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
