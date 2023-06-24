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
   * Constructs an empty instance of the [[zio.elasticsearch.aggregation.MultipleAggregations]].
   *
   * @return
   *   an instance of empty [[zio.elasticsearch.aggregation.MultipleAggregations]].
   */
  final def multipleAggregations: MultipleAggregations =
    Multiple(aggregations = Chunk.empty)

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
   *   an instance of [[zio.elasticsearch.aggregation.SumAggregation]] that represents avg aggregation to be performed.
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
   *   an instance of [[zio.elasticsearch.aggregation.SumAggregation]] that represents avg aggregation to be performed.
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
}
