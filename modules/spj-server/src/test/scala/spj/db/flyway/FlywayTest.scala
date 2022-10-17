package spj.db.flyway

import spj.*
import spj.db.DbConfig
import spj.db.flyway.Flyway

object FlywayTest {
  export Flyway.migrate
  def clean[F[_]](db: DbConfig)(using F: Async[F]): Resource[F, Unit] = Flyway.clean[F](db)
}
