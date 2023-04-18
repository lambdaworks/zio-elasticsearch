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

package zio.elasticsearch

import zio.elasticsearch.query.sort.Sort

package object request {
  private[elasticsearch] trait HasRefresh[R <: HasRefresh[R]] {

    /**
     * Configures whether or not to refresh the index after the operation in the [[ElasticRequest]].
     *
     * @param value
     *   `true` if the index should be refreshed after the operation, `false` otherwise
     * @return
     *   returns a new instance of the [[ElasticRequest]] with the `refresh` value set.
     */
    def refresh(value: Boolean): R

    /**
     * Sets `refresh` parameter to `false` in the [[ElasticRequest]].
     *
     * @return
     *   returns a new instance of the [[ElasticRequest]] with the `refresh` value set to `false`.
     */
    final def refreshFalse: R = refresh(false)

    /**
     * Sets `refresh` parameter to `true` in the [[ElasticRequest]].
     *
     * @return
     *   returns a new instance of the [[ElasticRequest]] with the `refresh` value set to `true`.
     */
    final def refreshTrue: R = refresh(true)
  }

  private[elasticsearch] trait HasRouting[R <: HasRouting[R]] {

    /**
     * Specifies a `routing` value to be used for this [[ElasticRequest]].
     *
     * @param value
     *   the [[Routing]] value to set for the [[ElasticRequest]]
     * @return
     *   returns a new instance of the [[ElasticRequest]] with the `routing` value set.
     */
    def routing(value: Routing): R
  }

  private[elasticsearch] trait HasFrom[R <: HasFrom[R]] {

    /**
     * Sets the starting offset from where the [[zio.elasticsearch.ElasticRequest.SearchRequest]] or the
     * [[zio.elasticsearch.ElasticRequest.SearchAndAggregateRequest]] should return results.
     *
     * @param value
     *   the `Int` value to set the `from` parameter in the [[ElasticRequest]]
     * @return
     *   returns a new instance of the [[ElasticRequest]] type with the `from` parameter set.
     */
    def from(value: Int): R
  }

  private[elasticsearch] trait HasSize[R <: HasSize[R]] {

    /**
     * Sets the number of results to return.
     *
     * @param value
     *   the `Int` value to set the `size` parameter in the [[ElasticRequest]]
     * @return
     *   returns a new instance of the [[ElasticRequest]] with the `size` value set.
     */
    def size(value: Int): R
  }

  private[elasticsearch] trait WithSort[R <: WithSort[R]] {

    /**
     * Sets the sorting criteria for the [[zio.elasticsearch.ElasticRequest.SearchRequest]] or the
     * [[zio.elasticsearch.ElasticRequest.SearchAndAggregateRequest]].
     *
     * @param sorts
     *   one or more [[Sort]] objects that define the sorting criteria
     * @return
     *   returns a new instance of the [[ElasticRequest]] with the sorting criteria set.
     */
    def sort(sorts: Sort*): R
  }
}
