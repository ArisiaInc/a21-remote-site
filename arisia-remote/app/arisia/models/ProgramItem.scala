package arisia.models

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalTime}
import scala.jdk.DurationConverters._

import arisia.util._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.concurrent.duration._

case class ProgramItemId(v: String) extends StdString
object ProgramItemId extends StdStringUtils(new ProgramItemId(_))

case class ProgramItemTitle(v: String) extends StdString
object ProgramItemTitle extends StdStringUtils(new ProgramItemTitle(_))

case class ProgramItemTag(v: String) extends StdString
object ProgramItemTag extends StdStringUtils(new ProgramItemTag(_))

case class ProgramItemLoc(v: String) extends StdString
object ProgramItemLoc extends StdStringUtils(new ProgramItemLoc(_))

case class ProgramItemPersonName(v: String) extends StdString
object ProgramItemPersonName extends StdStringUtils(new ProgramItemPersonName(_))

case class ProgramItemPerson(
  id: ProgramPersonId,
  name: ProgramItemPersonName
) {
  def matches(who: LoginUser): Boolean = {
    id.matches(who.badgeNumber)
  }
}
object ProgramItemPerson {
  implicit val fmt: Format[ProgramItemPerson] = Json.format
}

case class ProgramItemDesc(v: String) extends StdString
object ProgramItemDesc extends StdStringUtils(new ProgramItemDesc(_))

case class ProgramItemDate(d: LocalDate)
object ProgramItemDate {
  val dateFormat: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

  implicit val reads: Reads[ProgramItemDate] = JsUtils.stringReads(str => ProgramItemDate(LocalDate.parse(str, dateFormat)))

  implicit val writes: Writes[ProgramItemDate] = new Writes[ProgramItemDate] {
    override def writes(o: ProgramItemDate): JsValue = JsString(dateFormat.format(o.d))
  }
}

case class ProgramItemTime(t: LocalTime)
object ProgramItemTime {
  implicit val reads: Reads[ProgramItemTime] = JsUtils.stringReads(str => ProgramItemTime(LocalTime.parse(str)))

  implicit val writes: Writes[ProgramItemTime] = new Writes[ProgramItemTime] {
    override def writes(o: ProgramItemTime): JsValue = JsString(o.t.toString)
  }
}

case class ProgramItemTimestamp(t: Instant) {
  def toLong: Long = t.toEpochMilli
}
object ProgramItemTimestamp {
  implicit val reads: Reads[ProgramItemTimestamp] =
    JsUtils.stringReads(str => new ProgramItemTimestamp(Instant.parse(str)))

  implicit val writes: Writes[ProgramItemTimestamp] = (o: ProgramItemTimestamp) => JsString(o.t.toString)
}

/**
 * Represents a Program Item, in a way that exactly matches KonOpas.
 *
 * See https://konopas.org/data-fmt for the format we are ingesting to get this.
 *
 * Note that we are playing it cautious, and assuming that basically all fields are optional. The id is the only
 * absolutely-required field, since that's the primary key.
 */
case class ProgramItem(
  id: ProgramItemId,
  title: Option[ProgramItemTitle],
  tags: List[ProgramItemTag],
  date: Option[ProgramItemDate],
  time: Option[ProgramItemTime],
  // TODO: convert this String to Int on the way in and out:
  mins: Option[String],
  loc: List[ProgramItemLoc],
  people: List[ProgramItemPerson],
  desc: Option[ProgramItemDesc],
  timestamp: Option[ProgramItemTimestamp],
  prepFor: Option[ProgramItemId],
  zoomStart: Option[ProgramItemTimestamp],
  zoomEnd: Option[ProgramItemTimestamp],
  doorsOpen: Option[ProgramItemTimestamp],
  doorsClose: Option[ProgramItemTimestamp]
) {
  lazy val when: Instant = {
    timestamp.map(_.t).getOrElse(Instant.MIN)
  }
  lazy val duration: FiniteDuration = {
    mins.map(_.toInt.minutes).getOrElse(0.minutes)
  }
  lazy val end: Instant = when.plus(duration.toJava)
}
object ProgramItem {
  implicit val fmt: Format[ProgramItem] = (
    (JsPath \ "id").format[ProgramItemId] and
      (JsPath \ "title").formatNullable[ProgramItemTitle] and
      (JsPath \ "tags").formatWithDefault[List[ProgramItemTag]](List.empty) and
      (JsPath \ "date").formatNullable[ProgramItemDate] and
      (JsPath \ "time").formatNullable[ProgramItemTime] and
      (JsPath \ "mins").formatNullable[String] and
      (JsPath \ "loc").formatWithDefault[List[ProgramItemLoc]](List.empty) and
      (JsPath \ "people").formatWithDefault[List[ProgramItemPerson]](List.empty) and
      (JsPath \ "desc").formatNullable[ProgramItemDesc] and
      // Everything below here is synthesized by the backend
      (JsPath \ "timestamp").formatNullable[ProgramItemTimestamp] and
      // Set iff this is a prep session
      (JsPath \ "prepFor").formatNullable[ProgramItemId] and
      // The times that the Zoom meetings starts/stops -- only set for prep sessions
      (JsPath \ "zoomStart").formatNullable[ProgramItemTimestamp] and
      (JsPath \ "zoomEnd").formatNullable[ProgramItemTimestamp] and
      (JsPath \ "doorsOpen").formatNullable[ProgramItemTimestamp] and
      (JsPath \ "doorsClose").formatNullable[ProgramItemTimestamp]
    )(ProgramItem.apply, unlift(ProgramItem.unapply))
}
