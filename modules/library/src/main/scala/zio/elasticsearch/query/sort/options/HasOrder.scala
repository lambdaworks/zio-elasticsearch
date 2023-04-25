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

import zio.elasticsearch.query.sort.SortOrder

private[elasticsearch] trait HasOrder[S <: HasOrder[S]] {

  /**
   * Sets the `sort order` of the field.
   *
   * @param value
   *   the [[SortOrder]] of the field
   *   - [[SortOrder.Asc]]: sets ascending sorting order
   *   - [[SortOrder.Desc]]: sets descending sorting order
   * @return
   *   an instance of the [[zio.elasticsearch.query.sort.Sort]] enriched with the `sort order` parameter.
   */
  def order(value: SortOrder): S
}
