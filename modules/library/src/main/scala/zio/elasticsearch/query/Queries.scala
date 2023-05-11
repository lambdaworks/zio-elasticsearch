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

package zio.elasticsearch.query

import zio.Chunk
import zio.elasticsearch.ElasticPrimitive._
import zio.elasticsearch.query.options._
import zio.elasticsearch.query.sort.options.HasFormat
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Num, Obj, Str}
import zio.schema.Schema

import scala.annotation.unused

sealed trait ElasticQuery[-S] { self =>
  private[elasticsearch] def paramsToJson(fieldPath: Option[String]): Json

  private[elasticsearch] final def toJson: Obj =
    Obj("query" -> self.paramsToJson(None))
}

sealed trait BoolQuery[S] extends ElasticQuery[S] with HasBoost[BoolQuery[S]] with HasMinimumShouldMatch[BoolQuery[S]] {
  def filter[S1 <: S: Schema](queries: ElasticQuery[S1]*): BoolQuery[S1]

  def filter(queries: ElasticQuery[Any]*): BoolQuery[S]

  def must[S1 <: S: Schema](queries: ElasticQuery[S1]*): BoolQuery[S1]

  def must(queries: ElasticQuery[Any]*): BoolQuery[S]

  def mustNot[S1 <: S: Schema](queries: ElasticQuery[S1]*): BoolQuery[S1]

  def mustNot(queries: ElasticQuery[Any]*): BoolQuery[S]

  def should[S1 <: S: Schema](queries: ElasticQuery[S1]*): BoolQuery[S1]

  def should(queries: ElasticQuery[Any]*): BoolQuery[S]
}

private[elasticsearch] final case class Bool[S](
  filter: List[ElasticQuery[S]],
  must: List[ElasticQuery[S]],
  mustNot: List[ElasticQuery[S]],
  should: List[ElasticQuery[S]],
  boost: Option[Double],
  minimumShouldMatch: Option[Int]
) extends BoolQuery[S] { self =>
  def boost(value: Double): BoolQuery[S] =
    self.copy(boost = Some(value))

  def filter[S1 <: S: Schema](queries: ElasticQuery[S1]*): BoolQuery[S1] =
    self.copy(filter = filter ++ queries)

  def filter(queries: ElasticQuery[Any]*): BoolQuery[S] =
    self.copy(filter = filter ++ queries)

  def minimumShouldMatch(value: Int): BoolQuery[S] =
    self.copy(minimumShouldMatch = Some(value))

  def must[S1 <: S: Schema](queries: ElasticQuery[S1]*): BoolQuery[S1] =
    self.copy(must = must ++ queries)

  def must(queries: ElasticQuery[Any]*): BoolQuery[S] =
    self.copy(must = must ++ queries)

  def mustNot[S1 <: S: Schema](queries: ElasticQuery[S1]*): BoolQuery[S1] =
    self.copy(mustNot = mustNot ++ queries)

  def mustNot(queries: ElasticQuery[Any]*): BoolQuery[S] =
    self.copy(mustNot = mustNot ++ queries)

  def paramsToJson(fieldPath: Option[String]): Json = {
    val boolFields =
      List(
        if (filter.nonEmpty) Some("filter" -> Arr(filter.map(_.paramsToJson(fieldPath)): _*)) else None,
        if (must.nonEmpty) Some("must" -> Arr(must.map(_.paramsToJson(fieldPath)): _*)) else None,
        if (mustNot.nonEmpty) Some("must_not" -> Arr(mustNot.map(_.paramsToJson(fieldPath)): _*)) else None,
        if (should.nonEmpty) Some("should" -> Arr(should.map(_.paramsToJson(fieldPath)): _*)) else None,
        boost.map("boost" -> Num(_)),
        minimumShouldMatch.map("minimum_should_match" -> Num(_))
      ).collect { case Some(obj) => obj }

    Obj("bool" -> Obj(boolFields: _*))
  }

  def should[S1 <: S: Schema](queries: ElasticQuery[S1]*): BoolQuery[S1] =
    self.copy(should = should ++ queries)

  def should(queries: ElasticQuery[Any]*): BoolQuery[S] =
    self.copy(should = should ++ queries)
}

