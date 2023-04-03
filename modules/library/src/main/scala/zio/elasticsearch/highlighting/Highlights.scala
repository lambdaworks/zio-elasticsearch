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

package zio.elasticsearch.highlighting

import zio.Chunk
import zio.elasticsearch.highlighting.Highlights.HighlightConfig
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj}

final case class Highlights(
  fields: Chunk[HighlightField],
  config: HighlightConfig = Map.empty,
  explicitFieldOrder: Boolean = false
) { self =>
  def toJson: Json = Obj("highlight" -> Obj(configList: _*).merge(fieldsList))

  def withGlobalConfig(field: String, value: Json): Highlights =
    self.copy(config = self.config.updated(field, value))

  def withHighlight(field: String, config: HighlightConfig = Map.empty): Highlights =
    self.copy(fields = HighlightField(field, config) +: self.fields)

  def withExplicitFieldOrder: Highlights = self.copy(explicitFieldOrder = true)

  private def configList: List[(String, Json)] = config.toList
  private def fieldsList: Obj =
    if (explicitFieldOrder) {
      Obj("fields" -> Arr(fields.reverse.map(_.toJsonObj)))
    } else {
      Obj("fields" -> Obj(fields.reverse.map(_.toStringJsonPair): _*))
    }
}

object Highlights {
  type HighlightConfig = Map[String, Json]
}

final case class HighlightField(field: String, config: HighlightConfig = Map.empty) {
  def toStringJsonPair: (String, Obj) = field -> Obj(config.toList: _*)

  def toJsonObj: Json = Obj(field -> Obj(config.toList: _*))
}
