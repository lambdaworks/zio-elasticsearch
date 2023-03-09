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
import zio.elasticsearch.IgnoreUnmapped.WithIgnoreUnmapped
import zio.elasticsearch.ScoreMode.WithScoreMode
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Num, Obj, Str}

import scala.annotation.unused

sealed trait ElasticQuery[-S, EQT <: ElasticQueryType] { self =>

  def paramsToJson(fieldPath: Option[String]): Json

  final def toJson: Json = Obj("query" -> self.paramsToJson(None))

  final def boost(value: Double)(implicit wb: WithBoost[EQT]): ElasticQuery[S, EQT] =
    wb.withBoost(query = self, value = value)

  final def caseInsensitive(value: Boolean)(implicit wci: WithCaseInsensitive[EQT]): ElasticQuery[S, EQT] =
    wci.withCaseInsensitive(query = self, value = value)

  final def caseInsensitiveFalse(implicit wci: WithCaseInsensitive[EQT]): ElasticQuery[S, EQT] =
    caseInsensitive(value = false)

  final def caseInsensitiveTrue(implicit wci: WithCaseInsensitive[EQT]): ElasticQuery[S, EQT] =
    caseInsensitive(value = true)

  final def ignoreUnmapped(value: Boolean)(implicit wiu: WithIgnoreUnmapped[EQT]): ElasticQuery[S, EQT] =
    wiu.withIgnoreUnmapped(query = self, value = value)

  final def ignoreUnmappedFalse(implicit wiu: WithIgnoreUnmapped[EQT]): ElasticQuery[S, EQT] =
    ignoreUnmapped(value = false)

  final def ignoreUnmappedTrue(implicit wiu: WithIgnoreUnmapped[EQT]): ElasticQuery[S, EQT] =
    ignoreUnmapped(value = true)

  final def scoreMode(scoreMode: ScoreMode)(implicit wsm: WithScoreMode[EQT]): ElasticQuery[S, EQT] =
    wsm.withScoreMode(query = self, scoreMode = scoreMode)
}

object ElasticQuery {

  import ElasticQueryType._

  sealed trait ElasticPrimitive[A] {
    def toJson(value: A): Json
  }

  implicit object ElasticInt extends ElasticPrimitive[Int] {
    def toJson(value: Int): Json = Num(value)
  }

  implicit object ElasticString extends ElasticPrimitive[String] {
    def toJson(value: String): Json = Str(value)
  }

  implicit object ElasticBool extends ElasticPrimitive[Boolean] {
    def toJson(value: Boolean): Json = Json.Bool(value)
  }

  implicit object ElasticLong extends ElasticPrimitive[Long] {
    def toJson(value: Long): Json = Num(value)
  }

  final implicit class ElasticPrimitiveOps[A](private val value: A) extends AnyVal {
    def toJson(implicit EP: ElasticPrimitive[A]): Json = EP.toJson(value)
  }

  def contains(field: String, value: String): ElasticQuery[Any, Wildcard] =
    WildcardQuery(field = field, value = s"*$value*", boost = None, caseInsensitive = None)

  def exists[S](field: Field[S, _]): ElasticQuery[S, Exists] = ExistsQuery(field.toString)

  def exists(field: String): ElasticQuery[Any, Exists] = ExistsQuery(field)

  def filter[S](queries: ElasticQuery[S, _]*): BoolQuery[S] =
    BoolQuery[S](filter = queries.toList, must = Nil, should = Nil, boost = None)

  def matchAll: ElasticQuery[Any, MatchAll] = MatchAllQuery(boost = None)

  def matches[S, A: ElasticPrimitive](
    field: Field[S, A],
    multiField: Option[String] = None,
    value: A
  ): ElasticQuery[S, Match] =
    MatchQuery(field.toString ++ multiField.map("." ++ _).getOrElse(""), value)

  def matches[A: ElasticPrimitive](field: String, value: A): ElasticQuery[Any, Match] =
    MatchQuery(field, value)

  def must[S](queries: ElasticQuery[S, _]*): BoolQuery[S] =
    BoolQuery[S](filter = Nil, must = queries.toList, should = Nil, boost = None)

  def nested[S, A](path: Field[S, Seq[A]], query: ElasticQuery[A, _]): ElasticQuery[S, Nested] =
    NestedQuery(path.toString, query, None, None)

  def nested(path: String, query: ElasticQuery[_, _]): ElasticQuery[Any, Nested] =
    NestedQuery(path, query, None, None)

