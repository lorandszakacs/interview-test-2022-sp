package spj.schema

import io.circe.Json
import spj.*

/** This is what users upload
  */
type JsonSchemaUserInput = JsonSchemaUserInput.Type
object JsonSchemaUserInput extends SpjNewtype[Json]
