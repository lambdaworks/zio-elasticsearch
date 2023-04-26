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

private[elasticsearch] trait HasRefresh[R <: HasRefresh[R]] {

  /**
   * Configures whether or not to refresh the index after the operation in the [[zio.elasticsearch.ElasticRequest]].
   *
   * @param value
   *   should be `true` if the index should be refreshed after the executed operation, `false` otherwise
   * @return
   *   an instance of the [[zio.elasticsearch.ElasticRequest]] enriched with the `refresh` parameter.
   */
  def refresh(value: Boolean): R

  /**
   * Sets `refresh` parameter to `false` in the [[zio.elasticsearch.ElasticRequest]]. Same as [[refresh]](false).
   *
   * @return
   *   a new instance of the [[zio.elasticsearch.ElasticRequest]] with the `refresh` parameter set to `false`.
   * @see
   *   #refresh
   */
  final def refreshFalse: R = refresh(false)

  /**
   * Sets `refresh` parameter to `true` in the [[zio.elasticsearch.ElasticRequest]]. Same as [[refresh]](true).
   *
   * @return
   *   a new instance of the [[zio.elasticsearch.ElasticRequest]] with the `refresh` parameter set to `true`.
   * @see
   *   #refresh
   */
  final def refreshTrue: R = refresh(true)
}
