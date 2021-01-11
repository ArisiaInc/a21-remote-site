package arisia.general

import enumeratum.values._

sealed abstract class FileType(val value: String) extends StringEnumEntry

object FileType extends StringPlayEnum[FileType] {
  case object ArtshowMetadata extends FileType("artshow")
  case object DealersMetadata extends FileType("dealers")

  val values = findValues
}
