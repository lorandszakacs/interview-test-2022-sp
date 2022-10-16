package spj.config

import spj.*

/** Capability trait for reading config values since reading them requires Async[F] which is the most powerful
  * typeclass, and we want to make clear that we're only reading config, not doing arbitrarily complex things
  */
sealed trait ConfigLoader[F[_]] {
  def load[T](value: ConfigValue[F, T]): F[T]
}
object ConfigLoader {
  def make[F[_]](using F: Async[F]): Resource[F, ConfigLoader[F]] =
    new ConfigLoader[F] {
      override def load[T](value: ConfigValue[F, T]): F[T] = value.load[F]
    }.pure[Resource[F, *]]
}
