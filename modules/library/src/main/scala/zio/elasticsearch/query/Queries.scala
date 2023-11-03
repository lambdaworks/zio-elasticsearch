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
import zio.elasticsearch.Field
import zio.elasticsearch.query.options._
import zio.elasticsearch.query.sort.options.HasFormat
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj}
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
        boost.map("boost" -> _.toJson),
        minimumShouldMatch.map("minimum_should_match" -> _.toJson)
      ).collect { case Some(obj) => obj }

    Obj("bool" -> Obj(boolFields))
  }
}

sealed trait ConstantScoreQuery[S] extends ElasticQuery[S] with HasBoost[ConstantScoreQuery[S]]

private[elasticsearch] final case class ConstantScore[S](query: ElasticQuery[S], boost: Option[Double])
    extends ConstantScoreQuery[S] { self =>

  def boost(value: Double): ConstantScoreQuery[S] =
    self.copy(boost = Some(value))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "constant_score" -> (Obj("filter" -> query.toJson(fieldPath)) merge boost.fold(Obj())(b =>
        Obj("boost" -> b.toJson)
      ))
    )
}

sealed trait ExistsQuery[S] extends ElasticQuery[S] with HasBoost[ExistsQuery[S]]

private[elasticsearch] final case class Exists[S](field: String, boost: Option[Double]) extends ExistsQuery[S] { self =>

  def boost(value: Double): ExistsQuery[S] =
    self.copy(boost = Some(value))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "exists" -> (Obj("field" -> fieldPath.foldRight(field)(_ + "." + _).toJson) merge boost.fold(Obj())(b =>
        Obj("boost" -> b.toJson)
      ))
    )
}

sealed trait FunctionScoreQuery[S] extends ElasticQuery[S] with HasBoost[FunctionScoreQuery[S]] {

  /**
   * Sets the `boostMode` parameter for the [[zio.elasticsearch.query.FunctionScoreQuery]]. It defines how computed
   * score is combined with score of the query.
   *
   * @param value
   *   Computed score and score of the query can be combined in following ways:
   *   - [[zio.elasticsearch.query.FunctionScoreBoostMode.Avg]]
   *   - [[zio.elasticsearch.query.FunctionScoreBoostMode.Max]]
   *   - [[zio.elasticsearch.query.FunctionScoreBoostMode.Min]]
   *   - [[zio.elasticsearch.query.FunctionScoreBoostMode.Multiply]]
   *   - [[zio.elasticsearch.query.FunctionScoreBoostMode.Replace]]
   *   - [[zio.elasticsearch.query.FunctionScoreBoostMode.Sum]]
   *
   * @return
   *   an instance of [[zio.elasticsearch.query.FunctionScoreQuery]] enriched with the `boostMode` parameter.
   */
  def boostMode(value: FunctionScoreBoostMode): FunctionScoreQuery[S]

  /**
   * Sets the `maxBoost` parameter for the [[zio.elasticsearch.query.FunctionScoreQuery]]. It restricts the new score
   * not to exceed a certain limit by setting this parameter.
   *
   * @param value
   *   a non-negative real number used for the `maxBoost`
   * @return
   *   an instance of [[zio.elasticsearch.query.FunctionScoreQuery]] enriched with the `maxBoost` parameter.
   */
  def maxBoost(value: Double): FunctionScoreQuery[S]

  /**
   * Sets the `minScore` parameter for the [[zio.elasticsearch.query.FunctionScoreQuery]]. To exclude documents that do
   * not meet a certain score threshold the `minScore` parameter can be set to the desired score threshold.
   *
   * @param value
   *   a non-negative real number used for the `minScore`
   * @return
   *   an instance of [[zio.elasticsearch.query.FunctionScoreQuery]] enriched with the `minScore` parameter.
   */
  def minScore(value: Double): FunctionScoreQuery[S]

  /**
   * Sets the `query` parameter for the [[zio.elasticsearch.query.FunctionScoreQuery]]. Represents a query to be
   * executed in elasticsearch and modified by [[zio.elasticsearch.query.FunctionScore]] parameters.
   *
   * @param value
   *   a [[zio.elasticsearch.query.ElasticQuery]] to be executed
   * @tparam S1
   *   the type of the [[zio.elasticsearch.query.ElasticQuery]] for type shrinking
   * @return
   *   an instance of [[zio.elasticsearch.query.FunctionScoreQuery]] enriched with the `query` parameter.
   */
  def query[S1 <: S](value: ElasticQuery[S1]): FunctionScoreQuery[S1]

