import cats.effect.kernel as cekernel
import cats.effect as ce

/** Here we define our own "standard library". Alias all types you want here! I highly encourage this to be used and
  * tailored to the team's and projects' needs. This makes for good developer UX, and makes "which import do I make"
  * trivial. Rinse and repeat for other types of library "bundles" (e.g. logging, json, http, rest, etc.). I elided most
  * of these in this small projects for time reasons, but the extra few hours put into doing this in production projects
  * saves time manifold times over the lifetime of the project, additionally it gives us room for customization, extra
  * room to protect ourselves from source-breaking changes in downstream libraries, and it facilitates on-boarding
  *
  * Additionally, I am a strong advocate of using the compiler option "-Yno-imports" because:
  *   - 1) fs2.Stream is my Stream, not scala.Stream
  *   - 2) cats, cats-effect, and fs2 _are_ standard library for any sized project built with typelevel
  *   - 3) monix-newtypes (or your chosen newtype library) is widely used if you're disciplined about newtype-ing
  *     everything
  *   - 4) our structured exception model lives in the same package, and should by any means be our "standard library",
  *     while most java.lang exceptions are not needed at all in typelevel stack projects
  *
  * And in my experience, if you stick to the typelevel stack, and write programs-as-values... you'll find that
  * production projects barely use more of the standard library (baring primitive types) than is already exported
  * here... Which has made me consider that the "no blessed library" approach to language + ecosystem building is good,
  * actually.
  *
  * Some day I'll write a scalafix codegen that just generates such a prelude based on versions of the libraries you
  * have on your classpath.
  */
package object spj {
  export java.lang.String
  export scala.Predef.augmentString

  export java.lang.Throwable

  export scala.Nothing
  type Unit = scala.Unit
  export scala.Int
  export scala.Boolean

  export scala.Predef.ArrowAssoc
  export scala.Option
  export scala.Either
  export scala.Left
  export scala.Right
  type Attempt[+R] = Either[Throwable, R]

  export scala.List

  export cats.ApplicativeThrow
  export cats.Monad
  export cats.syntax.all.*

  export ce.IO
  export cekernel.Concurrent
  export cekernel.Async
  export cekernel.Resource
  export cekernel.MonadCancelThrow
  export ce.syntax.all.*

  export ce.std.Console

  export fs2.Stream
}
