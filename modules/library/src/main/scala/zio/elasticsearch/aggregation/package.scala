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

package zio.elasticsearch

package object aggregation {
  private[elasticsearch] trait WithSubAgg[A <: WithSubAgg[A]] {

    /**
     * Adds a sub-aggregation to the aggregation.
     *
     * @param subAgg
     *   the [[SingleElasticAggregation]] to add as sub-aggregation
     * @return
     *   returns a new instance of the [[ElasticAggregation]] with the given sub-aggregation.
     */
    def withSubAgg(subAgg: SingleElasticAggregation): A
  }

  private[elasticsearch] trait WithAgg {

    /**
     * Adds a new aggregation to the list of aggregations represented as [[MultipleAggregations]].
     *
     * @param agg
     *   the [[SingleElasticAggregation]] to add
     * @return
     *   returns a new instance of [[MultipleAggregations]] with the specified aggregation added to its list of
     *   aggregations.
     */
    def withAgg(agg: SingleElasticAggregation): MultipleAggregations
  }
}
