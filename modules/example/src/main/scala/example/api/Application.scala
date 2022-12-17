package example.api

import zio.http._
import zio.http.model.Method
import zio.json.ast.Json

object Application {

  final val Routes: Http[Any, Nothing, Any, Response] = Http.collect { case Method.GET -> !! / "health" =>
    Response.json(Json.Obj("name" -> Json.Str("zio-elasticsearch-example"), "status" -> Json.Str("up")).toJson)
  }

}