  def range[S, A](
    field: Field[S, A],
    multiField: Option[String] = None
  ): RangeQuery[S, A, Unbounded.type, Unbounded.type] =
    RangeQuery.empty(field.toString ++ multiField.map("." ++ _).getOrElse(""))

  def range(field: String): RangeQuery[Any, Any, Unbounded.type, Unbounded.type] = RangeQuery.empty[Any, Any](field)

  def should[S](queries: ElasticQuery[S, _]*): BoolQuery[S] =
    BoolQuery[S](filter = Nil, must = Nil, should = queries.toList, boost = None)

  def startsWith(field: String, value: String): ElasticQuery[Any, Wildcard] =
    WildcardQuery(field = field, value = s"$value*", boost = None, caseInsensitive = None)

  def term[S, A: ElasticPrimitive](
    field: Field[S, A],
    multiField: Option[String] = None,
    value: A
  ): ElasticQuery[S, Term[A]] =
    TermQuery(
      field = field.toString ++ multiField.map("." ++ _).getOrElse(""),
      value = value,
      boost = None,
      caseInsensitive = None
    )

  def term[A: ElasticPrimitive](field: String, value: A): ElasticQuery[Any, Term[A]] =
    TermQuery(field = field, value = value, boost = None, caseInsensitive = None)

  def wildcard(field: String, value: String): ElasticQuery[Any, Wildcard] =
    WildcardQuery(field = field, value = value, boost = None, caseInsensitive = None)

  private[elasticsearch] final case class BoolQuery[S](
    filter: List[ElasticQuery[S, _]],
    must: List[ElasticQuery[S, _]],
    should: List[ElasticQuery[S, _]],
    boost: Option[Double]
  ) extends ElasticQuery[S, Bool] { self =>

    def filter(queries: ElasticQuery[S, _]*): BoolQuery[S] =
      self.copy(filter = filter ++ queries)

    def must(queries: ElasticQuery[S, _]*): BoolQuery[S] =
      self.copy(must = must ++ queries)

    def paramsToJson(fieldPath: Option[String]): Json = {
      val boolFields =
        Some("filter" -> Arr(filter.map(_.paramsToJson(fieldPath)): _*)) ++
          Some("must" -> Arr(must.map(_.paramsToJson(fieldPath)): _*)) ++
          Some("should" -> Arr(should.map(_.paramsToJson(fieldPath)): _*)) ++
          boost.map("boost" -> Num(_))
      Obj("bool" -> Obj(boolFields.toList: _*))
    }

    def should(queries: ElasticQuery[S, _]*): BoolQuery[S] =
      self.copy(should = should ++ queries)
  }

  private[elasticsearch] final case class ExistsQuery[S](field: String) extends ElasticQuery[S, Exists] {
    def paramsToJson(fieldPath: Option[String]): Json = Obj(
      "exists" -> Obj("field" -> (fieldPath.getOrElse("") ++ field).toJson)
    )
  }

  private[elasticsearch] final case class MatchQuery[S, A: ElasticPrimitive](field: String, value: A)
      extends ElasticQuery[S, Match] {
    def paramsToJson(fieldPath: Option[String]): Json = Obj(
      "match" -> Obj(fieldPath.getOrElse("") ++ field -> value.toJson)
    )
  }

  private[elasticsearch] final case class MatchAllQuery(boost: Option[Double]) extends ElasticQuery[Any, MatchAll] {
    def paramsToJson(fieldPath: Option[String]): Json = Obj("match_all" -> Obj(boost.map("boost" -> Num(_)).toList: _*))
  }

  private[elasticsearch] final case class NestedQuery[S](
    path: String,
    query: ElasticQuery[_, _],
    scoreMode: Option[ScoreMode],
    ignoreUnmapped: Option[Boolean]
  ) extends ElasticQuery[S, Nested] {
    def paramsToJson(fieldPath: Option[String]): Json = Obj(
      "nested" -> Obj(
        List(
          "path"  -> Str(path),
          "query" -> query.paramsToJson(fieldPath.map(_ + path).orElse(Some(path)))
        ) ++ scoreMode.map(scoreMode => "score_mode" -> Str(scoreMode.toString.toLowerCase)) ++ ignoreUnmapped.map(
          "ignore_unmapped" -> Json.Bool(_)
        ): _*
      )
    )
  }

  sealed trait LowerBound {
    def toJson: Option[(String, Json)]
  }

  private[elasticsearch] final case class GreaterThan[A: ElasticPrimitive](value: A) extends LowerBound {
    def toJson: Option[(String, Json)] = Some("gt" -> value.toJson)
  }

