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

import zio.json.ast.Json

private[elasticsearch] trait HasSearchAfter[R <: HasSearchAfter[R]] {

  /**
   * Sets the `search_after` parameter for the [[zio.elasticsearch.ElasticRequest]].
   *
   * @param value
   *   the JSON value to be set as the `search_after` parameter
   * @return
   *   an instance of a [[zio.elasticsearch.ElasticRequest]] enriched with the `search_after` parameter.
   */
  def searchAfter(value: Json): R
}
