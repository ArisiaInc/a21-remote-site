package arisia.models

import arisia.util._
import play.api.libs.json.{Format, Json}

case class ProgramPersonId(v: String) extends StdString
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
  implicit val fmt: Format[ProgramPersonName] = Json.format
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
  implicit val fmt: Format[ProgramPerson] = Json.format
}
