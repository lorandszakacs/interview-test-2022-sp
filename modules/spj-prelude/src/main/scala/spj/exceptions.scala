package spj

import scala.util.control.NoStackTrace

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
  */
sealed abstract class Anomaly(
    message: String,
    cause: Option[Throwable]
) extends Throwable(message, cause.orNull)
    with NoStackTrace

abstract class InvalidInput(message: String, val cause: Option[Throwable]) extends Anomaly(message, Option.empty)

abstract class Unknown(message: String, val cause: Throwable) extends Anomaly(message, Option(cause))

abstract class InconsistentState(message: String, val cause: Option[Throwable]) extends Anomaly(message, cause)

object Anomaly {
  def unknown(cause: Throwable): Throwable =
    new Unknown(s"An unknown error occurred. Caused by: ${cause.getMessage}", cause) {}

  def invalidInput(message: String): Throwable = new InvalidInput(message = message, cause = Option.empty) {}
  def invalidInput(message: String, cause: Throwable): Throwable = new InvalidInput(message, Option(cause)) {}

  /** This always represents a bug. Usually arises due to the fact that we can't express everything we want in our type
    * system For instance, doing a write to the DB + a read, you "know" that the read should always return, but if it
    * doesn't, that's definitely a bug.
    */
  def inconsistent(message: String): Throwable = new InconsistentState(message = message, cause = Option.empty) {}
}
