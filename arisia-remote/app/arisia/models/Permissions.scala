package arisia.models

/**
 * Any extra permissions that a logged-in user might have.
 *
 * This does *not* exist for most users: it basically represents someone who can do more than a normal attendee.
 * It corresponds closely to the `permissions` table in the database.
 *
 * @param superAdmin Top-level admins, who can create other admins
 * @param admin Permits access to the Admin pages
 * @param earlyAccess Allows this user to log into the site before we open it to all current members
 */
case class Permissions(superAdmin: Boolean, admin: Boolean, earlyAccess: Boolean) {
  lazy val hasEarlyAccess = superAdmin || admin || earlyAccess
}

object Permissions {
  val empty = Permissions(false, false, false)
}
