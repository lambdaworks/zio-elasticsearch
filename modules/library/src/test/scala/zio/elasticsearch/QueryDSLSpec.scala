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

import zio.Scope
import zio.elasticsearch.ElasticQuery._
import zio.elasticsearch.ElasticRequest.Bulk
import zio.elasticsearch.domain._
import zio.elasticsearch.query._
import zio.elasticsearch.utils._
import zio.prelude.Validation
import zio.test.Assertion.equalTo
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assert}

object QueryDSLSpec extends ZIOSpecDefault {
  def spec: Spec[Environment with TestEnvironment with Scope, Any] =
    suite("Query DSL")(
      suite("creating ElasticQuery")(
        test("successfully create Match query using `matches` method") {
          val queryString = matches(field = "day_of_week", value = "Monday")
          val queryBool   = matches(field = "day_of_week", value = true)
          val queryLong   = matches(field = "day_of_week", value = 1L)

          assert(queryString)(equalTo(Match[Any, String](field = "day_of_week", value = "Monday", boost = None))) &&
          assert(queryBool)(equalTo(Match[Any, Boolean](field = "day_of_week", value = true, boost = None))) &&
          assert(queryLong)(equalTo(Match[Any, Long](field = "day_of_week", value = 1, boost = None)))
        },
        test("successfully create type-safe Match query using `matches` method") {
          val queryString = matches(field = TestSubDocument.stringField, value = "StringField")
          val queryInt    = matches(field = TestSubDocument.intField, value = 39)

          assert(queryString)(
            equalTo(Match[TestSubDocument, String](field = "stringField", value = "StringField", boost = None))
          ) &&
          assert(queryInt)(equalTo(Match[TestSubDocument, Int](field = "intField", value = 39, boost = None)))
        },
        test("successfully create type-safe Match query with suffix using `matches` method") {
          val query = matches(field = TestSubDocument.stringField.keyword, value = "StringField")

          assert(query)(
            equalTo(Match[TestSubDocument, String](field = "stringField.keyword", value = "StringField", boost = None))
          )
        },
        test("successfully create `Filter` query from two Match queries") {
          val query = filter(
            matches(field = TestDocument.stringField, value = "StringField"),
            matches(field = "customer_gender", value = "MALE")
          )

          assert(query)(
            equalTo(
              Bool[TestDocument](
                filter = List(
                  Match(field = "stringField", value = "StringField", boost = None),
                  Match(field = "customer_gender", value = "MALE", boost = None)
                ),
                must = Nil,
                mustNot = Nil,
                should = Nil,
                boost = None,
                minimumShouldMatch = None
              )
            )
          )
        },
        test("successfully create `Filter` query with boost from two Match queries") {
          val query = filter(
            matches(field = TestDocument.stringField, value = "StringField"),
            matches(field = "customer_gender", value = "MALE")
          ).boost(1.0)

          assert(query)(
            equalTo(
              Bool[TestDocument](
                filter = List(
                  Match(field = "stringField", value = "StringField", boost = None),
                  Match(field = "customer_gender", value = "MALE", boost = None)
                ),
                must = Nil,
                mustNot = Nil,
                should = Nil,
                boost = Some(1.0),
                minimumShouldMatch = None
              )
            )
          )
        },
        test("successfully create `Must` query from two Match queries") {
          val query =
            must(
              matches(field = TestDocument.stringField, value = "StringField"),
              matches(field = "customer_gender", value = "MALE")
            )

          assert(query)(
            equalTo(
              Bool[TestDocument](
                filter = Nil,
                must = List(
                  Match(field = "stringField", value = "StringField", boost = None),
                  Match(field = "customer_gender", value = "MALE", boost = None)
                ),
                mustNot = Nil,
                should = Nil,
                boost = None,
                minimumShouldMatch = None
              )
            )
          )
        },
        test("successfully create `MustNot` query from two Match queries") {
          val query =
            mustNot(
              matches(field = TestDocument.stringField, value = "StringField"),
              matches(field = "customer_gender", value = "MALE")
            )

          assert(query)(
            equalTo(
              Bool[TestDocument](
                filter = Nil,
                must = Nil,
                mustNot = List(
                  Match(field = "stringField", value = "StringField", boost = None),
                  Match(field = "customer_gender", value = "MALE", boost = None)
                ),
                should = Nil,
                boost = None,
                minimumShouldMatch = None
              )
            )
          )
        },
        test("successfully create `Should` query from two Match queries") {
          val query = should(
            matches(field = TestDocument.stringField, value = "StringField"),
            matches(field = "customer_gender", value = "MALE")
          )

          assert(query)(
            equalTo(
              Bool[TestDocument](
                filter = Nil,
                must = Nil,
                mustNot = Nil,
                should = List(
                  Match(field = "stringField", value = "StringField", boost = None),
                  Match(field = "customer_gender", value = "MALE", boost = None)
                ),
                boost = None,
                minimumShouldMatch = None
              )
            )
          )
        },
        test("successfully create `Filter/Must/MustNot/Should` mixed query with Filter containing two Match queries") {
          val query = filter(
            matches(field = TestDocument.stringField, value = "StringField"),
            matches(field = "customer_gender", value = "MALE")
          )
            .must(matches(field = "customer_age", value = 23))
            .mustNot(matches(field = TestDocument.intField, value = 17))
            .should(matches(field = "customer_id", value = 1))

          assert(query)(
            equalTo(
              Bool[TestDocument](
                filter = List(
                  Match(field = "stringField", value = "StringField", boost = None),
                  Match(field = "customer_gender", value = "MALE", boost = None)
                ),
                must = List(Match(field = "customer_age", value = 23, boost = None)),
                mustNot = List(Match(field = "intField", value = 17, boost = None)),
                should = List(Match(field = "customer_id", value = 1, boost = None)),
                boost = None,
                minimumShouldMatch = None
              )
            )
          )
        },
        test("successfully create `Filter/Must/MustNot/Should` mixed query with Must containing two Match queries") {
          val query = filter(matches(field = TestDocument.intField, value = 1))
            .must(
              matches(field = TestDocument.stringField, value = "StringField1"),
              matches(field = TestDocument.stringField, value = "StringField2")
            )
            .mustNot(matches(field = TestDocument.intField, value = 17))
            .should(matches(field = TestDocument.doubleField, value = 23.0))

          assert(query)(
            equalTo(
              Bool[TestDocument](
                filter = List(Match(field = "intField", value = 1, boost = None)),
                must = List(
                  Match(field = "stringField", value = "StringField1", boost = None),
                  Match(field = "stringField", value = "StringField2", boost = None)
                ),
                mustNot = List(Match(field = "intField", value = 17, boost = None)),
                should = List(Match(field = "doubleField", value = 23.0, boost = None)),
                boost = None,
                minimumShouldMatch = None
              )
            )
          )
        },
        test("successfully create `Filter/Must/MustNot/Should` mixed query with MustNot containing two Match queries") {
          val query = filter(matches(field = TestDocument.stringField, value = "StringField"))
            .must(matches(field = TestDocument.intField, value = 17))
            .mustNot(
              matches(field = "day_of_week", value = "Monday"),
              matches(field = "customer_gender", value = "MALE")
            )
            .should(matches(field = TestDocument.intField, value = 23))

          assert(query)(
            equalTo(
              Bool[TestDocument](
                filter = List(Match(field = "stringField", value = "StringField", boost = None)),
                must = List(Match(field = "intField", value = 17, boost = None)),
                mustNot = List(
                  Match(field = "day_of_week", value = "Monday", boost = None),
                  Match(field = "customer_gender", value = "MALE", boost = None)
                ),
                should = List(Match(field = "intField", value = 23, boost = None)),
                boost = None,
                minimumShouldMatch = None
              )
            )
          )
        },
        test(
          "successfully create `Filter/Must/MustNot/Should` mixed query with Should containing two Match queries and `minimumShouldMatch`"
        ) {
          val query = filter(matches(field = TestDocument.stringField, value = "StringField"))
            .must(matches(field = TestDocument.intField, value = 23))
            .mustNot(matches(field = "day_of_month", value = 17))
            .should(
              matches(field = "day_of_week", value = "Monday"),
              matches(field = "customer_gender", value = "MALE")
            )
            .minimumShouldMatch(2)

          assert(query)(
            equalTo(
              Bool[TestDocument](
                filter = List(Match(field = "stringField", value = "StringField", boost = None)),
                must = List(Match(field = "intField", value = 23, boost = None)),
                mustNot = List(Match(field = "day_of_month", value = 17, boost = None)),
                should = List(
                  Match(field = "day_of_week", value = "Monday", boost = None),
                  Match(field = "customer_gender", value = "MALE", boost = None)
                ),
                boost = None,
                minimumShouldMatch = Some(2)
              )
            )
          )
        },
        test("successfully create `Filter/Must/MustNot/Should` mixed query with boost") {
          val query = filter(matches(field = TestDocument.intField, value = 1))
            .must(matches(field = TestDocument.doubleField, value = 23.0))
            .mustNot(matches(field = TestDocument.intField, value = 17))
            .should(matches(field = TestDocument.stringField, value = "StringField"))
            .boost(1.0)

          assert(query)(
            equalTo(
              Bool[TestDocument](
                filter = List(Match(field = "intField", value = 1, boost = None)),
                must = List(Match(field = "doubleField", value = 23.0, boost = None)),
                mustNot = List(Match(field = "intField", value = 17, boost = None)),
                should = List(Match(field = "stringField", value = "StringField", boost = None)),
                boost = Some(1.0),
                minimumShouldMatch = None
              )
            )
          )
        },
        test("successfully create Exists Query") {
          val query = exists(field = "day_of_week")

          assert(query)(equalTo(Exists[Any](field = "day_of_week")))
        },
        test("successfully create Exists Query with accessor") {
          val query = exists(field = TestSubDocument.stringField)

          assert(query)(equalTo(Exists[TestSubDocument](field = "stringField")))
        },
        test("successfully create MatchAll Query") {
          val query = matchAll

          assert(query)(equalTo(MatchAll(None)))
        },
        test("successfully create MatchAll Query with boost") {
          val query = matchAll.boost(1.0)

          assert(query)(equalTo(MatchAll(boost = Some(1.0))))
        },
        test("successfully create Nested Query with MatchAll Query") {
          val query = nested(path = "customer", query = matchAll)

          assert(query)(
            equalTo(
              Nested[Any](
                path = "customer",
                query = MatchAll(boost = None),
                scoreMode = None,
                ignoreUnmapped = None,
                innerHitsField = None
              )
            )
          )
        },
        test("successfully create type-safe Nested Query with MatchAll Query") {
          val query = nested(path = TestDocument.subDocumentList, query = matchAll)

          assert(query)(
            equalTo(
              Nested[TestDocument](
                path = "subDocumentList",
                query = MatchAll(boost = None),
                scoreMode = None,
                ignoreUnmapped = None,
                innerHitsField = None
              )
            )
          )
        },
        test("successfully create Nested Query with MatchAll Query and score_mode") {
          val queryAvg  = nested(path = "customer", query = matchAll).scoreMode(ScoreMode.Avg)
          val queryMax  = nested(path = "customer", query = matchAll).scoreMode(ScoreMode.Max)
          val queryMin  = nested(path = "customer", query = matchAll).scoreMode(ScoreMode.Min)
          val queryNone = nested(path = "customer", query = matchAll).scoreMode(ScoreMode.None)
          val querySum  = nested(path = "customer", query = matchAll).scoreMode(ScoreMode.Sum)

          assert(queryAvg)(
            equalTo(
              Nested[Any](
                path = "customer",
                query = MatchAll(boost = None),
                scoreMode = Some(ScoreMode.Avg),
                ignoreUnmapped = None,
                innerHitsField = None
              )
            )
          ) &&
          assert(queryMax)(
            equalTo(
              Nested[Any](
                path = "customer",
                query = MatchAll(boost = None),
                scoreMode = Some(ScoreMode.Max),
                ignoreUnmapped = None,
                innerHitsField = None
              )
            )
          ) &&
          assert(queryMin)(
            equalTo(
              Nested[Any](
                path = "customer",
                query = MatchAll(boost = None),
                scoreMode = Some(ScoreMode.Min),
                ignoreUnmapped = None,
                innerHitsField = None
              )
            )
          ) &&
          assert(queryNone)(
            equalTo(
              Nested[Any](
                path = "customer",
                query = MatchAll(boost = None),
                scoreMode = Some(ScoreMode.None),
                ignoreUnmapped = None,
                innerHitsField = None
              )
            )
          ) &&
          assert(querySum)(
            equalTo(
              Nested[Any](
                path = "customer",
                query = MatchAll(boost = None),
                scoreMode = Some(ScoreMode.Sum),
                ignoreUnmapped = None,
                innerHitsField = None
              )
            )
          )
        },
        test("successfully create Nested Query with MatchAll Query and ignore_unmapped") {
          val query = nested(path = "customer", query = matchAll).ignoreUnmappedTrue

          assert(query)(
            equalTo(
              Nested[Any](
                path = "customer",
                query = MatchAll(boost = None),
                scoreMode = None,
                ignoreUnmapped = Some(true),
                innerHitsField = None
              )
            )
          )
        },
        test("successfully create Nested Query with MatchAll Query, score_mode and ignore_unmapped") {
          val query = nested(path = "customer", query = matchAll).scoreMode(ScoreMode.Avg).ignoreUnmappedFalse

          assert(query)(
            equalTo(
              Nested[Any](
                path = "customer",
                query = MatchAll(boost = None),
                scoreMode = Some(ScoreMode.Avg),
                ignoreUnmapped = Some(false),
                innerHitsField = None
              )
            )
          )
        },
        test("successfully create Nested Query with MatchAll Query and inner hits with empty body") {
          val query = nested(path = "customer", query = matchAll).innerHits

          assert(query)(
            equalTo(
              Nested[Any](
                path = "customer",
                query = MatchAll(boost = None),
                scoreMode = None,
                ignoreUnmapped = None,
                innerHitsField = Some(InnerHits(None, None, None))
              )
            )
          )
        },
        test("successfully create Nested Query with MatchAll Query and inner hits with from, size and name fields") {
          val query = nested(path = "customer", query = matchAll).innerHits(InnerHits.from(0).name("name").size(3))

          assert(query)(
            equalTo(
              Nested[Any](
                path = "customer",
                query = MatchAll(boost = None),
                scoreMode = None,
                ignoreUnmapped = None,
                innerHitsField = Some(InnerHits(from = Some(0), name = Some("name"), size = Some(3)))
              )
            )
          )
        },
        test("successfully create empty Range Query") {
          val query = range(field = "customer_age")

          assert(query)(
            equalTo(
              Range[Any, Any, Unbounded.type, Unbounded.type](
                field = "customer_age",
                lower = Unbounded,
                upper = Unbounded,
                boost = None
              )
            )
          )
        },
        test("successfully create empty type-safe Range Query") {
          val queryString = range(field = TestSubDocument.stringField)
          val queryInt    = range(field = TestSubDocument.intField)

          assert(queryString)(
            equalTo(
              Range[TestSubDocument, String, Unbounded.type, Unbounded.type](
                field = "stringField",
                lower = Unbounded,
                upper = Unbounded,
                boost = None
              )
            )
          ) &&
          assert(queryInt)(
            equalTo(
              Range[TestSubDocument, Int, Unbounded.type, Unbounded.type](
                field = "intField",
                lower = Unbounded,
                upper = Unbounded,
                boost = None
              )
            )
          )
        },
        test("successfully create empty type-safe Range Query with suffix") {
          val query = range(field = TestSubDocument.stringField.keyword)

          assert(query)(
            equalTo(
              Range[TestSubDocument, String, Unbounded.type, Unbounded.type](
                field = "stringField.keyword",
                lower = Unbounded,
                upper = Unbounded,
                boost = None
              )
            )
          )
        },
        test("successfully create Range Query with upper bound") {
          val query = range(field = "customer_age").lt(23)

          assert(query)(
            equalTo(
              Range[Any, Int, Unbounded.type, LessThan[Int]](
                field = "customer_age",
                lower = Unbounded,
                upper = LessThan(23),
                boost = None
              )
            )
          )
        },
        test("successfully create Range Query with lower bound") {
          val query = range(field = "customer_age").gt(23)

          assert(query)(
            equalTo(
              Range[Any, Int, GreaterThan[Int], Unbounded.type](
                field = "customer_age",
                lower = GreaterThan(23),
                upper = Unbounded,
                boost = None
              )
            )
          )
        },
        test("successfully create Range Query with inclusive upper bound") {
          val query = range(field = "customer_age").lte(23)

          assert(query)(
            equalTo(
              Range[Any, Int, Unbounded.type, LessThanOrEqualTo[Int]](
                field = "customer_age",
                lower = Unbounded,
                upper = LessThanOrEqualTo(23),
                boost = None
              )
            )
          )
        },
        test("successfully create Range Query with inclusive lower bound") {
          val query = range(field = "customer_age").gte(23)

          assert(query)(
            equalTo(
              Range[Any, Int, GreaterThanOrEqualTo[Int], Unbounded.type](
                field = "customer_age",
                lower = GreaterThanOrEqualTo(23),
                upper = Unbounded,
                boost = None
              )
            )
          )
        },
        test("successfully create Range Query with both upper and lower bound") {
          val query = range(field = "customer_age").gte(23).lt(50)

          assert(query)(
            equalTo(
              Range[Any, Int, GreaterThanOrEqualTo[Int], LessThan[Int]](
                field = "customer_age",
                lower = GreaterThanOrEqualTo(23),
                upper = LessThan(50),
                boost = None
              )
            )
          )
        },
        test("term query") {
          val termQuery                    = term("stringField", "test")
          val termQueryTs                  = term(TestDocument.stringField, "test")
          val termQueryWithSuffix          = term(TestDocument.stringField.keyword, "test")
          val termQueryWithBoost           = term(TestDocument.stringField, "test").boost(10.21)
          val termQueryWithCaseInsensitive = term(TestDocument.stringField, "test").caseInsensitiveTrue
          val termQueryAllParams           = term(TestDocument.stringField, "test").boost(3.14).caseInsensitiveFalse

          assert(termQuery)(
            equalTo(Term[Any](field = "stringField", value = "test", boost = None, caseInsensitive = None))
          ) &&
          assert(termQueryTs)(
            equalTo(Term[TestDocument](field = "stringField", value = "test", boost = None, caseInsensitive = None))
          ) &&
          assert(termQueryWithSuffix)(
            equalTo(
              Term[TestDocument](field = "stringField.keyword", value = "test", boost = None, caseInsensitive = None)
            )
          ) &&
          assert(termQueryWithBoost)(
            equalTo(
              Term[TestDocument](field = "stringField", value = "test", boost = Some(10.21), caseInsensitive = None)
            )
          ) &&
          assert(termQueryWithCaseInsensitive)(
            equalTo(
              Term[TestDocument](field = "stringField", value = "test", boost = None, caseInsensitive = Some(true))
            )
          ) &&
          assert(termQueryAllParams)(
            equalTo(
              Term[TestDocument](
                field = "stringField",
                value = "test",
                boost = Some(3.14),
                caseInsensitive = Some(false)
              )
            )
          )
        },
        test("terms query") {
          val termsQuery                    = terms("", "a", "b", "c")
          val termsQueryTs                  = terms(TestDocument.stringField, "a", "b", "c")
          val termsQueryWithSuffix          = terms(TestDocument.stringField.keyword, "a", "b", "c")
          val termsQueryWithBoost           = terms(TestDocument.stringField, "a", "b", "c").boost(10.21)
          val termsQueryWithCaseInsensitive = terms(TestDocument.stringField, "a", "b", "c").caseInsensitiveTrue
          val termsQueryAllParams           = terms(TestDocument.stringField, "a", "b", "c").boost(3.14).caseInsensitiveFalse

          assert(termsQuery)(
            equalTo(
              Terms[Any](field = "stringField", values = List("a", "b", "c"), boost = None, caseInsensitive = None)
            )
          ) &&
          assert(termsQueryTs)(
            equalTo(
              Terms[TestDocument](
                field = "stringField",
                values = List("a", "b", "c"),
                boost = None,
                caseInsensitive = None
              )
            )
          ) &&
          assert(termsQueryWithSuffix)(
            equalTo(
              Terms[TestDocument](
                field = "stringField.keyword",
                values = List("a", "b", "c"),
                boost = None,
                caseInsensitive = None
              )
            )
          ) &&
          assert(termsQueryWithBoost)(
            equalTo(
              Terms[TestDocument](
                field = "stringField",
                values = List("a", "b", "c"),
                boost = Some(10.21),
                caseInsensitive = None
              )
            )
          ) &&
          assert(termsQueryWithCaseInsensitive)(
            equalTo(
              Terms[TestDocument](
                field = "stringField",
                values = List("a", "b", "c"),
                boost = None,
                caseInsensitive = Some(true)
              )
            )
          ) &&
          assert(termsQueryAllParams)(
            equalTo(
              Terms[TestDocument](
                field = "stringField",
                values = List("a", "b", "c"),
                boost = Some(3.14),
                caseInsensitive = Some(false)
              )
            )
          )
        },
        test("successfully create Wildcard Query") {
          val wildcardQuery1 = contains(field = "day_of_week", value = "M")
          val wildcardQuery2 = startsWith(field = "day_of_week", value = "M")
          val wildcardQuery3 = wildcard(field = "day_of_week", value = "M*")

          assert(wildcardQuery1)(
            equalTo(Wildcard[Any](field = "day_of_week", value = "*M*", boost = None, caseInsensitive = None))
          ) &&
          assert(wildcardQuery2)(
            equalTo(Wildcard[Any](field = "day_of_week", value = "M*", boost = None, caseInsensitive = None))
          ) &&
          assert(wildcardQuery3)(
            equalTo(Wildcard[Any](field = "day_of_week", value = "M*", boost = None, caseInsensitive = None))
          )
        },
        test("successfully create type-safe Wildcard Query") {
          val wildcardQuery1 = contains(field = TestSubDocument.stringField, value = "M")
          val wildcardQuery2 = startsWith(field = TestSubDocument.stringField, value = "M")
          val wildcardQuery3 = wildcard(field = TestSubDocument.stringField, value = "M*")

          assert(wildcardQuery1)(
            equalTo(
              Wildcard[TestSubDocument](field = "stringField", value = "*M*", boost = None, caseInsensitive = None)
            )
          ) &&
          assert(wildcardQuery2)(
            equalTo(
              Wildcard[TestSubDocument](field = "stringField", value = "M*", boost = None, caseInsensitive = None)
            )
          ) &&
          assert(wildcardQuery3)(
            equalTo(
              Wildcard[TestSubDocument](field = "stringField", value = "M*", boost = None, caseInsensitive = None)
            )
          )
        },
        test("successfully create Wildcard Query with boost") {
          val wildcardQuery1 = contains(field = "day_of_week", value = "M").boost(1.0)
          val wildcardQuery2 = startsWith(field = "day_of_week", value = "M").boost(1.0)
          val wildcardQuery3 = wildcard(field = "day_of_week", value = "M*").boost(1.0)

          assert(wildcardQuery1)(
            equalTo(Wildcard[Any](field = "day_of_week", value = "*M*", boost = Some(1.0), caseInsensitive = None))
          ) &&
          assert(wildcardQuery2)(
            equalTo(Wildcard[Any](field = "day_of_week", value = "M*", boost = Some(1.0), caseInsensitive = None))
          ) &&
          assert(wildcardQuery3)(
            equalTo(Wildcard[Any](field = "day_of_week", value = "M*", boost = Some(1.0), caseInsensitive = None))
          )
        },
        test("successfully create case insensitive Wildcard Query") {
          val wildcardQuery1 = contains(field = "day_of_week", value = "M").caseInsensitiveTrue
          val wildcardQuery2 = startsWith(field = "day_of_week", value = "M").caseInsensitiveTrue
          val wildcardQuery3 = wildcard(field = "day_of_week", value = "M*").caseInsensitiveTrue

          assert(wildcardQuery1)(
            equalTo(
              Wildcard[Any](field = "day_of_week", value = "*M*", boost = None, caseInsensitive = Some(true))
            )
          ) &&
          assert(wildcardQuery2)(
            equalTo(Wildcard[Any](field = "day_of_week", value = "M*", boost = None, caseInsensitive = Some(true)))
          ) &&
          assert(wildcardQuery3)(
            equalTo(Wildcard[Any](field = "day_of_week", value = "M*", boost = None, caseInsensitive = Some(true)))
          )
        },
        test("successfully create case insensitive Wildcard Query with boost") {
          val wildcardQuery1 = contains(field = "day_of_week", value = "M").boost(1.0).caseInsensitiveTrue
          val wildcardQuery2 = startsWith(field = "day_of_week", value = "M").boost(1.0).caseInsensitiveTrue
          val wildcardQuery3 = wildcard(field = "day_of_week", value = "M*").boost(1.0).caseInsensitiveTrue

          assert(wildcardQuery1)(
            equalTo(
              Wildcard[Any](field = "day_of_week", value = "*M*", boost = Some(1.0), caseInsensitive = Some(true))
            )
          ) &&
          assert(wildcardQuery2)(
            equalTo(
              Wildcard[Any](field = "day_of_week", value = "M*", boost = Some(1.0), caseInsensitive = Some(true))
            )
          ) &&
          assert(wildcardQuery3)(
            equalTo(
              Wildcard[Any](field = "day_of_week", value = "M*", boost = Some(1.0), caseInsensitive = Some(true))
            )
          )
        }
      ),
      suite("encoding ElasticQuery as JSON")(
        test("term query") {
          val query                    = term(field = TestDocument.stringField, value = "test")
          val queryWithBoost           = term(field = TestDocument.stringField, value = "test").boost(10.21)
          val queryWithCaseInsensitive = term(field = TestDocument.stringField, value = "test").caseInsensitiveTrue
          val queryWithAllParams =
            term(field = TestDocument.stringField, value = "test").boost(3.14).caseInsensitiveFalse

          val expected =
            """
              |{
              |  "query": {
              |    "term": {
              |      "stringField": {
              |        "value": "test"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithBoost =
            """
              |{
              |  "query": {
              |    "term": {
              |      "stringField": {
              |        "value": "test",
              |        "boost": 10.21
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithCaseInsensitive =
            """
              |{
              |  "query": {
              |    "term": {
              |      "stringField": {
              |        "value": "test",
              |        "case_insensitive": true
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "query": {
              |    "term": {
              |      "stringField": {
              |        "value": "test",
              |        "boost": 3.14
              |        "case_insensitive": false
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson)) &&
          assert(queryWithBoost.toJson)(equalTo(expectedWithBoost.toJson)) &&
          assert(queryWithCaseInsensitive.toJson)(equalTo(expectedWithCaseInsensitive.toJson)) &&
          assert(queryWithAllParams.toJson)(equalTo(expectedWithAllParams.toJson))
        },
        test("terms query") {
          val query          = terms(field = TestDocument.stringField, values = "a", "b", "c")
          val queryWithBoost = terms(field = TestDocument.stringField, values = "a", "b", "c").boost(10.21)
          val queryWithCaseInsensitive =
            terms(field = TestDocument.stringField, values = "a", "b", "c").caseInsensitiveTrue
          val queryWithAllParams =
            terms(field = TestDocument.stringField, values = "a", "b", "c").boost(3.14).caseInsensitiveFalse

          val expected =
            """
              |{
              |  "query": {
              |    "terms": {
              |      "stringField": [ "a", "b", "c" ]
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithBoost =
            """
              |{
              |  "query": {
              |    "terms": {
              |      "stringField": [ "a", "b", "c" ],
              |      "boost": 10.21
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithCaseInsensitive =
            """
              |{
              |  "query": {
              |    "terms": {
              |      "stringField": [ "a", "b", "c" ],
              |      "case_insensitive": true
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "query": {
              |    "terms": {
              |      "stringField": [ "a", "b", "c" ],
              |      "boost": 3.14,
              |      "case_insensitive": false
              |    }
              |  }
              |}
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson)) &&
          assert(queryWithBoost.toJson)(equalTo(expectedWithBoost.toJson)) &&
          assert(queryWithCaseInsensitive.toJson)(equalTo(expectedWithCaseInsensitive.toJson)) &&
          assert(queryWithAllParams.toJson)(equalTo(expectedWithAllParams.toJson))
        },
        test("properly encode Match query") {
          val query = matches(field = "day_of_week", value = true)
          val expected =
            """
              |{
              |  "query": {
              |    "match": {
              |      "day_of_week": true
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with Filter containing `Match` leaf query") {
          val query = filter(matches(field = "day_of_week", value = "Monday"))
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
              |      "filter": [
              |        {
              |          "match": {
              |            "day_of_week": "Monday"
              |          }
              |        }
              |      ]
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with Filter containing `Match` leaf query with boost") {
          val query = filter(matches(field = "day_of_week", value = "Monday")).boost(1.0)
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
              |    "filter": [
              |        {
              |          "match": {
              |            "day_of_week": "Monday"
              |          }
              |        }
              |      ],
              |      "boost": 1.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with Must containing `Match` leaf query") {
          val query = must(matches(field = "day_of_week", value = "Monday"))
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
              |      "must": [
              |        {
              |          "match": {
              |            "day_of_week": "Monday"
              |          }
              |        }
              |      ]
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with MustNot containing `Match` leaf query") {
          val query = mustNot(matches(field = "day_of_week", value = "Monday"))
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
              |      "must_not": [
              |        {
              |          "match": {
              |            "day_of_week": "Monday"
              |          }
              |        }
              |      ]
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with Should containing `Match` leaf query") {
          val query = should(matches(field = "day_of_week", value = "Monday"))
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
              |      "should": [
              |        {
              |          "match": {
              |            "day_of_week": "Monday"
              |          }
              |        }
              |      ]
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with Filter, Must, MustNot and Should containing `Match` leaf query") {
          val query = filter(matches(field = "customer_age", value = 23))
            .must(matches(field = "customer_id", value = 1))
            .mustNot(matches(field = "day_of_month", value = 17))
            .should(matches(field = "day_of_week", value = "Monday"))
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
              |      "filter": [
              |        {
              |          "match": {
              |            "customer_age": 23
              |          }
              |        }
              |      ],
              |      "must": [
              |        {
              |          "match": {
              |            "customer_id": 1
              |          }
              |        }
              |      ],
              |      "must_not": [
              |        {
              |          "match": {
              |            "day_of_month": 17
              |          }
              |        }
              |      ],
              |      "should": [
              |        {
              |          "match": {
              |            "day_of_week": "Monday"
              |          }
              |        }
              |      ]
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test(
          "properly encode Bool Query with Filter, Must, MustNot and Should containing `Match` leaf query and with boost"
        ) {
          val query = filter(matches(field = "customer_age", value = 23))
            .must(matches(field = "customer_id", value = 1))
            .mustNot(matches(field = "day_of_month", value = 17))
            .should(matches(field = "day_of_week", value = "Monday"))
            .boost(1.0)
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
              |      "filter": [
              |        {
              |          "match": {
              |            "customer_age": 23
              |          }
              |        }
              |      ],
              |      "must": [
              |        {
              |          "match": {
              |            "customer_id": 1
              |          }
              |        }
              |      ],
              |      "must_not": [
              |        {
              |          "match": {
              |            "day_of_month": 17
              |          }
              |        }
              |      ],
              |      "should": [
              |        {
              |          "match": {
              |            "day_of_week": "Monday"
              |          }
              |        }
              |      ],
              |      "boost": 1.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test(
          "properly encode Bool Query with Filter, Must, MustNot and Should containing `Match` leaf query and with both boost and minimumShouldMatch"
        ) {
          val query = filter(matches(field = "customer_age", value = 23))
            .must(matches(field = "customer_id", value = 1))
            .mustNot(matches(field = "day_of_month", value = 17))
            .should(
              matches(field = "day_of_week", value = "Monday"),
              matches(field = "day_of_week", value = "Tuesday"),
              matches(field = "day_of_week", value = "Wednesday")
            )
            .boost(1.0)
            .minimumShouldMatch(2)
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
              |      "filter": [
              |        {
              |          "match": {
              |            "customer_age": 23
              |          }
              |        }
              |      ],
              |      "must": [
              |        {
              |          "match": {
              |            "customer_id": 1
              |          }
              |        }
              |      ],
              |      "must_not": [
              |        {
              |          "match": {
              |            "day_of_month": 17
              |          }
              |        }
              |      ],
              |      "should": [
              |        {
              |          "match": {
              |            "day_of_week": "Monday"
              |          }
              |        },
              |        {
              |          "match": {
              |            "day_of_week": "Tuesday"
              |          }
              |        },
              |        {
              |          "match": {
              |            "day_of_week": "Wednesday"
              |          }
              |        }
              |      ],
              |      "boost": 1.0,
              |      "minimum_should_match": 2
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Exists Query") {
          val query = exists(field = "day_of_week")
          val expected =
            """
              |{
              |  "query": {
              |    "exists": {
              |      "field": "day_of_week"
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode MatchAll Query") {
          val query = matchAll
          val expected =
            """
              |{
              |  "query": {
              |    "match_all": {}
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode MatchAll Query with boost") {
          val query = matchAll.boost(1.0)
          val expected =
            """
              |{
              |  "query": {
              |    "match_all": {
              |      "boost": 1.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("successfully construct MatchPhrase query") {
          val querySimple      = matchPhrase(field = "stringField", value = "this is a test")
          val queryRaw         = matchPhrase(field = "stringField.raw", value = "this is a test")
          val queryWithBoost   = matchPhrase(field = "stringField", value = "this is a test").boost(21.15)
          val querySimpleTs    = matchPhrase(field = TestDocument.stringField, value = "this is a test")
          val queryRawTs       = matchPhrase(field = TestDocument.stringField.raw, value = "this is a test")
          val queryWithBoostTs = matchPhrase(field = TestDocument.stringField, value = "this is a test").boost(21.15)

          assert(querySimple)(
            equalTo(MatchPhrase[Any](field = "stringField", value = "this is a test", boost = None))
          ) && assert(querySimpleTs)(
            equalTo(MatchPhrase[TestDocument](field = "stringField", value = "this is a test", boost = None))
          ) && assert(queryRaw)(
            equalTo(MatchPhrase[Any](field = "stringField.raw", value = "this is a test", boost = None))
          ) && assert(queryRawTs)(
            equalTo(MatchPhrase[TestDocument](field = "stringField.raw", value = "this is a test", boost = None))
          ) && assert(queryWithBoost)(
            equalTo(MatchPhrase[Any](field = "stringField", value = "this is a test", boost = Some(21.15)))
          ) && assert(queryWithBoostTs)(
            equalTo(MatchPhrase[TestDocument](field = "stringField", value = "this is a test", boost = Some(21.15)))
          )
        },
        test("successfully encode MatchPhrase query") {
          val querySimple      = matchPhrase(field = "stringField", value = "this is a test")
          val queryRaw         = matchPhrase(field = "stringField.raw", value = "this is a test")
          val queryWithBoost   = matchPhrase(field = "stringField", value = "this is a test").boost(21.15)
          val querySimpleTs    = matchPhrase(field = TestDocument.stringField, value = "this is a test")
          val queryRawTs       = matchPhrase(field = TestDocument.stringField.raw, value = "this is a test")
          val queryWithBoostTs = matchPhrase(field = TestDocument.stringField, value = "this is a test").boost(21.15)

          val querySimpleExpectedJson =
            """
              |{
              |  "query": {
              |    "match_phrase": {
              |      "stringField": "this is a test"
              |    }
              |  }
              |}
              |""".stripMargin

          val queryRawExpectedJson =
            """
              |{
              |  "query": {
              |    "match_phrase": {
              |      "stringField.raw": "this is a test"
              |    }
              |  }
              |}
              |""".stripMargin

          val queryWithBoostExpectedJson =
            """
              |{
              |  "query": {
              |    "match_phrase": {
              |      "stringField": "this is a test",
              |      "boost": 21.15
              |    }
              |  }
              |}
              |""".stripMargin

          assert(querySimple.toJson)(equalTo(querySimpleExpectedJson.toJson)) && assert(querySimpleTs.toJson)(
            equalTo(querySimpleExpectedJson.toJson)
          ) &&
          assert(queryRaw.toJson)(equalTo(queryRawExpectedJson.toJson)) && assert(queryRawTs.toJson)(
            equalTo(queryRawExpectedJson.toJson)
          ) &&
          assert(queryWithBoost.toJson)(equalTo(queryWithBoostExpectedJson.toJson)) && assert(queryWithBoostTs.toJson)(
            equalTo(queryWithBoostExpectedJson.toJson)
          )
        },
        test("successfully create type-safe Match query using `matches` method") {
          val queryString = matches(field = TestSubDocument.stringField, value = "StringField")
          val queryInt    = matches(field = TestSubDocument.intField, value = 39)

          assert(queryString)(
            equalTo(Match[TestSubDocument, String](field = "stringField", value = "StringField", boost = None))
          ) &&
          assert(queryInt)(equalTo(Match[TestSubDocument, Int](field = "intField", value = 39, boost = None)))
        },
        test("properly encode Nested Query with MatchAll Query") {
          val query = nested(path = "customer", query = matchAll)
          val expected =
            """
              |{
              |  "query": {
              |    "nested": {
              |      "path": "customer",
              |      "query": {
              |        "match_all": {}
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode nested Nested Queries with Term Query") {
          val query = nested(path = "customer", query = nested(path = "items", query = term("type", "clothing")))
          val expected =
            """
              |{
              |  "query": {
              |    "nested": {
              |      "path": "customer",
              |      "query": {
              |        "nested": {
              |          "path": "customer.items",
              |          "query": {
              |            "term": {
              |              "customer.items.type": {
              |                "value": "clothing"
              |              }
              |            }
              |          }
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Nested Query with MatchAll Query and score_mode") {
          val query = nested(path = "customer", query = matchAll).scoreMode(ScoreMode.Avg)
          val expected =
            """
              |{
              |  "query": {
              |    "nested": {
              |      "path": "customer",
              |      "query": {
              |        "match_all": {}
              |      },
              |      "score_mode": "avg"
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Nested Query with MatchAll Query and ignore_unmapped") {
          val query = nested(path = "customer", query = matchAll).ignoreUnmappedFalse
          val expected =
            """
              |{
              |  "query": {
              |    "nested": {
              |      "path": "customer",
              |      "query": {
              |        "match_all": {}
              |      },
              |      "ignore_unmapped": false
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Nested Query with MatchAll Query, score_mode and ignore_unmapped") {
          val query = nested(path = "customer", query = matchAll).scoreMode(ScoreMode.Avg).ignoreUnmappedFalse
          val expected =
            """
              |{
              |  "query": {
              |    "nested": {
              |      "path": "customer",
              |      "query": {
              |        "match_all": {}
              |      },
              |      "score_mode": "avg",
              |      "ignore_unmapped": false
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Nested Query with MatchAll Query and inner hits with empty body") {
          val query = nested(path = "customer", query = matchAll).innerHits
          val expected =
            """
              |{
              |  "query": {
              |    "nested": {
              |      "path": "customer",
              |      "query": {
              |        "match_all": {}
              |      },
              |      "inner_hits": {}
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Nested Query with MatchAll Query and inner hits with from, size and name fields") {
          val query = nested(path = "customer", query = matchAll).innerHits(
            InnerHits.from(0).size(3).name("name")
          )
          val expected =
            """
              |{
              |  "query": {
              |    "nested": {
              |      "path": "customer",
              |      "query": {
              |        "match_all": {}
              |      },
              |      "inner_hits": {
              |        "from": 0,
              |        "size": 3,
              |        "name": "name"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Unbounded Range Query") {
          val query = range(field = "field")
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "field": {
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Unbounded Range Query with boost") {
          val query = range(field = "field").boost(1.0)
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "field": {
              |      },
              |      "boost": 1.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Range Query with Lower Bound") {
          val query = range(field = "customer_age").gt(23)
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "customer_age": {
              |        "gt": 23
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Range Query with Upper Bound") {
          val query = range(field = "customer_age").lt(23)
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "customer_age": {
              |        "lt": 23
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Range Query with Inclusive Lower Bound") {
          val query = range(field = "expiry_date").gte("now")
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "expiry_date": {
              |        "gte": "now"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Range Query with inclusive Upper Bound") {
          val query = range(field = "customer_age").lte(100L)
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "customer_age": {
              |        "lte": 100
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Range Query with both Upper and Lower Bound") {
          val query = range(field = "customer_age").gte(10).lt(100)
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "customer_age": {
              |        "gte": 10,
              |        "lt": 100
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Range Query with both Upper and Lower Bound with boost") {
          val query = range(field = "customer_age").gte(10).lt(100).boost(1.0)
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "customer_age": {
              |        "gte": 10,
              |        "lt": 100
              |      },
              |      "boost": 1.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Wildcard query") {
          val query1 = contains(field = "day_of_week", value = "M")
          val query2 = startsWith(field = "day_of_week", value = "M")
          val query3 = wildcard(field = "day_of_week", value = "M*")
          val expected1 =
            """
              |{
              |  "query": {
              |    "wildcard": {
              |      "day_of_week": {
              |        "value": "*M*"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin
          val expected23 =
            """
              |{
              |  "query": {
              |    "wildcard": {
              |      "day_of_week": {
              |        "value": "M*"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query1.toJson)(equalTo(expected1.toJson)) &&
          assert(query2.toJson)(equalTo(expected23.toJson)) &&
          assert(query3.toJson)(equalTo(expected23.toJson))
        },
        test("properly encode Wildcard query with boost") {
          val query1 = contains(field = "day_of_week", value = "M").boost(1.0)
          val query2 = startsWith(field = "day_of_week", value = "M").boost(1.0)
          val query3 = wildcard(field = "day_of_week", value = "M*").boost(1.0)
          val expected1 =
            """
              |{
              |  "query": {
              |    "wildcard": {
              |      "day_of_week": {
              |        "value": "*M*",
              |        "boost": 1.0
              |      }
              |    }
              |  }
              |}
              |""".stripMargin
          val expected23 =
            """
              |{
              |  "query": {
              |    "wildcard": {
              |      "day_of_week": {
              |        "value": "M*",
              |        "boost": 1.0
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query1.toJson)(equalTo(expected1.toJson)) &&
          assert(query2.toJson)(equalTo(expected23.toJson)) &&
          assert(query3.toJson)(equalTo(expected23.toJson))
        },
        test("properly encode case insensitive Wildcard query") {
          val query1 = contains(field = "day_of_week", value = "M").caseInsensitiveTrue
          val query2 = startsWith(field = "day_of_week", value = "M").caseInsensitiveTrue
          val query3 = wildcard(field = "day_of_week", value = "M*").caseInsensitiveTrue
          val expected1 =
            """
              |{
              |  "query": {
              |    "wildcard": {
              |      "day_of_week": {
              |        "value": "*M*",
              |        "case_insensitive": true
              |      }
              |    }
              |  }
              |}
              |""".stripMargin
          val expected23 =
            """
              |{
              |  "query": {
              |    "wildcard": {
              |      "day_of_week": {
              |        "value": "M*",
              |        "case_insensitive": true
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query1.toJson)(equalTo(expected1.toJson)) &&
          assert(query2.toJson)(equalTo(expected23.toJson)) &&
          assert(query3.toJson)(equalTo(expected23.toJson))
        },
        test("properly encode case insensitive Wildcard query with boost") {
          val query1 = contains(field = "day_of_week", value = "M").boost(1.0).caseInsensitiveTrue
          val query2 = startsWith(field = "day_of_week", value = "M").boost(1.0).caseInsensitiveTrue
          val query3 = wildcard(field = "day_of_week", value = "M*").boost(1.0).caseInsensitiveTrue
          val expected1 =
            """
              |{
              |  "query": {
              |    "wildcard": {
              |      "day_of_week": {
              |        "value": "*M*",
              |        "boost": 1.0,
              |        "case_insensitive": true
              |      }
              |    }
              |  }
              |}
              |""".stripMargin
          val expected23 =
            """
              |{
              |  "query": {
              |    "wildcard": {
              |      "day_of_week": {
              |        "value": "M*",
              |        "boost": 1.0,
              |        "case_insensitive": true
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query1.toJson)(equalTo(expected1.toJson)) &&
          assert(query2.toJson)(equalTo(expected23.toJson)) &&
          assert(query3.toJson)(equalTo(expected23.toJson))
        },
        test("properly encode Bulk request body") {
          val bulkQuery = IndexName.make("users").map { index =>
            val nestedField = TestNestedField("NestedField", 1)
            val subDoc = TestSubDocument(
              stringField = "StringField",
              nestedField = nestedField,
              intField = 100,
              intFieldList = Nil
            )
            val req1 =
              ElasticRequest
                .create[TestSubDocument](index, DocumentId("ETux1srpww2ObCx"), subDoc.copy(intField = 65))
                .routing(unsafeWrap(subDoc.stringField)(Routing))
            val req2 =
              ElasticRequest.create[TestSubDocument](index, subDoc).routing(unsafeWrap(subDoc.stringField)(Routing))
            val req3 = ElasticRequest
              .upsert[TestSubDocument](index, DocumentId("yMyEG8iFL5qx"), subDoc.copy(stringField = "StringField2"))
              .routing(unsafeWrap(subDoc.stringField)(Routing))
            val req4 =
              ElasticRequest
                .deleteById(index, DocumentId("1VNzFt2XUFZfXZheDc"))
                .routing(unsafeWrap(subDoc.stringField)(Routing))
            ElasticRequest.bulk(req1, req2, req3, req4) match {
              case r: Bulk => Some(r.body)
              case _       => None
            }
          }

          val expectedBody =
            """|{ "create" : { "_index" : "users", "_id" : "ETux1srpww2ObCx", "routing" : "StringField" } }
               |{"stringField":"StringField","nestedField":{"stringField":"NestedField","longField":1},"intField":65,"intFieldList":[]}
               |{ "create" : { "_index" : "users", "routing" : "StringField" } }
               |{"stringField":"StringField","nestedField":{"stringField":"NestedField","longField":1},"intField":100,"intFieldList":[]}
               |{ "index" : { "_index" : "users", "_id" : "yMyEG8iFL5qx", "routing" : "StringField" } }
               |{"stringField":"StringField2","nestedField":{"stringField":"NestedField","longField":1},"intField":100,"intFieldList":[]}
               |{ "delete" : { "_index" : "users", "_id" : "1VNzFt2XUFZfXZheDc", "routing" : "StringField" } }
               |""".stripMargin

          assert(bulkQuery)(equalTo(Validation.succeed(Some(expectedBody))))
        }
      )
    )
}
