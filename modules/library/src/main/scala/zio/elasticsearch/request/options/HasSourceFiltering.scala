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

package zio.elasticsearch.request.options

import zio.schema.Schema

private[elasticsearch] trait HasSourceFiltering[R <: HasSourceFiltering[R]] {

  /**
   * Specifies one or more fields to be excluded in the response of a [[zio.elasticsearch.ElasticRequest.SearchRequest]]
   * or a [[zio.elasticsearch.ElasticRequest.SearchAndAggregateRequest]].
   *
   * @param field
   *   a field to be excluded
   * @param fields
   *   fields to be excluded
   * @return
   *   an instance of a [[zio.elasticsearch.ElasticRequest.SearchRequest]] or a
   *   [[zio.elasticsearch.ElasticRequest.SearchAndAggregateRequest]] with specified fields to be excluded.
   */
  def excludes(field: String, fields: String*): R

  /**
   * Specifies one or more fields to be included in the response of a [[zio.elasticsearch.ElasticRequest.SearchRequest]]
   * or a [[zio.elasticsearch.ElasticRequest.SearchAndAggregateRequest]].
   *
   * @param field
   *   a field to be included
   * @param fields
   *   fields to be included
   * @return
   *   an instance of a [[zio.elasticsearch.ElasticRequest.SearchRequest]] or a
   *   [[zio.elasticsearch.ElasticRequest.SearchAndAggregateRequest]] with specified fields to be included.
   */
  def includes(field: String, fields: String*): R

  /**
   * Specifies fields to be included in the response of a [[zio.elasticsearch.ElasticRequest.SearchRequest]] or a
   * [[zio.elasticsearch.ElasticRequest.SearchAndAggregateRequest]] based on the schema of a case class.
   *
   * @tparam A
   *   a case class whose fields will be included in the response
   * @param schema
   *   a record schema of [[A]]
   * @return
   *   an instance of a [[zio.elasticsearch.ElasticRequest.SearchRequest]] or a
   *   [[zio.elasticsearch.ElasticRequest.SearchAndAggregateRequest]] with specified fields to be excluded.
   */
  def includes[A](implicit schema: Schema.Record[A]): R

  protected final def getFieldNames(schema: Schema.Record[_]): List[String] = {
    def extractInnerSchema(schema: Schema[_]): Schema[_] =
      Schema.force(schema) match {
        case schema: Schema.Sequence[_, _, _] => Schema.force(schema.elementSchema)
        case schema                           => schema
      }

    def loop(schema: Schema.Record[_], prefix: Option[String]): List[String] =
      schema.fields.toList.flatMap { field =>
        extractInnerSchema(field.schema) match {
          case schema: Schema.Record[_] => loop(schema, prefix.map(_ + "." + field.name).orElse(Some(field.name)))
          case _                        => List(prefix.fold[String](field.name)(_ + "." + field.name))
        }
      }

    loop(schema, None)
  }
}
