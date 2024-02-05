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

package zio

import zio.elasticsearch.result._
import zio.prelude.Newtype
import zio.schema.Schema

package object elasticsearch extends IndexNameNewtype with IndexPatternNewtype with RoutingNewtype {
  object DocumentId extends Newtype[String]
  type DocumentId = DocumentId.Type

  final implicit class ZIOAggregationsOps[R](zio: RIO[R, ResultWithAggregation]) {

    /**
     * Executes the [[ElasticRequest.SearchRequest]] or the [[ElasticRequest.SearchAndAggregateRequest]].
     *
     * @param name
     *   the name of the aggregation to retrieve
     * @return
     *   a [[RIO]] effect that, when executed, will produce an optional [[result.AggregationResult]].
     */
    def aggregation(name: String): RIO[R, Option[AggregationResult]] =
      zio.flatMap(_.aggregation(name))

    /**
     * Executes the [[ElasticRequest.SearchRequest]] or the [[ElasticRequest.SearchAndAggregateRequest]].
     *
     * @param name
     *   the name of the aggregation to retrieve
     * @tparam A
     *   type of the aggregation to retrieve, must be subtype of [[result.AggregationResult]]
     * @return
     *   a [[RIO]] effect that, when executed, will produce the aggregation as instance of parameter A.
     */
    def aggregationAs[A <: AggregationResult](name: String): RIO[R, Option[A]] =
      zio.flatMap(_.aggregationAs[A](name))

    /**
     * Executes the [[ElasticRequest.SearchRequest]] or the [[ElasticRequest.SearchAndAggregateRequest]].
     *
     * @return
     *   a [[RIO]] effect that, when executed, will produce a Map of the aggregations name and
     *   [[result.AggregationResult]].
     */
    def aggregations: RIO[R, Map[String, AggregationResult]] =
      zio.flatMap(_.aggregations)

    /**
     * Executes the [[ElasticRequest.SearchRequest]] or the [[ElasticRequest.SearchAndAggregateRequest]].
     *
     * @param name
     *   the name of the aggregation to retrieve
     * @return
     *   a [[RIO]] effect that, when executed, will produce the aggregation as instance of
     *   [[result.AvgAggregationResult]].
     */
    def asAvgAggregation(name: String): RIO[R, Option[AvgAggregationResult]] =
      aggregationAs[AvgAggregationResult](name)

    /**
     * Executes the [[ElasticRequest.SearchRequest]] or the [[ElasticRequest.SearchAndAggregateRequest]].
     *
     * @param name
     *   the name of the aggregation to retrieve
     * @return
     *   a [[RIO]] effect that, when executed, will produce the aggregation as instance of
     *   [[result.CardinalityAggregationResult]].
     */
    def asCardinalityAggregation(name: String): RIO[R, Option[CardinalityAggregationResult]] =
      aggregationAs[CardinalityAggregationResult](name)

    /**
     * Executes the [[ElasticRequest.SearchRequest]] or the [[ElasticRequest.SearchAndAggregateRequest]].
     *
     * @param name
     *   the name of the aggregation to retrieve
     * @return
     *   a [[RIO]] effect that, when executed, will produce the aggregation as instance of
     *   [[result.ExtendedStatsAggregationResult]].
     */
    def asExtendedStatsAggregation(name: String): RIO[R, Option[ExtendedStatsAggregationResult]] =
      aggregationAs[ExtendedStatsAggregationResult](name)

    /**
     * Executes the [[ElasticRequest.SearchRequest]] or the [[ElasticRequest.SearchAndAggregateRequest]].
     *
     * @param name
     *   the name of the aggregation to retrieve
     * @return
     *   a [[RIO]] effect that, when executed, will produce the aggregation as instance of
     *   [[result.MaxAggregationResult]].
     */
    def asMaxAggregation(name: String): RIO[R, Option[MaxAggregationResult]] =
      aggregationAs[MaxAggregationResult](name)

    /**
     * Executes the [[ElasticRequest.SearchRequest]] or the [[ElasticRequest.SearchAndAggregateRequest]].
     *
     * @param name
     *   the name of the aggregation to retrieve
     * @return
     *   a [[RIO]] effect that, when executed, will produce the aggregation as instance of
     *   [[result.MinAggregationResult]].
     */
    def asMinAggregation(name: String): RIO[R, Option[MinAggregationResult]] =
      aggregationAs[MinAggregationResult](name)

    /**
     * Executes the [[ElasticRequest.SearchRequest]] or the [[ElasticRequest.SearchAndAggregateRequest]].
     *
     * @param name
     *   the name of the aggregation to retrieve
     * @return
     *   a [[RIO]] effect that, when executed, will produce the aggregation as instance of
     *   [[result.MissingAggregationResult]].
     */
    def asMissingAggregation(name: String): RIO[R, Option[MissingAggregationResult]] =
      aggregationAs[MissingAggregationResult](name)

    /**
     * Executes the [[ElasticRequest.SearchRequest]] or the [[ElasticRequest.SearchAndAggregateRequest]].
     *
     * @param name
     *   the name of the aggregation to retrieve
     * @return
     *   a [[RIO]] effect that, when executed, will produce the aggregation as instance of
     *   [[result.PercentileRanksAggregationResult]].
     */
    def asPercentileRanksAggregation(name: String): RIO[R, Option[PercentileRanksAggregationResult]] =
      aggregationAs[PercentileRanksAggregationResult](name)

    /**
     * Executes the [[ElasticRequest.SearchRequest]] or the [[ElasticRequest.SearchAndAggregateRequest]].
     *
     * @param name
     *   the name of the aggregation to retrieve
     * @return
     *   a [[RIO]] effect that, when executed, will produce the aggregation as instance of
     *   [[result.PercentilesAggregationResult]].
     */
    def asPercentilesAggregation(name: String): RIO[R, Option[PercentilesAggregationResult]] =
      aggregationAs[PercentilesAggregationResult](name)

    /**
     * Executes the [[ElasticRequest.SearchRequest]] or the [[ElasticRequest.SearchAndAggregateRequest]].
     *
     * @param name
     *   the name of the aggregation to retrieve
     * @return
     *   a [[RIO]] effect that, when executed, will produce the aggregation as instance of
     *   [[result.StatsAggregationResult]].
     */
    def asStatsAggregation(name: String): RIO[R, Option[StatsAggregationResult]] =
      aggregationAs[StatsAggregationResult](name)

    /**
     * Executes the [[ElasticRequest.SearchRequest]] or the [[ElasticRequest.SearchAndAggregateRequest]].
     *
     * @param name
     *   the name of the aggregation to retrieve
     * @return
     *   a [[RIO]] effect that, when executed, will produce the aggregation as instance of
     *   [[result.SumAggregationResult]].
     */
    def asSumAggregation(name: String): RIO[R, Option[SumAggregationResult]] =
      aggregationAs[SumAggregationResult](name)

    /**
     * Executes the [[ElasticRequest.SearchRequest]] or the [[ElasticRequest.SearchAndAggregateRequest]].
     *
     * @param name
     *   the name of the aggregation to retrieve
     * @return
     *   a [[RIO]] effect that, when executed, will produce the aggregation as instance of
     *   [[result.TermsAggregationResult]].
     */
    def asTermsAggregation(name: String): RIO[R, Option[TermsAggregationResult]] =
      aggregationAs[TermsAggregationResult](name)

    /**
     * Executes the [[ElasticRequest.SearchRequest]] or the [[ElasticRequest.SearchAndAggregateRequest]].
     *
     * @param name
     *   the name of the aggregation to retrieve
     * @return
     *   a [[RIO]] effect that, when executed, will produce the aggregation as instance of
     *   [[result.ValueCountAggregationResult]].
     */
    def asValueCountAggregation(name: String): RIO[R, Option[ValueCountAggregationResult]] =
      aggregationAs[ValueCountAggregationResult](name)

    /**
     * Executes the [[ElasticRequest.SearchRequest]] or the [[ElasticRequest.SearchAndAggregateRequest]].
     *
     * @param name
     *   the name of the aggregation to retrieve
     * @return
     *   a [[RIO]] effect that, when executed, will produce the aggregation as instance of
     *   [[result.WeightedAvgAggregationResult]].
     */
    def asWeightedAvgAggregation(name: String): RIO[R, Option[WeightedAvgAggregationResult]] =
      aggregationAs[WeightedAvgAggregationResult](name)

  }

  final implicit class ZIODocumentOps[R, F[_]](zio: RIO[R, DocumentResult[F]]) {

    /**
     * Fetches and deserializes a document as a specific type.
     *
     * @tparam A
     *   the type to deserialize the document to
     * @return
     *   a `RIO` effect that, when executed, is going to fetch and deserialize the document as type `A`
     */
    def documentAs[A: Schema]: RIO[R, F[A]] =
      zio.flatMap(_.documentAs[A])
  }
}
