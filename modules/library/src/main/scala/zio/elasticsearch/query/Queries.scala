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
  private[elasticsearch] def toJson(fieldPath: Option[String]): Json
}

sealed trait BoolQuery[S] extends ElasticQuery[S] with HasBoost[BoolQuery[S]] with HasMinimumShouldMatch[BoolQuery[S]] {

  /**
   * Adds specified `filter` queries to the [[zio.elasticsearch.query.BoolQuery]]. These queries must appear in matching
   * documents. Unlike `must` the score of the query will be ignored.
   *
   * @param queries
   *   the `filter` queries to be added
   * @tparam S1
   *   the type of the sub-queries, for which an implicit [[zio.schema.Schema]] is required
   * @return
   *   an instance of the [[zio.elasticsearch.query.BoolQuery]] with `filter` queries added.
   */
  def filter[S1 <: S: Schema](queries: ElasticQuery[S1]*): BoolQuery[S1]

  /**
   * Adds specified `filter` queries to the [[zio.elasticsearch.query.BoolQuery]]. These queries must appear in matching
   * documents. Unlike `must` the score of the query will be ignored.
   *
   * @param queries
   *   the `filter` queries to be added
   * @return
   *   an instance of the [[zio.elasticsearch.query.BoolQuery]] with `filter` queries added.
   */
  def filter(queries: ElasticQuery[Any]*): BoolQuery[S]

  /**
   * Adds specified `must` queries to the [[zio.elasticsearch.query.BoolQuery]]. These queries must appear in matching
   * documents and will contribute to the score.
   *
   * @param queries
   *   the `must` queries to be added
   * @tparam S1
   *   the type of the sub-queries, for which an implicit [[zio.schema.Schema]] is required
   * @return
   *   an instance of the [[zio.elasticsearch.query.BoolQuery]] with `must` queries added.
   */
  def must[S1 <: S: Schema](queries: ElasticQuery[S1]*): BoolQuery[S1]

  /**
   * Adds specified `must` queries to the [[zio.elasticsearch.query.BoolQuery]]. These queries must appear in matching
   * documents and will contribute to the score.
   *
   * @param queries
   *   the `must` queries to be added
   * @return
   *   an instance of the [[zio.elasticsearch.query.BoolQuery]] with `must` queries added.
   */
  def must(queries: ElasticQuery[Any]*): BoolQuery[S]

  /**
   * Adds specified `must not` queries to the [[zio.elasticsearch.query.BoolQuery]]. These queries must not appear in
   * matching documents.
   *
   * @param queries
   *   the `must not` queries to be added
   * @tparam S1
   *   the type of the sub-queries, for which an implicit [[zio.schema.Schema]] is required
   * @return
   *   an instance of the [[zio.elasticsearch.query.BoolQuery]] with `must not` queries added.
   */
  def mustNot[S1 <: S: Schema](queries: ElasticQuery[S1]*): BoolQuery[S1]

  /**
   * Adds specified `must not` queries to the [[zio.elasticsearch.query.BoolQuery]]. These queries must not appear in
   * matching documents.
   *
   * @param queries
   *   the `must not` queries to be added
   * @return
   *   an instance of the [[zio.elasticsearch.query.BoolQuery]] with `must not` queries added.
   */
  def mustNot(queries: ElasticQuery[Any]*): BoolQuery[S]

  /**
   * Adds specified `should` queries to the [[zio.elasticsearch.query.BoolQuery]]. These queries should appear in
   * matching documents.
   *
   * @param queries
   *   the `should` queries to be added
   * @tparam S1
   *   the type of the sub-queries, for which an implicit [[zio.schema.Schema]] is required
   * @return
   *   an instance of the [[zio.elasticsearch.query.BoolQuery]] with `should` queries added.
   */
  def should[S1 <: S: Schema](queries: ElasticQuery[S1]*): BoolQuery[S1]

  /**
   * Adds specified `should` queries to the [[zio.elasticsearch.query.BoolQuery]]. These queries should appear in
   * matching documents.
   *
   * @param queries
   *   the `should` queries to be added
   * @return
   *   an instance of the [[zio.elasticsearch.query.BoolQuery]] with `should` queries added.
   */
  def should(queries: ElasticQuery[Any]*): BoolQuery[S]
}

