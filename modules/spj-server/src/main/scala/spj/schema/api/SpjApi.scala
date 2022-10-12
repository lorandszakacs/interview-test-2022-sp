package spj.schema.api

import io.circe.Json
import skunk.Session
import spj.*
import spj.schema.*

/** Not entirely happy with this interface and the encoding of [[ApiResponse]]. It's clunky, bulky, but it does get the
  * job done. It definitely needs rethinking if this would be a more general pattern as the app grows. Had the API
  * responses been a bit different, say something like:
  * {{{
  *   {
  *      "action" : "X",
  *      "status: "success/error"
  *      "body" : {...}
  *   }
  * }}}
  * We could have returned directly F[SomeCaseClass], and the middleware which fills in:
  *   - "action" from the path, you can pattern match on the entire Uri to recreate the "validateSchema" name too :)
  *     which would not be reflected in just the Uri
  *   - "status" : "success" if success, and just inlined the Json produced by Encoder[SomeCaseClass] into "body"
  *   - "status" : "error" if failed, and put the Json produced Encoder[Anomaly] into "body" + return appropriate status
  *     code based on the specific subtype of [[Anomaly]]
  *
  * This would give consumers of the API a more predictable structure on which to build on as well. Of course, this
  * would have made generating a client that consumes this API more difficult, since not all information is expressed in
  * the types
  */
trait SpjApi[F[_]] {
  def upload(schemaId: SchemaId, schema: JsonSchemaUserInput): F[ApiResponse.Upload]
  def get(schemaId: SchemaId): F[ApiResponse.Get]
  def validate(schemaId: SchemaId, rawJson: Json): F[ApiResponse.Validate]
}

object SpjApi {

  /** Why return a Resource even though it's .pure? Well, for this project it's highly unlikely that this will ever not
    * be a pure value.
    *
    * But, consistently creating everything as a Resource, forces us to wire things together in ways in which the
    * lifecycles of all components are explicitly declared, while allowing us to arbitrarily add various things to this
    * later, e.g. caches, fiber supervisors for "fire-and-forget" actions, cron jobs attached to this service, etc. As a
    * real application evolves, more and more of these things pop up. If your tests, and your production app are already
    * wired up with Resource, then adding anything becomes easy.
    *
    * Essentially it's about paying a _tiny_ upfront cost for the ability to extend the system in the future.
    * Additionally, part of this boilerplate can be reduced by writing project-specific scalafix codegen to generate
    * companion objects, and wiring things together can be made easier using softwaremill's macwire which is luckily
    * syntactic sugar over .flatMap and passing parameters :) and doesn't do any magic. In my career I have learned to
    * avoid magic at initialization time, I will unapologetically keep this stance. I have seen guice, play, loading
    * modules from xml, and who knows whatever "DI" consistently cause _bugs_ in large software, or make running
    * arbitrarily complex initialization logic head-achingly painful, passing parameters and .flatMap is at most
    * "boring", and allows us to do almost anything. I'll take boring over bugs any day.
    */
  def server[F[_]](db: Resource[F, Session[F]])(using F: MonadCancelThrow[F]): Resource[F, SpjApi[F]] = for {
    _ <- Resource.unit[F]
    // eventually I'll create a logger here xD I promise
  } yield new SpjApi[F] {

    /** A "non-failing" variant of our F. Did I have to do this? Not really, it's local to the implementation, and is
      * not visible in the API (that would be cool). I haven't figured out how to make it work with any F[_].
      *
      * BUT! you can create an NIO which is an IO using this same pattern, add that to your spj.* package, and while it
      * doesn't offer the best UX in all cases, you can flatMap two NIO and get back an NIO which is pretty cool :)
      *
      * Since we don't have a BIO, we can at least sort-of make a bootleg NIO :D
      *
      * Improvements: I forgot the name of the thing that for ``SomeTrait[F[_]]`` it generates a .mapK method on the
      * companion that would allow us to apply F ~> NF easily. If this thing does not exist, someone should definitely
      * write one :)
      */
    type NF[A] = NF.Type[A]

    object NF extends monix.newtypes.NewsubtypeK[F] {
      extension [A](a: F[A]) {
        def unfailing(f: Throwable => A): NF[A] = unsafeCoerce(a.handleError(f))
      }
    }
    import NF.unfailing

    override def upload(schemaId: SchemaId, schema: JsonSchemaUserInput): NF[ApiResponse.Upload] = db
      .use { session =>
        ????[F, ApiResponse.Upload]
      }
      .unfailing(e => ApiResponse.UploadFailure(schemaId, e.anomaly))

    override def get(schemaId: SchemaId): NF[ApiResponse.Get] = db
      .use { session =>
        ????[F, ApiResponse.Get]
      }
      .unfailing(e => ApiResponse.GetFailure(schemaId, e.anomaly))

    override def validate(schemaId: SchemaId, rawJson: Json): NF[ApiResponse.Validate] = db
      .use { session =>
        ????[F, ApiResponse.Validate]
      }
      .unfailing(e => ApiResponse.ValidateFailure(schemaId, e.anomaly))

  }

  // def client[F[_]](root: Uri): Resource[F, SpjAPI] = ???
  // could be used to implement the interface using an http client, and would be rather useful for integration tests.
  // going to elide this in this test, and instead we'll be more explicitly encoding the requests in tests to make sure that
  // they match up with the spec. But had we used smithy4s, or tapir to define our API, then this client implementation
  // would have been trivial.
}
