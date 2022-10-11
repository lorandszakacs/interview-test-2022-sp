package spj.api

import io.circe.Json
import spj.*
import spj.schema.*

trait SpjAPI[F[_]] {
  def upload(schemaId: SchemaId, schema: JsonSchemaUserInput): F[APIResponse.Upload]
  def get(schemaId: SchemaId): F[APIResponse.Get]
  def validate(schemaId: SchemaId, rawJson: Json): F[APIResponse.Validate]
}
