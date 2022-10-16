package spj.schema

import io.circe.Encoder
import io.circe.Json
import spj.*

type ValidatedJson = ValidatedJson.Type
object ValidatedJson extends monix.newtypes.Newsubtype[Json] {
  given Encoder[ValidatedJson] = derive[Encoder]
  def validate[F[_]: ApplicativeThrow](rawJson: Json, schema: JsonSchema): F[ValidatedJson] = ????[F, ValidatedJson]
}
