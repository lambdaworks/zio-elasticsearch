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

sealed trait SortBy
    extends WithFormat[SortBy]
    with WithMissing[SortBy]
    with WithMode[SortBy]
    with WithNumericType[SortBy]
    with WithOrder[SortBy]
    with WithUnmappedType[SortBy] {
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

object SortBy {
  def sortBy(field: String): SortBy =
    SortByData(
      field = field,
      format = None,
      mode = None,
      missing = None,
      numericType = None,
      order = None,
      unmappedType = None
    )

  private[elasticsearch] final case class SortByData(
    field: String,
    format: Option[String],
    missing: Option[Missing],
    mode: Option[Mode],
    numericType: Option[NumericType],
    order: Option[Order],
    unmappedType: Option[String]
  ) extends SortBy { self =>
    def format(value: String): SortBy =
      self.copy(format = Some(value))

    def missing(value: Missing): SortBy =
      self.copy(missing = Some(value))

    def mode(value: Mode): SortBy =
      self.copy(mode = Some(value))

    def numericType(value: NumericType): SortBy =
      self.copy(numericType = Some(value))

    def order(value: Order): SortBy =
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

    def unmappedType(value: String): SortBy =
      self.copy(unmappedType = Some(value))
  }
}
