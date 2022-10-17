package spj.db
import com.comcast.ip4s.*
import spj.*

type DbUser = DbUser.Type
object DbUser extends SpjNewsubtype[String]

type DbPlaintextPassword = DbPlaintextPassword.Type
object DbPlaintextPassword extends SpjNewsubtype[String]

type DbName = DbName.Type
object DbName extends SpjNewsubtypeValidated[String] {
  override def refine[F[_]: ApplicativeThrow](value: String): F[DbName] = {
    // TODO: can add more validations, but this is here to prove a point
    if (value.trim.isEmpty)
      Anomaly.invalidInput("database name cannot be empty").raiseError[F, DbName]
    else if (value.length > 64)
      Anomaly.invalidInput("database name cannot be longer than 64 characters").raiseError[F, DbName]
    else unsafeCoerce(value).pure[F]
  }
}

final case class DbConfig(
    host: Host,
    port: Port,
    user: DbUser,
    password: DbPlaintextPassword,
    dbName: DbName
)
