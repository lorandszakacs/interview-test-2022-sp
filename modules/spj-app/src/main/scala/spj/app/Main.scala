package spj.app

import fs2.io.net.Network
import spj.*
import spj.config.ConfigLoader

/** Keep Main as short as possible so that we can easily reuse the rest in tests as well!
  */
object Main extends cats.effect.ResourceApp.Forever {
  def run(args: List[String]): Resource[IO, Unit] = for {
    _ <- Resource.unit[IO]
    given Console[IO] = Console.make[IO]
    given Network[IO] = Network.forAsync[IO]
    given ConfigLoader[IO] <- ConfigLoader.make[IO]
    serverConfig <- SpjConfig.load[IO](SpjConfig.Environment.Local)
    server <- SpjServer.make[IO](serverConfig)
    _ <- server.bindEmberServer
  } yield ()
}
