import sbt._

object Dependencies {
  object V {
    // format: off
    lazy val monixNewtypes      = "0.2.3"       // https://github.com/monix/newtypes/releases
    lazy val cats               = "2.8.0"       // https://github.com/typelevel/cats/releases
    lazy val catsEffect         = "3.3.13"      // https://github.com/typelevel/cats-effect/releases
    lazy val fs2                = "3.3.0"       // https://github.com/typelevel/fs2/releases
    lazy val ip4s               = "3.2.0"       // https://github.com/Comcast/ip4s
    lazy val circe              = "0.14.3"      // https://github.com/circe/circe/releases
    lazy val http4s             = "0.23.6"      // https://github.com/http4s/http4s/releases
    lazy val skunk              = "0.3.2"       // https://github.com/tpolecat/skunk/releases
    lazy val sourcePos          = "1.0.1"       // https://github.com/tpolecat/SourcePos/releases
    lazy val ciris              = "2.4.0"       // https://github.com/vlovgr/ciris/releases
    // testing
    lazy val weaverTest         = "0.8.0"       // https://github.com/disneystreaming/weaver-test/releases
    // java
    lazy val jsonSchema         = "2.2.14"      // https://github.com/java-json-tools/json-schema-validator/releases
    lazy val flyway             = "9.4.0"       // https://github.com/flyway/flyway/releases
    lazy val postgresqlJdbc     = "42.3.7"      // https://github.com/pgjdbc/pgjdbc/tags
    lazy val logbackClassic     = "1.3.4"       // https://github.com/qos-ch/logback/tags
    // format: on

    // scalafix rules
    lazy val organizeImports = "0.6.0" // https://github.com/liancheng/scalafix-organize-imports
  }

  object Libraries {
    lazy val monixNewtypes = "io.monix" %% "newtypes-core" % V.monixNewtypes withSources ()
    lazy val cats = "org.typelevel" %% "cats-core" % V.cats withSources ()
    lazy val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect withSources ()
    lazy val fs2 = "co.fs2" %% "fs2-core" % V.fs2 withSources ()

    lazy val ip4s = "com.comcast" %% "ip4s-core" % V.ip4s withSources ()

    lazy val circe = "io.circe" %% "circe-core" % V.circe withSources ()
    lazy val circeGeneric = "io.circe" %% "circe-generic" % V.circe withSources ()

    /** Initially used this to create json literals. Problem is that it's not available for scala 3. The implementation:
      * {{{
      * package io.circe
      *
      * package object literal {
      * extension (inline sc: StringContext)
      *   final inline def json(inline args: Any*): io.circe.Json = ???
      * }
      * }}}
      * Ups :)
      */
    lazy val circeLiteral = "io.circe" %% "circe-literal" % V.circe withSources ()

    lazy val http4sServer = "org.http4s" %% "http4s-ember-server" % V.http4s withSources ()
    lazy val http4sClient = "org.http4s" %% "http4s-ember-client" % V.http4s withSources ()
    lazy val http4sCirce = "org.http4s" %% "http4s-circe" % V.http4s withSources ()
    lazy val http4sDsl = "org.http4s" %% "http4s-dsl" % V.http4s withSources ()

    lazy val skunk = "org.tpolecat" %% "skunk-core" % V.skunk withSources ()
    lazy val skunkCirce = "org.tpolecat" %% "skunk-circe" % V.skunk withSources ()
    lazy val sourcePos = "org.tpolecat" %% "sourcepos" % V.sourcePos withSources ()

    lazy val ciris = "is.cir" %% "ciris" % V.ciris withSources ()

    lazy val weaverTest = "com.disneystreaming" %% "weaver-cats" % V.weaverTest withSources ()

    object java {
      lazy val jsonSchemaValidator =
        "com.github.java-json-tools" % "json-schema-validator" % V.jsonSchema withSources ()
      lazy val flyway = "org.flywaydb" % "flyway-core" % V.flyway withSources ()
      // required by flyway
      lazy val postgresqlJdbc = "org.postgresql" % "postgresql" % V.postgresqlJdbc withSources ()

      lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % V.logbackClassic withSources ()
    }
  }

  object Tools {
    lazy val organizeImports = "com.github.liancheng" %% "organize-imports" % V.organizeImports
  }

}
