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

package zio.elasticsearch.result

import zio.ZIO

import scala.reflect.ClassTag

/**
 * Typed accessors for aggregation results, shared by results that contain aggregations and by effects that produce
 * them.
 *
 * @tparam R
 *   the environment of the returned effects
 * @tparam E
 *   the error type of the returned effects
 */
private[elasticsearch] trait AggregationAccessors[R, E] {

  /**
   * Retrieves the aggregation with the given name as an instance of `A`.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @tparam A
   *   the type of the aggregation to retrieve, a subtype of [[AggregationResult]]
   * @return
   *   an effect that produces the aggregation as an instance of `A`, or `None` if there is no aggregation with the
   *   given name. It fails with a [[DecodingException]] if the aggregation is not of type `A`.
   */
  def aggregationAs[A <: AggregationResult: ClassTag](name: String): ZIO[R, E, Option[A]]

  /**
   * Retrieves the `avg` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[AvgAggregationResult]], or `None` if there is no
   *   aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asAvgAggregation(name: String): ZIO[R, E, Option[AvgAggregationResult]] =
    aggregationAs[AvgAggregationResult](name)

  /**
   * Retrieves the `avg_bucket` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[SimpleValueAggregationResult]], or `None` if there is
   *   no aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asAvgBucketAggregation(name: String): ZIO[R, E, Option[SimpleValueAggregationResult]] =
    aggregationAs[SimpleValueAggregationResult](name)

  /**
   * Retrieves the `cardinality` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[CardinalityAggregationResult]], or `None` if there is
   *   no aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asCardinalityAggregation(name: String): ZIO[R, E, Option[CardinalityAggregationResult]] =
    aggregationAs[CardinalityAggregationResult](name)

  /**
   * Retrieves the `date_histogram` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[DateHistogramAggregationResult]], or `None` if there
   *   is no aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another
   *   type.
   */
  final def asDateHistogramAggregation(name: String): ZIO[R, E, Option[DateHistogramAggregationResult]] =
    aggregationAs[DateHistogramAggregationResult](name)

  /**
   * Retrieves the `extended_stats` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[ExtendedStatsAggregationResult]], or `None` if there
   *   is no aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another
   *   type.
   */
  final def asExtendedStatsAggregation(name: String): ZIO[R, E, Option[ExtendedStatsAggregationResult]] =
    aggregationAs[ExtendedStatsAggregationResult](name)

  /**
   * Retrieves the `filter` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[FilterAggregationResult]], or `None` if there is no
   *   aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asFilterAggregation(name: String): ZIO[R, E, Option[FilterAggregationResult]] =
    aggregationAs[FilterAggregationResult](name)

  /**
   * Retrieves the `filters` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[FiltersAggregationResult]], or `None` if there is no
   *   aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asFiltersAggregation(name: String): ZIO[R, E, Option[FiltersAggregationResult]] =
    aggregationAs[FiltersAggregationResult](name)

  /**
   * Retrieves the `histogram` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[HistogramAggregationResult]], or `None` if there is
   *   no aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asHistogramAggregation(name: String): ZIO[R, E, Option[HistogramAggregationResult]] =
    aggregationAs[HistogramAggregationResult](name)

  /**
   * Retrieves the `ip_range` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[IpRangeAggregationResult]], or `None` if there is no
   *   aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asIpRangeAggregation(name: String): ZIO[R, E, Option[IpRangeAggregationResult]] =
    aggregationAs[IpRangeAggregationResult](name)

  /**
   * Retrieves the `max` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[MaxAggregationResult]], or `None` if there is no
   *   aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asMaxAggregation(name: String): ZIO[R, E, Option[MaxAggregationResult]] =
    aggregationAs[MaxAggregationResult](name)

  /**
   * Retrieves the `max_bucket` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[BucketMetricValueAggregationResult]], or `None` if
   *   there is no aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of
   *   another type.
   */
  final def asMaxBucketAggregation(name: String): ZIO[R, E, Option[BucketMetricValueAggregationResult]] =
    aggregationAs[BucketMetricValueAggregationResult](name)

  /**
   * Retrieves the `min` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[MinAggregationResult]], or `None` if there is no
   *   aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asMinAggregation(name: String): ZIO[R, E, Option[MinAggregationResult]] =
    aggregationAs[MinAggregationResult](name)

  /**
   * Retrieves the `min_bucket` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[BucketMetricValueAggregationResult]], or `None` if
   *   there is no aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of
   *   another type.
   */
  final def asMinBucketAggregation(name: String): ZIO[R, E, Option[BucketMetricValueAggregationResult]] =
    aggregationAs[BucketMetricValueAggregationResult](name)

  /**
   * Retrieves the `missing` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[MissingAggregationResult]], or `None` if there is no
   *   aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asMissingAggregation(name: String): ZIO[R, E, Option[MissingAggregationResult]] =
    aggregationAs[MissingAggregationResult](name)

  /**
   * Retrieves the `percentile_ranks` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[PercentileRanksAggregationResult]], or `None` if
   *   there is no aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of
   *   another type.
   */
  final def asPercentileRanksAggregation(name: String): ZIO[R, E, Option[PercentileRanksAggregationResult]] =
    aggregationAs[PercentileRanksAggregationResult](name)

  /**
   * Retrieves the `percentiles` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[PercentilesAggregationResult]], or `None` if there is
   *   no aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asPercentilesAggregation(name: String): ZIO[R, E, Option[PercentilesAggregationResult]] =
    aggregationAs[PercentilesAggregationResult](name)

  /**
   * Retrieves the `range` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[RangeAggregationResult]], or `None` if there is no
   *   aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asRangeAggregation(name: String): ZIO[R, E, Option[RangeAggregationResult]] =
    aggregationAs[RangeAggregationResult](name)

  /**
   * Retrieves the `sampler` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[SamplerAggregationResult]], or `None` if there is no
   *   aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asSamplerAggregation(name: String): ZIO[R, E, Option[SamplerAggregationResult]] =
    aggregationAs[SamplerAggregationResult](name)

  /**
   * Retrieves the `stats` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[StatsAggregationResult]], or `None` if there is no
   *   aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asStatsAggregation(name: String): ZIO[R, E, Option[StatsAggregationResult]] =
    aggregationAs[StatsAggregationResult](name)

  /**
   * Retrieves the `stats_bucket` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[StatsAggregationResult]], or `None` if there is no
   *   aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asStatsBucketAggregation(name: String): ZIO[R, E, Option[StatsAggregationResult]] =
    aggregationAs[StatsAggregationResult](name)

  /**
   * Retrieves the `sum` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[SumAggregationResult]], or `None` if there is no
   *   aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asSumAggregation(name: String): ZIO[R, E, Option[SumAggregationResult]] =
    aggregationAs[SumAggregationResult](name)

  /**
   * Retrieves the `sum_bucket` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[SimpleValueAggregationResult]], or `None` if there is
   *   no aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asSumBucketAggregation(name: String): ZIO[R, E, Option[SimpleValueAggregationResult]] =
    aggregationAs[SimpleValueAggregationResult](name)

  /**
   * Retrieves the `terms` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[TermsAggregationResult]], or `None` if there is no
   *   aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asTermsAggregation(name: String): ZIO[R, E, Option[TermsAggregationResult]] =
    aggregationAs[TermsAggregationResult](name)

  /**
   * Retrieves the `value_count` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[ValueCountAggregationResult]], or `None` if there is
   *   no aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asValueCountAggregation(name: String): ZIO[R, E, Option[ValueCountAggregationResult]] =
    aggregationAs[ValueCountAggregationResult](name)

  /**
   * Retrieves the `weighted_avg` aggregation with the given name.
   *
   * @param name
   *   the name of the aggregation to retrieve
   * @return
   *   an effect that produces the aggregation as an instance of [[WeightedAvgAggregationResult]], or `None` if there is
   *   no aggregation with the given name. It fails with a [[DecodingException]] if the aggregation is of another type.
   */
  final def asWeightedAvgAggregation(name: String): ZIO[R, E, Option[WeightedAvgAggregationResult]] =
    aggregationAs[WeightedAvgAggregationResult](name)
}
