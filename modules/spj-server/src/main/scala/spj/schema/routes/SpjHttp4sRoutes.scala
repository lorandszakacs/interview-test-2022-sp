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
          resp <- upload match {
            case _: ApiResponse.UploadSuccess => Created(jsonBody(upload))
            case f: ApiResponse.UploadFailure => failureResponse(upload)(f)
          }
        } yield resp

      case req @ (POST -> Root / "validate" / SchemaIdMatcher(schemaId)) =>
        for {
          rawJson <- req.asJson
          validate <- spjApi.validate(schemaId, rawJson)
          resp <- validate match {
            case _: ApiResponse.ValidateSuccess => Ok(jsonBody(validate))
            case f: ApiResponse.ValidateFailure => failureResponse(validate)(f)
          }
        } yield resp

      case GET -> Root / "schema" / SchemaIdMatcher(schemaId) =>
        for {
          get <- spjApi.get(schemaId)
          resp <- get match {
            case _: ApiResponse.GetSuccess => Ok(jsonBody(get))
            case f: ApiResponse.GetFailure => failureResponse(get)(f)
          }
        } yield resp
    }
  }

  /** Realized that the encoding for the API that I chose makes for bad generic handling of errors... So this is a stop
    * gap without rewriting the encoding.
    *
    * Ideally we could just return F[HappyPath] from all responses, and have a generic error handler do this pattern
    * match here. See Scaladoc on [[SpjApi]]
    */
  private def failureResponse[T: Encoder](body: T)(f: ApiResponse.FailureResponse) = f.message match {
    case _: ConflictAnomaly          => Conflict(jsonBody(body))
    case _: InvalidInputAnomaly      => BadRequest(jsonBody(body))
    case _: InconsistentStateAnomaly => InternalServerError(jsonBody(body))
    case _: UnimplementedAnomaly     => NotImplemented(body)
    case _: UnknownAnomaly           => InternalServerError(jsonBody(body))
    case _: NotFoundAnomaly          => NotFound(jsonBody(body)) // arguably we should return no body on not found
  }
}
object SpjHttp4sRoutes {
  def make[F[_]](spjApi: SpjApi[F])(using F: Concurrent[F]): Resource[F, SpjHttp4sRoutes[F]] =
    new SpjHttp4sRoutes[F](spjApi).pure[Resource[F, *]]
}
