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

import zio.elasticsearch.Routing

private[elasticsearch] trait HasRouting[R <: HasRouting[R]] {

  /**
   * Specifies a `routing` value to be used for this [[zio.elasticsearch.ElasticRequest]].
   *
   * @param value
   *   the [[Routing]] value to set for the [[zio.elasticsearch.ElasticRequest]]
   * @return
   *   an instance of the [[zio.elasticsearch.ElasticRequest]] enriched with the `routing` parameter.
   */
  def routing(value: Routing): R
}
