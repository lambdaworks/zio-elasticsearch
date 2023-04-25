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

trait HasBoost[Q <: HasBoost[Q]] {

  /**
   * Sets the `boost` parameter for this [[zio.elasticsearch.query.ElasticQuery]]. The `boost` value is a positive
   * multiplier applied to the score of documents matching the query. A value greater than 1 increases the relevance
   * score of matching documents, while a value less than 1 decreases it. The default `boost` value is 1.
   *
   * @param value
   *   a non-negative real number to set `boost` parameter to
   * @return
   *   a new instance of the [[zio.elasticsearch.query.ElasticQuery]] with the `boost` value set.
   */
  def boost(value: Double): Q
}
