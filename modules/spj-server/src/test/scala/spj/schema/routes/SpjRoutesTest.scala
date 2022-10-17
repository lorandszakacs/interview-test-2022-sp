package spj.schema.routes

import spj.*
import spj.db.*
import spj.config.*
import spj.schema.*
import spj.db.flyway.*
import spj.testkit.*
import io.circe.parser
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityEncoder.{*, given}
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import spj.schema.JsonSchemaUserInput
import io.circe.*
import io.circe.syntax.*

object SpjRoutesTest extends ServerTestHarness {

  test("POST schema + GET schema + POST validate - happy path") { server =>
    val postTest = for {
      jsonBody <- parseJson("""
          |{
          |  "$schema": "http://json-schema.org/draft-04/schema#",
          |  "type": "object",
          |  "properties": {
          |    "source": {
          |      "type": "string"
          |    },
          |    "destination": {
          |      "type": "string"
          |    },
          |    "timeout": {
          |      "type": "integer",
          |      "minimum": 0,
          |      "maximum": 32767
          |    },
          |    "chunks": {
          |      "type": "object",
          |      "properties": {
          |        "size": {
          |          "type": "integer"
          |        },
          |        "number": {
          |          "type": "integer"
          |        }
          |      },
          |      "required": ["size"]
          |    }
          |  },
          |  "required": ["source", "destination"]
          |}
          |""".stripMargin)
      createReq = Request[IO](method = Method.POST, uri = uri"/schema/config-schema").withEntity(jsonBody)
      createResp <- server.app.run(createReq)
      _ <- assert(createResp.status == Status.Created).failFast
      _ <- expectJsonBody(createResp)(
        """
          |{
          |  "action" : "uploadSchema",
          |  "status" : "success",
          |  "id" : "config-schema"
          |}
          |""".stripMargin
      )
    } yield ()

    // assumes postTest
    val getTest = for {
      getReq <- Request[IO](method = Method.GET, uri = uri"/schema/config-schema").pure[IO]

      getResp <- server.app.run(getReq)
      _ <- assert(getResp.status == Status.Ok).failFast
      _ <- expectJsonBody(getResp)(
        """{
          |  "action" : "getSchema",
          |  "status" : "success",
          |  "id" : "config-schema",
          |  "schema" : {
          |    "type" : "object",
          |    "$schema" : "http://json-schema.org/draft-04/schema#",
          |    "required" : [
          |      "source",
          |      "destination"
          |    ],
          |    "properties" : {
          |      "chunks" : {
          |        "type" : "object",
          |        "required" : [
          |          "size"
          |        ],
          |        "properties" : {
          |          "size" : {
          |            "type" : "integer"
          |          },
          |          "number" : {
          |            "type" : "integer"
          |          }
          |        }
          |      },
          |      "source" : {
          |        "type" : "string"
          |      },
          |      "timeout" : {
          |        "type" : "integer",
          |        "maximum" : 32767,
          |        "minimum" : 0
          |      },
          |      "destination" : {
          |        "type" : "string"
          |      }
          |    }
          |  }
          |}
          |""".stripMargin
      )
    } yield ()

    // assumes postTest
    val validateTest = for {
      jsonMatchesSchema <- parseJson(
        """{
          |  "source": "/home/alice/image.iso",
          |  "destination": "/mnt/storage",
          |  "timeout": null,
          |  "chunks": {
          |    "size": 1024,
          |    "number": null
          |  }
          |}""".stripMargin
      )
      validReq = Request[IO](method = Method.POST, uri = uri"/validate/config-schema")
        .withEntity(jsonMatchesSchema)

      validResp <- server.app.run(validReq)
      _ <- assert(validResp.status == Status.Ok).failFast
      _ <- expectJsonBody(validResp)("""
          |{
          |  "action" : "validateDocument",
          |  "status" : "success",
          |  "id" : "config-schema",
          |  "validated" : {
          |    "source" : "/home/alice/image.iso",
          |    "destination" : "/mnt/storage",
          |    "chunks" : {
          |      "size" : 1024
          |    }
          |  }
          |}
          |""".stripMargin)

      invalidReq = Request[IO](method = Method.POST, uri = uri"/validate/config-schema")
        .withEntity("{}")

      invalidResp <- server.app.run(invalidReq)
      _ <- expectJsonBody(invalidResp)(
        """{
      |  "action" : "validateDocument",
      |  "status" : "error",
      |  "id" : "config-schema",
      |  "message" : {
      |    "details" : "instance type (string) does not match any allowed primitive type (allowed: [\"object\"])",
      |    "errorType" : "spj.InvalidInput"
      |  }
      |}""".stripMargin
      )
    } yield ()
    for {
      _ <- postTest
      _ <- getTest
      _ <- validateTest
    } yield success
  }

}
