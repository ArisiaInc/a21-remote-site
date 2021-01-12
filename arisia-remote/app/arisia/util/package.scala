package arisia

import scala.concurrent.Future

package object util {
  case object Done
  type Done = Done.type

  def fut[T](t: T) = Future.successful(t)
}
