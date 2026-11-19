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

package zio.elasticsearch.executor.response

import zio.Chunk
import zio.json.ast.Json
import zio.json.{DeriveJsonDecoder, JsonDecoder}

private[elasticsearch] final case class MultiSearchResponse(took: Int, responses: Chunk[Json])

private[elasticsearch] object MultiSearchResponse {
  implicit val decoder: JsonDecoder[MultiSearchResponse] = DeriveJsonDecoder.gen[MultiSearchResponse]
}

private[elasticsearch] final case class MultiSearchResponseError(error: Error, status: Int)

private[elasticsearch] object MultiSearchResponseError {
  implicit val decoder: JsonDecoder[MultiSearchResponseError] = DeriveJsonDecoder.gen[MultiSearchResponseError]
}
