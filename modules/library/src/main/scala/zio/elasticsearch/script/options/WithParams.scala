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

package zio.elasticsearch.script.options

private[elasticsearch] trait WithParams[S <: WithParams[S]] {

  /**
   * Adds additional parameters to a script field.
   *
   * @param values
   *   a sequence of pairs of parameter names and their values to be added to the script field
   * @return
   *   an instance of the [[zio.elasticsearch.script.Script]] enriched with the `params` parameter.
   */
  def withParams(values: (String, Any)*): S
}
