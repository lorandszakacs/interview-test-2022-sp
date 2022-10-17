package spj

import spj.*
import spj.config.{*, given}
import spj.db.*

/** With time, add all configs required to run server here
  */
case class SpjConfig(
    dbConfig: DbConfig
)

object SpjConfig {
  def load[F[_]](using config: ConfigLoader[F]): Resource[F, SpjConfig] = {
    Resource.eval(config.load(dbConfigValue.map(SpjConfig.apply)))
  }

  /** Default values are the same as the ones for running docker locally. */
  private val dbConfigValue: ConfigValue[CirisEffect, DbConfig] =
    (
      default(host"localhost"),
      default(port"25432"),
      parsedDefault[DbUser]("spj_server"),
      parsedDefault[DbPlaintextPassword]("spj_local_password"),
      parsedDefault[DbName]("json_schemas")
    ).parMapN(DbConfig.apply)

}
