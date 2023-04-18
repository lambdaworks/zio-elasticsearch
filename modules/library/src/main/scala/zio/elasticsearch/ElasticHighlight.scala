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

package zio.elasticsearch

import zio.Chunk
import zio.elasticsearch.highlights.{HighlightField, Highlights}
import zio.json.ast.Json

object ElasticHighlight {
  final def highlight(field: Field[_, _]): Highlights =
    Highlights(Chunk(HighlightField(field.toString, Map.empty)))

  final def highlight(field: String): Highlights =
    Highlights(Chunk(HighlightField(field, Map.empty)))

  final def highlight(field: Field[_, _], config: Map[String, Json]): Highlights =
    Highlights(Chunk(HighlightField(field.toString, config)))

  final def highlight(field: String, config: Map[String, Json]): Highlights =
    Highlights(Chunk(HighlightField(field, config)))
}
