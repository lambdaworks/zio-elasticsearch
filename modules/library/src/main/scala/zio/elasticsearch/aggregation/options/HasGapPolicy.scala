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

import zio.elasticsearch.aggregation.GapPolicy

private[elasticsearch] trait HasGapPolicy[A <: HasGapPolicy[A]] {

  /**
   * Sets the `gap_policy` parameter for the [[zio.elasticsearch.aggregation.PipelineAggregation]]. The gap policy
   * defines how the aggregation handles buckets with missing or empty values.
   *
   * @param value
   *   the [[zio.elasticsearch.aggregation.GapPolicy]] to apply
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.PipelineAggregation]] enriched with the `gap_policy`
   *   parameter.
   */
  def gapPolicy(value: GapPolicy): A
}
