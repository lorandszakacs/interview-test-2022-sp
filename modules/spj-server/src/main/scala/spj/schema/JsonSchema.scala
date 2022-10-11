package spj.schema

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.github.fge.jsonschema.processors.syntax.SyntaxValidator
import io.circe.Json
import spj.*

import scala.jdk.CollectionConverters.*

type JsonSchema = JsonSchema.Type
object JsonSchema extends SpjNewtypeValidated[Json] {

  // docs says it's thread safe, so we can only initialize it once
  private val jsdkObjectMapper: ObjectMapper = new ObjectMapper()
  private val syntaxValidator: SyntaxValidator = JsonSchemaFactory.byDefault().getSyntaxValidator

  override def refine[F[_]: ApplicativeThrow](value: Json): F[Type] = {
    // converting from circe to string to parse to jackson really is inefficient.
    // we could use circe-json-schema to avoid this. Or we could just newtype a raw string. At least for the purposes
    // of this app it wouldn't matter much.
    // But I guess I'll have to pick up maintenance on circe-json-schema
    val jsonNode: JsonNode = jsdkObjectMapper.readTree(value.noSpacesSortKeys)
    val report = syntaxValidator.validateSchema(jsonNode)
    if (report.isSuccess) unsafeCoerce(value).pure[F]
    else Anomaly.invalidInput(report.iterator().asScala.map(msg => msg.getMessage).mkString(";")).raiseError[F, Type]
  }
}
