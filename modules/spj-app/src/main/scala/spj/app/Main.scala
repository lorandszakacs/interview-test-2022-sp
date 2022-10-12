package spj.app

import spj.*

/** Keep Main as a one-liner ideally so that we can easily reuse the rest in tests as well!
  */
object Main extends cats.effect.ResourceApp.Forever:
  def run(args: List[String]): Resource[IO, Unit] = Resource.eval(IO.println("running-forever"))
end Main
