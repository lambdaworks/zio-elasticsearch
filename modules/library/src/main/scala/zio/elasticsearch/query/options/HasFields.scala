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

import zio.Chunk
import zio.elasticsearch.Field
import zio.schema.Schema
private[elasticsearch] trait HasFields[Q[_], S] {

  /**
   * Sets the `fields` parameter for this [[zio.elasticsearch.query.ElasticQuery]]. The `fields` parameter is array of
   * fields that will be searched.
   *
   * @param field
   *   the first field to include in the `fields` parameter
   * @param fields
   *   an array of fields to set `fields` parameter to
   * @return
   *   an instance of the [[zio.elasticsearch.query.ElasticQuery]] enriched with the `fields` parameter.
   */
  def fields(field: String, fields: String*): Q[S]

  /**
   * Sets the type-safe `fields` parameter for this [[zio.elasticsearch.query.ElasticQuery]]. This version allows
   * specifying multiple fields of different types (e.g. String, Int, Boolean) in a type-safe way using their respective
   * definitions.
   *
   * @param fields
   *   a chunk of type-safe fields to search within. These fields may be of any supported scalar type (such as String,
   *   Int, Boolean, etc.), and must be part of the document schema `S1`.
   * @tparam S1
   *   a subtype of the base document type `S` representing the schema that contains the selected fields
   * @return
   *   an instance of the [[zio.elasticsearch.query.ElasticQuery]] enriched with the provided type-safe `fields`.
   */
  def fields[S1 <: S: Schema](fields: Chunk[Field[S1, _]]): Q[S1]

  /**
   * Sets the type-safe `fields` parameter for this [[zio.elasticsearch.query.ElasticQuery]]. The `fields` parameter is
   * array of type-safe fields that will be searched.
   *
   * @param field
   *   the first type-safe field to search within
   * @param fields
   *   an array of type-safe fields to set `fields` parameter to
   * @tparam S1
   *   a subtype of the base document type `S` representing the schema that contains the selected fields
   * @return
   *   an instance of the [[zio.elasticsearch.query.ElasticQuery]] enriched with the type-safe `fields` parameter.
   */
  def fields[S1 <: S: Schema](field: Field[S1, _], fields: Field[S1, _]*): Q[S1]
}