sealed trait ExistsQuery[S] extends ElasticQuery[S]

private[elasticsearch] final case class Exists[S](field: String) extends ExistsQuery[S] {
  def paramsToJson(fieldPath: Option[String]): Json =
    Obj("exists" -> Obj("field" -> fieldPath.foldRight(field)(_ + "." + _).toJson))
}

sealed trait HasChildQuery[S]
    extends ElasticQuery[S]
    with HasIgnoreUnmapped[HasChildQuery[S]]
    with HasInnerHits[HasChildQuery[S]]
    with HasScoreMode[HasChildQuery[S]] {

  /**
   * Sets the `maxChildren` parameter for the [[HasChildQuery]].
   *
   * Indicates maximum number of child documents that match the query allowed for a returned parent document. If the
   * parent document exceeds this limit, it is excluded from the search results.
   *
   * @param value
   *   the [[scala.Int]] value for `score` parameter
   * @return
   *   a new instance of the [[HasChildQuery]] with the `score` value set.
   */
  def maxChildren(value: Int): HasChildQuery[S]

  /**
   * Sets the `minChildren` parameter for the [[HasChildQuery]].
   *
   * Indicates minimum number of child documents that match the query required to match the query for a returned parent
   * document. If the parent document does not meet this limit, it is excluded from the search results.
   *
   * @param value
   *   the [[scala.Int]] value for `score` parameter
   * @return
   *   a new instance of the [[HasChildQuery]] with the `score` value set.
   */
  def minChildren(value: Int): HasChildQuery[S]
}

private[elasticsearch] final case class HasChild[S](
  childType: String,
  query: ElasticQuery[S],
  ignoreUnmapped: Option[Boolean] = None,
  innerHitsField: Option[InnerHits] = None,
  maxChildren: Option[Int] = None,
  minChildren: Option[Int] = None,
  scoreMode: Option[ScoreMode] = None
) extends HasChildQuery[S] { self =>

  def ignoreUnmapped(value: Boolean): HasChildQuery[S] = self.copy(ignoreUnmapped = Some(value))

  def innerHits(innerHits: InnerHits): HasChildQuery[S] = self.copy(innerHitsField = Some(innerHits))

  def maxChildren(value: Int): HasChildQuery[S] = self.copy(maxChildren = Some(value))

  def minChildren(value: Int): HasChildQuery[S] = self.copy(minChildren = Some(value))

  def paramsToJson(fieldPath: Option[String]): Json =
    Obj(
      "has_child" -> Obj(
        List(
          Some("type"  -> Str(childType)),
          Some("query" -> query.paramsToJson(None)),
          ignoreUnmapped.map("ignore_unmapped" -> Json.Bool(_)),
          innerHitsField.map(_.toStringJsonPair),
          maxChildren.map("max_children" -> Json.Num(_)),
          minChildren.map("min_children" -> Json.Num(_)),
          scoreMode.map(sm => "score_mode" -> Json.Str(sm.toString.toLowerCase))
        ).flatten: _*
      )
    )

  def scoreMode(value: ScoreMode): HasChildQuery[S] = self.copy(scoreMode = Some(value))
}

sealed trait HasParentQuery[S]
    extends ElasticQuery[S]
    with HasIgnoreUnmapped[HasParentQuery[S]]
    with HasInnerHits[HasParentQuery[S]] {

  /**
   * Sets the `score` parameter parameter for the [[HasParentQuery]].
   *
   * Indicates whether the relevance score of a matching parent document is aggregated into its child documents.
   * Defaults to false.
   *
   * @param value
   *   the [[scala.Boolean]] value for `score` parameter
   * @return
   *   a new instance of the [[HasParentQuery]] with the `score` value set.
   */
  def withScore(value: Boolean): HasParentQuery[S]

  /**
   * Sets the `score` parameter to `false` for this [[HasParentQuery]]. Same as [[withScore]](false).
   *
   * @return
   *   a new instance of the [[HasParentQuery]] with the `score` value set to `false`.
   * @see
   *   #withScore
   */
  final def withScoreFalse: HasParentQuery[S] = withScore(false)

  /**
   * Sets the `score` parameter to `true` for this [[HasParentQuery]]. Same as [[withScore]](true).
   *
   * @return
   *   a new instance of the [[HasParentQuery]] with the `score` value set to `true`.
   * @see
   *   #withScore
   */
  final def withScoreTrue: HasParentQuery[S] = withScore(true)

}

