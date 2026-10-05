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

package zio.elasticsearch.query.options

import zio.elasticsearch.query.MinimumShouldMatch

private[elasticsearch] trait HasMinimumShouldMatch[Q <: HasMinimumShouldMatch[Q]] {

  /**
   * Sets the `minimumShouldMatch` parameter for this [[zio.elasticsearch.ElasticQuery]]. The `minimumShouldMatch` value
   * is the minimum number of optional clauses (e.g. `should` clauses of a [[zio.elasticsearch.query.BoolQuery]] or
   * terms of a full text query) that returned documents must match. Its default value depends on the query type.
   *
   * This is a shorthand for `minimumShouldMatch(MinimumShouldMatch.Count(value))`.
   *
   * @param value
   *   a number to set `minimumShouldMatch` parameter to; a negative number is the number of optional clauses that may
   *   be missing
   * @return
   *   a new instance of the [[zio.elasticsearch.ElasticQuery]] with the `minimumShouldMatch` value set.
   */
  final def minimumShouldMatch(value: Int): Q =
    minimumShouldMatch(MinimumShouldMatch.Count(value))

  /**
   * Sets the `minimumShouldMatch` parameter for this [[zio.elasticsearch.ElasticQuery]]. The `minimumShouldMatch` value
   * is the minimum number of optional clauses (e.g. `should` clauses of a [[zio.elasticsearch.query.BoolQuery]] or
   * terms of a full text query) that returned documents must match. Its default value depends on the query type.
   *
   * @param value
   *   the [[zio.elasticsearch.query.MinimumShouldMatch]] to set `minimumShouldMatch` parameter to, possible values are:
   *   - [[zio.elasticsearch.query.MinimumShouldMatch.Count]]: a fixed number of clauses, e.g. `3` or `-2`
   *   - [[zio.elasticsearch.query.MinimumShouldMatch.Percentage]]: a percentage of clauses, e.g. `75%` or `-25%`
   *   - [[zio.elasticsearch.query.MinimumShouldMatch.Combination]]: one or more conditional specifications, e.g.
   *     `3<90%` or `2<-25% 9<-3`
   * @return
   *   a new instance of the [[zio.elasticsearch.ElasticQuery]] with the `minimumShouldMatch` value set.
   */
  def minimumShouldMatch(value: MinimumShouldMatch): Q
}
