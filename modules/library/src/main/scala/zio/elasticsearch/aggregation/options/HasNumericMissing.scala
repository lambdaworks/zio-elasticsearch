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

package zio.elasticsearch.aggregation.options

import zio.elasticsearch.ElasticPrimitive.ElasticPrimitive

private[elasticsearch] trait HasNumericMissing[A <: HasNumericMissing[A]] {

  /**
   * Sets the `missing` parameter for the [[zio.elasticsearch.aggregation.ElasticAggregation]]. The `missing` parameter
   * provides a value to use when a document is missing the field that the aggregation is running on. The aggregation
   * works only on numeric fields, so the value has to be a number.
   *
   * @param value
   *   the value to use for missing documents
   * @tparam B
   *   the numeric type of the value, for which an implicit [[zio.elasticsearch.ElasticPrimitive.ElasticPrimitive]] is
   *   in scope
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.ElasticAggregation]] enriched with the `missing` parameter.
   */
  def missing[B: ElasticPrimitive: Numeric](value: B): A
}
