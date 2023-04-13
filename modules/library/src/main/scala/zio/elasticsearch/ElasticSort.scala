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
   * Specifies a type-safe [[zio.elasticsearch.query.sort.SortByField]].
   *
   * @param field
   *   a type-safe field which is used for sorting
   * @tparam S
   *   document on which fields query is executed
   * @return
   *   returns specified SortByField.
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

  <<<<<<< HEAD
  final def sortBy(field: String): SortByField =
    =======

  /**
   * Specifies [[zio.elasticsearch.query.sort.SortByField]].
   *
   * @param field
   *   field which is used for sorting
   * @return
   *   returns specified SortByField.
   */
  def sortBy(field: String): SortByField =
    >>>>>>> df390a8 (Backup)
  SortByFieldOptions(
    field = field,
    format = None,
    mode = None,
    missing = None,
    numericType = None,
    order = None,
    unmappedType = None
  )

  <<<<<<< HEAD
  final def sortBy(script: Script, sourceType: SourceType): SortByScript =
    =======

  /**
   * Specifies [[zio.elasticsearch.query.sort.SortByScript]].
   *
   * @param script
   *   specifies [[zio.elasticsearch.script.Script]] that represents sort in this case
   * @param sourceType
   *   type of SortByScript source - [[zio.elasticsearch.query.sort.SourceType]]
   * @return
   */
  def sortBy(script: Script, sourceType: SourceType): SortByScript =
    >>>>>>> df390a8 (Backup)
  SortByScriptOptions(
    script = script,
    sourceType = sourceType,
    mode = None,
    order = None
  )
}
