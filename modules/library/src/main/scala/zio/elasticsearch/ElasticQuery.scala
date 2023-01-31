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

import zio.elasticsearch.Boost.WithBoost
import zio.elasticsearch.CaseInsensitive.WithCaseInsensitive
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Num, Obj, Str}

import scala.annotation.unused

sealed trait ElasticQuery[EQT <: ElasticQueryType] { self =>

  def toJson: Json

  final def toJsonBody: Json = Obj("query" -> self.toJson)

  final def boost(value: Double)(implicit wb: WithBoost[EQT]): ElasticQuery[EQT] =
    wb.withBoost(query = self, value = value)

  final def caseInsensitive(value: Boolean)(implicit wci: WithCaseInsensitive[EQT]): ElasticQuery[EQT] =
    wci.withCaseInsensitive(query = self, value = value)

  final def caseInsensitiveTrue(implicit wci: WithCaseInsensitive[EQT]): ElasticQuery[EQT] =
    wci.withCaseInsensitive(query = self, value = true)

  final def caseInsensitiveFalse(implicit wci: WithCaseInsensitive[EQT]): ElasticQuery[EQT] =
    wci.withCaseInsensitive(query = self, value = false)
}

object ElasticQuery {

  import ElasticQueryType._

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
    override def toJson(value: Boolean): Json = Json.Bool(value)
  }

  implicit object ElasticLong extends ElasticPrimitive[Long] {
    override def toJson(value: Long): Json = Num(value)
  }

  implicit class ElasticPrimitiveOps[A](private val value: A) extends AnyVal {
    def toJson(implicit EP: ElasticPrimitive[A]): Json = EP.toJson(value)
  }

  def boolQuery(): BoolQuery = BoolQuery.empty

  def contains(field: String, value: String): ElasticQuery[Wildcard] = WildcardQuery(field, s"*$value*")

  def exists(field: Field[_, _]): ElasticQuery[Exists] = ExistsQuery(field.toString)

  def exists(field: String): ElasticQuery[Exists] = ExistsQuery(field)

  def matchAll(): ElasticQuery[MatchAll] = MatchAllQuery()

  def matches[A: ElasticPrimitive](
    field: Field[_, A],
    multiField: Option[String] = None,
    value: A
  ): ElasticQuery[Match] =
    MatchQuery(field.toString ++ multiField.map("." ++ _).getOrElse(""), value)

  def matches[A: ElasticPrimitive](field: String, value: A): ElasticQuery[Match] =
    MatchQuery(field, value)

  def range[A](
    field: Field[_, A],
    multiField: Option[String] = None
  ): RangeQuery[A, Unbounded.type, Unbounded.type] =
    RangeQuery.empty(field.toString ++ multiField.map("." ++ _).getOrElse(""))

  def range(field: String): RangeQuery[Any, Unbounded.type, Unbounded.type] = RangeQuery.empty[Any](field)

  def startsWith(field: String, value: String): ElasticQuery[Wildcard] = WildcardQuery(field, s"$value*")

  def term[A: ElasticPrimitive](
    field: Field[_, A],
    multiField: Option[String] = None,
    value: A
  ): ElasticQuery[Term[A]] =
    TermQuery(field.toString ++ multiField.map("." ++ _).getOrElse(""), value)

  def term[A: ElasticPrimitive](field: String, value: A): ElasticQuery[Term[A]] = TermQuery(field, value)

  def wildcard(field: String, value: String): ElasticQuery[Wildcard] = WildcardQuery(field, value)

  private[elasticsearch] final case class BoolQuery(must: List[ElasticQuery[_]], should: List[ElasticQuery[_]])
      extends ElasticQuery[Bool] { self =>

    override def toJson: Json =
      Obj("bool" -> Obj("must" -> Arr(must.map(_.toJson): _*), "should" -> Arr(should.map(_.toJson): _*)))

    def must(queries: ElasticQuery[_]*): BoolQuery =
      self.copy(must = must ++ queries)

    def should(queries: ElasticQuery[_]*): BoolQuery =
      self.copy(should = should ++ queries)
  }

  private[elasticsearch] object BoolQuery {
    def empty: BoolQuery = BoolQuery(Nil, Nil)
  }

  private[elasticsearch] final case class ExistsQuery private (field: String) extends ElasticQuery[Exists] {
    override def toJson: Json = Obj("exists" -> Obj("field" -> field.toJson))
  }

  private[elasticsearch] final case class MatchQuery[A: ElasticPrimitive](field: String, value: A)
      extends ElasticQuery[Match] {
    override def toJson: Json = Obj("match" -> Obj(field -> value.toJson))
  }

  private[elasticsearch] final case class MatchAllQuery(boost: Option[Double] = None) extends ElasticQuery[MatchAll] {
    override def toJson: Json = Obj("match_all" -> Obj(boost.map("boost" -> Num(_)).toList: _*))
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

  private[elasticsearch] final case class RangeQuery[A, LB <: LowerBound, UB <: UpperBound] private (
    field: String,
    lower: LB,
    upper: UB
  ) extends ElasticQuery[Range] { self =>

    def gt[B <: A: ElasticPrimitive](value: B)(implicit
      @unused ev: LB =:= Unbounded.type
    ): RangeQuery[B, GreaterThan[B], UB] =
      self.copy(lower = GreaterThan(value))

    def gte[B <: A: ElasticPrimitive](value: B)(implicit
      @unused ev: LB =:= Unbounded.type
    ): RangeQuery[B, GreaterThanOrEqualTo[B], UB] =
      self.copy(lower = GreaterThanOrEqualTo(value))

    def lt[B <: A: ElasticPrimitive](value: B)(implicit
      @unused ev: UB =:= Unbounded.type
    ): RangeQuery[B, LB, LessThan[B]] =
      self.copy(upper = LessThan(value))

    def lte[B <: A: ElasticPrimitive](value: B)(implicit
      @unused ev: UB =:= Unbounded.type
    ): RangeQuery[B, LB, LessThanOrEqualTo[B]] =
      self.copy(upper = LessThanOrEqualTo(value))

    override def toJson: Json = Obj("range" -> Obj(field -> Obj(List(lower.toJson, upper.toJson).flatten: _*)))
  }

  private[elasticsearch] object RangeQuery {
    def empty[A](field: String): RangeQuery[A, Unbounded.type, Unbounded.type] =
      RangeQuery[A, Unbounded.type, Unbounded.type](field, Unbounded, Unbounded)
  }

  private[elasticsearch] final case class TermQuery[A: ElasticPrimitive](
    field: String,
    value: A,
    boost: Option[Double] = None,
    caseInsensitive: Option[Boolean] = None
  ) extends ElasticQuery[Term[A]] { self =>
    override def toJson: Json = {
      val termFields = Some("value" -> value.toJson) ++ boost.map("boost" -> Num(_)) ++ caseInsensitive.map(
        "case_insensitive" -> Json.Bool(_)
      )
      Obj("term" -> Obj(field -> Obj(termFields.toList: _*)))
    }
  }

  private[elasticsearch] final case class WildcardQuery(
    field: String,
    value: String,
    boost: Option[Double] = None,
    caseInsensitive: Option[Boolean] = None
  ) extends ElasticQuery[Wildcard] { self =>
    override def toJson: Json = {
      val wildcardFields = Some("value" -> value.toJson) ++ boost.map("boost" -> Num(_)) ++ caseInsensitive.map(
        "case_insensitive" -> Json.Bool(_)
      )
      Obj("wildcard" -> Obj(field -> Obj(wildcardFields.toList: _*)))
    }
  }

}

sealed trait ElasticQueryType

object ElasticQueryType {
  trait Bool     extends ElasticQueryType
  trait Exists   extends ElasticQueryType
  trait Match    extends ElasticQueryType
  trait MatchAll extends ElasticQueryType
  trait Range    extends ElasticQueryType
  trait Term[A]  extends ElasticQueryType
  trait Wildcard extends ElasticQueryType
}
