package spj.api

import spj.*
import io.circe.Encoder

sealed trait APIResponse

object APIResponse {
  given Encoder[APIResponse] = ???

  /** Example success:
    * {{{
    * {"action":"uploadSchema","id":"config-schema","status":"success"}
    * }}}
    *
    * Example failure:
    * {{{
    * {"action":"uploadSchema","id":"config-schema","status":"error","message":"Invalid JSON"}
    * }}}
    */
  case class Upload() extends APIResponse

  /** Not specified: // schema is not specified
    * {{{
    * {"action":"getSchema","id":"config-schema","status":"success","schema":{}}
    * }}}
    */
  case class Get() extends APIResponse

  /** Example stuff: // validated is unspecified
    * {{{
    *   {"action": "validateDocument", "id": "config-schema", "status": "success", "validated": {}}
    * }}}
    */
  case class Validate() extends APIResponse
}
