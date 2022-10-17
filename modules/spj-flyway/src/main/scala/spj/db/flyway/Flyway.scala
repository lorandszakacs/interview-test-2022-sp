package spj.db.flyway

import spj.*
import spj.db.DbConfig
import org.flywaydb.core.Flyway as JFlyway

object Flyway {

  def migrate[F[_]](db: DbConfig)(using F: Async[F]): Resource[F, Unit] = {
    for {
      jflyway <- Resource.eval(flywayInit[F](db, disableClean = true))
      _ <- Resource.eval(F.blocking(jflyway.migrate()))
    } yield ()
  }

  /** making package private to allow forwarding this in tests. Definitely don't want access to it "in prod" */
  private[flyway] def clean[F[_]](db: DbConfig)(using F: Async[F]): Resource[F, Unit] = {
    for {
      jflyway <- Resource.eval(flywayInit[F](db, disableClean = false))
      _ <- Resource.eval(F.blocking(jflyway.clean()))
    } yield ()
  }

  private def flywayInit[F[_]](
      db: DbConfig,
      disableClean: Boolean
  )(implicit F: Async[F]): F[JFlyway] = {
    db.host.resolve.map { host =>
      val jdbcUrl = s"jdbc:postgresql://${host.toUriString}:${db.port.value}/${db.dbName}"
      val fwConfig = JFlyway.configure()
      fwConfig.dataSource(jdbcUrl, db.user, db.password)
      fwConfig.mixed(true)
      fwConfig.cleanDisabled(disableClean)
      new JFlyway(fwConfig)
    }
  }
}
