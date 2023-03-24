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

import zio.elasticsearch.query.Query._
import zio.elasticsearch.query._

import ElasticPrimitive.ElasticPrimitive

object ElasticQuery {
  def contains[S](field: Field[S, _], value: String): WildcardQuery[S] =
    Wildcard(field = field.toString, value = s"*$value*", boost = None, caseInsensitive = None)

  def contains(field: String, value: String): WildcardQuery[Any] =
    Wildcard(field = field, value = s"*$value*", boost = None, caseInsensitive = None)

  def exists[S](field: Field[S, _]): ExistsQuery[S] =
    Exists(field = field.toString)

  def exists(field: String): ExistsQuery[Any] =
    Exists(field = field)

  def filter[S](queries: Query[S]*): BoolQuery[S] =
    Bool[S](filter = queries.toList, must = Nil, mustNot = Nil, should = Nil, boost = None)

  def matchAll: MatchAllQuery =
    MatchAll(boost = None)

  def matches[S, A: ElasticPrimitive](field: Field[S, A], multiField: Option[String] = None, value: A): MatchQuery[S] =
    Match(field = field.toString ++ multiField.map("." ++ _).getOrElse(""), value = value)

  def matches[A: ElasticPrimitive](field: String, value: A): MatchQuery[Any] =
    Match(field = field, value = value)

  def must[S](queries: Query[S]*): BoolQuery[S] =
    Bool[S](filter = Nil, must = queries.toList, mustNot = Nil, should = Nil, boost = None)

  def mustNot[S](queries: Query[S]*): BoolQuery[S] =
    Bool[S](filter = Nil, must = Nil, mustNot = queries.toList, should = Nil, boost = None)

  def nested[S, A](path: Field[S, Seq[A]], query: Query[A]): NestedQuery[S] =
    Nested(path = path.toString, query = query, scoreMode = None, ignoreUnmapped = None)

  def nested(path: String, query: Query[_]): NestedQuery[Any] =
    Nested(path = path, query = query, scoreMode = None, ignoreUnmapped = None)

  def range[S, A](
    field: Field[S, A],
    multiField: Option[String] = None
  ): RangeQuery[S, A, Unbounded.type, Unbounded.type] =
    Range.empty(field.toString ++ multiField.map("." ++ _).getOrElse(""))

  def range(field: String): RangeQuery[Any, Any, Unbounded.type, Unbounded.type] =
    Range.empty[Any, Any](field = field)

  def should[S](queries: Query[S]*): BoolQuery[S] =
    Bool[S](filter = Nil, must = Nil, mustNot = Nil, should = queries.toList, boost = None)

  def startsWith[S](field: Field[S, _], value: String): WildcardQuery[S] =
    Wildcard(field = field.toString, value = s"$value*", boost = None, caseInsensitive = None)

  def startsWith(field: String, value: String): WildcardQuery[Any] =
    Wildcard(field = field, value = s"$value*", boost = None, caseInsensitive = None)

  def term[S, A: ElasticPrimitive](field: Field[S, A], multiField: Option[String] = None, value: A): TermQuery[S] =
    Term(
      field = field.toString ++ multiField.map("." ++ _).getOrElse(""),
      value = value,
      boost = None,
      caseInsensitive = None
    )

  def term[A: ElasticPrimitive](field: String, value: A): Term[Any, A] =
    Term(field = field, value = value, boost = None, caseInsensitive = None)

  def wildcard[S](field: Field[S, _], value: String): Wildcard[S] =
    Wildcard(field = field.toString, value = value, boost = None, caseInsensitive = None)

  def wildcard(field: String, value: String): Wildcard[Any] =
    Wildcard(field = field, value = value, boost = None, caseInsensitive = None)
}
