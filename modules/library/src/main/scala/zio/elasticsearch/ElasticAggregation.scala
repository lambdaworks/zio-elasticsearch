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

import zio.elasticsearch.aggregation._

object ElasticAggregation {

  /**
   * Constructs an empty instance of the [[zio.elasticsearch.aggregation.MultipleAggregations]].
   *
   * @return
   *   an instance of empty [[zio.elasticsearch.aggregation.MultipleAggregations]].
   */
  final def multipleAggregations: MultipleAggregations =
    Multiple(aggregations = Nil)

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
    Terms(name = name, field = field.toString, order = Nil, subAggregations = Nil, size = None)

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
    Terms(name = name, field = field, order = Nil, subAggregations = Nil, size = None)

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.aggregation.MaxAggregation]] using the specified parameters.
   *
   * @param name
   *   aggregation name
   * @param field
   *   the type-safe field for which max aggregation will be executed
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.MaxAggregation]] that represents max aggregation to be performed.
   */
  final def maxAggregation(name: String, field: Field[_, Any]): MaxAggregation =
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
}
