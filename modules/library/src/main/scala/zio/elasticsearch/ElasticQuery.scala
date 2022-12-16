package zio.elasticsearch

import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Bool, Num, Obj, Str}

import scala.annotation.unused

sealed trait ElasticQuery { self =>

  def toJson: Json

  final def toJsonBody: Json = Obj("query" -> self.toJson)
}

object ElasticQuery {

  sealed trait ElasticPrimitive[A] {
    def toJson(value: A): Json
  }

  implicit object ElasticInt extends ElasticPrimitive[Int] {
    override def toJson(value: Int): Json = Num(value)
  }

  implicit object ElasticString extends ElasticPrimitive[String] {
    override def toJson(value: String): Json = Str(value)
  }

  implicit object ElasticBool extends ElasticPrimitive[Boolean] {
    override def toJson(value: Boolean): Json = Bool(value)
  }

  implicit object ElasticLong extends ElasticPrimitive[Long] {
    override def toJson(value: Long): Json = Num(value)
  }

  implicit class ElasticPrimitiveOps[A](private val value: A) extends AnyVal {
    def toJson(implicit EP: ElasticPrimitive[A]): Json = EP.toJson(value)
  }

  def matches[A: ElasticPrimitive](field: String, value: A): ElasticQuery =
    Match(field, value)

  def term(field: String, value: String): ElasticQuery =
    Term(field, value)

  def boolQuery(): BoolQuery = BoolQuery.empty

  def exists(field: String): Exists = Exists(field)

  def matchAll(): MatchAll = MatchAll()

  def range(field: String): Range[Unbounded.type, Unbounded.type] = Range.empty(field)

  private[elasticsearch] final case class BoolQuery(must: List[ElasticQuery], should: List[ElasticQuery])
      extends ElasticQuery { self =>

    override def toJson: Json =
      Obj("bool" -> Obj("must" -> Arr(must.map(_.toJson): _*), "should" -> Arr(should.map(_.toJson): _*)))

    def must(queries: ElasticQuery*): BoolQuery =
      self.copy(must = must ++ queries)

    def should(queries: ElasticQuery*): BoolQuery =
      self.copy(should = should ++ queries)
  }

  private[elasticsearch] object BoolQuery {
    def empty: BoolQuery = BoolQuery(Nil, Nil)
  }

  private[elasticsearch] final case class Exists private (field: String) extends ElasticQuery {
    override def toJson: Json = Obj("exists" -> Obj("field" -> field.toJson))
  }

  private[elasticsearch] final case class Match[A: ElasticPrimitive](field: String, value: A) extends ElasticQuery {
    override def toJson: Json = Obj("match" -> Obj(field -> value.toJson))
  }

  private[elasticsearch] final case class MatchAll() extends ElasticQuery {
    override def toJson: Json = Obj("match_all" -> Obj())
  }

  private[elasticsearch] final case class Term(field: String, value: String) extends ElasticQuery {
    override def toJson: Json =
      Obj("term" -> Obj(field -> Str(value)))
  }

  sealed trait LowerBound {
    def toJson: Option[(String, Json)]
  }

  private[elasticsearch] final case class GreaterThan[A: ElasticPrimitive](value: A) extends LowerBound {
    override def toJson: Option[(String, Json)] = Some("gt" -> value.toJson)
  }

  private[elasticsearch] final case class GreaterThanOrEqualTo[A: ElasticPrimitive](value: A) extends LowerBound {
    def toJson: Option[(String, Json)] = Some("gte" -> value.toJson)
  }

  sealed trait UpperBound {
    def toJson: Option[(String, Json)]
  }

  private[elasticsearch] final case class LessThan[A: ElasticPrimitive](value: A) extends UpperBound {
    override def toJson: Option[(String, Json)] = Some("lt" -> value.toJson)
  }

  private[elasticsearch] final case class LessThanOrEqualTo[A: ElasticPrimitive](value: A) extends UpperBound {
    override def toJson: Option[(String, Json)] = Some("lte" -> value.toJson)
  }

  private[elasticsearch] final case object Unbounded extends LowerBound with UpperBound {
    override def toJson: Option[(String, Json)] = None
  }

  private[elasticsearch] final case class Range[LB <: LowerBound, UB <: UpperBound] private (
    field: String,
    lower: LB,
    upper: UB
  ) extends ElasticQuery { self =>

    def gt[A: ElasticPrimitive](value: A)(implicit @unused ev: LB =:= Unbounded.type): Range[GreaterThan[A], UB] =
      self.copy(lower = GreaterThan(value))

    def gte[A: ElasticPrimitive](value: A)(implicit
      @unused ev: LB =:= Unbounded.type
    ): Range[GreaterThanOrEqualTo[A], UB] =
      self.copy(lower = GreaterThanOrEqualTo(value))

    def lt[A: ElasticPrimitive](value: A)(implicit @unused ev: UB =:= Unbounded.type): Range[LB, LessThan[A]] =
      self.copy(upper = LessThan(value))

    def lte[A: ElasticPrimitive](value: A)(implicit
      @unused ev: UB =:= Unbounded.type
    ): Range[LB, LessThanOrEqualTo[A]] =
      self.copy(upper = LessThanOrEqualTo(value))

    override def toJson: Json = Obj("range" -> Obj(field -> Obj(List(lower.toJson, upper.toJson).flatten: _*)))
  }

  private[elasticsearch] object Range {
    def empty(field: String): Range[Unbounded.type, Unbounded.type] = Range(field, Unbounded, Unbounded)
  }
}