  private[elasticsearch] final case class GreaterThanOrEqualTo[A: ElasticPrimitive](value: A) extends LowerBound {
    def toJson: Option[(String, Json)] = Some("gte" -> value.toJson)
  }

  sealed trait UpperBound {
    def toJson: Option[(String, Json)]
  }

  private[elasticsearch] final case class LessThan[A: ElasticPrimitive](value: A) extends UpperBound {
    def toJson: Option[(String, Json)] = Some("lt" -> value.toJson)
  }

  private[elasticsearch] final case class LessThanOrEqualTo[A: ElasticPrimitive](value: A) extends UpperBound {
    def toJson: Option[(String, Json)] = Some("lte" -> value.toJson)
  }

  private[elasticsearch] final case object Unbounded extends LowerBound with UpperBound {
    def toJson: Option[(String, Json)] = None
  }

  private[elasticsearch] final case class RangeQuery[S, A, LB <: LowerBound, UB <: UpperBound] private (
    field: String,
    lower: LB,
    upper: UB,
    boost: Option[Double]
  ) extends ElasticQuery[S, Range[A, LB, UB]] { self =>

    def gt[B <: A: ElasticPrimitive](value: B)(implicit
      @unused ev: LB =:= Unbounded.type
    ): RangeQuery[S, B, GreaterThan[B], UB] =
      self.copy(lower = GreaterThan(value))

    def gte[B <: A: ElasticPrimitive](value: B)(implicit
      @unused ev: LB =:= Unbounded.type
    ): RangeQuery[S, B, GreaterThanOrEqualTo[B], UB] =
      self.copy(lower = GreaterThanOrEqualTo(value))

    def lt[B <: A: ElasticPrimitive](value: B)(implicit
      @unused ev: UB =:= Unbounded.type
    ): RangeQuery[S, B, LB, LessThan[B]] =
      self.copy(upper = LessThan(value))

    def lte[B <: A: ElasticPrimitive](value: B)(implicit
      @unused ev: UB =:= Unbounded.type
    ): RangeQuery[S, B, LB, LessThanOrEqualTo[B]] =
      self.copy(upper = LessThanOrEqualTo(value))

    def paramsToJson(fieldPath: Option[String]): Json = {
      val rangeFields = Some(
        fieldPath.getOrElse("") ++ field -> Obj(List(lower.toJson, upper.toJson).flatten: _*)
      ) ++ boost.map("boost" -> Num(_))
      Obj("range" -> Obj(rangeFields.toList: _*))
    }
  }

  private[elasticsearch] object RangeQuery {
    def empty[S, A](field: String): RangeQuery[S, A, Unbounded.type, Unbounded.type] =
      RangeQuery[S, A, Unbounded.type, Unbounded.type](
        field = field,
        lower = Unbounded,
        upper = Unbounded,
        boost = None
      )
  }

  private[elasticsearch] final case class TermQuery[S, A: ElasticPrimitive](
    field: String,
    value: A,
    boost: Option[Double],
    caseInsensitive: Option[Boolean]
  ) extends ElasticQuery[S, Term[A]] { self =>
    def paramsToJson(fieldPath: Option[String]): Json = {
      val termFields = Some("value" -> value.toJson) ++ boost.map("boost" -> Num(_)) ++ caseInsensitive.map(
        "case_insensitive" -> Json.Bool(_)
      )
      Obj("term" -> Obj(fieldPath.getOrElse("") ++ field -> Obj(termFields.toList: _*)))
    }
  }

  private[elasticsearch] final case class WildcardQuery[S](
    field: String,
    value: String,
    boost: Option[Double],
    caseInsensitive: Option[Boolean]
  ) extends ElasticQuery[S, Wildcard] { self =>
    def paramsToJson(fieldPath: Option[String]): Json = {
      val wildcardFields = Some("value" -> value.toJson) ++ boost.map("boost" -> Num(_)) ++ caseInsensitive.map(
        "case_insensitive" -> Json.Bool(_)
      )
      Obj("wildcard" -> Obj(fieldPath.getOrElse("") ++ field -> Obj(wildcardFields.toList: _*)))
    }
  }

}

sealed trait ElasticQueryType

object ElasticQueryType {
  sealed trait Bool             extends ElasticQueryType
  sealed trait Exists           extends ElasticQueryType
  sealed trait Match            extends ElasticQueryType
  sealed trait MatchAll         extends ElasticQueryType
  sealed trait Nested           extends ElasticQueryType
  sealed trait Range[A, LB, UB] extends ElasticQueryType
  sealed trait Term[A]          extends ElasticQueryType
  sealed trait Wildcard         extends ElasticQueryType
}
