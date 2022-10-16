package spj.schema

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.github.fge.jsonschema.processors.syntax.SyntaxValidator
import io.circe.{Json, Decoder, Encoder}
import spj.*

import scala.jdk.CollectionConverters.*

type JsonSchema = JsonSchema.Type
object JsonSchema extends SpjNewtypeValidated[Json] {
  given Encoder[JsonSchema] = derive[Encoder]
  given Decoder[JsonSchema] = derive[Decoder]

  // docs says it's thread safe, so we can only initialize it once
  private val jsdkObjectMapper: ObjectMapper = new ObjectMapper()
  private val syntaxValidator: SyntaxValidator = JsonSchemaFactory.byDefault().getSyntaxValidator

  override def refine[F[_]: ApplicativeThrow](input: Json): F[Type] = {
    // it is a waste to make the conversion from circe -> jackson
    // for the purposes of this simple app we could just newtype raw Strings
    // but that isn't ideal for production either.
    // The ideal solution performance-wise would be:
    // - to use circe-json-schema. Guess I'll have to pick up some maintenance on that.
    // - use jackson node everywhere... which we could pull off if we newtype-it properly so the java doesn't leak
    val jsonNode: JsonNode = jsdkObjectMapper.readTree(input.noSpacesSortKeys)
    val report = syntaxValidator.validateSchema(jsonNode)
    if (report.isSuccess) unsafeCoerce(input).pure[F]
    else Anomaly.invalidInput(report.iterator().asScala.map(msg => msg.getMessage).mkString(";")).raiseError[F, Type]
  }

  def fromUserInput[F[_]: ApplicativeThrow](input: JsonSchemaUserInput) =
    refine[F](input.value)
}
