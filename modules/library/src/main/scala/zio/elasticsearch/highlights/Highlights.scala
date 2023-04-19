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

package zio.elasticsearch.highlights

import zio.Chunk
import zio.elasticsearch.Field
import zio.elasticsearch.highlights.Highlights.HighlightConfig
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj}

final case class Highlights(
  fields: Chunk[HighlightField],
  config: HighlightConfig = Map.empty,
  explicitFieldOrder: Boolean = false
) { self =>

  /**
   * Sets `explicitFieldOrder` parameter to `true` in the [[Highlights]].
   *
   * @return
   *   a new instance of the [[Highlights]] with `explicitFieldOrder` set to `true`.
   */
  def withExplicitFieldOrder: Highlights = self.copy(explicitFieldOrder = true)

  /**
   * Specifies configuration of the [[Highlights]].
   *
   * @param field
   *   the name of the configuration parameter to add or update
   * @param value
   *   the value to add to the configuration parameter
   * @return
   *   a new instance of the [[Highlights]] with the specified configuration.
   */
  def withGlobalConfig(field: String, value: Json): Highlights =
    self.copy(config = self.config.updated(field, value))

  /**
   * Adds a new type-safe [[HighlightField]] to the list of [[HighlightField]] for the [[Highlights]].
   *
   * @param field
   *   the [[Field]] object which represents the type-safe name of the [[HighlightField]]
   * @return
   *   a new instance of the [[Highlights]] with the additional [[HighlightField]].
   */
  def withHighlight(field: Field[_, _]): Highlights =
    self.copy(fields = HighlightField(field.toString, Map.empty) +: self.fields)

  /**
   * Adds a new [[HighlightField]] to the list of [[HighlightField]] for the [[Highlights]].
   *
   * @param field
   *   the name of the [[HighlightField]]
   * @return
   *   a new instance of the [[Highlights]] with the additional [[HighlightField]].
   */
  def withHighlight(field: String): Highlights =
    self.copy(fields = HighlightField(field, Map.empty) +: self.fields)

  /**
   * Adds a new type-safe [[HighlightField]] with its specific configuration to the list of [[HighlightField]] for the
   * [[Highlights]].
   *
   * @param field
   *   the [[Field]] object which represents the type-safe name of the [[HighlightField]]
   * @param config
   *   the configuration to apply to the [[HighlightField]]
   * @return
   *   a new instance of the [[Highlights]] with the additional [[HighlightField]] and its specific configuration.
   */
  def withHighlight(field: Field[_, _], config: HighlightConfig): Highlights =
    self.copy(fields = HighlightField(field.toString, config) +: self.fields)

  /**
   * Adds a new [[HighlightField]] with its specific configuration to the list of [[HighlightField]] for the
   * [[Highlights]].
   *
   * @param field
   *   the name of the [[HighlightField]]
   * @param config
   *   the configuration to apply to the [[HighlightField]]
   * @return
   *   a new instance of the [[Highlights]] with the additional [[HighlightField]] and its specific configuration.
   */
  def withHighlight(field: String, config: HighlightConfig): Highlights =
    self.copy(fields = HighlightField(field, config) +: self.fields)

  private[elasticsearch] def toJson: Json = Obj("highlight" -> Obj(configList: _*).merge(fieldsJson))

  private lazy val configList: List[(String, Json)] = config.toList

  private lazy val fieldsJson: Json =
    if (explicitFieldOrder) {
      Obj("fields" -> Arr(fields.reverse.map(_.toJsonObj)))
    } else {
      Obj("fields" -> Obj(fields.reverse.map(_.toStringJsonPair): _*))
    }
}

object Highlights {
  type HighlightConfig = Map[String, Json]
}

private[elasticsearch] final case class HighlightField(field: String, config: HighlightConfig = Map.empty) {
  def toStringJsonPair: (String, Obj) = field -> Obj(config.toList: _*)

  def toJsonObj: Json = Obj(field -> Obj(config.toList: _*))
}
