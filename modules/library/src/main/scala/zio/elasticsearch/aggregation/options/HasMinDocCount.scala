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

private[elasticsearch] trait HasMinDocCount[A <: HasMinDocCount[A]] {

  /**
   * Sets the `min_doc_count` parameter for the [[zio.elasticsearch.aggregation.SingleElasticAggregation]]. Only buckets
   * with at least this many documents are returned.
   *
   * @param value
   *   a non-negative number to set the `min_doc_count` parameter
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.SingleElasticAggregation]] enriched with the `min_doc_count`
   *   parameter.
   */
  def minDocCount(value: Int): A
}