private[elasticsearch] final case class Bool[S](
  filter: Chunk[ElasticQuery[S]],
  must: Chunk[ElasticQuery[S]],
  mustNot: Chunk[ElasticQuery[S]],
  should: Chunk[ElasticQuery[S]],
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

  def should[S1 <: S: Schema](queries: ElasticQuery[S1]*): BoolQuery[S1] =
    self.copy(should = should ++ queries)

  def should(queries: ElasticQuery[Any]*): BoolQuery[S] =
    self.copy(should = should ++ queries)

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json = {
    val boolFields =
      Chunk(
        if (filter.nonEmpty) Some("filter" -> Arr(filter.map(_.toJson(fieldPath)))) else None,
        if (must.nonEmpty) Some("must" -> Arr(must.map(_.toJson(fieldPath)))) else None,
        if (mustNot.nonEmpty) Some("must_not" -> Arr(mustNot.map(_.toJson(fieldPath)))) else None,
        if (should.nonEmpty) Some("should" -> Arr(should.map(_.toJson(fieldPath)))) else None,
        boost.map("boost" -> Num(_)),
        minimumShouldMatch.map("minimum_should_match" -> Num(_))
      ).collect { case Some(obj) => obj }

    Obj("bool" -> Obj(boolFields))
  }
}

sealed trait ExistsQuery[S] extends ElasticQuery[S]

private[elasticsearch] final case class Exists[S](field: String) extends ExistsQuery[S] {
  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj("exists" -> Obj("field" -> fieldPath.foldRight(field)(_ + "." + _).toJson))
}

sealed trait GeoDistanceQuery[S] extends ElasticQuery[S] {

  /**
   * Sets the `distance` parameter for the [[zio.elasticsearch.query.GeoDistanceQuery]]. `Distance` represents the
   * radius of the circle centred on the specified location. Points which fall into this circle are considered to be
   * matches. The distance can be specified in various units. See [[zio.elasticsearch.query.DistanceUnit]].
   *
   * @param value
   *   a non-negative real number used for distance
   * @param unit
   *   the [[zio.elasticsearch.query.DistanceUnit]] in which we want to represent the distance
   * @return
   *   a new instance of the [[zio.elasticsearch.query.GeoDistanceQuery]] enriched with the `distance` parameter.
   */
  def distance(value: Double, unit: DistanceUnit): GeoDistanceQuery[S]

  /**
   * Sets the `distanceType` parameter for the [[zio.elasticsearch.query.GeoDistanceQuery]]. Defines how to compute the
   * distance.
   *
   * @param value
   *   defines how to compute the distance
   *   - [[zio.elasticsearch.query.DistanceType.Arc]]: Default algorithm
   *   - [[zio.elasticsearch.query.DistanceType.Plane]]: Faster, but inaccurate on long distances and close to the poles
   * @return
   *   a new instance of the [[zio.elasticsearch.query.GeoDistanceQuery]] enriched with the `distanceType` parameter.
   */
  def distanceType(value: DistanceType): GeoDistanceQuery[S]

  /**
   * Sets the `queryName` parameter for the [[zio.elasticsearch.query.GeoDistanceQuery]]. Represents the optional name
   * field to identify the query
   *
   * @param value
   *   the [[String]] value to represent the name field
   * @return
   *   a new instance of the [[zio.elasticsearch.query.GeoDistanceQuery]] enriched with the `queryName` parameter.
   */
  def name(value: String): GeoDistanceQuery[S]

  /**
   * Sets the `validationMethod` parameter for the [[zio.elasticsearch.query.GeoDistanceQuery]]. Defines handling of
   * incorrect coordinates.
   *
   * @param value
   *   defines how to handle invalid latitude nad longitude:
   *   - [[zio.elasticsearch.query.ValidationMethod.Strict]]: Default method
   *   - [[zio.elasticsearch.query.ValidationMethod.IgnoreMalformed]]: Accepts geo points with invalid latitude or
   *     longitude
   *   - [[zio.elasticsearch.query.ValidationMethod.Coerce]]: Additionally try and infer correct coordinates
   * @return
   *   a new instance of the [[zio.elasticsearch.query.GeoDistanceQuery]] enriched with the `validationMethod`
   *   parameter.
   */
  def validationMethod(value: ValidationMethod): GeoDistanceQuery[S]
}