  /**
   * Sets the `scoreMode` parameter for the [[zio.elasticsearch.query.FunctionScoreQuery]]. The `scoreMode` parameter
   * specifies how the computed scores are combined.
   *
   * @param value
   *   a value that we want to set `scoreMode` to, possible values are:
   *   - [[zio.elasticsearch.query.FunctionScoreScoreMode.Avg]]
   *   - [[zio.elasticsearch.query.FunctionScoreScoreMode.First]]
   *   - [[zio.elasticsearch.query.FunctionScoreScoreMode.Max]]
   *   - [[zio.elasticsearch.query.FunctionScoreScoreMode.Min]]
   *   - [[zio.elasticsearch.query.FunctionScoreScoreMode.Multiply]]
   *   - [[zio.elasticsearch.query.FunctionScoreScoreMode.None]]
   *   - [[zio.elasticsearch.query.FunctionScoreScoreMode.Sum]]
   * @return
   *   an instance of [[zio.elasticsearch.query.FunctionScoreQuery]] enriched with the `scoreMode` parameter.
   */
  def scoreMode(value: FunctionScoreScoreMode): FunctionScoreQuery[S]

  /**
   * Adds one or multiple [[zio.elasticsearch.query.FunctionScoreFunction]] to existing
   * [[zio.elasticsearch.query.FunctionScore]] query.
   *
   * @param functions
   *   multiple [[zio.elasticsearch.query.FunctionScoreFunction]] to be added to query
   * @tparam S1
   *   the type of the [[zio.elasticsearch.query.FunctionScoreFunction]] for type shrinking
   * @return
   *   an instance of [[zio.elasticsearch.query.FunctionScoreQuery]] enriched with the `functionScoreFunctions`
   *   parameter.
   */
  def withFunctions[S1 <: S](functions: FunctionScoreFunction[S1]*): FunctionScoreQuery[S1]
}

private[elasticsearch] final case class FunctionScore[S](
  functionScoreFunctions: Chunk[FunctionScoreFunction[S]],
  boost: Option[Double],
  boostMode: Option[FunctionScoreBoostMode],
  maxBoost: Option[Double],
  minScore: Option[Double],
  query: Option[ElasticQuery[S]],
  scoreMode: Option[FunctionScoreScoreMode]
) extends FunctionScoreQuery[S] { self =>

  def boost(value: Double): FunctionScoreQuery[S] =
    self.copy(boost = Some(value))

  def boostMode(value: FunctionScoreBoostMode): FunctionScoreQuery[S] =
    self.copy(boostMode = Some(value))

  def maxBoost(value: Double): FunctionScoreQuery[S] =
    self.copy(maxBoost = Some(value))

  def minScore(value: Double): FunctionScoreQuery[S] =
    self.copy(minScore = Some(value))

  def query[S1 <: S](value: ElasticQuery[S1]): FunctionScoreQuery[S1] =
    self.copy(query = Some(value))

  def scoreMode(value: FunctionScoreScoreMode): FunctionScoreQuery[S] =
    self.copy(scoreMode = Some(value))

  def withFunctions[S1 <: S](functions: FunctionScoreFunction[S1]*): FunctionScoreQuery[S1] =
    self.copy(functionScoreFunctions = functionScoreFunctions ++ functions)

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "function_score" -> Obj(
        Chunk(
          Some("functions" -> Arr(functionScoreFunctions.map(_.toJson))),
          boost.map("boost" -> _.toJson),
          boostMode.map("boost_mode" -> _.toString.toLowerCase.toJson),
          maxBoost.map("max_boost" -> _.toJson),
          minScore.map("min_score" -> _.toJson),
          query.map("query" -> _.toJson(None)),
          scoreMode.map("score_mode" -> _.toString.toLowerCase.toJson)
        ).flatten
      )
    )
}

sealed trait FuzzyQuery[S] extends ElasticQuery[S] {

