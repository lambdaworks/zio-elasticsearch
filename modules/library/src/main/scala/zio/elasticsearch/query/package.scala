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

package object query {
  private[elasticsearch] trait HasBoost[Q <: HasBoost[Q]] {

    /**
     * Sets the `boost` parameter for this [[ElasticQuery]].
     *
     * @param value
     *   a double `boost` value to set
     * @return
     *   returns a new instance of the [[ElasticQuery]] with the `boost` value set.
     */
    def boost(value: Double): Q
  }

  private[elasticsearch] trait HasCaseInsensitive[Q <: HasCaseInsensitive[Q]] {

    /**
     * Sets the `caseInsensitive` parameter for this [[ElasticQuery]].
     *
     * @param value
     *   the `boolean` value for `caseInsensitive` parameter
     * @return
     *   returns a new instance of the [[ElasticQuery]] with the `caseInsensitive` value set.
     */
    def caseInsensitive(value: Boolean): Q

    /**
     * Sets the `caseInsensitive` parameter to `false` for this [[ElasticQuery]].
     *
     * @return
     *   returns a new instance of the [[ElasticQuery]] with the `caseInsensitive` value set to `false`.
     */
    final def caseInsensitiveFalse: Q = caseInsensitive(false)

    /**
     * Sets the `caseInsensitive` parameter to `true` for this [[ElasticQuery]].
     *
     * @return
     *   returns a new instance of the [[ElasticQuery]] with the `caseInsensitive` value set to `true`.
     */
    final def caseInsensitiveTrue: Q = caseInsensitive(true)
  }

  private[elasticsearch] trait HasIgnoreUnmapped[Q <: HasIgnoreUnmapped[Q]] {

    /**
     * Sets the `ignoreUnmapped` parameter to control whether to ignore unmapped fields and return empty hits
     *
     * @param value
     *   the `boolean` value for `ignore_unmapped` parameter
     * @return
     *   returns a new instance of the [[ElasticQuery]] with the `ignoreUnmapped` value set.
     */
    def ignoreUnmapped(value: Boolean): Q

    /**
     * Sets the `ignoreUnmapped` parameter to `false` for this [[ElasticQuery]].
     *
     * @return
     *   returns a new instance of the [[ElasticQuery]] with the `ignoreUnmapped` value set to `false`.
     */
    final def ignoreUnmappedFalse: Q = ignoreUnmapped(false)

    /**
     * Sets the `ignoreUnmapped` parameter to `true` for this [[ElasticQuery]].
     *
     * @return
     *   returns a new instance of the [[ElasticQuery]] with the `ignoreUnmapped` value set to `true`.
     */
    final def ignoreUnmappedTrue: Q = ignoreUnmapped(true)
  }

  private[elasticsearch] trait HasInnerHits[Q <: HasInnerHits[Q]] {

    /**
     * Sets the inner hits for this [[ElasticQuery]] to the default `InnerHits()` value.
     *
     * @return
     *   returns a new instance of the [[ElasticQuery]] with the inner hits set to the default value.
     */
    final def innerHits: Q = innerHits(InnerHits())

    /**
     * Sets the inner hits configuration for the [[NestedQuery]].
     *
     * @param innerHits
     *   the configuration for inner hits
     * @return
     *   returns a new instance of the [[ElasticQuery]] with the specified inner hits configuration.
     */
    def innerHits(innerHits: InnerHits): Q
  }

  private[elasticsearch] trait HasScoreMode[Q <: HasScoreMode[Q]] {

    /**
     * Sets the [[ScoreMode]] for the [[NestedQuery]]. The [[ScoreMode]] specifies how scores of matching documents are
     * combined for the [[NestedQuery]].
     *
     * @param scoreMode
     *   the [[ScoreMode]] to use for this [[NestedQuery]]
     * @return
     *   returns a new instance of the [[ElasticQuery]] with the specified [[ScoreMode]].
     */
    def scoreMode(scoreMode: ScoreMode): Q
  }
}
