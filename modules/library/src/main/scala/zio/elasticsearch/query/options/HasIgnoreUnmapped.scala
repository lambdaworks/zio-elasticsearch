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

private[elasticsearch] trait HasIgnoreUnmapped[Q <: HasIgnoreUnmapped[Q]] {

  /**
   * Sets the `ignoreUnmapped` parameter to control whether to ignore unmapped fields and return empty hits.
   *
   * @param value
   *   the `boolean` value for `ignoreUnmapped` parameter
   * @return
   *   a new instance of the [[zio.elasticsearch.query.ElasticQuery]] with the `ignoreUnmapped` value set.
   */
  def ignoreUnmapped(value: Boolean): Q

  /**
   * Sets the `ignoreUnmapped` parameter to `false` for this [[zio.elasticsearch.query.ElasticQuery]]. Same as
   * [[ignoreUnmapped]](false).
   *
   * @return
   *   a new instance of the [[zio.elasticsearch.query.ElasticQuery]] with the `ignoreUnmapped` value set to `false`.
   * @see
   *   #ignoreUnmapped
   */
  final def ignoreUnmappedFalse: Q = ignoreUnmapped(false)

  /**
   * Sets the `ignoreUnmapped` parameter to `true` for this [[zio.elasticsearch.query.ElasticQuery]]. Same as
   * [[ignoreUnmapped]](true).
   *
   * @return
   *   a new instance of the [[zio.elasticsearch.query.ElasticQuery]] with the `ignoreUnmapped` value set to `true`.
   * @see
   *   #ignoreUnmapped
   */
  final def ignoreUnmappedTrue: Q = ignoreUnmapped(true)
}
