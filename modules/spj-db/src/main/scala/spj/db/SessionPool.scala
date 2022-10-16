package spj.db

import fs2.io.net.Network
import natchez.Trace
import spj.*

/** Pseudo-companion object for the skunk.SessionPool type alias w/ our own flavor :)
  */
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
