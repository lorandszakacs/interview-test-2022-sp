package spj.schema.api

import io.circe.Encoder
import io.circe.Json
import io.circe.JsonObject
import io.circe.generic.semiauto
import spj.*
import spj.schema.*

sealed trait ApiResponse

object ApiResponse {

  sealed trait FailureResponse {
    def message: Anomaly
  }

  sealed trait Upload extends ApiResponse
  object Upload {
    given Encoder[Upload] = {
      case s: UploadSuccess => UploadSuccess.given_Encoder_T(s)
      case f: UploadFailure => UploadFailure.given_Encoder_T(f)
    }
  }

  /** Example success:
    * {{{
    * {"action":"uploadSchema","id":"config-schema","status":"success"}
    * }}}
    */
  final case class UploadSuccess(id: SchemaId) extends Upload
  object UploadSuccess extends SuccessCompanion[UploadSuccess]("uploadSchema")(semiauto.deriveEncoder)

  /** Example failure:
    * {{{
    * {"action":"uploadSchema","id":"config-schema","status":"error","message":"Invalid JSON"}
    * }}}
    */
  final case class UploadFailure(id: SchemaId, message: Anomaly) extends Upload with FailureResponse
  object UploadFailure extends FailureCompanion[UploadFailure]("uploadSchema")(semiauto.deriveEncoder)

  sealed trait Get extends ApiResponse
  object Get {
    given Encoder[Get] = {
      case s: GetSuccess => GetSuccess.given_Encoder_T(s)
      case f: GetFailure => GetFailure.given_Encoder_T(f)
    }
  }

  /** {{{
    * {"action":"getSchema","id":"config-schema","status":"success","schema":{}}
    * }}}
    */
  final case class GetSuccess(id: SchemaId, schema: JsonSchema) extends Get
  object GetSuccess extends SuccessCompanion[GetSuccess]("getSchema")(semiauto.deriveEncoder)

  final case class GetFailure(id: SchemaId, message: Anomaly) extends Get with FailureResponse
  object GetFailure extends FailureCompanion[GetFailure]("getSchema")(semiauto.deriveEncoder)

  sealed trait Validate extends ApiResponse
  object Validate {
    given Encoder[Validate] = {
      case s: ValidateSuccess => ValidateSuccess.given_Encoder_T(s)
      case f: ValidateFailure => ValidateFailure.given_Encoder_T(f)
    }
  }

  /** N.B.: "validated" is not specified in the problem statement, but without it we just validate the JSON, remove
    * nulls, and then just throw away the value?
    * {{{
    *   {"action": "validateDocument", "id": "config-schema", "status": "success", "validated": {}}
    * }}}
    */
  final case class ValidateSuccess(id: SchemaId, validated: ValidatedJson) extends Validate
  object ValidateSuccess extends SuccessCompanion[ValidateSuccess]("validateDocument")(semiauto.deriveEncoder)

  final case class ValidateFailure(id: SchemaId, message: Anomaly) extends Validate with FailureResponse
  object ValidateFailure extends FailureCompanion[ValidateFailure]("validateDocument")(semiauto.deriveEncoder)

  private[ApiResponse] sealed trait SuccessCompanion[T <: ApiResponse](action: String)(enc: Encoder[T]) {
    private lazy val constant = Json.fromJsonObject(
      JsonObject(
        "action" -> Json.fromString(action),
        "status" -> Json.fromString("success")
      )
    )

    given Encoder[T] = enc.mapJson(_.deepMerge(constant))
  }

  private given Encoder[Anomaly] = Encoder { anomaly =>
    Json.fromJsonObject(
      JsonObject(
        "details" -> Json.fromString(anomaly.getMessage),
        "errorType" -> Json.fromString(anomaly.getClass.getCanonicalName)
      )
    )
  }

  private[ApiResponse] sealed trait FailureCompanion[T <: ApiResponse](action: String)(enc: Encoder[T]) {
    private lazy val constant = Json.fromJsonObject(
      JsonObject(
        "action" -> Json.fromString(action),
        "status" -> Json.fromString("error")
      )
    )

    given Encoder[T] = enc.mapJson(_.deepMerge(constant))
  }
}
