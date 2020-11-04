package arisia.util

import play.api.libs.json._

import scala.reflect.ClassTag

trait TypeableString[T <: StdString] {
  def stringToT(s: String): T
}

object TypeableString {
  implicit def stdFormat[T <: StdString](implicit typeable: TypeableString[T]): Format[T] =
    Format(
      __.read[String].map(typeable.stringToT(_)),
      Writes(t => JsString(t.v))
    )
}

trait StdString {
  def v: String
  override def toString = v
}

/**
 * This is the "usual" way of representing a strongly-typed String.
 */
class StdStringUtils[T <: StdString : ClassTag](
  toT: String => T,
) {
  implicit val typeable: TypeableString[T] = new TypeableString[T] {
    override def stringToT(s: String) = toT(s)
  }

  implicit val format: Format[T] = TypeableString.stdFormat
}
