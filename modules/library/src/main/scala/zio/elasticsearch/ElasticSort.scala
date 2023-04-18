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

import zio.elasticsearch.query.sort._
import zio.elasticsearch.script.Script

object ElasticSort {

  /**
   * Constructs a type-safe instance of [[SortByField]] using the specified parameters.
   *
   * @param field
   *   the [[Field]] object representing the type-safe field to sort by
   * @tparam S
   *   document by which field sort is performed
   * @return
   *   returns an instance of [[SortByField]] that represents sort by field operation to be performed.
   */
  final def sortBy[S](field: Field[S, _]): SortByField =
    SortByFieldOptions(
      field = field.toString,
      format = None,
      mode = None,
      missing = None,
      numericType = None,
      order = None,
      unmappedType = None
    )

  /**
   * Constructs an instance of [[SortByField]] using the specified parameters.
   *
   * @param field
   *   the field to sort by
   * @return
   *   returns an instance of [[SortByField]] that represents sort by field operation to be performed.
   */
  final def sortBy(field: String): SortByField =
    SortByFieldOptions(
      field = field,
      format = None,
      mode = None,
      missing = None,
      numericType = None,
      order = None,
      unmappedType = None
    )

  /**
   * Constructs an instance of [[SortByScript]] using the specified parameters.
   *
   * @param script
   *   a [[Script]] object containing sort logic
   * @param sourceType
   *   type of script source - [[SourceType]]
   * @return
   *   returns an instance of [[SortByScript]] that represents sort by script operation to be performed.
   */
  final def sortBy(script: Script, sourceType: SourceType): SortByScript =
    SortByScriptOptions(
      script = script,
      sourceType = sourceType,
      mode = None,
      order = None
    )
}
