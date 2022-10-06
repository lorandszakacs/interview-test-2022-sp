// format: off

// https://github.com/scalameta/sbt-scalafmt/releases
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")

// https://github.com/typelevel/sbt-tpolecat
// here so I don't copy paste the same options over and over again
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.4.1")

// https://github.com/scalacenter/sbt-scalafix
// used to run the organize imports rule
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.34")
