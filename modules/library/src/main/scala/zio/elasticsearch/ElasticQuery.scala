package zio.elasticsearch

import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Bool, Num, Obj, Str}

import scala.annotation.unused

sealed trait ElasticQuery { self =>

  def asJson: Json

  final def asJsonBody: Json = Obj("query" -> self.asJson)

}

object ElasticQuery {

  sealed trait ElasticPrimitive[A] {
    def toJson(a: A): Json
  }
  implicit object IntElasticPrimitive extends ElasticPrimitive[Int] {
    override def toJson(a: Int): Json = Num(a)
  }
  implicit object StringElasticPrimitive extends ElasticPrimitive[String] {
    override def toJson(a: String): Json = Str(a)
  }

  implicit object BooleanElasticPrimitive extends ElasticPrimitive[Boolean] {
    override def toJson(a: Boolean): Json = Bool(a)
  }

  implicit object LongElasticPrimitive extends ElasticPrimitive[Long] {
    override def toJson(a: Long): Json = Num(a)
  }

  implicit class RichPrimitive[A](private val value: A) extends AnyVal {
    def toJson(implicit EP: ElasticPrimitive[A]): Json = EP.toJson(value)
  }

  def matches[A: ElasticPrimitive](field: String, query: A): ElasticQuery =
    Match(field, query)

  def boolQuery(): BoolQuery = BoolQuery.empty

  def range(field: String): Range[Unbounded.type, Unbounded.type] = Range.make(field)

  private[elasticsearch] final case class BoolQuery(must: List[ElasticQuery], should: List[ElasticQuery])
      extends ElasticQuery { self =>

    override def asJson: Json =
      Obj("bool" -> Obj("must" -> Arr(must.map(_.asJson): _*), "should" -> Arr(should.map(_.asJson): _*)))

    def must(queries: ElasticQuery*): BoolQuery =
      self.copy(must = must ++ queries)

    def should(queries: ElasticQuery*): BoolQuery =
      self.copy(should = should ++ queries)
  }

  private[elasticsearch] object BoolQuery {
    def empty: BoolQuery = BoolQuery(Nil, Nil)
  }

  private[elasticsearch] final case class Match[A: ElasticPrimitive](field: String, query: A) extends ElasticQuery {
    override def asJson: Json = Obj("match" -> Obj(field -> query.toJson))
  }

  sealed trait LowerBound {
    def toJson: Option[(String, Json)]
  }
  private[elasticsearch] case class Greater[A: ElasticPrimitive](a: A) extends LowerBound {
    override def toJson: Option[(String, Json)] = Option("gt" -> a.toJson)
  }
  private[elasticsearch] case class GreaterEqual[A: ElasticPrimitive](a: A) extends LowerBound {
    def toJson: Option[(String, Json)] = Option("gte" -> a.toJson)
  }

  sealed trait UpperBound {
    def toJson: Option[(String, Json)]
  }
  private[elasticsearch] case class Less[A: ElasticPrimitive](a: A) extends UpperBound {
    override def toJson: Option[(String, Json)] = Option("lt" -> a.toJson)
  }
  private[elasticsearch] case class LessEqual[A: ElasticPrimitive](a: A) extends UpperBound {
    override def toJson: Option[(String, Json)] = Option("lte" -> a.toJson)
  }

  private[elasticsearch] case object Unbounded extends LowerBound with UpperBound {
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

    override def asJson: Json = Obj("range" -> Obj(field -> Obj(List(lower.toJson, upper.toJson).flatten: _*)))
  }

  private[elasticsearch] object Range {
    def make(field: String): Range[Unbounded.type, Unbounded.type] = Range(field = field, Unbounded, Unbounded)
  }
}
