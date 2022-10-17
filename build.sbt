ThisBuild / scalafixDependencies += Dependencies.Tools.organizeImports

lazy val root = project
  .in(file("."))
  .settings(
    name := "sp-json-validator"
  )
  .aggregate(
    `spj-prelude`,
    `spj-testkit`,
    `spj-db`,
    `spj-flyway`,
    `spj-config`,
    `spj-server`,
    `spj-app`
  )

lazy val `spj-app` = project
  .in(file("modules/spj-app"))
  .settings(commonSettings)
  .dependsOn(
    `spj-prelude`,
    `spj-server`
  )

lazy val `spj-server` = project
  .in(file("modules/spj-server"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Libraries.circe,
      Dependencies.Libraries.circeGeneric,
      Dependencies.Libraries.http4sServer,
      Dependencies.Libraries.http4sCirce,
      Dependencies.Libraries.http4sDsl,
      Dependencies.Libraries.java.jsonSchemaValidator,
      Dependencies.Libraries.ciris,
      Dependencies.Libraries.http4sClient % Test
    )
  )
  .dependsOn(
    `spj-prelude`,
    `spj-db`,
    `spj-flyway`,
    `spj-config`,
    asTestingLibrary(`spj-testkit`)
  )

lazy val `spj-db` = project
  .in(file("modules/spj-db"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Libraries.skunk,
      Dependencies.Libraries.skunkCirce,
      Dependencies.Libraries.ip4s
    )
  )
  .dependsOn(
    `spj-prelude`
  )

lazy val `spj-flyway` = project
  .in(file("modules/spj-flyway"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Libraries.java.flyway,
      Dependencies.Libraries.java.postgresqlJdbc
    )
  )
  .dependsOn(
    `spj-prelude`,
    `spj-db`
  )

lazy val `spj-config` = project
  .in(file("modules/spj-config"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Libraries.ciris,
      Dependencies.Libraries.ip4s
    )
  )
  .dependsOn(
    `spj-prelude`
  )

lazy val `spj-testkit` = project
  .in(file("modules/spj-testkit"))
  .settings(commonSettings)
  .settings(
    libraryDependencies += Dependencies.Libraries.weaverTest
  )
  .dependsOn(
    `spj-prelude`
  )

lazy val `spj-prelude` = project
  .in(file("modules/spj-prelude"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      Dependencies.Libraries.monixNewtypes,
      Dependencies.Libraries.cats,
      Dependencies.Libraries.catsEffect,
      Dependencies.Libraries.fs2,
      Dependencies.Libraries.sourcePos
    )
  )

def commonSettings = Seq(
  scalaVersion := "3.2.0",
  // https://disneystreaming.github.io/weaver-test/docs/cats
  testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
  // required for scalafix. For production projects this might slow down compilation too much
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision,
  tpolecatScalacOptions ~= { options =>
    options.+(ScalacOptions.sourceFuture).+(ScalacOptions.other("-Yno-imports"))
  }
)

/** See SBT docs: https://www.scala-sbt.org/release/docs/Multi-Project.html#Per-configuration+classpath+dependencies
  */
def asTestingLibrary(p: Project): ClasspathDependency = p % "test -> compile"
