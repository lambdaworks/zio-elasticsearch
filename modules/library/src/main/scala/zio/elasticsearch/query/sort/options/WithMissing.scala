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

package zio.elasticsearch.query.sort.options

import zio.elasticsearch.query.sort.Missing

private[elasticsearch] trait WithMissing[S <: WithMissing[S]] {

  /**
   * Sets the value to use when a document is missing a value for the field being sorted.
   *
   * @param value
   *   the `missing` value behaviour
   *   - [[Missing.First]]: treated as first
   *   - [[Missing.Last]]: treated as last
   * @return
   *   an instance of the [[zio.elasticsearch.query.sort.SortByField]] enriched with the `missing` parameter.
   */
  def missing(value: Missing): S
}
