package example.api

import zio.http._
import zio.http.model.Method
import zio.json.ast.Json._

object HealthCheck {

  final val Route: Http[Any, Nothing, Any, Response] = Http.collect { case Method.GET -> !! / "health" =>
    Response.json(Obj("name" -> Str("zio-elasticsearch-example"), "status" -> Str("up")).toJson)
  }

}
