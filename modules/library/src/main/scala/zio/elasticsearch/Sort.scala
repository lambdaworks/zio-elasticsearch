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

import zio.elasticsearch.ElasticPrimitive.ElasticPrimitiveOps
import zio.json.ast.Json
import zio.json.ast.Json.Obj

sealed trait Missing

object Missing {
  final case object First extends Missing {
    override def toString: String = "_first"
  }

  final case object Last extends Missing {
    override def toString: String = "_last"
  }
}

sealed trait SortMode

object SortMode {
  final case object Avg extends SortMode {
    override def toString: String = "avg"
  }

  final case object Max extends SortMode {
    override def toString: String = "max"
  }

  final case object Median extends SortMode {
    override def toString: String = "median"
  }

  final case object Min extends SortMode {
    override def toString: String = "min"
  }

  final case object Sum extends SortMode {
    override def toString: String = "sum"
  }
}

sealed trait NumericType

object NumericType {
  final case object Double extends NumericType {
    override def toString: String = "double"
  }

  final case object Long extends NumericType {
    override def toString: String = "long"
  }

  final case object Date extends NumericType {
    override def toString: String = "date"
  }

  final case object DateNanos extends NumericType {
    override def toString: String = "date_nanos"
  }
}

sealed trait SortOrder

object SortOrder {
  final case object Asc extends SortOrder {
    override def toString: String = "asc"
  }

  final case object Desc extends SortOrder {
    override def toString: String = "desc"
  }
}

sealed trait Sort
    extends WithFormat[Sort]
    with WithMissing[Sort]
    with WithMode[Sort]
    with WithNumericType[Sort]
    with WithOrder[Sort]
    with WithUnmappedType[Sort] {
  def paramsToJson: Json
}

sealed trait WithFormat[S <: WithFormat[S]] {
  def format(value: String): S
}

sealed trait WithMode[S <: WithMode[S]] {
  def mode(value: SortMode): S
}

sealed trait WithMissing[S <: WithMissing[S]] {
  def missing(value: Missing): S
}

sealed trait WithNumericType[S <: WithNumericType[S]] {
  def numericType(value: NumericType): S
}

sealed trait WithOrder[S <: WithOrder[S]] {
  def order(value: SortOrder): S
}

sealed trait WithUnmappedType[S <: WithUnmappedType[S]] {
  def unmappedType(value: String): S
}

object Sort {
  def sortBy[S](field: Field[S, _]): Sort =
    SortOptions(
      field = field.toString,
      format = None,
      mode = None,
      missing = None,
      numericType = None,
      order = None,
      unmappedType = None
    )

  def sortBy(field: String): Sort =
    SortOptions(
      field = field,
      format = None,
      mode = None,
      missing = None,
      numericType = None,
      order = None,
      unmappedType = None
    )

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
}
