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

import zio.elasticsearch.query._
import zio.schema.Schema

import ElasticPrimitive.ElasticPrimitive

object ElasticQuery {
  final def contains[S](field: Field[S, _], multiField: Option[String] = None, value: String): WildcardQuery[S] =
    Wildcard(
      field = field.toString ++ multiField.map("." ++ _).getOrElse(""),
      value = s"*$value*",
      boost = None,
      caseInsensitive = None
    )

  final def contains(field: String, value: String): WildcardQuery[Any] =
    Wildcard(field = field, value = s"*$value*", boost = None, caseInsensitive = None)

  final def exists[S](field: Field[S, _]): ExistsQuery[S] =
    Exists(field = field.toString)

  final def exists(field: String): ExistsQuery[Any] =
    Exists(field = field)

  final def filter[S: Schema](queries: ElasticQuery[S]*): BoolQuery[S] =
    Bool[S](filter = queries.toList, must = Nil, mustNot = Nil, should = Nil, boost = None)

  final def filter(queries: ElasticQuery[Any]*): BoolQuery[Any] =
    Bool[Any](filter = queries.toList, must = Nil, mustNot = Nil, should = Nil, boost = None)

  final def matchAll: MatchAllQuery =
    MatchAll(boost = None)

  final def matches[S, A: ElasticPrimitive](
    field: Field[S, A],
    multiField: Option[String] = None,
    value: A
  ): MatchQuery[S] =
    Match(field = field.toString ++ multiField.map("." ++ _).getOrElse(""), value = value)

  def matches[A: ElasticPrimitive](field: String, value: A): MatchQuery[Any] =
    Match(field = field, value = value)

  final def must[S: Schema](queries: ElasticQuery[S]*): BoolQuery[S] =
    Bool[S](filter = Nil, must = queries.toList, mustNot = Nil, should = Nil, boost = None)

  final def must(queries: ElasticQuery[Any]*): BoolQuery[Any] =
    Bool[Any](filter = Nil, must = queries.toList, mustNot = Nil, should = Nil, boost = None)

  final def mustNot[S: Schema](queries: ElasticQuery[S]*): BoolQuery[S] =
    Bool[S](filter = Nil, must = Nil, mustNot = queries.toList, should = Nil, boost = None)

  final def mustNot(queries: ElasticQuery[Any]*): BoolQuery[Any] =
    Bool[Any](filter = Nil, must = Nil, mustNot = queries.toList, should = Nil, boost = None)

  final def nested[S, A](path: Field[S, Seq[A]], query: ElasticQuery[A]): NestedQuery[S] =
    Nested(path = path.toString, query = query, scoreMode = None, ignoreUnmapped = None, innerHitsConfig = None)

  final def nested(path: String, query: ElasticQuery[_]): NestedQuery[Any] =
    Nested(path = path, query = query, scoreMode = None, ignoreUnmapped = None, innerHitsConfig = None)

  final def range[S, A](
    field: Field[S, A],
    multiField: Option[String] = None
  ): RangeQuery[S, A, Unbounded.type, Unbounded.type] =
    Range.empty(field.toString ++ multiField.map("." ++ _).getOrElse(""))

  final def range(field: String): RangeQuery[Any, Any, Unbounded.type, Unbounded.type] =
    Range.empty[Any, Any](field = field)

  final def should[S: Schema](queries: ElasticQuery[S]*): BoolQuery[S] =
    Bool[S](filter = Nil, must = Nil, mustNot = Nil, should = queries.toList, boost = None)

  final def should(queries: ElasticQuery[Any]*): BoolQuery[Any] =
    Bool[Any](filter = Nil, must = Nil, mustNot = Nil, should = queries.toList, boost = None)

  final def startsWith[S](field: Field[S, _], multiField: Option[String] = None, value: String): WildcardQuery[S] =
    Wildcard(
      field = field.toString ++ multiField.map("." ++ _).getOrElse(""),
      value = s"$value*",
      boost = None,
      caseInsensitive = None
    )

  final def startsWith(field: String, value: String): WildcardQuery[Any] =
    Wildcard(field = field, value = s"$value*", boost = None, caseInsensitive = None)

  final def term[S, A: ElasticPrimitive](
    field: Field[S, A],
    multiField: Option[String] = None,
    value: A
  ): TermQuery[S] =
    Term(
      field = field.toString ++ multiField.map("." ++ _).getOrElse(""),
      value = value,
      boost = None,
      caseInsensitive = None
    )

  final def term[A: ElasticPrimitive](field: String, value: A): Term[Any, A] =
    Term(field = field, value = value, boost = None, caseInsensitive = None)

  final def wildcard[S](field: Field[S, _], multiField: Option[String] = None, value: String): Wildcard[S] =
    Wildcard(
      field = field.toString ++ multiField.map("." ++ _).getOrElse(""),
      value = value,
      boost = None,
      caseInsensitive = None
    )

  final def wildcard(field: String, value: String): Wildcard[Any] =
    Wildcard(field = field, value = value, boost = None, caseInsensitive = None)
}
