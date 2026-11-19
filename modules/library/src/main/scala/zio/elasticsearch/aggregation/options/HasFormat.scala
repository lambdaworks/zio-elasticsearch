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

package zio.elasticsearch.aggregation.options

private[elasticsearch] trait HasFormat[A <: HasFormat[A]] {

  /**
   * Sets the `format` parameter for the [[zio.elasticsearch.aggregation.SingleElasticAggregation]]. The `format`
   * parameter defines how values are formatted in the `*_as_string` properties of the response.
   *
   * @param value
   *   the format pattern (e.g. a decimal pattern such as `0.00` or a date pattern such as `yyyy-MM-dd`)
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.SingleElasticAggregation]] enriched with the `format`
   *   parameter.
   */
  def format(value: String): A
}
