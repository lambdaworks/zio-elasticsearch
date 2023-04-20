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

package zio.elasticsearch.query

package object sort {
  private[elasticsearch] trait WithFormat[S <: WithFormat[S]] {

    /**
     * Sets the date format for the [[SortByField]]. This method is only applicable to fields of type `date`.
     *
     * @param value
     *   the `date` format to set
     * @return
     *   an instance of the [[SortByField]] enriched with the `format` parameter.
     */
    def format(value: String): S
  }

  private[elasticsearch] trait WithMode[S <: WithMode[S]] {

    /**
     * Sets the `mode` parameter for the [[Sort]]. The `mode` parameter controls how Elasticsearch selects a single
     * value from a set of sorted documents. The default `mode` is `Average`.
     *
     * @param value
     *   the [[SortMode]] used to pick a value among the sorted set of documents:
     *   - [[SortMode.Avg]]: uses the average of all values as sort value. Only applicable for number based array fields
     *   - [[SortMode.Max]]: picks the highest value
     *   - [[SortMode.Median]]: uses the median of all values as sort value. Only applicable for number based array
     *     fields
     *   - [[SortMode.Min]]: picks the lowest value
     *   - [[SortMode.Sum]]: uses the sum of all values as sort value. Only applicable for number based array fields
     * @return
     *   an instance of the [[Sort]] enriched with the `mode` parameter.
     */
    def mode(value: SortMode): S
  }

  private[elasticsearch] trait WithMissing[S <: WithMissing[S]] {

    /**
     * Sets the value to use when a document is missing a value for the field being sorted.
     *
     * @param value
     *   the `missing` value behaviour
     *   - [[Missing.First]]: treated as first
     *   - [[Missing.Last]]: treated as last
     * @return
     *   an instance of the [[SortByField]] enriched with the `missing` parameter.
     */
    def missing(value: Missing): S
  }

  private[elasticsearch] trait WithNumericType[S <: WithNumericType[S]] {

    /**
     * Sets the `numeric type` that should be used for sorting the field. With `numeric type` it is possible to cast the
     * values from one type to another.
     *
     * @param value
     *   the [[NumericType]] that should be used for sorting the field.
     *   - [[NumericType.Date]]: converts values do Date
     *   - [[NumericType.DateNanos]]: converts values do DateNanos
     *   - [[NumericType.Double]]: converts values do Double
     *   - [[NumericType.Long]]: converts values do Long
     * @return
     *   an instance of the [[SortByField]] enriched with the `numeric type` parameter.
     */
    def numericType(value: NumericType): S
  }

  private[elasticsearch] trait WithOrder[S <: WithOrder[S]] {

    /**
     * Sets the `sort order` of the field.
     *
     * @param value
     *   the [[SortOrder]] of the field
     *   - [[SortOrder.Asc]]: sets Ascending sorting order
     *   - [[SortOrder.Desc]]: sets Descending sorting order
     * @return
     *   an instance of the [[Sort]] enriched with the `sort order` parameter.
     */
    def order(value: SortOrder): S
  }

  private[elasticsearch] trait WithUnmappedType[S <: WithUnmappedType[S]] {

    /**
     * Sets the `unmapped type` which is used when the mapped field doesn't exist in the index.
     *
     * @param value
     *   the type to use when the mapped field doesn't exist in the index
     * @return
     *   an instance of the [[SortByField]] enriched with the `unmapped type` parameter.
     */
    def unmappedType(value: String): S
  }
}