  /**
   * Sets the `fuzziness` parameter for this [[zio.elasticsearch.query.ElasticQuery]]. The `fuzziness` value refers to
   * the ability to find results that are similar to, but not exactly the same as, the search term or query.
   *
   * @param value
   *   the text value to represent the 'fuzziness' field
   * @return
   *   an instance of the [[zio.elasticsearch.query.ElasticQuery]] enriched with the `fuzziness` parameter.
   */
  def fuzziness(value: String): FuzzyQuery[S]

  /**
   * Sets the `maxExpansions` parameter for this [[zio.elasticsearch.query.ElasticQuery]]. The `maxExpansions` value
   * defines the maximum number of terms the fuzzy query will match before halting the search.
   *
   * @param value
   *   the positive whole number value for `maxExpansions` parameter
   * @return
   *   an instance of the [[zio.elasticsearch.query.ElasticQuery]] enriched with the `maxExpansions` parameter.
   */
  def maxExpansions(value: Int): FuzzyQuery[S]

  /**
   * Sets the `prefixLength` parameter for this [[zio.elasticsearch.query.ElasticQuery]]. The `prefixLength` value
   * refers to the number of beginning characters left unchanged when creating expansions.
   *
   * @param value
   *   the positive whole number value for `prefixLength` parameter
   * @return
   *   an instance of the [[zio.elasticsearch.query.ElasticQuery]] enriched with the `prefixLength` parameter.
   */
  def prefixLength(value: Int): FuzzyQuery[S]
}

private[elasticsearch] final case class Fuzzy[S](
  field: String,
  value: String,
  fuzziness: Option[String],
  maxExpansions: Option[Int],
  prefixLength: Option[Int]
) extends FuzzyQuery[S] { self =>

  def fuzziness(value: String): FuzzyQuery[S] =
    self.copy(fuzziness = Some(value))

  def maxExpansions(value: Int): FuzzyQuery[S] =
    self.copy(maxExpansions = Some(value))

  def prefixLength(value: Int): FuzzyQuery[S] =
    self.copy(prefixLength = Some(value))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json = {
    val fuzzyFields = Some("value" -> value.toJson) ++ fuzziness.map("fuzziness" -> _.toJson) ++ maxExpansions.map(
      "max_expansions" -> _.toJson
    ) ++ prefixLength.map("prefix_length" -> _.toJson)
    Obj("fuzzy" -> Obj(fieldPath.foldRight(field)(_ + "." + _) -> Obj(Chunk.fromIterable(fuzzyFields))))
  }
}

sealed trait GeoDistanceQuery[S] extends ElasticQuery[S] {

  /**
   * Sets the `distanceType` parameter for the [[zio.elasticsearch.query.GeoDistanceQuery]]. Defines how to compute the
   * distance.
   *
   * @param value
   *   defines how to compute the distance
   *   - [[zio.elasticsearch.query.DistanceType.Arc]]: Default algorithm
   *   - [[zio.elasticsearch.query.DistanceType.Plane]]: Faster, but inaccurate on long distances and close to the poles
   * @return
   *   an instance of [[zio.elasticsearch.query.GeoDistanceQuery]] enriched with the `distanceType` parameter.
   */
  def distanceType(value: DistanceType): GeoDistanceQuery[S]

  /**
   * Sets the `queryName` parameter for the [[zio.elasticsearch.query.GeoDistanceQuery]]. Represents the optional name
   * field to identify the query
   *
   * @param value
   *   the [[String]] value to represent the name field
   * @return
   *   an instance of [[zio.elasticsearch.query.GeoDistanceQuery]] enriched with the `queryName` parameter.
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
   *   an instance of [[zio.elasticsearch.query.GeoDistanceQuery]] enriched with the `validationMethod` parameter.
   */
  def validationMethod(value: ValidationMethod): GeoDistanceQuery[S]
}

