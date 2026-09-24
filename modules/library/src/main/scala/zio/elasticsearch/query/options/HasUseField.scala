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

import zio.elasticsearch.Field
import zio.schema.Schema

private[elasticsearch] trait HasUseField[Q[-_], -S] {

  /**
   * Sets the type-safe `use_field` parameter for this [[zio.elasticsearch.query.IntervalRule]].
   *
   * @param field
   *   the type-safe field to use from the document definition
   * @tparam S1
   *   a subtype of the base document type `S` representing the schema that contains the selected field
   * @return
   *   a new instance of the query with the `use_field` value set.
   */
  def useField[S1 <: S: Schema](field: Field[S1, _]): Q[S1]

  /**
   * Sets the `use_field` parameter using a plain string.
   *
   * @param field
   *   the name of the field as a string
   * @return
   *   a new instance of the query with the `use_field` value set.
   */
  def useField(field: String): Q[S]
}
