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

import zio.elasticsearch.ElasticPrimitive.ElasticPrimitive

private[elasticsearch] trait HasMeta[A <: HasMeta[A]] {

  /**
   * Adds an entry to the `meta` object of the [[zio.elasticsearch.aggregation.SingleElasticAggregation]]. Elasticsearch
   * returns the `meta` object together with the aggregation result. Setting the same key again replaces its value.
   *
   * @param key
   *   the key of the metadata entry
   * @param value
   *   the value of the metadata entry
   * @tparam B
   *   the type of the value, for which an implicit [[zio.elasticsearch.ElasticPrimitive.ElasticPrimitive]] is in scope
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.SingleElasticAggregation]] enriched with the `meta` entry.
   */
  def meta[B: ElasticPrimitive](key: String, value: B): A
}
