package spj

import spj.*
import spj.config.{*, given}
import spj.db.*

case class HttpConfig(
    host: Host,
    port: Port
)

/** With time, add all configs required to run server here
  */
case class SpjConfig(
    dbConfig: DbConfig,
    httpConfig: HttpConfig,
    environment: SpjConfig.Environment
)

object SpjConfig {
  sealed trait Environment

  object Environment {
    case object Local extends Environment
    case object Test extends Environment
  }

  /** @param environment
    *   For prod setup we'd also load this value from config. But we have no prod so :shrug:
    */
  def load[F[_]](environment: Environment)(using config: ConfigLoader[F]): Resource[F, SpjConfig] = {
    Resource.eval(config.load(allConfig(environment)))
  }

  private def allConfig(e: Environment): ConfigValue[CirisEffect, SpjConfig] = {
    (dbConfigValue(e), httpConfigValue, default[Environment](e)).parMapN(SpjConfig.apply)
  }

  /** Default values are the same as the ones for running docker locally. See ./ops/docker-postgresql.sh */
  private def dbConfigValue(e: Environment): ConfigValue[CirisEffect, DbConfig] =
    (
      default(host"localhost"),
      e match {
        case Environment.Local => default(port"25432")
        case Environment.Test  => default(port"25431")
      },
      parsedDefault[DbUser]("spj_server"),
      parsedDefault[DbPlaintextPassword]("spj_local_password"),
      parsedDefault[DbName]("json_schemas")
    ).parMapN(DbConfig.apply)

  private def httpConfigValue: ConfigValue[CirisEffect, HttpConfig] =
    (
      default(host"0.0.0.0"),
      default(port"11321")
    ).parMapN(HttpConfig.apply)

}
