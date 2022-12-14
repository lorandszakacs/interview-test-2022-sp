package spj.testkit

import io.circe.*
import io.circe.parser
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.{*, given}
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import spj.*
import spj.config.*
import spj.db.*
import spj.db.flyway.*
import spj.testkit.*

abstract class ServerTestHarness extends SpjTest {
  final case class Test(
      server: SpjServer[IO]
  ) {
    lazy val app: HttpApp[IO] = server.routes.routes.orNotFound
  }

  override type Res = Test

  override def sharedResource: Resource[IO, Res] =
    for {
      given ConfigLoader[IO] <- ConfigLoader.make[IO]
      config <- SpjConfig.load[IO](SpjConfig.Environment.Test)
      _ <- FlywayTest.clean[IO](config.dbConfig)
      server <- SpjServer.make[IO](config)
    } yield Test(server)

  protected def parseJson(rawJson: String): IO[Json] = parser.parse(rawJson).liftTo[IO]

  /** @return
    *   the parsed JSON in the response body. N.B. this drains the body of the response so do not attempt to do so
    *   again.
    */
  protected def expectJsonBody(resp: Response[IO])(j: String): IO[Json] = {
    for {
      responseJson <- resp.body.through(fs2.text.utf8.decode[IO]).compile.string.flatMap(parseJson)
      expectedJson <- parseJson(j)
      _ <- assert(expectedJson.noSpacesSortKeys == responseJson.noSpacesSortKeys).failFast
    } yield responseJson
  }
}
