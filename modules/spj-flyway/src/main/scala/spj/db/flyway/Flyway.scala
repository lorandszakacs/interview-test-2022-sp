package spj.db.flyway

import spj.*
import spj.db.DbConfig
import org.flywaydb.core.Flyway as JFlyway

object Flyway {

  def migrate[F[_]](db: DbConfig)(using F: Async[F]): Resource[F, Unit] = {
    for {
      jflyway <- Resource.eval(flywayInit[F](db))
      _ <- Resource.eval(F.blocking(jflyway.migrate()))
    } yield ()
  }

  private def flywayInit[F[_]](
      db: DbConfig
  )(implicit F: Async[F]): F[JFlyway] = {
    db.host.resolve.map { host =>
      val jdbcUrl = s"jdbc:postgresql://${db.host.resolve}:${host.toUriString}/${db.dbName}"
      val fwConfig = JFlyway.configure()
      fwConfig.dataSource(jdbcUrl, db.user, db.password)
      fwConfig.mixed(true)
      new JFlyway(fwConfig)
    }
  }
}
