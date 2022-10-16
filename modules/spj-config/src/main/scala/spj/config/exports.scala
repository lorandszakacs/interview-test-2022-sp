package spj.config

import monix.newtypes.BuildFailure
import spj.*

export com.comcast.ip4s.Port
export com.comcast.ip4s.Host
// literal syntax for constant values
export com.comcast.ip4s.host
export com.comcast.ip4s.port

export ciris.Effect as CirisEffect
export ciris.default
export ciris.ConfigValue
export ciris.ConfigDecoder

given ConfigDecoder[String, Port] =
  ConfigDecoder[String, Int].mapOption("Port")(Port.fromInt)

given ConfigDecoder[String, Host] =
  ConfigDecoder[String, String].mapOption("Host")(Host.fromString)

given newtypesCirisDecoder[T, S](using
    builder: monix.newtypes.HasBuilder.Aux[T, S],
    decoder: ConfigDecoder[String, S]
): ConfigDecoder[String, T] = decoder.mapEither { (cfgKey, value) =>
  builder
    .build(value)
    .leftMap((failure: BuildFailure[T]) => ciris.ConfigError(s"$cfgKey --> ${failure.toReadableString}"))
}

/** Similar to default, but parses from the given string, using the appropriate decoder. Useful for providing default
  * values for newtypes with little boilerplate.
  *
  * TODO: needs better name
  */
def parsedDefault[T](s: String)(using decoder: ConfigDecoder[String, T]): ConfigValue[CirisEffect, T] = {
  decoder.decode(Option.empty, s) match {
    case Left(err) => ConfigValue.failed[T](err)
    case Right(t)  => ConfigValue.default(t)
  }
}
