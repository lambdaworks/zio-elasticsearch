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

private[elasticsearch] trait HasMinimumShouldMatch[Q <: HasMinimumShouldMatch[Q]] {

  /**
   * Sets the `minimumShouldMatch` parameter for this [[zio.elasticsearch.ElasticQuery]]. The `minimumShouldMatch` value
   * is the number of should clauses returned documents must match. If the [[zio.elasticsearch.query.BoolQuery]]
   * includes at least one `should` clause and no `must`/`filter` clauses, the default value is 1. Otherwise, the
   * default value is 0.
   *
   * @param value
   *   a number to set `minimumShouldMatch` parameter to
   * @return
   *   a new instance of the [[zio.elasticsearch.ElasticQuery]] with the `minimumShouldMatch` value set.
   */
  def minimumShouldMatch(value: Int): Q
}
