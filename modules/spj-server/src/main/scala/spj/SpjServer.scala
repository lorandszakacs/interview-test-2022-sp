package spj

import fs2.io.net.Network
import spj.db.*
import spj.schema.api.SpjApi
import spj.schema.routes.SpjHttp4sRoutes

case class SpjServer[F[_]] private (
    api: SpjApi[F],
    routes: SpjHttp4sRoutes[F]
)

object SpjServer {
  def make[F[_]](config: SpjConfig)(using
      F: Concurrent[F],
      network: Network[F],
      console: Console[F]
  ): Resource[F, SpjServer[F]] = {
    for {
      db <- SessionPool.make(config.dbConfig)
      api <- SpjApi.server[F](db)
      routes <- SpjHttp4sRoutes.make[F](api)
    } yield SpjServer(
      api = api,
      routes = routes
    )
  }
}
