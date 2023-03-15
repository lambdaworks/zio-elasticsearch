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

import zio.elasticsearch.ElasticPrimitive.{ElasticPrimitive, ElasticPrimitiveOps}
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Num, Obj, Str}

import scala.annotation.unused

trait HasBoost[Q <: HasBoost[Q]] {
  def boost(value: Double): Q
}

sealed trait HasCaseInsensitive[Q <: HasCaseInsensitive[Q]] {
  def caseInsensitive(value: Boolean): Q

  def caseInsensitiveFalse: Q

  def caseInsensitiveTrue: Q
}

sealed trait HasIgnoreUnmapped[Q <: HasIgnoreUnmapped[Q]] {
  def ignoreUnmapped(value: Boolean): Q

  def ignoreUnmappedFalse: Q

  def ignoreUnmappedTrue: Q
}

sealed trait HasScoreMode[Q <: HasScoreMode[Q]] {
  def scoreMode(scoreMode: ScoreMode): Q
}

sealed trait ElasticQuery[-S] { self =>
  def paramsToJson(fieldPath: Option[String]): Json

  final def toJson: Json =
    Obj("query" -> self.paramsToJson(None))
}

object ElasticQuery {
  def contains[S](field: Field[S, _], value: String): WildcardQuery[S] =
    Wildcard(field = field.toString, value = s"*$value*", boost = None, caseInsensitive = None)

  def contains(field: String, value: String): WildcardQuery[Any] =
    Wildcard(field = field, value = s"*$value*", boost = None, caseInsensitive = None)

  def exists[S](field: Field[S, _]): ExistsQuery[S] =
    Exists(field = field.toString)

  def exists(field: String): ExistsQuery[Any] =
    Exists(field = field)

  def filter[S](queries: ElasticQuery[S]*): BoolQuery[S] =
    Bool[S](filter = queries.toList, must = Nil, should = Nil, boost = None)

  def matchAll: MatchAllQuery =
    MatchAll(boost = None)

  def matches[S, A: ElasticPrimitive](field: Field[S, A], multiField: Option[String] = None, value: A): MatchQuery[S] =
    Match(field = field.toString ++ multiField.map("." ++ _).getOrElse(""), value = value)

  def matches[A: ElasticPrimitive](field: String, value: A): MatchQuery[Any] =
    Match(field = field, value = value)

  def must[S](queries: ElasticQuery[S]*): BoolQuery[S] =
    Bool[S](filter = Nil, must = queries.toList, should = Nil, boost = None)

  def nested[S, A](path: Field[S, Seq[A]], query: ElasticQuery[A]): NestedQuery[S] =
    Nested(path = path.toString, query = query, scoreMode = None, ignoreUnmapped = None)

  def nested(path: String, query: ElasticQuery[_]): NestedQuery[Any] =
    Nested(path = path, query = query, scoreMode = None, ignoreUnmapped = None)

  def range[S, A](
    field: Field[S, A],
    multiField: Option[String] = None
  ): RangeQuery[S, A, Unbounded.type, Unbounded.type] =
    Range.empty(field.toString ++ multiField.map("." ++ _).getOrElse(""))

  def range(field: String): RangeQuery[Any, Any, Unbounded.type, Unbounded.type] =
    Range.empty[Any, Any](field = field)

  def should[S](queries: ElasticQuery[S]*): BoolQuery[S] =
    Bool[S](filter = Nil, must = Nil, should = queries.toList, boost = None)

  def startsWith[S](field: Field[S, _], value: String): WildcardQuery[S] =
    Wildcard(field = field.toString, value = s"$value*", boost = None, caseInsensitive = None)

  def startsWith(field: String, value: String): WildcardQuery[Any] =
    Wildcard(field = field, value = s"$value*", boost = None, caseInsensitive = None)

  def term[S, A: ElasticPrimitive](field: Field[S, A], multiField: Option[String] = None, value: A): TermQuery[S] =
    Term(
      field = field.toString ++ multiField.map("." ++ _).getOrElse(""),
      value = value,
      boost = None,
      caseInsensitive = None
    )

  def term[A: ElasticPrimitive](field: String, value: A): Term[Any, A] =
    Term(field = field, value = value, boost = None, caseInsensitive = None)

  def wildcard[S](field: Field[S, _], value: String): Wildcard[S] =
    Wildcard(field = field.toString, value = value, boost = None, caseInsensitive = None)

