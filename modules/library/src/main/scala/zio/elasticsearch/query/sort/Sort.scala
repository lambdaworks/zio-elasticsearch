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

import zio.elasticsearch.ElasticPrimitive.ElasticPrimitiveOps
import zio.json.ast.Json
import zio.json.ast.Json.Obj

sealed trait Sort {
  def paramsToJson: Json
}

sealed trait SortByField
    extends Sort
    with WithFormat[SortByField]
    with WithMissing[SortByField]
    with WithMode[SortByField]
    with WithNumericType[SortByField]
    with WithOrder[SortByField]
    with WithUnmappedType[SortByField] {
  def paramsToJson: Json
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

  def paramsToJson: Json =
    Obj(
      self.field -> Obj(
        List(
          self.order.map(order => "order" -> order.toString.toJson),
          self.format.map(format => "format" -> format.toJson),
          self.numericType.map(numericType => "numeric_type" -> numericType.toString.toJson),
          self.mode.map(mode => "mode" -> mode.toString.toJson),
          self.missing.map(missing => "missing" -> missing.toString.toJson),
          self.unmappedType.map(unmappedType => "unmapped_type" -> unmappedType.toJson)
        ).collect { case Some(obj) => obj }: _*
      )
    )

  def unmappedType(value: String): SortByField =
    self.copy(unmappedType = Some(value))
}

sealed trait SortByScript
    extends Sort
    with WithLang[SortByScript]
    with WithMode[SortByScript]
    with WithOrder[SortByScript]
    with WithParam[SortByScript]

private[elasticsearch] final case class SortByScriptOptions(
  params: Map[String, Any],
  source: String,
  sourceType: SourceType,
  lang: Option[String],
  mode: Option[SortMode],
  order: Option[SortOrder]
) extends SortByScript { self =>
  def lang(value: String): SortByScript =
    self.copy(lang = Some(value))

  def mode(value: SortMode): SortByScript =
    self.copy(mode = Some(value))

  def order(value: SortOrder): SortByScript =
    self.copy(order = Some(value))

  def paramsToJson: Json =
    Obj(
      "_script" -> Obj(
        List(
          Some("type"   -> self.sourceType.toString.toJson),
          Some("script" -> scriptToJson),
          self.order.map(order => "order" -> order.toString.toJson),
          self.mode.map(mode => "mode" -> mode.toString.toJson)
        ).collect { case Some(obj) => obj }: _*
      )
    )

  private def scriptToJson: Json =
    Obj(
      List(
        self.lang.map(lang => "lang" -> lang.toJson),
        Some("source" -> source.toJson),
        if (params.nonEmpty)
          Some("params" -> Obj(params.map { case (key, value) =>
            value match {
              case value: BigDecimal => key -> value.toJson
              case value: Double     => key -> value.toJson
              case value: Int        => key -> value.toJson
              case value: Long       => key -> value.toJson
              case _                 => key -> value.toString.toJson
            }
          }.toList: _*))
        else
          None
      ).collect { case Some(obj) => obj }: _*
    )

  def withParam(value: (String, Any)): SortByScript =
    self.copy(params = params + value)
}
