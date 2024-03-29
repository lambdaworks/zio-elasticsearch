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

package zio.elasticsearch.request.options

private[elasticsearch] trait HasFrom[R <: HasFrom[R]] {

  /**
   * Sets the starting offset from where the [[zio.elasticsearch.ElasticRequest.SearchRequest]] or the
   * [[zio.elasticsearch.ElasticRequest.SearchAndAggregateRequest]] return results.
   *
   * @param value
   *   a non-negative number to set the `from` parameter in the [[zio.elasticsearch.ElasticRequest]]
   * @return
   *   an instance of the [[zio.elasticsearch.ElasticRequest]] enriched with the `from` parameter.
   */
  def from(value: Int): R
}
