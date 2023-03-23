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

import zio.json.ast.Json
import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}

private[elasticsearch] final case class Hit(
  @jsonField("_index")
  index: String,
  @jsonField("_type")
  `type`: String,
  @jsonField("_id")
  id: String,
  @jsonField("_score")
  score: Option[Double] = None,
  @jsonField("_source")
  source: Json,
  sort: Option[Json]
)

private[elasticsearch] object Hit {
  implicit val decoder: JsonDecoder[Hit] = DeriveJsonDecoder.gen[Hit]
}
