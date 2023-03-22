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

import zio.elasticsearch.utils.ElasticPrimitive.ElasticPrimitiveOps
import zio.json.ast.Json
import zio.json.ast.Json.Obj

sealed trait Sort
    extends WithFormat[Sort]
    with WithMissing[Sort]
    with WithMode[Sort]
    with WithNumericType[Sort]
    with WithOrder[Sort]
    with WithUnmappedType[Sort] {
  def paramsToJson: Json
}

private[elasticsearch] final case class SortOptions(
  field: String,
  format: Option[String],
  missing: Option[Missing],
  mode: Option[SortMode],
  numericType: Option[NumericType],
  order: Option[SortOrder],
  unmappedType: Option[String]
) extends Sort { self =>
  def format(value: String): Sort =
    self.copy(format = Some(value))

  def missing(value: Missing): Sort =
    self.copy(missing = Some(value))

  def mode(value: SortMode): Sort =
    self.copy(mode = Some(value))

  def numericType(value: NumericType): Sort =
    self.copy(numericType = Some(value))

  def order(value: SortOrder): Sort =
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

  def unmappedType(value: String): Sort =
    self.copy(unmappedType = Some(value))
}