private[elasticsearch] final case class HasParent[S](
  parentType: String,
  query: ElasticQuery[S],
  ignoreUnmapped: Option[Boolean] = None,
  innerHitsField: Option[InnerHits] = None,
  score: Option[Boolean] = None
) extends HasParentQuery[S] {
  self =>

  def ignoreUnmapped(value: Boolean): HasParentQuery[S] =
    self.copy(ignoreUnmapped = Some(value))

  def innerHits(innerHits: InnerHits): HasParentQuery[S] =
    self.copy(innerHitsField = Some(innerHits))

  def paramsToJson(fieldPath: Option[String]): Json =
    Obj(
      "has_parent" -> Obj(
        List(
          Some("parent_type" -> Str(parentType)),
          Some("query"       -> query.paramsToJson(None)),
          ignoreUnmapped.map("ignore_unmapped" -> Json.Bool(_)),
          score.map("score" -> Json.Bool(_)),
          innerHitsField.map(_.toStringJsonPair)
        ).flatten: _*
      )
    )

  def withScore(value: Boolean): HasParent[S] =
    self.copy(score = Some(value))
}

sealed trait MatchQuery[S] extends ElasticQuery[S] with HasBoost[MatchQuery[S]]

private[elasticsearch] final case class Match[S, A: ElasticPrimitive](field: String, value: A, boost: Option[Double])
    extends MatchQuery[S] { self =>
  def boost(value: Double): MatchQuery[S] =
    self.copy(boost = Some(value))

  def paramsToJson(fieldPath: Option[String]): Json = {
    val matchFields = Some(fieldPath.foldRight(field)(_ + "." + _) -> value.toJson) ++ boost.map("boost" -> Num(_))
    Obj("match" -> Obj(matchFields.toList: _*))
  }
}

sealed trait MatchAllQuery extends ElasticQuery[Any] with HasBoost[MatchAllQuery]

private[elasticsearch] final case class MatchAll(boost: Option[Double]) extends MatchAllQuery { self =>
  def boost(value: Double): MatchAllQuery =
    self.copy(boost = Some(value))

  def paramsToJson(fieldPath: Option[String]): Json =
    Obj("match_all" -> Obj(boost.map("boost" -> Num(_)).toList: _*))
}

sealed trait MatchPhraseQuery[S] extends ElasticQuery[S] with HasBoost[MatchPhraseQuery[S]]

private[elasticsearch] final case class MatchPhrase[S](field: String, value: String, boost: Option[Double])
    extends MatchPhraseQuery[S] { self =>
  def boost(value: Double): MatchPhraseQuery[S] =
    self.copy(boost = Some(value))

  def paramsToJson(fieldPath: Option[String]): Json = {
    val matchPhraseFields =
      Some(fieldPath.foldRight(field)(_ + "." + _) -> value.toJson) ++ boost.map("boost" -> Num(_))
    Obj("match_phrase" -> Obj(matchPhraseFields.toList: _*))
  }
}

sealed trait NestedQuery[S]
    extends ElasticQuery[S]
    with HasIgnoreUnmapped[NestedQuery[S]]
    with HasInnerHits[NestedQuery[S]]
    with HasScoreMode[NestedQuery[S]]

