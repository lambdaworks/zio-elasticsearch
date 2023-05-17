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

import zio.elasticsearch.query.InnerHits

private[elasticsearch] trait HasInnerHits[Q <: HasInnerHits[Q]] {

  /**
   * Sets the inner hits for this [[zio.elasticsearch.query.ElasticQuery]] to the default `InnerHits()` value.
   *
   * @return
   *   a new instance of the [[zio.elasticsearch.query.ElasticQuery]] with the default inner hits.
   */
  final def innerHits: Q = innerHits(InnerHits.empty)

  /**
   * Sets the inner hits configuration for the [[zio.elasticsearch.query.NestedQuery]].
   *
   * @param innerHits
   *   the configuration for inner hits
   * @return
   *   a new instance of the [[zio.elasticsearch.query.ElasticQuery]] with the specified inner hits configuration.
   */
  def innerHits(innerHits: InnerHits): Q
}
