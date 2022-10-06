// https://github.com/liancheng/scalafix-organize-imports
ThisBuild / scalafixDependencies += Dependencies.Tools.organizeImports

lazy val root = project
  .in(file("."))
  .settings(
    name := "sp-json-validator"
  )
  .aggregate(`spj-prelude`, `spj-testkit`, `spj-server`, `spj-app`)

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
      Dependencies.Libraries.skunk,
      Dependencies.Libraries.http4sServer,
      Dependencies.Libraries.http4sCirce,
      Dependencies.Libraries.http4sDsl,
      Dependencies.Libraries.java.jsonSchemaValidator,
      Dependencies.Libraries.http4sClient % Test
    )
  )
  .dependsOn(
    `spj-prelude`,
    asTestingLibrary(`spj-testkit`)
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
      Dependencies.Libraries.fs2
    )
  )

def commonSettings = Seq(
  scalaVersion := "2.13.7",
  // https://disneystreaming.github.io/weaver-test/docs/cats
  testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
  // required for scalafix. For production projects this might slow down compilation too much
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision
)

/** See SBT docs: https://www.scala-sbt.org/release/docs/Multi-Project.html#Per-configuration+classpath+dependencies
  */
def asTestingLibrary(p: Project): ClasspathDependency = p % "test -> compile"
