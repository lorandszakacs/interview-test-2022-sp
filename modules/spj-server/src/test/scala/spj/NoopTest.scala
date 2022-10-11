package spj

import spj.testkit.*

object NoopTest extends SpjTest {
  test("no-op") {
    for {
      _ <- IO.println("remove-me")
    } yield succeed[Unit](())
  }
}