private[elasticsearch] final case class GeoDistance[S](
  field: String,
  point: String,
  distance: Option[Distance],
  distanceType: Option[DistanceType],
  queryName: Option[String],
  validationMethod: Option[ValidationMethod]
) extends GeoDistanceQuery[S] { self =>

  def distance(value: Double, unit: DistanceUnit): GeoDistanceQuery[S] =
    self.copy(distance = Some(Distance(value, unit)))

  def distanceType(value: DistanceType): GeoDistanceQuery[S] = self.copy(distanceType = Some(value))

  def name(value: String): GeoDistanceQuery[S] = self.copy(queryName = Some(value))

  def validationMethod(value: ValidationMethod): GeoDistanceQuery[S] = self.copy(validationMethod = Some(value))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "geo_distance" -> Obj(
        Chunk(
          Some(field -> Str(point)),
          distance.map(d => "distance" -> Str(d.toString)),
          distanceType.map(dt => "distance_type" -> Str(dt.toString)),
          queryName.map(qn => "_name" -> Str(qn)),
          validationMethod.map(vm => "validation_method" -> Str(vm.toString))
        ).flatten: _*
      )
    )

}

sealed trait HasChildQuery[S]
    extends ElasticQuery[S]
    with HasIgnoreUnmapped[HasChildQuery[S]]
    with HasInnerHits[HasChildQuery[S]]
    with HasScoreMode[HasChildQuery[S]] {

  /**
   * Sets the `maxChildren` parameter for the [[HasChildQuery]]. Indicates maximum number of child documents that match
   * the query allowed for a returned parent document. If the parent document exceeds this limit, it is excluded from
   * the search results.
   *
   * @param value
   *   the [[scala.Int]] value for `score` parameter
   * @return
   *   a new instance of the [[HasChildQuery]] with the `score` value set.
   */
  def maxChildren(value: Int): HasChildQuery[S]

  /**
   * Sets the `minChildren` parameter for the [[HasChildQuery]]. Indicates minimum number of child documents that match
   * the query required to match the query for a returned parent document. If the parent document does not meet this
   * limit, it is excluded from the search results.
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
  ignoreUnmapped: Option[Boolean],
  innerHitsField: Option[InnerHits],
  maxChildren: Option[Int],
  minChildren: Option[Int],
  scoreMode: Option[ScoreMode]
) extends HasChildQuery[S] { self =>

  def ignoreUnmapped(value: Boolean): HasChildQuery[S] =
    self.copy(ignoreUnmapped = Some(value))

  def innerHits(innerHits: InnerHits): HasChildQuery[S] =
    self.copy(innerHitsField = Some(innerHits))

  def maxChildren(value: Int): HasChildQuery[S] =
    self.copy(maxChildren = Some(value))

  def minChildren(value: Int): HasChildQuery[S] =
    self.copy(minChildren = Some(value))

  def scoreMode(value: ScoreMode): HasChildQuery[S] =
    self.copy(scoreMode = Some(value))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "has_child" -> Obj(
        Chunk(
          Some("type"  -> Str(childType)),
          Some("query" -> query.toJson(None)),
          ignoreUnmapped.map("ignore_unmapped" -> Json.Bool(_)),
          innerHitsField.map(_.toStringJsonPair),
          maxChildren.map("max_children" -> Json.Num(_)),
          minChildren.map("min_children" -> Json.Num(_)),
          scoreMode.map(sm => "score_mode" -> Json.Str(sm.toString.toLowerCase))
        ).flatten
      )
    )
}

sealed trait HasParentQuery[S]
    extends ElasticQuery[S]
    with HasIgnoreUnmapped[HasParentQuery[S]]
    with HasInnerHits[HasParentQuery[S]] {

  /**
   * Sets the `score` parameter parameter for the [[HasParentQuery]]. Indicates whether the relevance score of a
   * matching parent document is aggregated into its child documents. Defaults to false.
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
  ignoreUnmapped: Option[Boolean],
  innerHitsField: Option[InnerHits],
  score: Option[Boolean]
) extends HasParentQuery[S] {
  self =>

  def ignoreUnmapped(value: Boolean): HasParentQuery[S] =
    self.copy(ignoreUnmapped = Some(value))

  def innerHits(innerHits: InnerHits): HasParentQuery[S] =
    self.copy(innerHitsField = Some(innerHits))

  def withScore(value: Boolean): HasParent[S] =
    self.copy(score = Some(value))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "has_parent" -> Obj(
        Chunk(
          Some("parent_type" -> Str(parentType)),
          Some("query"       -> query.toJson(None)),
          ignoreUnmapped.map("ignore_unmapped" -> Json.Bool(_)),
          score.map("score" -> Json.Bool(_)),
          innerHitsField.map(_.toStringJsonPair)
        ).flatten
      )
    )
}

sealed trait MatchQuery[S] extends ElasticQuery[S]

private[elasticsearch] final case class Match[S, A: ElasticPrimitive](field: String, value: A) extends MatchQuery[S] {
  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj("match" -> Obj(fieldPath.foldRight(field)(_ + "." + _) -> value.toJson))
}

sealed trait MatchAllQuery extends ElasticQuery[Any] with HasBoost[MatchAllQuery]

private[elasticsearch] final case class MatchAll(boost: Option[Double]) extends MatchAllQuery { self =>
  def boost(value: Double): MatchAllQuery =
    self.copy(boost = Some(value))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj("match_all" -> Obj(Chunk.fromIterable(boost.map("boost" -> Num(_)))))
}

sealed trait MatchPhraseQuery[S] extends ElasticQuery[S]

private[elasticsearch] final case class MatchPhrase[S](field: String, value: String) extends MatchPhraseQuery[S] {
  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj("match_phrase" -> Obj(fieldPath.foldRight(field)(_ + "." + _) -> value.toJson))
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

  def scoreMode(scoreMode: ScoreMode): NestedQuery[S] =
    self.copy(scoreMode = Some(scoreMode))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "nested" -> Obj(
        Chunk(
          Some("path"  -> fieldPath.map(fieldPath => Str(fieldPath + "." + path)).getOrElse(Str(path))),
          Some("query" -> query.toJson(fieldPath.map(_ + "." + path).orElse(Some(path)))),
          scoreMode.map(scoreMode => "score_mode" -> Str(scoreMode.toString.toLowerCase)),
          ignoreUnmapped.map("ignore_unmapped" -> Json.Bool(_)),
          innerHitsField.map(_.toStringJsonPair)
        ).flatten
      )
    )
}

sealed trait LowerBound {
  private[elasticsearch] def toJson: Option[(String, Json)]
}

private[elasticsearch] final case class GreaterThan[A: ElasticPrimitive](value: A) extends LowerBound {
  private[elasticsearch] def toJson: Option[(String, Json)] = Some("gt" -> value.toJson)
}

private[elasticsearch] final case class GreaterThanOrEqualTo[A: ElasticPrimitive](value: A) extends LowerBound {
  private[elasticsearch] def toJson: Option[(String, Json)] = Some("gte" -> value.toJson)
}

sealed trait UpperBound {
  private[elasticsearch] def toJson: Option[(String, Json)]
}

private[elasticsearch] final case class LessThan[A: ElasticPrimitive](value: A) extends UpperBound {
  private[elasticsearch] def toJson: Option[(String, Json)] = Some("lt" -> value.toJson)
}

private[elasticsearch] final case class LessThanOrEqualTo[A: ElasticPrimitive](value: A) extends UpperBound {
  private[elasticsearch] def toJson: Option[(String, Json)] = Some("lte" -> value.toJson)
}

private[elasticsearch] case object Unbounded extends LowerBound with UpperBound {
  private[elasticsearch] def toJson: Option[(String, Json)] = None
}

sealed trait RangeQuery[S, A, LB <: LowerBound, UB <: UpperBound]
    extends ElasticQuery[S]
    with HasBoost[RangeQuery[S, A, LB, UB]]
    with HasFormat[RangeQuery[S, A, LB, UB]] {

  /**
   * Sets the greater-than bound for the [[zio.elasticsearch.query.RangeQuery]].
   *
   * @param value
   *   the value for the greater-than bound
   * @tparam B
   *   the type of the value, constrained by the [[zio.elasticsearch.ElasticPrimitive]]
   * @return
   *   a new instance of the [[zio.elasticsearch.query.RangeQuery]] enriched with the greater-than bound set.
   */
  def gt[B <: A: ElasticPrimitive](value: B)(implicit
    @unused ev: LB =:= Unbounded.type
  ): RangeQuery[S, B, GreaterThan[B], UB]

  /**
   * Sets the greater-than-or-equal-to bound for the [[zio.elasticsearch.query.RangeQuery]].
   *
   * @param value
   *   the value for the greater-than-or-equal-to bound
   * @tparam B
   *   the type of the value, constrained by the [[zio.elasticsearch.ElasticPrimitive]]
   * @return
   *   a new instance of the [[zio.elasticsearch.query.RangeQuery]] enriched with the greater-than-or-equal-to bound
   *   set.
   */
  def gte[B <: A: ElasticPrimitive](value: B)(implicit
    @unused ev: LB =:= Unbounded.type
  ): RangeQuery[S, B, GreaterThanOrEqualTo[B], UB]

  /**
   * Sets the less-than bound for the [[zio.elasticsearch.query.RangeQuery]].
   *
   * @param value
   *   the value for the less-than bound
   * @tparam B
   *   the type of the value, constrained by the [[zio.elasticsearch.ElasticPrimitive]]
   * @return
   *   a new instance of the [[zio.elasticsearch.query.RangeQuery]] enriched with the less-than bound set.
   */
  def lt[B <: A: ElasticPrimitive](value: B)(implicit
    @unused ev: UB =:= Unbounded.type
  ): RangeQuery[S, B, LB, LessThan[B]]

  /**
   * Sets the less-than-or-equal-to bound for the [[zio.elasticsearch.query.RangeQuery]].
   *
   * @param value
   *   the value for the less-than-or-equal-to bound
   * @tparam B
   *   the type of the value, constrained by the [[zio.elasticsearch.ElasticPrimitive]]
   * @return
   *   a new instance of the [[zio.elasticsearch.query.RangeQuery]] enriched with the less-than-or-equal-to bound set.
   */
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

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "range" -> Obj(
        fieldPath.foldRight(field)(_ + "." + _) -> Obj(
          Chunk(
            lower.toJson,
            upper.toJson,
            boost.map("boost" -> Num(_)),
            format.map("format" -> Str(_))
          ).flatten
        )
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

sealed trait ScriptQuery extends ElasticQuery[Any] with HasBoost[ScriptQuery]

private[elasticsearch] final case class Script(script: zio.elasticsearch.script.Script, boost: Option[Double])
    extends ScriptQuery { self =>
  def boost(value: Double): ScriptQuery =
    self.copy(boost = Some(value))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj("script" -> Obj(("script" -> script.toJson) +: Chunk.fromIterable(boost.map("boost" -> Num(_)))))
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

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json = {
    val termFields = Some("value" -> value.toJson) ++ boost.map("boost" -> Num(_)) ++ caseInsensitive.map(
      "case_insensitive" -> Json.Bool(_)
    )
    Obj("term" -> Obj(fieldPath.foldRight(field)(_ + "." + _) -> Obj(Chunk.fromIterable(termFields))))
  }
}

sealed trait TermsQuery[S] extends ElasticQuery[S] with HasBoost[TermsQuery[S]]

private[elasticsearch] final case class Terms[S](
  field: String,
  values: Chunk[String],
  boost: Option[Double]
) extends TermsQuery[S] { self =>
  def boost(value: Double): TermsQuery[S] =
    self.copy(boost = Some(value))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json = {
    val termsFields =
      Some(fieldPath.foldRight(field)(_ + "." + _) -> Arr(values.map(Str(_)))) ++ boost.map("boost" -> Num(_))
    Obj("terms" -> Obj(Chunk.fromIterable(termsFields)))
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

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json = {
    val wildcardFields = Some("value" -> value.toJson) ++ boost.map("boost" -> Num(_)) ++ caseInsensitive.map(
      "case_insensitive" -> Json.Bool(_)
    )
    Obj("wildcard" -> Obj(fieldPath.foldRight(field)(_ + "." + _) -> Obj(Chunk.fromIterable(wildcardFields))))
  }
}
