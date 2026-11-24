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

private[elasticsearch] trait WithSubAgg[A <: WithSubAgg[A, S], S] {

  /**
   * Adds a sub-aggregation to the aggregation. Sub-aggregations are kept in the order in which they are added and must
   * have unique names.
   *
   * A [[zio.elasticsearch.aggregation.MultiBucketAggregation]] accepts any
   * [[zio.elasticsearch.aggregation.SingleElasticAggregation]], while a
   * [[zio.elasticsearch.aggregation.SingleBucketAggregation]] accepts any
   * [[zio.elasticsearch.aggregation.ElasticAggregation]], since parent pipeline aggregations require a multi-bucket
   * parent.
   *
   * @param subAgg
   *   the aggregation to add as sub-aggregation
   * @return
   *   a new instance of the [[zio.elasticsearch.aggregation.BucketAggregation]] with the given sub-aggregation.
   */
  def withSubAgg(subAgg: S): A
}