  def wildcard(field: String, value: String): Wildcard[Any] =
    Wildcard(field = field, value = value, boost = None, caseInsensitive = None)

  sealed trait BoolQuery[S] extends ElasticQuery[S] with HasBoost[BoolQuery[S]] {
    def filter(queries: ElasticQuery[S]*): BoolQuery[S]

    def must(queries: ElasticQuery[S]*): BoolQuery[S]

    def should(queries: ElasticQuery[S]*): BoolQuery[S]
  }

  private[elasticsearch] final case class Bool[S](
    filter: List[ElasticQuery[S]],
    must: List[ElasticQuery[S]],
    should: List[ElasticQuery[S]],
    boost: Option[Double]
  ) extends BoolQuery[S] { self =>
    def boost(value: Double): BoolQuery[S] =
      self.copy(boost = Some(value))

    def filter(queries: ElasticQuery[S]*): BoolQuery[S] =
      self.copy(filter = filter ++ queries)

    def must(queries: ElasticQuery[S]*): BoolQuery[S] =
      self.copy(must = must ++ queries)

    def paramsToJson(fieldPath: Option[String]): Json = {
      val boolFields =
        Some("filter" -> Arr(filter.map(_.paramsToJson(fieldPath)): _*)) ++
          Some("must" -> Arr(must.map(_.paramsToJson(fieldPath)): _*)) ++
          Some("should" -> Arr(should.map(_.paramsToJson(fieldPath)): _*)) ++
          boost.map("boost" -> Num(_))
      Obj("bool" -> Obj(boolFields.toList: _*))
    }

    def should(queries: ElasticQuery[S]*): BoolQuery[S] =
      self.copy(should = should ++ queries)
  }

  sealed trait ExistsQuery[S] extends ElasticQuery[S]

  private[elasticsearch] final case class Exists[S](field: String) extends ExistsQuery[S] {
    def paramsToJson(fieldPath: Option[String]): Json =
      Obj(
        "exists" -> Obj("field" -> (fieldPath.map(_ + ".").getOrElse("") + field).toJson)
      )
  }

  sealed trait MatchQuery[S] extends ElasticQuery[S]

  private[elasticsearch] final case class Match[S, A: ElasticPrimitive](field: String, value: A) extends MatchQuery[S] {
    def paramsToJson(fieldPath: Option[String]): Json =
      Obj(
        "match" -> Obj(fieldPath.map(_ + ".").getOrElse("") + field -> value.toJson)
      )
  }

  sealed trait MatchAllQuery extends ElasticQuery[Any] with HasBoost[MatchAllQuery]

  private[elasticsearch] final case class MatchAll(boost: Option[Double]) extends MatchAllQuery { self =>
    def boost(value: Double): MatchAllQuery =
      self.copy(boost = Some(value))

    def paramsToJson(fieldPath: Option[String]): Json =
      Obj("match_all" -> Obj(boost.map("boost" -> Num(_)).toList: _*))
  }

  sealed trait NestedQuery[S]
      extends ElasticQuery[S]
      with HasIgnoreUnmapped[NestedQuery[S]]
      with HasScoreMode[NestedQuery[S]]

