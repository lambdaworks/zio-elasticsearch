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

import zio.elasticsearch.query.ScoreMode

private[elasticsearch] trait HasScoreMode[Q <: HasScoreMode[Q]] {

  /**
   * Sets the [[ScoreMode]] for the [[zio.elasticsearch.query.NestedQuery]]. The [[zio.elasticsearch.query.ScoreMode]]
   * specifies how scores of matching documents are combined for the [[zio.elasticsearch.query.NestedQuery]].
   *
   * @param scoreMode
   *   the [[ScoreMode]] to use for the [[zio.elasticsearch.query.NestedQuery]]
   *   - [[ScoreMode.Avg]]: uses the mean relevance score of all matching child objects
   *   - [[ScoreMode.Max]]: uses the highest relevance score of all matching child objects
   *   - [[ScoreMode.Min]]: uses the lowest relevance score of all matching child objects
   *   - [[ScoreMode.None]]: ignores relevance scores of matching child objects and uses 0 as a score
   *   - [[ScoreMode.Sum]]: adds together the relevance scores of all matching child objects
   * @return
   *   a new instance of the [[zio.elasticsearch.query.ElasticQuery]] with the specified
   *   [[zio.elasticsearch.query.ScoreMode]].
   */
  def scoreMode(scoreMode: ScoreMode): Q
}
