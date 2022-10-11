package spj

package object schema {
  // I am tempted to just use scala 3 so I can write this as a top-level definition next to its companion
  type SchemaId = SchemaId.Type
  type JsonSchema = JsonSchema.Type
}