private[elasticsearch] final case class GeoDistance[S](
  field: String,
  point: String,
  distance: Distance,
  distanceType: Option[DistanceType],
  queryName: Option[String],
  validationMethod: Option[ValidationMethod]
) extends GeoDistanceQuery[S] { self =>

  def distanceType(value: DistanceType): GeoDistanceQuery[S] = self.copy(distanceType = Some(value))

  def name(value: String): GeoDistanceQuery[S] = self.copy(queryName = Some(value))

  def validationMethod(value: ValidationMethod): GeoDistanceQuery[S] = self.copy(validationMethod = Some(value))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "geo_distance" -> Obj(
        Chunk(
          Some(field      -> point.toJson),
          Some("distance" -> distance.toString.toJson),
          distanceType.map("distance_type" -> _.toString.toJson),
          queryName.map("_name" -> _.toJson),
          validationMethod.map("validation_method" -> _.toString.toJson)
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
   *   the [[scala.Int]] value for `maxChildren` parameter
   * @return
   *   an instance of [[HasChildQuery]] enriched with the `maxChildren` parameter.
   */
  def maxChildren(value: Int): HasChildQuery[S]

  /**
   * Sets the `minChildren` parameter for the [[HasChildQuery]]. Indicates minimum number of child documents that match
   * the query required to match the query for a returned parent document. If the parent document does not meet this
   * limit, it is excluded from the search results.
   *
   * @param value
   *   the whole number value for `minChildren` parameter
   * @return
   *   an instance of [[HasChildQuery]] enriched with the `minChildren` parameter.
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
          Some("type"  -> childType.toJson),
          Some("query" -> query.toJson(None)),
          ignoreUnmapped.map("ignore_unmapped" -> _.toJson),
          innerHitsField.map(_.toStringJsonPair(None)),
          maxChildren.map("max_children" -> _.toJson),
          minChildren.map("min_children" -> _.toJson),
          scoreMode.map("score_mode" -> _.toString.toLowerCase.toJson)
        ).flatten
      )
    )
}

sealed trait HasParentQuery[S]
    extends ElasticQuery[S]
    with HasBoost[HasParentQuery[S]]
    with HasIgnoreUnmapped[HasParentQuery[S]]
    with HasInnerHits[HasParentQuery[S]] {

  /**
   * Sets the `score` parameter parameter for the [[HasParentQuery]]. Indicates whether the relevance score of a
   * matching parent document is aggregated into its child documents. Defaults to false.
   *
   * @param value
   *   the [[scala.Boolean]] value for `score` parameter
   * @return
   *   an instance of [[HasParentQuery]] enriched with the `score` parameter.
   */
  def withScore(value: Boolean): HasParentQuery[S]

  /**
   * Sets the `score` parameter to `false` for this [[HasParentQuery]]. Same as [[withScore]](false).
   *
   * @return
   *   an instance of [[HasParentQuery]] with the `score` value set to `false`.
   * @see
   *   #withScore
   */
  final def withScoreFalse: HasParentQuery[S] = withScore(false)

  /**
   * Sets the `score` parameter to `true` for this [[HasParentQuery]]. Same as [[withScore]](true).
   *
   * @return
   *   an instance of [[HasParentQuery]] with the `score` value set to `true`.
   * @see
   *   #withScore
   */
  final def withScoreTrue: HasParentQuery[S] = withScore(true)

}

private[elasticsearch] final case class HasParent[S](
  parentType: String,
  query: ElasticQuery[S],
  boost: Option[Double],
  ignoreUnmapped: Option[Boolean],
  innerHitsField: Option[InnerHits],
  score: Option[Boolean]
) extends HasParentQuery[S] { self =>

  def boost(value: Double): HasParentQuery[S] =
    self.copy(boost = Some(value))

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
          Some("parent_type" -> parentType.toJson),
          Some("query"       -> query.toJson(None)),
          boost.map("boost" -> _.toJson),
          ignoreUnmapped.map("ignore_unmapped" -> _.toJson),
          score.map("score" -> _.toJson),
          innerHitsField.map(_.toStringJsonPair(None))
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
    Obj("match_all" -> Obj(Chunk.fromIterable(boost.map("boost" -> _.toJson))))
}

sealed trait MatchBooleanPrefixQuery[S] extends ElasticQuery[S] with HasMinimumShouldMatch[MatchBooleanPrefixQuery[S]]

private[elasticsearch] final case class MatchBooleanPrefix[S, A: ElasticPrimitive](
  field: String,
  value: A,
  minimumShouldMatch: Option[Int]
) extends MatchBooleanPrefixQuery[S] { self =>

  def minimumShouldMatch(value: Int): MatchBooleanPrefixQuery[S] =
    self.copy(minimumShouldMatch = Some(value))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "match_bool_prefix" -> Obj(
        fieldPath.foldRight(field)(_ + "." + _) -> minimumShouldMatch.fold(value.toJson)(m =>
          Obj("query" -> value.toJson) merge Obj("minimum_should_match" -> m.toJson)
        )
      )
    )
}

sealed trait MatchPhraseQuery[S] extends ElasticQuery[S] with HasBoost[MatchPhraseQuery[S]]

private[elasticsearch] final case class MatchPhrase[S](field: String, value: String, boost: Option[Double])
    extends MatchPhraseQuery[S] { self =>

  def boost(value: Double): MatchPhraseQuery[S] =
    self.copy(boost = Some(value))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "match_phrase" -> Obj(
        fieldPath.foldRight(field)(_ + "." + _) -> boost.fold(value.toJson)(b =>
          Obj("query" -> value.toJson) merge Obj("boost" -> b.toJson)
        )
      )
    )
}

