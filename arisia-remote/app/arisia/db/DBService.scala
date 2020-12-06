package arisia.db

import doobie._
import doobie.implicits._
import cats._
import cats.effect._
import cats.implicits._
import play.api.Configuration

import scala.concurrent.{Future, ExecutionContext}

trait DBService {
  /**
   * Given the Doobie description of a database operation, actually run it.
   *
   * Anyone who likes functional programming will throw their hands up in the air here and scream "Why?!?!?".
   * The answer is that Justin is the only FP engineer on this project, and so I'm not going to try to
   * FP-ify the relatively Future-oriented Play systems in our limited available time. If this thing lives
   * on beyond Arisia '21, we might refactor the whole system to be IO-based, but for now we're going to
   * KISS.
   */
  def run[T](op: ConnectionIO[T]): Future[T]
}

class DBServiceImpl(
  config: Configuration
)(
  implicit ec: ExecutionContext
) extends DBService {
  implicit val nonBlockingCS = IO.contextShift(ec)

  val xa = Transactor.fromDriverManager[IO](
    config.get[String]("db.default.driver"),
    config.get[String]("db.default.url")
  )

  def run[T](op: ConnectionIO[T]): Future[T] = {
    val io = op.transact(xa)
    io.unsafeToFuture()
  }
}
