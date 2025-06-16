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

private[elasticsearch] trait HasAnalyzer[Q <: HasAnalyzer[Q]] {

  /**
   * Sets the `analyzer` parameter for this [[zio.elasticsearch.query.ElasticIntervalQuery]] query.
   *
   * @param value
   *   the name of the analyzer to use
   * @return
   *   a new instance of the query with the `analyzer` value set
   */
  def analyzer(value: String): Q
}
