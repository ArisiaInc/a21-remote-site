package arisia.models

/**
 * Represents the mapping from a Zoom Meeting/Webinar to our semantics.
 */
case class ZoomRoom(
  id: Int,
  displayName: String,
  zoomId: String,
  zambiaName: String,
  discordName: String,
  isManual: Boolean,
  isWebinar: Boolean
)
object ZoomRoom {
  val empty = ZoomRoom(0, "", "", "", "", false, false)
}
