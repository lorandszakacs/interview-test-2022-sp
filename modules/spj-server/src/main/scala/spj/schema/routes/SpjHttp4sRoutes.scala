package spj.schema.routes

import io.circe.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.*
import spj.*
import spj.schema.*
import spj.schema.api.*

/** For larger projects I would use something like tapir, or smithy4s for defining APIs, but the ceremony to get them
  * just right for production use is a bit too much for this tiny project, so I'll elide it.
  */
final class SpjHttp4sRoutes[F[_]] private (
    spjApi: SpjApi[F]
)(using F: Concurrent[F]) {
  private val dslF = Http4sDsl.apply[F]
  import dslF.{*, given}
  import CirceEntityEncoder.*
  private def jsonBody[A: Encoder](a: A): Json = a.asJson

  private object SchemaIdMatcher {
    def unapply(str: String): Option[SchemaId] = SchemaId.refine[Attempt](str).toOption
  }
  def routes: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req @ (POST -> Root / "schema" / SchemaIdMatcher(schemaId)) =>
        for {
          rawJson <- req.asJson
          upload <- spjApi.upload(schemaId, JsonSchemaUserInput(rawJson))
          // we could build some machinery to avoid having to call .asJson on every case
          // class, but this is fine. I'd prioritize creating an spj.rest package that brings
          // in json stuff as well and uniformizes the formats of our endpoints for better dev UX
          resp <- Created(jsonBody(upload))
        } yield resp

      case req @ (POST -> Root / "validate" / SchemaIdMatcher(schemaId)) =>
        for {
          rawJson <- req.asJson
          validate <- spjApi.validate(schemaId, rawJson)
          resp <- Ok(jsonBody(validate))
        } yield resp

      case GET -> Root / "schema" / SchemaIdMatcher(schemaId) =>
        for {
          get <- spjApi.get(schemaId)
          resp <- Ok(jsonBody(get))
        } yield resp
    }
  }
}
object SpjHttp4sRoutes {
  def make[F[_]](spjApi: SpjApi[F])(using F: Concurrent[F]): Resource[F, SpjHttp4sRoutes[F]] =
    new SpjHttp4sRoutes[F](spjApi).pure[Resource[F, *]]
}
