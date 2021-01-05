package arisia.auth

import enumeratum.values._
import play.api.libs.json._
import play.api.libs.functional.syntax._

sealed abstract class AgeCategory(val value: String) extends StringEnumEntry

object AgeCategory extends StringEnum[AgeCategory] {
  case object Adult extends AgeCategory("adult")
  case object Teen extends AgeCategory("teen")
  case object Under12 extends AgeCategory("under12")
  case object Comp extends AgeCategory("comp")
  case object UnknownAge extends AgeCategory("unknown")

  val values = findValues
}

sealed abstract class MembershipType(val value: String, val ageCategory: AgeCategory) extends StringEnumEntry

object MembershipType extends StringEnum[MembershipType] {
  import AgeCategory._

  // The membership types we anticipate maybe getting from Convention Master, based on what I can see in the
  // CM database. This list is *not* comprehensive (there are hundreds of types in CM), but should have all the
  // types that we expect to be in use at A'21.
  // TODO: these should be in data, not code. A redundant local version of the CM event_membership_types table,
  // maybe? Or something like that. But for now, we'll just code it.
  // The standard kiosk-purchase types:
  case object NoMembership extends MembershipType("", UnknownAge)
  case object AdultStandard extends MembershipType("VIRTAD", Adult)
  case object FastTrack extends MembershipType("VIRTFT", Under12)
  case object TeenStandard extends MembershipType("VIRTTN", Teen)
  case object AdultScholarship extends MembershipType("VRTAD0", Adult)
  case object AdultSustaining extends MembershipType("VRTADS", Adult)
  case object FastTrackScholarship extends MembershipType("VRTFT0", Under12)
  case object FastTrackSustaining extends MembershipType("VRTFTS", Under12)
  case object TeenScholarship extends MembershipType("VRTTN0", Teen)
  case object TeenSustaining extends MembershipType("VRTTNS", Teen)

  // Various special cases:
  case object Adult55 extends MembershipType("ADT55", Adult)
  case object AdultYearAhead extends MembershipType("ADTYA", Adult)
  case object PartyDiscount extends MembershipType("PTYDSC", Adult)
  case object FastTrackYearAhead extends MembershipType("FTYA", Under12)
  case object Student extends MembershipType("STDNT", UnknownAge)
  case object StudentYA extends MembershipType("STDYA", Teen)
  case object TTYA extends MembershipType("TTYA", Teen)

  // Comps:
  case object EarnedComp extends MembershipType("CMPERN", Comp)
  case object ProgrammingComp extends MembershipType("CMPPRG", Comp)
  case object RolloverComp extends MembershipType("CMPROL", Comp)
  case object GamingComp extends MembershipType("CMPGAM", Comp)
  case object GOHComp extends MembershipType("CMPGOH", Comp)

  val values = findValues

  implicit val fmt: Format[MembershipType] = new Format[MembershipType] {
    override def writes(o: MembershipType): JsValue = JsObject(Seq(
      "value" -> JsString(o.value),
      "ageCategory" -> JsString(o.ageCategory.value)
    ))

    override def reads(json: JsValue): JsResult[MembershipType] = {
      json match {
        case JsObject(o) => {
          val result = for {
            JsString(str) <- o.get("value")
            typ <- withValueOpt(str)
          }
            yield JsSuccess(typ)

          result.getOrElse(JsError())
        }
        case _ => JsError()
      }
    }
  }
}
