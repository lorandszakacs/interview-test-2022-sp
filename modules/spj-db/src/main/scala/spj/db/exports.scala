package spj.db

import spj.*
import fs2.io.net.Network
import natchez.Trace
import com.comcast.ip4s.*

export skunk.Session
export skunk.SessionPool
export skunk.~
export skunk.Command
export skunk.Query
export skunk.Decoder
export skunk.Encoder
export skunk.Codec
export skunk.Void
export skunk.implicits.*
export skunk.codec.all.*
export skunk.circe.codec.json.jsonb

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
    else if (value.length <= 64)
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

object SessionPool {
  def make[F[_]](config: DbConfig)(using
      F: Concurrent[F],
      network: Network[F],
      console: Console[F]
  ): SessionPool[F] = {
    given Trace[F] = natchez.Trace.Implicits.noop[F]
    skunk.Session.pooled(
      host = config.host.toString,
      port = config.port.value,
      user = config.user,
      database = config.dbName,
      password = Option(config.password),
      max = 10
    )
  }
}
