package spj.routes

import io.circe.*
import io.circe.syntax.*
import spj.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.circe.*
import spj.api.*
import spj.schema.*

/** For larger projects I would use something like tapir, or smithy4s for defining APIs, but the ceremony to get them
  * just right for production use is a bit too much for this tiny project, so I'll elide it. I do have examples of using
  * tapi public though:
  */
final class SpjHttp4sRoutes[F[_]](
    spjAPI: SpjAPI[F]
)(using F: Concurrent[F]) {
  private val dslF = Http4sDsl.apply[F]
  import dslF.{*, given}
  import CirceEntityEncoder.*

  private object SchemaIdMatcher {
    def unapply(str: String): Option[SchemaId] = SchemaId.refine[Attempt](str).toOption
  }
  def apply: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req @ (POST -> Root / "schema" / SchemaIdMatcher(schemaId)) =>
        for {
          rawJson <- req.asJson
          upload: APIResponse <- spjAPI.upload(schemaId, JsonSchemaUserInput(rawJson))
          // we could build some machinery to avoid having to call .asJson on every case
          // class, but this is fine. I'd prioritize creating an spj.rest package that brings
          // in json stuff as well and uniformizes the formats of our endpoints for better dev UX
          resp <- Created(upload.asJson)
        } yield resp

      case req @ (PUT -> Root / "schema" / SchemaIdMatcher(schemaId)) =>
        for {
          rawJson <- req.asJson
          validate: APIResponse <- spjAPI.validate(schemaId, rawJson)
          resp <- Ok(validate.asJson)
        } yield resp

      case req @ (GET -> Root / "schema" / SchemaIdMatcher(schemaId)) =>
        for {
          get: APIResponse <- spjAPI.get(schemaId)
          resp <- Ok(get.asJson)
        } yield resp
    }
  }

}
