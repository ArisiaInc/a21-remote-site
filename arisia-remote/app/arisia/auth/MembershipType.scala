package arisia.auth

import enumeratum.values._

sealed abstract class MembershipType(val value: String) extends StringEnumEntry

object MembershipType extends StringEnum[MembershipType] {
  // The membership types we anticipate maybe getting from Convention Master, based on what I can see in the
  // CM database.
  // TODO: these should be in data, not code. A redundant local version of the CM event_membership_types table,
  // maybe? Or something like that. But for now, we'll just code it.
  case object NoMembership extends MembershipType("")
  case object Adult extends MembershipType("VIRTAD")
  case object FastTrack extends MembershipType("VIRTFT")
  case object Teen extends MembershipType("VIRTTN")
  case object AdultScholarship extends MembershipType("VRTAD0")
  case object AdultSustaining extends MembershipType("VRTADS")
  case object FastTrackScholarship extends MembershipType("VRTFT0")
  case object FastTrackSustaining extends MembershipType("VRTFTS")
  case object TeenScholarship extends MembershipType("VRTTN0")
  case object TeenSustaining extends MembershipType("VRTTNS")

  val values = findValues
}
