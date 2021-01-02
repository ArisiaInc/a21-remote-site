package arisia.models

import arisia.util._

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class ProgramPersonId(v: String) extends StdString {
  def matches(badgeNumber: BadgeNumber): Boolean = {
    v == badgeNumber.v
  }
}
object ProgramPersonId extends StdStringUtils(new ProgramPersonId(_))

/**
 * The details of the name of a program participant.
 *
 * Yes, I object to this format -- it makes some of the classic incorrect programmer mistakes about name structure,
 * and is inherently Western-centric. But it's the data we have.
 */
case class ProgramPersonName(
  first: Option[String],
  last: Option[String],
  prefix: Option[String],
  suffix: Option[String]
)
object ProgramPersonName {
  // The serde for ProgramPersonName is pretty complex, because the JSON format is so weird:
  implicit val reads: Reads[ProgramPersonName] = new Reads[ProgramPersonName] {
    override def reads(json: JsValue): JsResult[ProgramPersonName] = {
      json match {
        case JsArray(jsStrVals) => {
          def namePart(n: Int): Option[String] =
            if (jsStrVals.length > n) {
              val JsString(str) = jsStrVals(n)
              Some(str)
            } else
              None


          JsSuccess(ProgramPersonName(
            namePart(0),
            namePart(1),
            namePart(2),
            namePart(3)
          ))
        }
        case _ => {
          // TODO: log this error!
          JsError()
        }
      }
    }
  }

  implicit val writes: Writes[ProgramPersonName] = new Writes[ProgramPersonName] {
    override def writes(o: ProgramPersonName): JsValue = {
      val strs: Vector[String] = Vector(o.first, o.last, o.prefix, o.suffix).flatten
      new JsArray(strs.map(JsString(_)))
    }
  }
}

case class ProgramPersonTag(v: String) extends StdString
object ProgramPersonTag extends StdStringUtils(new ProgramPersonTag(_))

case class ProgramPersonLink(
  key: String,
  v: String
)
object ProgramPersonLink {
  implicit val fmt: Format[ProgramPersonLink] = Json.format
}

case class ProgramPersonBio(v: String) extends StdString
object ProgramPersonBio extends StdStringUtils(new ProgramPersonBio(_))

case class ProgramPerson(
  id: ProgramPersonId,
  name: Option[ProgramPersonName],
  tags: List[ProgramPersonTag],
  prog: List[ProgramItemId],
  links: List[ProgramPersonLink],
  bio: Option[ProgramPersonBio]
)
object ProgramPerson {
  implicit val fmt: Format[ProgramPerson] = (
    (JsPath \ "id").format[ProgramPersonId] and
      (JsPath \ "name").formatNullable[ProgramPersonName] and
      (JsPath \ "tags").formatWithDefault[List[ProgramPersonTag]](List.empty) and
      (JsPath \ "prog").formatWithDefault[List[ProgramItemId]](List.empty) and
      (JsPath \ "links").formatWithDefault[List[ProgramPersonLink]](List.empty) and
      (JsPath \ "bio").formatNullable[ProgramPersonBio]
  )(ProgramPerson.apply, unlift(ProgramPerson.unapply))
}
