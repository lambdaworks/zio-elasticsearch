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

import zio.elasticsearch.query.IntervalFilter

private[elasticsearch] trait HasIntervalFilter[Q[-_], -S] {

  /**
   * Sets the `filter` parameter for this [[zio.elasticsearch.query.IntervalRule]], restricting matches to those that
   * also satisfy the given [[zio.elasticsearch.query.IntervalFilter]].
   *
   * @param f
   *   the interval filter to apply
   * @tparam S1
   *   a subtype of the base document type `S` for which the filter is defined
   * @return
   *   a new instance of the interval rule with the `filter` value set.
   */
  def filter[S1 <: S](f: IntervalFilter[S1]): Q[S1]
}
