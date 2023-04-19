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

  /**
   * Constructs a type-safe instance of [[WildcardQuery]] using the specified parameters.
   *
   * @param field
   *   the [[Field]] object representing the type-safe field for which query is specified for
   * @param value
   *   text value that will be used for the query in the pattern that represents `contains`
   * @tparam S
   *   document for which field query is executed
   * @return
   *   an instance of [[WildcardQuery]] that represents the wildcard query to be performed.
   */
  final def contains[S](field: Field[S, _], value: String): WildcardQuery[S] =
    Wildcard(
      field = field.toString,
      value = s"*$value*",
      boost = None,
      caseInsensitive = None
    )

  /**
   * Constructs an instance of [[WildcardQuery]] using the specified parameters.
   *
   * @param field
   *   the field for which query is specified for
   * @param value
   *   text value that will be used for the query in the pattern that represents `contains`
   * @return
   *   an instance of [[WildcardQuery]] that represents the wildcard query to be performed.
   */
  final def contains(field: String, value: String): WildcardQuery[Any] =
    Wildcard(field = field, value = s"*$value*", boost = None, caseInsensitive = None)

  /**
   * Constructs a type-safe instance of [[ExistsQuery]] using the specified parameters.
   *
   * @param field
   *   the [[Field]] representing the type-safe field for which query is specified for
   * @tparam S
   *   document for which field query is executed
   * @return
   *   an instance of [[ExistsQuery]] that represents the exists query to be performed.
   */
  final def exists[S](field: Field[S, _]): ExistsQuery[S] =
    Exists(field = field.toString)

  /**
   * Constructs an instance of [[ExistsQuery]] using the specified parameters.
   *
   * @param field
   *   the field for which query is specified for
   * @return
   *   an instance of [[ExistsQuery]] that represents the exists query to be performed.
   */
  final def exists(field: String): ExistsQuery[Any] =
    Exists(field = field)

  /**
   * Constructs an instance of [[BoolQuery]] with queries that must satisfy the criteria using the specified parameters.
   *
   * @param queries
   *   a list of queries to add to `filter` inside of the `Bool` query
   * @tparam S
   *   document for which field query is executed. An implicit `Schema` instance must be in scope
   * @return
   *   an instance of [[BoolQuery]] that represents the bool query with queries that must satisfy the criteria.
   */
  final def filter[S: Schema](queries: ElasticQuery[S]*): BoolQuery[S] =
    Bool[S](filter = queries.toList, must = Nil, mustNot = Nil, should = Nil, boost = None)

  /**
   * Constructs an instance of [[BoolQuery]] with queries that must satisfy the criteria using the specified parameters.
   *
   * @param queries
   *   a list of queries to add to `filter` inside of the `Bool` query
   * @return
   *   an instance of [[BoolQuery]] that represents the bool query with queries that must satisfy the criteria.
   */
  final def filter(queries: ElasticQuery[Any]*): BoolQuery[Any] =
    Bool[Any](filter = queries.toList, must = Nil, mustNot = Nil, should = Nil, boost = None)

  /**
   * Constructs an instance of [[MatchAllQuery]].
   *
   * @return
   *   an instance of [[MatchAllQuery]] that represents the match all query to be performed.
   */
  final def matchAll: MatchAllQuery =
    MatchAll(boost = None)

  /**
   * Constructs a type-safe instance of [[MatchQuery]] using the specified parameters.
   *
   * @param field
   *   the [[Field]] object representing the type-safe field for which query is specified for
   * @param value
   *   the value to be matched, represented by an instance of type `A`
   * @tparam S
   *   document for which field query is executed
   * @tparam A
   *   the type of value to be matched. A JSON decoder must be in scope for this type
   * @return
   *   an instance of [[MatchQuery]] that represents the match query to be performed.
   */
  final def matches[S, A: ElasticPrimitive](field: Field[S, A], value: A): MatchQuery[S] =
    Match(field = field.toString, value = value)

  /**
   * Constructs an instance of [[MatchQuery]] using the specified parameters.
   *
   * @param field
   *   the field for which query is specified for
   * @param value
   *   the value to be matched, represented by an instance of type `A`
   * @tparam A
   *   the type of value to be matched. A JSON decoder must be in scope for this type
   * @return
   *   an instance of [[MatchQuery]] that represents the match query to be performed.
   */
  final def matches[A: ElasticPrimitive](field: String, value: A): MatchQuery[Any] =
    Match(field = field, value = value)

  /**
   * Constructs an instance of [[BoolQuery]] with queries that must satisfy the criteria using the specified parameters.
   *
   * @param queries
   *   a list of queries to add to `must` inside of the `Bool` query
   * @tparam S
   *   document for which field query is executed. An implicit `Schema` instance must be in scope
   * @return
   *   an instance of [[BoolQuery]] that represents the bool query with queries that must satisfy the criteria.
   */
  final def must[S: Schema](queries: ElasticQuery[S]*): BoolQuery[S] =
    Bool[S](filter = Nil, must = queries.toList, mustNot = Nil, should = Nil, boost = None)

  /**
   * Constructs an instance of [[BoolQuery]] with queries that must satisfy the criteria using the specified parameters.
   *
   * @param queries
   *   a list of queries to add to `must` inside of the `Bool` query
   * @return
   *   an instance of [[BoolQuery]] that represents the bool query with queries that must satisfy the criteria.
   */
  final def must(queries: ElasticQuery[Any]*): BoolQuery[Any] =
    Bool[Any](filter = Nil, must = queries.toList, mustNot = Nil, should = Nil, boost = None)

  /**
   * Constructs an instance of [[BoolQuery]] with queries that must not satisfy the criteria using the specified
   * parameters.
   *
   * @param queries
   *   a list of queries to add to `mustNot` inside of the `Bool` query
   * @tparam S
   *   document for which field query is executed. An implicit `Schema` instance must be in scope
   * @return
   *   an instance of [[BoolQuery]] that represents the bool query with queries that must not satisfy the criteria.
   */
  final def mustNot[S: Schema](queries: ElasticQuery[S]*): BoolQuery[S] =
    Bool[S](filter = Nil, must = Nil, mustNot = queries.toList, should = Nil, boost = None)

  /**
   * Constructs an instance of [[BoolQuery]] with queries that must not satisfy the criteria using the specified
   * parameters.
   *
   * @param queries
   *   a list of queries to add to `mustNot` inside of the `Bool` query
   * @return
   *   an instance of [[BoolQuery]] that represents the bool query with queries that must not satisfy the criteria.
   */
  final def mustNot(queries: ElasticQuery[Any]*): BoolQuery[Any] =
    Bool[Any](filter = Nil, must = Nil, mustNot = queries.toList, should = Nil, boost = None)

  /**
   * Constructs a type-safe instance of [[NestedQuery]] using the specified parameters.
   *
   * @param path
   *   the [[Field]] object representing the type-safe path to the field for which query is specified for
   * @param query
   *   the [[ElasticQuery]] object representing the query to execute on nested objects.
   * @tparam S
   *   document for which field query is executed
   * @tparam A
   *   the type of the value that will be used for the query
   * @return
   *   an instance of [[NestedQuery]] that represents the nested query to be performed.
   */
  final def nested[S, A](path: Field[S, Seq[A]], query: ElasticQuery[A]): NestedQuery[S] =
    Nested(path = path.toString, query = query, scoreMode = None, ignoreUnmapped = None, innerHitsField = None)

  /**
   * Constructs an instance of [[NestedQuery]] using the specified parameters.
   *
   * @param path
   *   the path to the field for which query is specified for
   * @param query
   *   the [[ElasticQuery]] object representing the query to execute on nested objects.
   * @return
   *   an instance of [[NestedQuery]] that represents the nested query to be performed.
   */
  final def nested(path: String, query: ElasticQuery[_]): NestedQuery[Any] =
    Nested(path = path, query = query, scoreMode = None, ignoreUnmapped = None, innerHitsField = None)

  /**
   * Constructs a type-safe instance of [[RangeQuery]] using the specified parameters.
   *
   * @param field
   *   the [[Field]] object representing the type-safe field for which query is specified for
   * @tparam S
   *   document for which field query is executed
   * @tparam A
   *   the type of the value that will be used for the query
   * @return
   *   an instance of [[RangeQuery]] that represents the range query to be performed.
   */
  final def range[S, A](field: Field[S, A]): RangeQuery[S, A, Unbounded.type, Unbounded.type] =
    Range.empty(field.toString)

  /**
   * Constructs an instance of [[RangeQuery]] using the specified parameters.
   *
   * @param field
   *   the field for which query is specified for
   * @return
   *   an instance of [[RangeQuery]] that represents the range query to be performed.
   */
  final def range(field: String): RangeQuery[Any, Any, Unbounded.type, Unbounded.type] =
    Range.empty[Any, Any](field = field)

  /**
   * Constructs an instance of [[BoolQuery]] with queries that should satisfy the criteria using the specified
   * parameters.
   *
   * @param queries
   *   a list of queries to add to `should` inside of the `Bool` query
   * @tparam S
   *   document for which field query is executed. An implicit `Schema` instance must be in scope
   * @return
   *   an instance of [[BoolQuery]] that represents the bool query with queries that should satisfy the criteria.
   */
  final def should[S: Schema](queries: ElasticQuery[S]*): BoolQuery[S] =
    Bool[S](filter = Nil, must = Nil, mustNot = Nil, should = queries.toList, boost = None)

  /**
   * Constructs an instance of [[BoolQuery]] with queries that should satisfy the criteria using the specified
   * parameters.
   *
   * @param queries
   *   a list of queries to add to `should` inside of the `Bool` query
   * @return
   *   an instance of [[BoolQuery]] that represents the bool query with queries that should satisfy the criteria.
   */
  final def should(queries: ElasticQuery[Any]*): BoolQuery[Any] =
    Bool[Any](filter = Nil, must = Nil, mustNot = Nil, should = queries.toList, boost = None)

  /**
   * Constructs a type-safe instance of [[WildcardQuery]] using the specified parameters.
   *
   * @param field
   *   the [[Field]] object representing the type-safe field for which query is specified for
   * @param value
   *   text value that will be used for the query
   * @tparam S
   *   document for which field query is executed in the pattern that represents `startsWith`
   * @return
   *   an instance of [[WildcardQuery]] that represents the wildcard query to be performed.
   */
  final def startsWith[S](field: Field[S, _], value: String): WildcardQuery[S] =
    Wildcard(
      field = field.toString,
      value = s"$value*",
      boost = None,
      caseInsensitive = None
    )

  /**
   * Constructs an instance of [[WildcardQuery]] using the specified parameters.
   *
   * @param field
   *   the field for which query is specified for
   * @param value
   *   text value that will be used for the query in the pattern that represents `startsWith`
   * @return
   *   an instance of [[WildcardQuery]] that represents the wildcard query to be performed.
   */
  final def startsWith(field: String, value: String): WildcardQuery[Any] =
    Wildcard(field = field, value = s"$value*", boost = None, caseInsensitive = None)

  /**
   * Constructs a type-safe instance of [[TermQuery]] using the specified parameters.
   *
   * @param field
   *   the [[Field]] object representing the type-safe field for which query is specified for
   * @param value
   *   the value that will be used for the query, represented by an instance of type `A`
   * @tparam S
   *   document for which field query is executed
   * @tparam A
   *   the type of value that will be used for the query. A JSON decoder must be in scope for this type
   * @return
   *   an instance of [[TermQuery]] that represents the term query to be performed.
   */
  final def term[S, A: ElasticPrimitive](field: Field[S, A], value: A): TermQuery[S] =
    Term(
      field = field.toString,
      value = value,
      boost = None,
      caseInsensitive = None
    )

  /**
   * Constructs a type-safe instance of [[TermQuery]] using the specified parameters.
   *
   * @param field
   *   the field for which query is specified for
   * @param value
   *   the value that will be used for the query, represented by an instance of type `A`
   * @tparam A
   *   the type of value that will be used for the query. A JSON decoder must be in scope for this type
   * @return
   *   an instance of [[TermQuery]] that represents the term query to be performed.
   */
  final def term[A: ElasticPrimitive](field: String, value: A): Term[Any, A] =
    Term(field = field, value = value, boost = None, caseInsensitive = None)

  /**
   * Constructs a type-safe instance of [[WildcardQuery]] using the specified parameters.
   *
   * @param field
   *   the [[Field]] object representing the type-safe field for which query is specified for
   * @param value
   *   text value that will be used for the query
   * @tparam S
   *   document for which field query is executed
   * @return
   *   an instance of [[WildcardQuery]] that represents the wildcard query to be performed.
   */
  final def wildcard[S](field: Field[S, _], value: String): Wildcard[S] =
    Wildcard(
      field = field.toString,
      value = value,
      boost = None,
      caseInsensitive = None
    )

  /**
   * Constructs an instance of [[WildcardQuery]] using the specified parameters.
   *
   * @param field
   *   the field for which query is specified for
   * @param value
   *   text value that will be used for the query
   * @return
   *   an instance of [[WildcardQuery]] that represents the wildcard query to be performed.
   */
  final def wildcard(field: String, value: String): Wildcard[Any] =
    Wildcard(field = field, value = value, boost = None, caseInsensitive = None)
}
