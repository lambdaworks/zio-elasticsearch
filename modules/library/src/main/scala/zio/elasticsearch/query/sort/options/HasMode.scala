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

package zio.elasticsearch.query.sort.options

import zio.elasticsearch.query.sort.SortMode

private[elasticsearch] trait HasMode[S <: HasMode[S]] {

  /**
   * Sets the `mode` parameter for the [[zio.elasticsearch.query.sort.Sort]]. The `mode` parameter controls how
   * Elasticsearch selects a single value from a set of sorted documents. The default `mode` is `Average`.
   *
   * @param value
   *   the [[SortMode]] used to pick a value among the sorted set of documents:
   *   - [[SortMode.Avg]]: uses the average of all values as sort value. Only applicable for number based array fields
   *   - [[SortMode.Max]]: picks the highest value
   *   - [[SortMode.Median]]: uses the median of all values as sort value. Only applicable for number based array fields
   *   - [[SortMode.Min]]: picks the lowest value
   *   - [[SortMode.Sum]]: uses the sum of all values as sort value. Only applicable for number based array fields
   * @return
   *   an instance of the [[zio.elasticsearch.query.sort.Sort]] enriched with the `mode` parameter.
   */
  def mode(value: SortMode): S
}