private[elasticsearch] final case class Nested[S](
  path: String,
  query: ElasticQuery[_],
  ignoreUnmapped: Option[Boolean],
  innerHitsField: Option[InnerHits],
  scoreMode: Option[ScoreMode]
) extends NestedQuery[S] { self =>
  def ignoreUnmapped(value: Boolean): NestedQuery[S] =
    self.copy(ignoreUnmapped = Some(value))

  def innerHits(innerHits: InnerHits): NestedQuery[S] =
    self.copy(innerHitsField = Some(innerHits))

  def paramsToJson(fieldPath: Option[String]): Json =
    Obj(
      "nested" -> Obj(
        List(
          Some("path"  -> fieldPath.map(fieldPath => Str(fieldPath + "." + path)).getOrElse(Str(path))),
          Some("query" -> query.paramsToJson(fieldPath.map(_ + "." + path).orElse(Some(path)))),
          scoreMode.map(scoreMode => "score_mode" -> Str(scoreMode.toString.toLowerCase)),
          ignoreUnmapped.map("ignore_unmapped" -> Json.Bool(_)),
          innerHitsField.map(_.toStringJsonPair)
        ).flatten: _*
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

private[elasticsearch] case object Unbounded extends LowerBound with UpperBound {
  def toJson: Option[(String, Json)] = None
}

sealed trait RangeQuery[S, A, LB <: LowerBound, UB <: UpperBound]
    extends ElasticQuery[S]
    with HasBoost[RangeQuery[S, A, LB, UB]]
    with HasFormat[RangeQuery[S, A, LB, UB]] {
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

private[elasticsearch] final case class Range[S, A, LB <: LowerBound, UB <: UpperBound](
  field: String,
  lower: LB,
  upper: UB,
  boost: Option[Double],
  format: Option[String]
) extends RangeQuery[S, A, LB, UB] { self =>
  def boost(value: Double): RangeQuery[S, A, LB, UB] =
    self.copy(boost = Some(value))

  def format(value: String): RangeQuery[S, A, LB, UB] =
    self.copy(format = Some(value))

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

  def paramsToJson(fieldPath: Option[String]): Json =
    Obj(
      "range" -> Obj(
        Chunk(
          Some(fieldPath.foldRight(field)(_ + "." + _) -> Obj(List(lower.toJson, upper.toJson).flatten: _*)),
          boost.map("boost" -> Num(_)),
          format.map("format" -> Str(_))
        ).flatten: _*
      )
    )
}

private[elasticsearch] object Range {
  def empty[S, A](field: String): Range[S, A, Unbounded.type, Unbounded.type] =
    Range[S, A, Unbounded.type, Unbounded.type](
      field = field,
      lower = Unbounded,
      upper = Unbounded,
      boost = None,
      format = None
    )
}

sealed trait TermQuery[S] extends ElasticQuery[S] with HasBoost[TermQuery[S]] with HasCaseInsensitive[TermQuery[S]]

private[elasticsearch] final case class Term[S](
  field: String,
  value: String,
  boost: Option[Double],
  caseInsensitive: Option[Boolean]
) extends TermQuery[S] { self =>
  def boost(value: Double): TermQuery[S] =
    self.copy(boost = Some(value))

  def caseInsensitive(value: Boolean): TermQuery[S] =
    self.copy(caseInsensitive = Some(value))

  def paramsToJson(fieldPath: Option[String]): Json = {
    val termFields = Some("value" -> value.toJson) ++ boost.map("boost" -> Num(_)) ++ caseInsensitive.map(
      "case_insensitive" -> Json.Bool(_)
    )
    Obj("term" -> Obj(fieldPath.foldRight(field)(_ + "." + _) -> Obj(termFields.toList: _*)))
  }
}

sealed trait TermsQuery[S] extends ElasticQuery[S] with HasBoost[TermsQuery[S]]

private[elasticsearch] final case class Terms[S](
  field: String,
  values: List[String],
  boost: Option[Double]
) extends TermsQuery[S] { self =>
  def boost(value: Double): TermsQuery[S] =
    self.copy(boost = Some(value))

  def paramsToJson(fieldPath: Option[String]): Json = {
    val termsFields =
      Some(fieldPath.foldRight(field)(_ + "." + _) -> Arr(values.map(Str(_)): _*)) ++ boost.map("boost" -> Num(_))
    Obj("terms" -> Obj(termsFields.toList: _*))
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

  def paramsToJson(fieldPath: Option[String]): Json = {
    val wildcardFields = Some("value" -> value.toJson) ++ boost.map("boost" -> Num(_)) ++ caseInsensitive.map(
      "case_insensitive" -> Json.Bool(_)
    )
    Obj("wildcard" -> Obj(fieldPath.foldRight(field)(_ + "." + _) -> Obj(wildcardFields.toList: _*)))
  }
}
