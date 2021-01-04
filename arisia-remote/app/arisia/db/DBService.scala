package arisia.db

import doobie._
import doobie.implicits._
import cats._
import cats.effect._
import cats.implicits._
import play.api.{Configuration, Logging}

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success}

/**
 * This service does the actual interfacing with the database.
 *
 * In other services, you define SQL queries as needed -- they should then call DBService.run() to actually
 * execute them.
 *
 * We are using Doobie as the database engine. For additional information, see:
 *
 *   https://tpolecat.github.io/doobie/index.html
 *
 * Suffice it to say, Doobie is one of the best DB interfaces for Scala. It is strongly-typed and prevents you
 * from easily shooting yourself in the foot or committing security violations, while still providing an
 * interface that looks and feels very much like writing ordinary SQL.
 *
 * Note that the database itself is defined via Play Evolutions, an unrelated mechanism. The DDL is defined in
 * the files in conf/evolutions/default. That runs at startup, so the DB should be fully designed by the time
 * we get to this code.
 */
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
) extends DBService with Logging {
  implicit val nonBlockingCS = IO.contextShift(ec)

  val xa = Transactor.fromDriverManager[IO](
    config.get[String]("db.default.driver"),
    config.get[String]("db.default.url")
  )

  def run[T](op: ConnectionIO[T]): Future[T] = {
    val io = op.transact(xa)
    io.unsafeToFuture().andThen {
      // Log all DB errors:
      case Failure(th) => logger.error(s"Error from DB: $th")
      case Success(value) =>
    }
  }
}