sealed trait MatchPhrasePrefixQuery[S] extends ElasticQuery[S]

private[elasticsearch] final case class MatchPhrasePrefix[S](field: String, value: String)
    extends MatchPhrasePrefixQuery[S] {

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj("match_phrase_prefix" -> Obj(fieldPath.foldRight(field)(_ + "." + _) -> Obj("query" -> value.toJson)))
}

sealed trait MultiMatchQuery[S]
    extends ElasticQuery[S]
    with HasBoost[MultiMatchQuery[S]]
    with HasMinimumShouldMatch[MultiMatchQuery[S]] {

  /**
   * Sets the `fields` parameter for this [[zio.elasticsearch.query.ElasticQuery]]. The `fields` parameter is array of
   * fields that will be searched.
   *
   * @param fields
   *   a array of fields to set `fields` parameter to
   * @return
   *   an instance of the [[zio.elasticsearch.query.ElasticQuery]] enriched with the `fields` parameter.
   */
  def fields(field: String, fields: String*): MultiMatchQuery[S]

  /**
   * Sets the type-safe `fields` parameter for this [[zio.elasticsearch.query.ElasticQuery]]. The `fields` parameter is
   * array of type-safe fields that will be searched.
   *
   * @param fields
   *   a array of type-safe fields to set `fields` parameter to
   * @return
   *   an instance of the [[zio.elasticsearch.query.ElasticQuery]] enriched with the type-safe `fields` parameter.
   */
  def fields[S1 <: S: Schema](field: Field[S1, String], fields: Field[S1, String]*): MultiMatchQuery[S1]

  /**
   * Sets the `type` parameter for this [[zio.elasticsearch.query.ElasticQuery]]. The `type` parameter decides the way
   * [[zio.elasticsearch.query.ElasticQuery]] is executed internally.
   *
   * @param matchingType
   *   the [[zio.elasticsearch.query.MultiMatchType]] value of 'type' parameter, possible values are:
   *   - [[zio.elasticsearch.query.MultiMatchType.BestFields]]: runs a [[zio.elasticsearch.query.MatchQuery]] on each
   *     field and uses the score of the single best matching field
   *   - [[zio.elasticsearch.query.MultiMatchType.BoolPrefix]]: runs a
   *     [[zio.elasticsearch.query.MatchBooleanPrefixQuery]] on each field and combines the score from each field
   *   - [[zio.elasticsearch.query.MultiMatchType.CrossFields]]: looks for each word in any field
   *   - [[zio.elasticsearch.query.MultiMatchType.MostFields]]: runs a [[zio.elasticsearch.query.MatchQuery]] on each
   *     field and combines the score from each field
   *   - [[zio.elasticsearch.query.MultiMatchType.Phrase]]: runs a [[zio.elasticsearch.query.MatchPhraseQuery]] on each
   *     field and uses the score of the single best matching field
   *   - [[zio.elasticsearch.query.MultiMatchType.PhrasePrefix]]: runs a
   *     [[zio.elasticsearch.query.MatchPhrasePrefixQuery]] on each field and uses the score of the single best matching
   *     field
   * @return
   *   an instance of the [[zio.elasticsearch.query.ElasticQuery]] enriched with the `type` parameter.
   */
  def matchingType(matchingType: MultiMatchType): MultiMatchQuery[S]
}

