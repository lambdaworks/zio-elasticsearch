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

private[elasticsearch] trait WithFormat[S <: WithFormat[S]] {

  /**
   * Sets the date format for the [[zio.elasticsearch.query.sort.SortByField]]. This method is only applicable to fields
   * of type `date`.
   *
   * @param value
   *   the `date` format to set
   * @return
   *   an instance of the [[zio.elasticsearch.query.sort.SortByField]] enriched with the `format` parameter.
   */
  def format(value: String): S
}
