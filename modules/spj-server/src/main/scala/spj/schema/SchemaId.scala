package spj.schema

import spj._

object SchemaId extends SpjNewtypeValidated[String] {
  override def refine[F[_]: ApplicativeThrow](value: String): F[SchemaId] = {
    val trimmed = value.trim
    // not using .blank because that is only available on JDK 11+.
    // Not going to force the usage of a newer java just for this one method,
    // even though JDK 8 should be abandoned by now :P
    if (trimmed.isEmpty) {
      Anomaly.invalidInput("SchemaId cannot be only whitespace").raiseError[F, SchemaId]
    } else {
      this.unsafeCoerce(value).pure[F]
    }

  }
}
