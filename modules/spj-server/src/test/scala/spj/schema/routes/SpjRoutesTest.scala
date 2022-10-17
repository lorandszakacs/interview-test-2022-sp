package spj.schema.routes

import spj.*
import spj.db.*
import spj.config.*
import spj.db.flyway.*
import spj.testkit.*

object SpjRoutesTest extends SpjTest {
  override type Res = SpjServer[IO]

  override def sharedResource: Resource[IO, Res] =
    for {
      given ConfigLoader[IO] <- ConfigLoader.make[IO]
      config <- SpjConfig.load[IO](SpjConfig.Environment.Test)
      _ <- FlywayTest.clean[IO](config.dbConfig)
      server <- SpjServer.make[IO](config)
    } yield server

  test("wip") { server =>
    for {
      _ <- IO.println("connected to DB")
    } yield success
  }
}
