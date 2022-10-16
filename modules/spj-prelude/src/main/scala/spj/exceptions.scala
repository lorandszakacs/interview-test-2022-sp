package spj

import org.tpolecat.sourcepos.SourcePos

import scala.annotation.targetName
import scala.util.control.NoStackTrace

/** A much better version of the method if you ask me :) points to the source position, doesn't go through an annoying
  * stack trace to find where you forgot to implement things
  */
@targetName("unimplementedException")
def ???(using pos: SourcePos): Nothing = throw Anomaly.unimplemented

@targetName("unimplementedExceptionF")
def ????[F[_], A](using pos: SourcePos, F: ApplicativeThrow[F]): F[A] = F.raiseError(Anomaly.unimplemented)

/** Provides straightforward way of wrapping unknown Throwables, useful at the boundaries of our application
  */
extension (t: Throwable) {
  def anomaly: Anomaly = t match {
    case a: Anomaly => a
    case _          => Anomaly.unknownAnomaly(t)
  }
}

/** We could further refine the API, obviously. But the point I wanted to illustrate is that in projects I do like do
  * better define the domain of the exceptions as well.
  *
  * I find it limiting to rely directly on standard library exceptions to express what goes on in your application.
  *
  * It's better to define "this is how we do exceptions in this project" once, and refactor + add functionality from
  * there when necessary.
  *
  * And this approach works very well with our "prelude", because our exceptions are now part of the "stadard library"
  * of this project! => good dev UX.
  *
  * Potential improvements:
  *   - add pos: SourcePos to all Anomaly types. this can be trivially done without a source breaking change in
  *     downstream code by adding a (using pos: SourcePos) to all methods
  */
sealed abstract class Anomaly(
    message: String,
    cause: Option[Throwable]
) extends Throwable(message, cause.orNull)
    with NoStackTrace

open class InvalidInput(message: String, val cause: Option[Throwable]) extends Anomaly(message, Option.empty)

open class NotFound(message: String) extends Anomaly(message, Option.empty)

open class Unknown(message: String, val cause: Throwable) extends Anomaly(message, Option(cause))

open class InconsistentState(message: String, val cause: Option[Throwable]) extends Anomaly(message, cause)

open class Unimplemented(pos: SourcePos) extends Anomaly(s"Unimplemented @ $pos", Option.empty)

/** The reason why these methods return Throwable, instead of Anomaly is because type inference routinely will fail for
  * things for similar shape to that of cats.MonadThrow. The reason is that it's invariant in its error type :( Meaning
  * that cats.syntax.all.* related to errors will barely work since all syntax is expressed in terms of type classes,
  * not concrete types.
  *
  * Sometimes in real software we hack around libraries to give ourselves a nice api.
  */
object Anomaly {
  def unknown(cause: Throwable): Throwable = unknownAnomaly(cause)

  def unknownAnomaly(cause: Throwable): Anomaly =
    new Unknown(s"An unknown error occurred. Caused by: ${cause.getMessage}", cause)

  def invalidInput(message: String): Throwable = new InvalidInput(message = message, cause = Option.empty)
  def invalidInput(message: String, cause: Throwable): Throwable = new InvalidInput(message, Option(cause))

  def notFound(message: String) = new NotFound(message)

  /** This always represents a bug. Usually arises due to the fact that we can't express everything we want in our type
    * system For instance, doing a write to the DB + a read, you "know" that the read should always return, but if it
    * doesn't, that's definitely a bug.
    */
  def inconsistent(message: String): Throwable = new InconsistentState(message = message, cause = Option.empty)

  def unimplemented(using pos: SourcePos): Throwable = new Unimplemented(pos)
}
