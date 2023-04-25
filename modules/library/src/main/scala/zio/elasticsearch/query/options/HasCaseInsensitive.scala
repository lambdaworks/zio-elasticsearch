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

private[elasticsearch] trait HasCaseInsensitive[Q <: HasCaseInsensitive[Q]] {

  /**
   * Sets the `caseInsensitive` parameter for the [[zio.elasticsearch.query.ElasticQuery]]. Case-insensitive queries
   * match text regardless of the case of the characters in the query string. By default, queries are case-sensitive.
   *
   * @param value
   *   the [[scala.Boolean]] value for `caseInsensitive` parameter
   * @return
   *   a new instance of the [[zio.elasticsearch.query.ElasticQuery]] with the `caseInsensitive` value set.
   */
  def caseInsensitive(value: Boolean): Q

  /**
   * Sets the `caseInsensitive` parameter to `false` for this [[zio.elasticsearch.query.ElasticQuery]]. Same as
   * [[caseInsensitive]](false).
   *
   * @return
   *   a new instance of the [[zio.elasticsearch.query.ElasticQuery]] with the `caseInsensitive` value set to `false`.
   * @see
   *   #caseInsensitive
   */
  final def caseInsensitiveFalse: Q = caseInsensitive(false)

  /**
   * Sets the `caseInsensitive` parameter to `true` for this [[zio.elasticsearch.query.ElasticQuery]]. Same as
   * [[caseInsensitive]](true).
   *
   * @return
   *   a new instance of the [[zio.elasticsearch.query.ElasticQuery]] with the `caseInsensitive` value set to `true`.
   * @see
   *   #caseInsensitive
   */
  final def caseInsensitiveTrue: Q = caseInsensitive(true)
}