  private[elasticsearch] final case class Nested[S](
    path: String,
    query: ElasticQuery[_],
    scoreMode: Option[ScoreMode],
    ignoreUnmapped: Option[Boolean]
  ) extends NestedQuery[S] { self =>
    def ignoreUnmapped(value: Boolean): NestedQuery[S] =
      self.copy(ignoreUnmapped = Some(value))

    def ignoreUnmappedFalse: NestedQuery[S] =
      ignoreUnmapped(false)

    def ignoreUnmappedTrue: NestedQuery[S] =
      ignoreUnmapped(true)

    def paramsToJson(fieldPath: Option[String]): Json =
      Obj(
        "nested" -> Obj(
          List(
            "path"  -> fieldPath.map(fieldPath => Str(fieldPath + "." + path)).getOrElse(Str(path)),
            "query" -> query.paramsToJson(fieldPath.map(_ + "." + path).orElse(Some(path)))
          ) ++ scoreMode.map(scoreMode => "score_mode" -> Str(scoreMode.toString.toLowerCase)) ++ ignoreUnmapped.map(
            "ignore_unmapped" -> Json.Bool(_)
          ): _*
        )
      )

    def scoreMode(scoreMode: ScoreMode): NestedQuery[S] =
      self.copy(scoreMode = Some(scoreMode))
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

  sealed trait RangeQuery[S, A, LB <: LowerBound, UB <: UpperBound]
      extends ElasticQuery[S]
      with HasBoost[RangeQuery[S, A, LB, UB]] {
    def gt[B <: A: ElasticPrimitive](value: B)(implicit
      @unused ev: LB =:= Unbounded.type
    ): RangeQuery[S, B, GreaterThan[B], UB]

    def gte[B <: A: ElasticPrimitive](value: B)(implicit
      @unused ev: LB =:= Unbounded.type
    ): RangeQuery[S, B, GreaterThanOrEqualTo[B], UB]

    def lt[B <: A: ElasticPrimitive](value: B)(implicit
      @unused ev: UB =:= Unbounded.type
    ): RangeQuery[S, B, LB, LessThan[B]]

    def lte[B <: A: ElasticPrimitive](value: B)(implicit
      @unused ev: UB =:= Unbounded.type
    ): RangeQuery[S, B, LB, LessThanOrEqualTo[B]]
  }

  private[elasticsearch] final case class Range[S, A, LB <: LowerBound, UB <: UpperBound] private (
    field: String,
    lower: LB,
    upper: UB,
    boost: Option[Double]
  ) extends RangeQuery[S, A, LB, UB] { self =>

    def boost(value: Double): RangeQuery[S, A, LB, UB] =
      self.copy(boost = Some(value))

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
        fieldPath.map(_ + ".").getOrElse("") + field -> Obj(List(lower.toJson, upper.toJson).flatten: _*)
      ) ++ boost.map("boost" -> Num(_))
      Obj("range" -> Obj(rangeFields.toList: _*))
    }
  }

  private[elasticsearch] object Range {
    def empty[S, A](field: String): Range[S, A, Unbounded.type, Unbounded.type] =
      Range[S, A, Unbounded.type, Unbounded.type](
        field = field,
        lower = Unbounded,
        upper = Unbounded,
        boost = None
      )
  }

  sealed trait TermQuery[S] extends ElasticQuery[S] with HasBoost[TermQuery[S]] with HasCaseInsensitive[TermQuery[S]]

  private[elasticsearch] final case class Term[S, A: ElasticPrimitive](
    field: String,
    value: A,
    boost: Option[Double],
    caseInsensitive: Option[Boolean]
  ) extends TermQuery[S] { self =>
    def boost(value: Double): TermQuery[S] =
      self.copy(boost = Some(value))

    def caseInsensitive(value: Boolean): TermQuery[S] =
      self.copy(caseInsensitive = Some(value))

    def caseInsensitiveFalse: TermQuery[S] =
      caseInsensitive(false)

    def caseInsensitiveTrue: TermQuery[S] =
      caseInsensitive(true)

    def paramsToJson(fieldPath: Option[String]): Json = {
      val termFields = Some("value" -> value.toJson) ++ boost.map("boost" -> Num(_)) ++ caseInsensitive.map(
        "case_insensitive" -> Json.Bool(_)
      )
      Obj("term" -> Obj(fieldPath.map(_ + ".").getOrElse("") + field -> Obj(termFields.toList: _*)))
    }
  }

  sealed trait WildcardQuery[S]
      extends ElasticQuery[S]
      with HasBoost[WildcardQuery[S]]
      with HasCaseInsensitive[WildcardQuery[S]]

  private[elasticsearch] final case class Wildcard[S](
    field: String,
    value: String,
    boost: Option[Double],
    caseInsensitive: Option[Boolean]
  ) extends WildcardQuery[S] { self =>
    def boost(value: Double): WildcardQuery[S] =
      self.copy(boost = Some(value))

    def caseInsensitive(value: Boolean): WildcardQuery[S] =
      self.copy(caseInsensitive = Some(value))

    def caseInsensitiveFalse: WildcardQuery[S] =
      caseInsensitive(false)

    def caseInsensitiveTrue: WildcardQuery[S] =
      caseInsensitive(true)

    def paramsToJson(fieldPath: Option[String]): Json = {
      val wildcardFields = Some("value" -> value.toJson) ++ boost.map("boost" -> Num(_)) ++ caseInsensitive.map(
        "case_insensitive" -> Json.Bool(_)
      )
      Obj("wildcard" -> Obj(fieldPath.map(_ + ".").getOrElse("") + field -> Obj(wildcardFields.toList: _*)))
    }
  }

}