private[elasticsearch] final case class MultiMatch[S](
  fields: Chunk[String],
  value: String,
  boost: Option[Double],
  matchingType: Option[MultiMatchType],
  minimumShouldMatch: Option[Int]
) extends MultiMatchQuery[S] { self =>

  def boost(boost: Double): MultiMatchQuery[S] =
    self.copy(boost = Some(boost))

  def fields(field: String, fields: String*): MultiMatchQuery[S] =
    self.copy(fields = Chunk.fromIterable(field +: fields))

  def fields[S1 <: S: Schema](field: Field[S1, String], fields: Field[S1, String]*): MultiMatchQuery[S1] =
    self.copy(fields = Chunk.fromIterable((field +: fields).map(_.toString)))

  def matchingType(matchingType: MultiMatchType): MultiMatchQuery[S] =
    self.copy(matchingType = Some(matchingType))

  def minimumShouldMatch(minimumShouldMatch: Int): MultiMatchQuery[S] =
    self.copy(minimumShouldMatch = Some(minimumShouldMatch))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json = {
    val multiMatchFields =
      matchingType.map("type" -> _.toString.toJson) ++ (if (fields.nonEmpty) Some("fields" -> Arr(fields.map(_.toJson)))
                                                        else None) ++
        boost.map("boost" -> _.toJson) ++ minimumShouldMatch.map("minimum_should_match" -> _.toJson)

    Obj(
      "multi_match" -> (Obj("query" -> fieldPath.foldRight(value)(_ + "." + _).toJson) merge Obj(
        Chunk.fromIterable(multiMatchFields)
      ))
    )
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

  def scoreMode(scoreMode: ScoreMode): NestedQuery[S] =
    self.copy(scoreMode = Some(scoreMode))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj(
      "nested" -> Obj(
        Chunk(
          Some("path"  -> fieldPath.map(_ + "." + path).map(_.toJson).getOrElse(path.toJson)),
          Some("query" -> query.toJson(fieldPath.map(_ + "." + path).orElse(Some(path)))),
          scoreMode.map("score_mode" -> _.toString.toLowerCase.toJson),
          ignoreUnmapped.map("ignore_unmapped" -> _.toJson),
          innerHitsField.map(_.toStringJsonPair(fieldPath.map(_ + "." + path).orElse(Some(path))))
        ).flatten
      )
    )
}

sealed trait LowerBound {

  private[elasticsearch] def toJson: Option[(String, Json)]
}

private[elasticsearch] final case class GreaterThan[A: ElasticPrimitive](value: A) extends LowerBound {

  private[elasticsearch] def toJson: Option[(String, Json)] =
    Some("gt" -> value.toJson)
}

private[elasticsearch] final case class GreaterThanOrEqualTo[A: ElasticPrimitive](value: A) extends LowerBound {

  private[elasticsearch] def toJson: Option[(String, Json)] =
    Some("gte" -> value.toJson)
}

sealed trait UpperBound {

  private[elasticsearch] def toJson: Option[(String, Json)]
}

private[elasticsearch] final case class LessThan[A: ElasticPrimitive](value: A) extends UpperBound {

  private[elasticsearch] def toJson: Option[(String, Json)] =
    Some("lt" -> value.toJson)
}

private[elasticsearch] final case class LessThanOrEqualTo[A: ElasticPrimitive](value: A) extends UpperBound {

  private[elasticsearch] def toJson: Option[(String, Json)] =
    Some("lte" -> value.toJson)
}

private[elasticsearch] case object Unbounded extends LowerBound with UpperBound {

  private[elasticsearch] def toJson: Option[(String, Json)] =
    None
}

sealed trait PrefixQuery[S] extends ElasticQuery[S] with HasCaseInsensitive[PrefixQuery[S]]

private[elasticsearch] final case class Prefix[S](
  field: String,
  value: String,
  caseInsensitive: Option[Boolean]
) extends PrefixQuery[S] { self =>

  def caseInsensitive(value: Boolean): PrefixQuery[S] =
    self.copy(caseInsensitive = Some(value))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json = {
    val prefixFields = Some("value" -> value.toJson) ++ caseInsensitive.map(
      "case_insensitive" -> _.toJson
    )
    Obj("prefix" -> Obj(fieldPath.foldRight(field)(_ + "." + _) -> Obj(Chunk.fromIterable(prefixFields))))
  }
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
   *   an instance of [[zio.elasticsearch.query.RangeQuery]] enriched with the greater-than bound set.
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
   *   an instance of [[zio.elasticsearch.query.RangeQuery]] enriched with the greater-than-or-equal-to bound set.
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
   *   an instance of [[zio.elasticsearch.query.RangeQuery]] enriched with the less-than bound set.
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
   *   an instance of [[zio.elasticsearch.query.RangeQuery]] enriched with the less-than-or-equal-to bound set.
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
            boost.map("boost" -> _.toJson),
            format.map("format" -> _.toJson)
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

sealed trait RegexpQuery[S] extends ElasticQuery[S] with HasCaseInsensitive[RegexpQuery[S]]

private[elasticsearch] final case class Regexp[S](
  field: String,
  value: String,
  caseInsensitive: Option[Boolean]
) extends RegexpQuery[S] { self =>

  def caseInsensitive(value: Boolean): RegexpQuery[S] =
    self.copy(caseInsensitive = Some(value))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json = {
    val regexpFields = Some("value" -> value.toJson) ++ caseInsensitive.map("case_insensitive" -> _.toJson)
    Obj("regexp" -> Obj(fieldPath.foldRight(field)(_ + "." + _) -> Obj(Chunk.fromIterable(regexpFields))))
  }
}

sealed trait ScriptQuery extends ElasticQuery[Any] with HasBoost[ScriptQuery]

private[elasticsearch] final case class Script(script: zio.elasticsearch.script.Script, boost: Option[Double])
    extends ScriptQuery { self =>

  def boost(value: Double): ScriptQuery =
    self.copy(boost = Some(value))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj("script" -> Obj(("script" -> script.toJson) +: Chunk.fromIterable(boost.map("boost" -> _.toJson))))
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

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json = {
    val termFields = Some("value" -> value.toJson) ++ boost.map("boost" -> _.toJson) ++ caseInsensitive.map(
      "case_insensitive" -> _.toJson
    )
    Obj("term" -> Obj(fieldPath.foldRight(field)(_ + "." + _) -> Obj(Chunk.fromIterable(termFields))))
  }
}

sealed trait TermsQuery[S] extends ElasticQuery[S] with HasBoost[TermsQuery[S]]

private[elasticsearch] final case class Terms[S, A: ElasticPrimitive](
  field: String,
  values: Chunk[A],
  boost: Option[Double]
) extends TermsQuery[S] { self =>

  def boost(value: Double): TermsQuery[S] =
    self.copy(boost = Some(value))

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json = {
    val termsFields =
      Some(fieldPath.foldRight(field)(_ + "." + _) -> Arr(values.map(_.toJson))) ++ boost.map("boost" -> _.toJson)
    Obj("terms" -> Obj(Chunk.fromIterable(termsFields)))
  }
}

sealed trait TermsSetQuery[S] extends ElasticQuery[S] with HasMinimumShouldMatch[TermsSetQuery[S]]

private[elasticsearch] final case class TermsSet[S, A: ElasticPrimitive](
  field: String,
  values: Chunk[A],
  minimumShouldMatch: Option[Int]
) extends TermsSetQuery[S] { self =>

  def minimumShouldMatch(value: Int): TermsSetQuery[S] =
    self.copy(minimumShouldMatch = Some(value))

  println(field)
  println(values)
  println(minimumShouldMatch)
  private[elasticsearch] def toJson(fieldPath: Option[String]): Json = {
    val termsSetFields =
      Some("terms" -> Arr(values.map(_.toJson))) ++ minimumShouldMatch.map("minimum_should_match_field" -> _.toJson)
    Obj("terms_set" -> Obj(fieldPath.foldRight(field)(_ + "." + _) -> Obj(Chunk.fromIterable(termsSetFields))))
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
    val wildcardFields = Some("value" -> value.toJson) ++ boost.map("boost" -> _.toJson) ++ caseInsensitive.map(
      "case_insensitive" -> _.toJson
    )
    Obj("wildcard" -> Obj(fieldPath.foldRight(field)(_ + "." + _) -> Obj(Chunk.fromIterable(wildcardFields))))
  }
}

sealed trait IdsQuery[S] extends ElasticQuery[S]

private[elasticsearch] final case class Ids[S](values: Chunk[String]) extends IdsQuery[S] { self =>

  private[elasticsearch] def toJson(fieldPath: Option[String]): Json =
    Obj("ids" -> Obj("values" -> Arr(values.map(_.toJson))))
}
