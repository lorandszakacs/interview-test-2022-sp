package spj

import fs2.io.net.Network
import spj.db.*
import spj.db.flyway.Flyway
import spj.config.ConfigLoader
import spj.schema.api.SpjApi
import spj.schema.routes.SpjHttp4sRoutes
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.{Router, Server}

final case class SpjServer[F[_]] private (
    api: SpjApi[F],
    routes: SpjHttp4sRoutes[F],
    config: SpjConfig
) {

  /** {{{
    *    val services = tweetService <+> helloWorldService
    * val httpApp = Router("/" -> helloWorldService, "/api" -> services).orNotFound
    * val server = EmberServerBuilder
    * .default[IO]
    * .withHost(ipv4"0.0.0.0")
    * .withPort(port"8080")
    * .withHttpApp(httpApp)
    * .build
    * }}}
    *
    * @return
    */

  def bindEmberServer(using F: Async[F]): Resource[F, Server] = {
    val httpApp = Router("/" -> routes.routes).orNotFound
    val server = EmberServerBuilder
      .default[F]
      .withHost(config.httpConfig.host)
      .withPort(config.httpConfig.port)
      .withHttpApp(httpApp)
      .build
    server
  }
}

object SpjServer {
  def make[F[_]](config: SpjConfig)(using
      F: Async[F],
      network: Network[F],
      console: Console[F],
      configLoader: ConfigLoader[F]
  ): Resource[F, SpjServer[F]] = {
    for {
      _ <- Flyway.migrate(config.dbConfig)
      db <- SessionPool.make(config.dbConfig)
      api <- SpjApi.server[F](db)
      routes <- SpjHttp4sRoutes.make[F](api)
    } yield SpjServer(
      api = api,
      routes = routes,
      config = config
    )
  }
}
