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

import zio.elasticsearch.query.sort.NumericType

private[elasticsearch] trait HasNumericType[S <: HasNumericType[S]] {

  /**
   * Sets the `numeric type` that should be used for sorting the field. With `numeric type` it is possible to cast the
   * values from one type to another.
   *
   * @param value
   *   the [[NumericType]] that should be used for sorting the field
   *   - [[NumericType.Date]]: converts values to Date
   *   - [[NumericType.DateNanos]]: converts values to DateNanos
   *   - [[NumericType.Double]]: converts values to Double
   *   - [[NumericType.Long]]: converts values to Long
   * @return
   *   an instance of the [[zio.elasticsearch.query.sort.SortByField]] enriched with the `numeric type` parameter.
   */
  def numericType(value: NumericType): S
}
