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
    def toJson(a: A): Json
  }

  implicit object ElasticInt extends ElasticPrimitive[Int] {
    override def toJson(a: Int): Json = Num(a)
  }

  implicit object ElasticString extends ElasticPrimitive[String] {
    override def toJson(a: String): Json = Str(a)
  }

  implicit object ElasticBool extends ElasticPrimitive[Boolean] {
    override def toJson(a: Boolean): Json = Bool(a)
  }

  implicit object ElasticLong extends ElasticPrimitive[Long] {
    override def toJson(a: Long): Json = Num(a)
  }

  implicit class ElasticPrimitiveOps[A](private val value: A) extends AnyVal {
    def toJson(implicit EP: ElasticPrimitive[A]): Json = EP.toJson(value)
  }

  def matches[A: ElasticPrimitive](field: String, query: A): ElasticQuery =
    Match(field, query)

  def boolQuery(): BoolQuery = BoolQuery.empty

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

  private[elasticsearch] final case class Match[A: ElasticPrimitive](field: String, query: A) extends ElasticQuery {
    override def toJson: Json = Obj("match" -> Obj(field -> query.toJson))
  }

  sealed trait LowerBound {
    def toJson: Option[(String, Json)]
  }

  private[elasticsearch] final case class Greater[A: ElasticPrimitive](a: A) extends LowerBound {
    override def toJson: Option[(String, Json)] = Some("gt" -> a.toJson)
  }

  private[elasticsearch] final case class GreaterEqual[A: ElasticPrimitive](a: A) extends LowerBound {
    def toJson: Option[(String, Json)] = Some("gte" -> a.toJson)
  }

  sealed trait UpperBound {
    def toJson: Option[(String, Json)]
  }

  private[elasticsearch] final case class Less[A: ElasticPrimitive](a: A) extends UpperBound {
    override def toJson: Option[(String, Json)] = Some("lt" -> a.toJson)
  }

  private[elasticsearch] final case class LessEqual[A: ElasticPrimitive](a: A) extends UpperBound {
    override def toJson: Option[(String, Json)] = Some("lte" -> a.toJson)
  }

  private[elasticsearch] final case object Unbounded extends LowerBound with UpperBound {
    override def toJson: Option[(String, Json)] = None
  }

  private[elasticsearch] final case class Range[LB <: LowerBound, UB <: UpperBound] private (
    field: String,
    lower: LB,
    upper: UB
  ) extends ElasticQuery { self =>

    def greaterThan[A: ElasticPrimitive](a: A)(implicit @unused ev: LB =:= Unbounded.type): Range[Greater[A], UB] =
      self.copy(lower = Greater(a))

    def greaterEqual[A: ElasticPrimitive](a: A)(implicit
      @unused ev: LB =:= Unbounded.type
    ): Range[GreaterEqual[A], UB] =
      self.copy(lower = GreaterEqual(a))

    def lessThan[A: ElasticPrimitive](a: A)(implicit @unused ev: UB =:= Unbounded.type): Range[LB, Less[A]] =
      self.copy(upper = Less(a))

    def lessEqual[A: ElasticPrimitive](a: A)(implicit @unused ev: UB =:= Unbounded.type): Range[LB, LessEqual[A]] =
      self.copy(upper = LessEqual(a))

    override def toJson: Json = Obj("range" -> Obj(field -> Obj(List(lower.toJson, upper.toJson).flatten: _*)))
  }

  private[elasticsearch] object Range {
    def empty(field: String): Range[Unbounded.type, Unbounded.type] = Range(field, Unbounded, Unbounded)
  }
}
