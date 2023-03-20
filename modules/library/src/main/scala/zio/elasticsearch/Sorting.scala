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

sealed trait Mode

object Mode {
  final case object Avg extends Mode {
    override def toString: String = "avg"
  }

  final case object Max extends Mode {
    override def toString: String = "max"
  }

  final case object Median extends Mode {
    override def toString: String = "median"
  }

  final case object Min extends Mode {
    override def toString: String = "min"
  }

  final case object Sum extends Mode {
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

sealed trait Order

object Order {
  final case object Asc extends Order {
    override def toString: String = "asc"
  }

  final case object Desc extends Order {
    override def toString: String = "desc"
  }
}

sealed trait Sorting
    extends WithFormat[Sorting]
    with WithMissing[Sorting]
    with WithMode[Sorting]
    with WithNumericType[Sorting]
    with WithOrder[Sorting]
    with WithUnmappedType[Sorting] {
  def paramsToJson: Json
}

trait WithFormat[S <: WithFormat[S]] {
  def format(value: String): S
}

trait WithMode[S <: WithMode[S]] {
  def mode(value: Mode): S
}

trait WithMissing[S <: WithMissing[S]] {
  def missing(value: Missing): S
}

trait WithNumericType[S <: WithNumericType[S]] {
  def numericType(value: NumericType): S
}

trait WithOrder[S <: WithOrder[S]] {
  def order(value: Order): S
}

trait WithUnmappedType[S <: WithUnmappedType[S]] {
  def unmappedType(value: String): S
}

object Sorting {
  def sortBy[S](field: Field[S, _]): Sorting =
    SortOptions(
      field = field.toString,
      format = None,
      mode = None,
      missing = None,
      numericType = None,
      order = None,
      unmappedType = None
    )

  def sortBy(field: String): Sorting =
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
    mode: Option[Mode],
    numericType: Option[NumericType],
    order: Option[Order],
    unmappedType: Option[String]
  ) extends Sorting { self =>
    def format(value: String): Sorting =
      self.copy(format = Some(value))

    def missing(value: Missing): Sorting =
      self.copy(missing = Some(value))

    def mode(value: Mode): Sorting =
      self.copy(mode = Some(value))

    def numericType(value: NumericType): Sorting =
      self.copy(numericType = Some(value))

    def order(value: Order): Sorting =
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

    def unmappedType(value: String): Sorting =
      self.copy(unmappedType = Some(value))
  }
}
