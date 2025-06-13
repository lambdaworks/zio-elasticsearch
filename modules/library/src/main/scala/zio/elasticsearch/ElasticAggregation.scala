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

import zio.Chunk
import zio.elasticsearch.aggregation._
import zio.elasticsearch.query.ElasticQuery
import zio.elasticsearch.script.Script

object ElasticAggregation {

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.aggregation.AvgAggregation]] using the specified parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the type-safe field for which avg aggregation will be executed
   * @tparam A
   *   expected number type
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.AvgAggregation]] that represents avg aggregation to be performed.
   */
  final def avgAggregation[A: Numeric](name: String, field: Field[_, A]): AvgAggregation =
    Avg(name = name, field = field.toString, missing = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.aggregation.AvgAggregation]] using the specified parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the field for which avg aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.AvgAggregation]] that represents avg aggregation to be performed.
   */
  final def avgAggregation(name: String, field: String): AvgAggregation =
    Avg(name = name, field = field, missing = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.aggregation.BucketSelectorAggregation]] using the specified
   * parameters.
   *
   * @param name
   *   aggregation name
   * @param script
   *   The script to run for this aggregation. The script can be inline, file or indexed
   * @param bucketsPath
   *   A map of script variables and their associated path to the buckets we wish to use for the variable
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.BucketSelectorAggregation]] that represents bucket selector
   *   aggregation to be performed.
   */
  final def bucketSelectorAggregation(
    name: String,
    script: Script,
    bucketsPath: Map[String, String]
  ): BucketSelectorAggregation =
    BucketSelector(name = name, script = script, bucketsPath = bucketsPath)

  /**
   * Constructs an instance of [[zio.elasticsearch.aggregation.BucketSortAggregation]] using the specified parameters.
   *
   * @param name
   *   aggregation name
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.BucketSortAggregation]] that represents bucket sort aggregation to
   *   be performed.
   */
  final def bucketSortAggregation(name: String): BucketSortAggregation =
    BucketSort(name = name, sortBy = Chunk.empty, from = None, size = None)

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.aggregation.CardinalityAggregation]] using the specified
   * parameters. It calculates an approximate count of distinct values.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the type-safe field for which cardinality aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.CardinalityAggregation]] that represents cardinality aggregation
   *   to be performed.
   */
  final def cardinalityAggregation(name: String, field: Field[_, Any]): CardinalityAggregation =
    Cardinality(name = name, field = field.toString, missing = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.aggregation.CardinalityAggregation]] using the specified parameters.
   * It calculates an approximate count of distinct values.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the field for which cardinality aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.CardinalityAggregation]] that represents cardinality aggregation
   *   to be performed.
   */
  final def cardinalityAggregation(name: String, field: String): CardinalityAggregation =
    Cardinality(name = name, field = field, missing = None)

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.aggregation.ExtendedStatsAggregation]] using the specified
   * parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the type-safe field for which extended stats aggregation will be executed
   * @tparam A
   *   expected number type
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.ExtendedStatsAggregation]] that represents extended stats
   *   aggregation to be performed.
   */
  final def extendedStatsAggregation[A: Numeric](name: String, field: Field[_, A]): ExtendedStatsAggregation =
    ExtendedStats(name = name, field = field.toString, missing = None, sigma = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.aggregation.ExtendedStatsAggregation]] using the specified
   * parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the field for which extended stats aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.ExtendedStatsAggregation]] that represents extended stats
   *   aggregation to be performed.
   */
  final def extendedStatsAggregation(name: String, field: String): ExtendedStatsAggregation =
    ExtendedStats(name = name, field = field, missing = None, sigma = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.aggregation.FilterAggregation]] using the specified parameters.
   *
   * @param name
   *   aggregation name
   * @param query
   *   a query which the documents must match
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.FilterAggregation]] that represents filter aggregation to be
   *   performed.
   */
  final def filterAggregation(name: String, query: ElasticQuery[_]): FilterAggregation =
    Filter(name = name, query = query, subAggregations = Chunk.empty)

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.aggregation.MaxAggregation]] using the specified parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the type-safe field for which max aggregation will be executed
   * @tparam A
   *   expected number type
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.MaxAggregation]] that represents max aggregation to be performed.
   */
  final def maxAggregation[A: Numeric](name: String, field: Field[_, A]): MaxAggregation =
    Max(name = name, field = field.toString, missing = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.aggregation.MaxAggregation]] using the specified parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the field for which max aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.MaxAggregation]] that represents max aggregation to be performed.
   */
  final def maxAggregation(name: String, field: String): MaxAggregation =
    Max(name = name, field = field, missing = None)

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.aggregation.MinAggregation]] using the specified parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the type-safe field for which min aggregation will be executed
   * @tparam A
   *   expected number type
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.MinAggregation]] that represents min aggregation to be performed.
   */
  final def minAggregation[A: Numeric](name: String, field: Field[_, A]): MinAggregation =
    Min(name = name, field = field.toString, missing = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.aggregation.MinAggregation]] using the specified parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the field for which min aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.MinAggregation]] that represents min aggregation to be performed.
   */
  final def minAggregation(name: String, field: String): MinAggregation =
    Min(name = name, field = field, missing = None)

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.aggregation.MissingAggregation]] using the specified
   * parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the type-safe field for which missing aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.MissingAggregation]] that represents missing aggregation to be
   *   performed.
   */
  final def missingAggregation(name: String, field: Field[_, String]): MissingAggregation =
    Missing(name = name, field = field.toString)

  /**
   * Constructs an instance of [[zio.elasticsearch.aggregation.MissingAggregation]] using the specified parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the field for which missing aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.MissingAggregation]] that represents missing aggregation to be
   *   performed.
   */
  final def missingAggregation(name: String, field: String): MissingAggregation =
    Missing(name = name, field = field)

  /**
   * Constructs an empty instance of the [[zio.elasticsearch.aggregation.MultipleAggregations]].
   *
   * @return
   *   an instance of empty [[zio.elasticsearch.aggregation.MultipleAggregations]].
   */
  final def multipleAggregations: MultipleAggregations =
    Multiple(aggregations = Chunk.empty)

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.aggregation.PercentileRanksAggregation]] using the specified
   * parameters.
   *
   * @param name
   *   the name of the aggregation
   * @param field
   *   the type-safe field for which percentile ranks aggregation will be executed
   * @param value
   *   the first value to be calculated for [[zio.elasticsearch.aggregation.PercentileRanksAggregation]]
   * @param values
   *   an array of values to be calculated for [[zio.elasticsearch.aggregation.PercentileRanksAggregation]]
   * @tparam A
   *   expected number type
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.PercentileRanksAggregation]] that represents percentile ranks
   *   aggregation to be performed.
   */
  final def percentileRanksAggregation[A: Numeric](
    name: String,
    field: Field[_, A],
    value: BigDecimal,
    values: BigDecimal*
  ): PercentileRanksAggregation =
    PercentileRanks(
      name = name,
      field = field.toString,
      values = value +: Chunk.fromIterable(values),
      missing = None
    )

  /**
   * Constructs an instance of [[zio.elasticsearch.aggregation.PercentileRanksAggregation]] using the specified
   * parameters.
   *
   * @param name
   *   the name of the aggregation
   * @param field
   *   the field for which percentile ranks aggregation will be executed
   * @param value
   *   the first value to be calculated for [[zio.elasticsearch.aggregation.PercentileRanksAggregation]]
   * @param values
   *   an array of values to be calculated for [[zio.elasticsearch.aggregation.PercentileRanksAggregation]]
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.PercentileRanksAggregation]] that represents percentile ranks
   *   aggregation to be performed.
   */
  final def percentileRanksAggregation(
    name: String,
    field: String,
    value: BigDecimal,
    values: BigDecimal*
  ): PercentileRanksAggregation =
    PercentileRanks(
      name = name,
      field = field,
      values = value +: Chunk.fromIterable(values),
      missing = None
    )

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.aggregation.PercentilesAggregation]] using the specified
   * parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the type-safe field for which percentiles aggregation will be executed
   * @tparam A
   *   expected number type
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.PercentilesAggregation]] that represents percentiles aggregation
   *   to be performed.
   */
  final def percentilesAggregation[A: Numeric](name: String, field: Field[_, A]): PercentilesAggregation =
    Percentiles(name = name, field = field.toString, percents = Chunk.empty, missing = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.aggregation.PercentilesAggregation]] using the specified parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the field for which percentiles aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.PercentilesAggregation]] that represents percentiles aggregation
   *   to be performed.
   */
  final def percentilesAggregation(name: String, field: String): PercentilesAggregation =
    Percentiles(name = name, field = field, percents = Chunk.empty, missing = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.aggregation.SamplerAggregation]] using the specified parameters.
   *
   * @param name
   *   the name of the aggregation.
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.SamplerAggregation]] that represents sampler aggregation to be
   *   performed.
   */
  final def samplerAggregation(name: String): SamplerAggregation =
    Sampler(name = name, shardSizeValue = None, subAggregations = Chunk.empty)

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.aggregation.StatsAggregation]] using the specified
   * parameters.
   *
   * @param name
   *   the name of the aggregation
   * @param field
   *   the type-safe field for which stats aggregation will be executed
   * @tparam A
   *   expected number type
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.StatsAggregation]] that represents stats aggregation to be
   *   performed.
   */
  final def statsAggregation[A: Numeric](name: String, field: Field[_, A]): StatsAggregation =
    Stats(name = name, field = field.toString, missing = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.aggregation.StatsAggregation]] using the specified parameters.
   *
   * @param name
   *   the name of the aggregation
   * @param field
   *   the field for which stats aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.StatsAggregation]] that represents stats aggregation to be
   *   performed.
   */
  final def statsAggregation(name: String, field: String): StatsAggregation =
    Stats(name = name, field = field, missing = None)

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.aggregation.SumAggregation]] using the specified parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the type-safe field for which sum aggregation will be executed
   * @tparam A
   *   expected number type
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.SumAggregation]] that represents sum aggregation to be performed.
   */
  final def sumAggregation[A: Numeric](name: String, field: Field[_, A]): SumAggregation =
    Sum(name = name, field = field.toString, missing = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.aggregation.SumAggregation]] using the specified parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the field for which sum aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.SumAggregation]] that represents sum aggregation to be performed.
   */
  final def sumAggregation(name: String, field: String): SumAggregation =
    Sum(name = name, field = field, missing = None)

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.aggregation.TermsAggregation]] using the specified
   * parameters.
   *
   * @param name
   *   the name of the aggregation
   * @param field
   *   the type-safe field for which the aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.TermsAggregation]] that represents terms aggregation to be
   *   performed.
   */
  final def termsAggregation(name: String, field: Field[_, String]): TermsAggregation =
    Terms(name = name, field = field.toString, order = Chunk.empty, subAggregations = Chunk.empty, size = None)

  /**
   * Constructs an instance of [[zio.elasticsearch.aggregation.TermsAggregation]] using the specified parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the field for which terms aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.TermsAggregation]] that represents terms aggregation to be
   *   performed.
   */
  final def termsAggregation(name: String, field: String): TermsAggregation =
    Terms(name = name, field = field, order = Chunk.empty, subAggregations = Chunk.empty, size = None)

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.aggregation.ValueCountAggregation]] using the specified
   * parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the type-safe field for which value count aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.ValueCountAggregation]] that represents value count aggregation to
   *   be performed.
   */
  final def valueCountAggregation(name: String, field: Field[_, Any]): ValueCountAggregation =
    ValueCount(name = name, field = field.toString)

  /**
   * Constructs an instance of [[zio.elasticsearch.aggregation.ValueCountAggregation]] using the specified parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the field for which value count aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.ValueCountAggregation]] that represents value count aggregation to
   *   be performed.
   */
  final def valueCountAggregation(name: String, field: String): ValueCountAggregation =
    ValueCount(name = name, field = field)

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.aggregation.WeightedAvgAggregation]] using the specified
   * parameters.
   *
   * @param name
   *   aggregation name
   * @param valueField
   *   the type-safe field that represents value for which weighted avg aggregation will be executed
   * @param weightField
   *   the type-safe field that represents weight for which weighted avg aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.WeightedAvgAggregation]] that represents weighted avg aggregation
   *   to be performed.
   */
  final def weightedAvgAggregation(
    name: String,
    valueField: Field[_, Any],
    weightField: Field[_, Any]
  ): WeightedAvgAggregation =
    WeightedAvg(
      name = name,
      valueField = valueField.toString,
      weightField = weightField.toString,
      valueMissing = None,
      weightMissing = None
    )

  /**
   * Constructs an instance of [[zio.elasticsearch.aggregation.WeightedAvgAggregation]] using the specified parameters.
   *
   * @param name
   *   aggregation name
   * @param valueField
   *   the field that represents value for which weighted avg aggregation will be executed
   * @param weightField
   *   the field that represents weight for which weighted avg aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.WeightedAvgAggregation]] that represents weighted avg aggregation
   *   to be performed.
   */
  final def weightedAvgAggregation(name: String, valueField: String, weightField: String): WeightedAvgAggregation =
    WeightedAvg(
      name = name,
      valueField = valueField,
      weightField = weightField,
      valueMissing = None,
      weightMissing = None
    )

}
