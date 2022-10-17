package spj.schema

import io.circe.Encoder
import io.circe.Json
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.{JsonSchemaFactory, JsonValidator}
import com.github.fge.jsonschema.processors.syntax.SyntaxValidator
import spj.*

import scala.jdk.CollectionConverters.*

type ValidatedJson = ValidatedJson.Type
object ValidatedJson extends monix.newtypes.Newsubtype[Json] {
  given Encoder[ValidatedJson] = derive[Encoder]

  private val jsdkObjectMapper: ObjectMapper = new ObjectMapper()
  private val validator: JsonValidator = JsonSchemaFactory.byDefault().getValidator

  def validate[F[_]: ApplicativeThrow](rawJson: Json, schema: JsonSchema): F[ValidatedJson] = {
    val schemaNode: JsonNode = jsdkObjectMapper.readTree(schema.value.noSpacesSortKeys)
    val cleanedInput = rawJson.deepDropNullValues
    val inputNode: JsonNode = jsdkObjectMapper.readTree(cleanedInput.noSpacesSortKeys)
    val report = validator.validate(schemaNode, inputNode)
    if (report.isSuccess) {
      this.unsafeCoerce(cleanedInput).pure[F]
    } else {
      Anomaly
        .invalidInput(report.iterator().asScala.map(msg => msg.getMessage).mkString(";"))
        .raiseError[F, ValidatedJson]
    }
  }
}
