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

private[elasticsearch] trait HasKeyed[A <: HasKeyed[A]] {

  /**
   * Sets the `keyed` parameter to `true` for the [[zio.elasticsearch.aggregation.SingleElasticAggregation]]. Buckets
   * are then returned as an object in which each bucket is associated with a unique string key, instead of an array.
   *
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.SingleElasticAggregation]] enriched with the `keyed`
   *   parameter.
   */
  def keyed: A
}
