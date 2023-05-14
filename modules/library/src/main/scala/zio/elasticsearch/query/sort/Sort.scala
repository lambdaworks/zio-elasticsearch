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

package zio.elasticsearch.query.sort

import zio.Chunk
import zio.elasticsearch.ElasticPrimitive.ElasticPrimitiveOps
import zio.elasticsearch.query.sort.options._
import zio.elasticsearch.script.Script
import zio.json.ast.Json
import zio.json.ast.Json.Obj

sealed trait Sort {
  def paramsToJson: Json
}

sealed trait SortByField
    extends Sort
    with HasFormat[SortByField]
    with HasMissing[SortByField]
    with HasMode[SortByField]
    with HasNumericType[SortByField]
    with HasOrder[SortByField]
    with HasUnmappedType[SortByField] {
  def paramsToJson: Json
}

object SortByField {

  /**
   * Constructs an instance of [[SortByFieldOptions]] to sort by `_count` field in the context of an aggregation.
   *
   * @return
   *   an instance of [[SortByFieldOptions]] with the `field` set to `_count`.
   */
  def byCount: SortByFieldOptions = SortByFieldOptions(
    field = "_count",
    format = None,
    missing = None,
    mode = None,
    numericType = None,
    order = None,
    unmappedType = None
  )

  /**
   * Constructs an instance of [[SortByFieldOptions]] to sort search results by the `_doc` field.
   *
   * @return
   *   an instance of [[SortByFieldOptions]] with the `field` set to `_doc`.
   */
  def byDoc: SortByFieldOptions = SortByFieldOptions(
    field = "_doc",
    format = None,
    missing = None,
    mode = None,
    numericType = None,
    order = None,
    unmappedType = None
  )

  /**
   * Constructs an instance of [[SortByFieldOptions]] to sort by `_key` field in the context of an aggregation.
   *
   * @return
   *   an instance of [[SortByFieldOptions]] with the `field` set to `_key`.
   */
  def byKey: SortByFieldOptions = SortByFieldOptions(
    field = "_key",
    format = None,
    missing = None,
    mode = None,
    numericType = None,
    order = None,
    unmappedType = None
  )

  /**
   * Constructs an instance of [[SortByFieldOptions]] to sort search results by the `_score` field.
   *
   * @return
   *   an instance of [[SortByFieldOptions]] with the `field` set to `_score`.
   */
  def byScore: SortByFieldOptions = SortByFieldOptions(
    field = "_score",
    format = None,
    missing = None,
    mode = None,
    numericType = None,
    order = None,
    unmappedType = None
  )
}

private[elasticsearch] final case class SortByFieldOptions(
  field: String,
  format: Option[String],
  missing: Option[Missing],
  mode: Option[SortMode],
  numericType: Option[NumericType],
  order: Option[SortOrder],
  unmappedType: Option[String]
) extends SortByField { self =>
  def format(value: String): SortByField =
    self.copy(format = Some(value))

  def missing(value: Missing): SortByField =
    self.copy(missing = Some(value))

  def mode(value: SortMode): SortByField =
    self.copy(mode = Some(value))

  def numericType(value: NumericType): SortByField =
    self.copy(numericType = Some(value))

  def order(value: SortOrder): SortByField =
    self.copy(order = Some(value))

  def paramsToJson: Json = {
    val allParams = Chunk(
      self.order.map(order => "order" -> order.toString.toJson),
      self.format.map(format => "format" -> format.toJson),
      self.numericType.map(numericType => "numeric_type" -> numericType.toString.toJson),
      self.mode.map(mode => "mode" -> mode.toString.toJson),
      self.missing.map(missing => "missing" -> missing.toString.toJson),
      self.unmappedType.map(unmappedType => "unmapped_type" -> unmappedType.toJson)
    ).flatten

    if (allParams.isEmpty) self.field.toJson else Obj(self.field -> Obj(allParams))
  }

  def unmappedType(value: String): SortByField =
    self.copy(unmappedType = Some(value))
}

sealed trait SortByScript extends Sort with HasMode[SortByScript] with HasOrder[SortByScript]

private[elasticsearch] final case class SortByScriptOptions(
  script: Script,
  sourceType: SourceType,
  mode: Option[SortMode],
  order: Option[SortOrder]
) extends SortByScript { self =>
  def mode(value: SortMode): SortByScript =
    self.copy(mode = Some(value))

  def order(value: SortOrder): SortByScript =
    self.copy(order = Some(value))

  def paramsToJson: Json =
    Obj(
      "_script" -> Obj(
        Chunk(
          Some("type"   -> self.sourceType.toString.toJson),
          Some("script" -> script.toJson),
          self.order.map(order => "order" -> order.toString.toJson),
          self.mode.map(mode => "mode" -> mode.toString.toJson)
        ).flatten
      )
    )
}
