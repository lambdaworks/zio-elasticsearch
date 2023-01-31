/*
 * Copyright 2022 LambdaWorks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example.api

import zio.http._
import zio.http.model.Method
import zio.json.ast.Json._

object HealthCheck {

  final val Route: Http[Any, Nothing, Any, Response] = Http.collect { case Method.GET -> !! / "health" =>
    Response.json(Obj("name" -> Str("zio-elasticsearch-example"), "status" -> Str("up")).toJson)
  }

}
