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

private[elasticsearch] trait WithUnmappedType[S <: WithUnmappedType[S]] {

  /**
   * Sets the `unmapped type` which is used when the mapped field doesn't exist in the index.
   *
   * @param value
   *   the type to use when the mapped field doesn't exist in the index
   * @return
   *   an instance of the [[zio.elasticsearch.query.sort.SortByField]] enriched with the `unmapped type` parameter.
   */
  def unmappedType(value: String): S
}
