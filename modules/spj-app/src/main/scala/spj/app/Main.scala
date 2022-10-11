package spj.app

import spj.*

object Main extends cats.effect.ResourceApp.Forever {
  def run(args: List[String]): Resource[IO, Unit] = Resource.eval(IO.println("running-forever"))
}
