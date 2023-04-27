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
      suite("constructing ElasticQuery")(
        suite("bool")(
          test("filter") {
            val query = filter(matches(TestDocument.stringField, "test"), matches(field = "testField", "test field"))
            val queryWithBoost =
              filter(matches(TestDocument.stringField, "test").boost(3.14), matches(TestDocument.intField, 22))
                .boost(10.21)

            assert(query)(
              equalTo(
                Bool[TestDocument](
                  filter = List(
                    Match(field = "stringField", value = "test", boost = None),
                    Match(field = "testField", value = "test field", boost = None)
                  ),
                  must = Nil,
                  mustNot = Nil,
                  should = Nil,
                  boost = None,
                  minimumShouldMatch = None
                )
              )
            ) && assert(queryWithBoost)(
              equalTo(
                Bool[TestDocument](
                  filter = List(
                    Match(field = "stringField", value = "test", boost = Some(3.14)),
                    Match(field = "intField", value = 22, boost = None)
                  ),
                  must = Nil,
                  mustNot = Nil,
                  should = Nil,
                  boost = Some(10.21),
                  minimumShouldMatch = None
                )
              )
            )
          },
          test("must") {
            val query = must(matches(TestDocument.stringField, "test"), matches("testField", "test field"))
            val queryWithBoost =
              must(matches(TestDocument.stringField.keyword, "test"), matches(TestDocument.intField, 22)).boost(10.21)

            assert(query)(
              equalTo(
                Bool[TestDocument](
                  filter = Nil,
                  must = List(
                    Match(field = "stringField", value = "test", boost = None),
                    Match(field = "testField", value = "test field", boost = None)
                  ),
                  mustNot = Nil,
                  should = Nil,
                  boost = None,
                  minimumShouldMatch = None
                )
              )
            ) && assert(queryWithBoost)(
              equalTo(
                Bool[TestDocument](
                  filter = Nil,
                  must = List(
                    Match(field = "stringField.keyword", value = "test", boost = None),
                    Match(field = "intField", value = 22, boost = None)
                  ),
                  mustNot = Nil,
                  should = Nil,
                  boost = Some(10.21),
                  minimumShouldMatch = None
                )
              )
            )
          },
          test("mustNot") {
            val query = mustNot(matches(TestDocument.stringField, "test"), matches("testField", "test field"))
            val queryWithBoost =
              mustNot(matches(TestDocument.stringField.keyword, "test"), matches(TestDocument.intField, 22))
                .boost(10.21)

            assert(query)(
              equalTo(
                Bool[TestDocument](
                  filter = Nil,
                  must = Nil,
                  mustNot = List(
                    Match(field = "stringField", value = "test", boost = None),
                    Match(field = "testField", value = "test field", boost = None)
                  ),
                  should = Nil,
                  boost = None,
                  minimumShouldMatch = None
                )
              )
            ) && assert(queryWithBoost)(
              equalTo(
                Bool[TestDocument](
                  filter = Nil,
                  must = Nil,
                  mustNot = List(
                    Match(field = "stringField.keyword", value = "test", boost = None),
                    Match(field = "intField", value = 22, boost = None)
                  ),
                  should = Nil,
                  boost = Some(10.21),
                  minimumShouldMatch = None
                )
              )
            )
          },
          test("should") {
            val query = should(matches(TestDocument.stringField, "test"), matches("testField", "test field"))
            val queryWithBoost =
              should(matches(TestDocument.stringField.keyword, "test"), matches(TestDocument.intField, 22)).boost(10.21)
            val queryWithMinimumShouldMatch = should(
              matches(TestDocument.stringField.keyword, "test"),
              matches(TestDocument.intField, 22),
              exists(TestDocument.booleanField)
            ).minimumShouldMatch(2)
            val queryWithAllParams = should(
              matches(TestDocument.stringField.keyword, "test"),
              matches(TestDocument.intField, 22),
              exists(TestDocument.booleanField)
            ).boost(3.14).minimumShouldMatch(2)

            assert(query)(
              equalTo(
                Bool[TestDocument](
                  filter = Nil,
                  must = Nil,
                  mustNot = Nil,
                  should = List(
                    Match(field = "stringField", value = "test", boost = None),
                    Match(field = "testField", value = "test field", boost = None)
                  ),
                  boost = None,
                  minimumShouldMatch = None
                )
              )
            ) && assert(queryWithBoost)(
              equalTo(
                Bool[TestDocument](
                  filter = Nil,
                  must = Nil,
                  mustNot = Nil,
                  should = List(
                    Match(field = "stringField.keyword", value = "test", boost = None),
                    Match(field = "intField", value = 22, boost = None)
                  ),
                  boost = Some(10.21),
                  minimumShouldMatch = None
                )
              )
            ) && assert(queryWithMinimumShouldMatch)(
              equalTo(
                Bool[TestDocument](
                  filter = Nil,
                  must = Nil,
                  mustNot = Nil,
                  should = List(
                    Match(field = "stringField.keyword", value = "test", boost = None),
                    Match(field = "intField", value = 22, boost = None),
                    Exists(field = "booleanField")
                  ),
                  boost = None,
                  minimumShouldMatch = Some(2)
                )
              )
            ) && assert(queryWithAllParams)(
              equalTo(
                Bool[TestDocument](
                  filter = Nil,
                  must = Nil,
                  mustNot = Nil,
                  should = List(
                    Match(field = "stringField.keyword", value = "test", boost = None),
                    Match(field = "intField", value = 22, boost = None),
                    Exists(field = "booleanField")
                  ),
                  boost = Some(3.14),
                  minimumShouldMatch = Some(2)
                )
              )
            )
          },
          test("filter + must + mustNot + should") {
            val query1 =
              filter(matchPhrase(TestDocument.stringField, "test")).must(matches(TestDocument.booleanField, true))
            val query2 = must(terms(TestDocument.stringField, "a", "b", "c"))
              .mustNot(matches(TestDocument.doubleField, 3.14), matches("testField", true), exists("anotherTestField"))
            val query3 = must(terms(TestDocument.stringField, "a", "b", "c"))
              .should(range(TestDocument.intField).gt(1).lte(100), matches(TestDocument.stringField, "test"))
              .mustNot(matches(TestDocument.intField, 50))
            val queryWithBoost              = query1.boost(3.14)
            val queryWithMinimumShouldMatch = query2.minimumShouldMatch(2)
            val queryWithAllParams          = query3.boost(3.14).minimumShouldMatch(3)

            assert(query1)(
              equalTo(
                Bool[TestDocument](
                  filter = List(MatchPhrase(field = "stringField", value = "test", boost = None)),
                  must = List(Match(field = "booleanField", value = true, boost = None)),
                  mustNot = Nil,
                  should = Nil,
                  boost = None,
                  minimumShouldMatch = None
                )
              )
            ) &&
            assert(query2)(
              equalTo(
                Bool[TestDocument](
                  filter = Nil,
                  must = List(Terms(field = "stringField", values = List("a", "b", "c"), boost = None)),
                  mustNot = List(
                    Match(field = "doubleField", value = 3.14, boost = None),
                    Match(field = "testField", value = true, boost = None),
                    Exists("anotherTestField")
                  ),
                  should = Nil,
                  boost = None,
                  minimumShouldMatch = None
                )
              )
            ) &&
            assert(query3)(
              equalTo(
                Bool[TestDocument](
                  filter = Nil,
                  must = List(Terms(field = "stringField", values = List("a", "b", "c"), boost = None)),
                  mustNot = List(Match(field = "intField", value = 50, boost = None)),
                  should = List(
                    Range(field = "intField", lower = GreaterThan(1), upper = LessThanOrEqualTo(100), boost = None),
                    Match(field = "stringField", value = "test", boost = None)
                  ),
                  boost = None,
                  minimumShouldMatch = None
                )
              )
            ) &&
            assert(queryWithBoost)(
              equalTo(
                Bool[TestDocument](
                  filter = List(MatchPhrase(field = "stringField", value = "test", boost = None)),
                  must = List(Match(field = "booleanField", value = true, boost = None)),
                  mustNot = Nil,
                  should = Nil,
                  boost = Some(3.14),
                  minimumShouldMatch = None
                )
              )
            ) &&
            assert(queryWithMinimumShouldMatch)(
              equalTo(
                Bool[TestDocument](
                  filter = Nil,
                  must = List(Terms(field = "stringField", values = List("a", "b", "c"), boost = None)),
                  mustNot = List(
                    Match(field = "doubleField", value = 3.14, boost = None),
                    Match(field = "testField", value = true, boost = None),
                    Exists("anotherTestField")
                  ),
                  should = Nil,
                  boost = None,
                  minimumShouldMatch = Some(2)
                )
              )
            ) &&
            assert(queryWithAllParams)(
              equalTo(
                Bool[TestDocument](
                  filter = Nil,
                  must = List(Terms(field = "stringField", values = List("a", "b", "c"), boost = None)),
                  mustNot = List(Match(field = "intField", value = 50, boost = None)),
                  should = List(
                    Range(field = "intField", lower = GreaterThan(1), upper = LessThanOrEqualTo(100), boost = None),
                    Match(field = "stringField", value = "test", boost = None)
                  ),
                  boost = Some(3.14),
                  minimumShouldMatch = Some(3)
                )
              )
            )
          }
        ),
        test("contains") {
          val query                    = contains("testField", "test")
          val queryTs                  = contains(TestDocument.stringField, "test")
          val queryWithSuffix          = contains(TestDocument.stringField.raw, "test")
          val queryWithBoost           = contains(TestDocument.stringField, "test").boost(10.21)
          val queryWithCaseInsensitive = contains(TestDocument.stringField, "test").caseInsensitiveTrue
          val queryAllParams           = contains(TestDocument.stringField, "test").boost(3.14).caseInsensitiveFalse

          assert(query)(
            equalTo(Wildcard[Any](field = "testField", value = "*test*", boost = None, caseInsensitive = None))
          ) &&
          assert(queryTs)(
            equalTo(
              Wildcard[TestDocument](field = "stringField", value = "*test*", boost = None, caseInsensitive = None)
            )
          ) &&
          assert(queryWithSuffix)(
            equalTo(
              Wildcard[TestDocument](field = "stringField.raw", value = "*test*", boost = None, caseInsensitive = None)
            )
          ) &&
          assert(queryWithBoost)(
            equalTo(
              Wildcard[TestDocument](
                field = "stringField",
                value = "*test*",
                boost = Some(10.21),
                caseInsensitive = None
              )
            )
          ) &&
          assert(queryWithCaseInsensitive)(
            equalTo(
              Wildcard[TestDocument](
                field = "stringField",
                value = "*test*",
                boost = None,
                caseInsensitive = Some(true)
              )
            )
          ) &&
          assert(queryAllParams)(
            equalTo(
              Wildcard[TestDocument](
                field = "stringField",
                value = "*test*",
                boost = Some(3.14),
                caseInsensitive = Some(false)
              )
            )
          )
        },
        test("exists") {
          val query   = exists("testField")
          val queryTs = exists(TestDocument.intField)

          assert(query)(equalTo(Exists[Any](field = "testField"))) &&
          assert(queryTs)(equalTo(Exists[TestDocument](field = "intField")))
        },
        test("hasChild") {
          val query = hasChild("child", matchAll)
          val queryWithIgnoreUnmapped = hasChild("child", matchAll).ignoreUnmappedTrue
          val queryWithInnerHits = hasChild("child", matchAll).innerHits
          val queryWithMaxChildren = hasChild("child", matchAll).maxChildren(5)
          val queryWithMinChildren = hasChild("child", matchAll).minChildren(1)
          val queryWithScoreMode = hasChild("child", matchAll).scoreMode(ScoreMode.Avg)
          val queryWithAllParams = hasChild("child", matchAll)
            .scoreMode(ScoreMode.Avg)
            .ignoreUnmappedTrue
            .innerHits
            .maxChildren(5)
            .minChildren(1)

          assert(query)(
            equalTo(HasChild[Any](childType = "child", query = matchAll))
          ) && assert(queryWithIgnoreUnmapped)(
            equalTo(HasChild[Any](childType = "child", query = matchAll, ignoreUnmapped = Some(true)))
          ) && assert(queryWithInnerHits)(
            equalTo(HasChild[Any](childType = "child", query = matchAll, innerHitsField = Some(InnerHits())))
          ) && assert(queryWithMaxChildren)(
            equalTo(HasChild[Any](childType = "child", query = matchAll, maxChildren = Some(5)))
          ) && assert(queryWithMinChildren)(
            equalTo(HasChild[Any](childType = "child", query = matchAll, minChildren = Some(1)))
          ) && assert(queryWithScoreMode)(
            equalTo(HasChild[Any](childType = "child", query = matchAll, scoreMode = Some(ScoreMode.Avg)))
          ) && assert(queryWithAllParams)(
            equalTo(
              HasChild[Any](
                childType = "child",
                query = matchAll,
                ignoreUnmapped = Some(true),
                innerHitsField = Some(InnerHits()),
                maxChildren = Some(5),
                minChildren = Some(1),
                scoreMode = Some(ScoreMode.Avg)
              )
            )
          )
        },
        test("hasParent") {
          val query                        = hasParent("parent", matchAll)
          val queryWithScoreTrue           = hasParent("parent", matchAll).withScoreTrue
          val queryWithScoreFalse          = hasParent("parent", matchAll).withScoreFalse
          val queryWithIgnoreUnmappedTrue  = hasParent("parent", matchAll).ignoreUnmappedTrue
          val queryWithIgnoreUnmappedFalse = hasParent("parent", matchAll).ignoreUnmappedFalse
          val queryWithAllParams           = hasParent("parent", matchAll).ignoreUnmappedFalse.withScoreTrue

          assert(query)(
            equalTo(HasParent[Any](parentType = "parent", query = matchAll, ignoreUnmapped = None, score = None))
          ) && assert(queryWithScoreTrue)(
            equalTo(HasParent[Any](parentType = "parent", query = matchAll, ignoreUnmapped = None, score = Some(true)))
          ) && assert(queryWithScoreFalse)(
            equalTo(HasParent[Any](parentType = "parent", query = matchAll, ignoreUnmapped = None, score = Some(false)))
          ) && assert(queryWithIgnoreUnmappedTrue)(
            equalTo(HasParent[Any](parentType = "parent", query = matchAll, ignoreUnmapped = Some(true), score = None))
          ) && assert(queryWithIgnoreUnmappedFalse)(
            equalTo(HasParent[Any](parentType = "parent", query = matchAll, ignoreUnmapped = Some(false), score = None))
          ) && assert(queryWithAllParams)(
            equalTo(
              HasParent[Any](parentType = "parent", query = matchAll, ignoreUnmapped = Some(false), score = Some(true))
            )
          )
        },
        test("matchAll") {
          val query          = matchAll
          val queryWithBoost = matchAll.boost(3.14)

          assert(query)(equalTo(MatchAll(boost = None))) && assert(queryWithBoost)(
            equalTo(MatchAll(boost = Some(3.14)))
          )
        },
        test("matches") {
          val queryString     = matches("stringField", "test")
          val queryBool       = matches("booleanField", true)
          val queryInt        = matches("intField", 1)
          val queryStringTs   = matches(TestDocument.stringField, value = "test")
          val queryBoolTs     = matches(TestDocument.booleanField, value = true)
          val queryIntTs      = matches(TestDocument.intField, value = 1)
          val queryWithSuffix = matches(TestDocument.stringField.raw, "test")
          val queryWithBoost  = matches(TestDocument.doubleField, 3.14).boost(10.21)

          assert(queryString)(equalTo(Match[Any, String](field = "stringField", value = "test", boost = None))) &&
          assert(queryBool)(equalTo(Match[Any, Boolean](field = "booleanField", value = true, boost = None))) &&
          assert(queryInt)(equalTo(Match[Any, Int](field = "intField", value = 1, boost = None))) &&
          assert(queryStringTs)(
            equalTo(Match[TestDocument, String](field = "stringField", value = "test", boost = None))
          ) &&
          assert(queryBoolTs)(
            equalTo(Match[TestDocument, Boolean](field = "booleanField", value = true, boost = None))
          ) &&
          assert(queryIntTs)(equalTo(Match[TestDocument, Int](field = "intField", value = 1, boost = None))) &&
          assert(queryWithSuffix)(
            equalTo(Match[TestDocument, String](field = "stringField.raw", value = "test", boost = None))
          ) &&
          assert(queryWithBoost)(
            equalTo(Match[TestDocument, Double](field = "doubleField", value = 3.14, boost = Some(10.21)))
          )
        },
        test("matchPhrase") {
          val query           = matchPhrase(field = "stringField", value = "this is a test")
          val queryTs         = matchPhrase(field = TestDocument.stringField, value = "this is a test")
          val queryWithSuffix = matchPhrase(field = TestDocument.stringField.raw, value = "this is a test")
          val queryWithBoost  = matchPhrase(field = TestDocument.stringField, value = "this is a test").boost(21.15)

          assert(query)(equalTo(MatchPhrase[Any](field = "stringField", value = "this is a test", boost = None))) &&
          assert(queryTs)(
            equalTo(MatchPhrase[TestDocument](field = "stringField", value = "this is a test", boost = None))
          ) &&
          assert(queryWithSuffix)(
            equalTo(MatchPhrase[TestDocument](field = "stringField.raw", value = "this is a test", boost = None))
          ) &&
          assert(queryWithBoost)(
            equalTo(MatchPhrase[TestDocument](field = "stringField", value = "this is a test", boost = Some(21.15)))
          )
        },
        test("nested") {
          val query                   = nested("testField", matchAll)
          val queryTs                 = nested(TestDocument.subDocumentList, matchAll)
          val queryWithIgnoreUnmapped = nested(TestDocument.subDocumentList, matchAll).ignoreUnmappedTrue
          val queryWithInnerHits =
            nested(TestDocument.subDocumentList, matchAll).innerHits(InnerHits.from(0).name("innerHitName").size(3))
          val queryWithInnerHitsEmpty = nested(TestDocument.subDocumentList, matchAll).innerHits
          val queryWithScoreMode      = nested(TestDocument.subDocumentList, matchAll).scoreMode(ScoreMode.Avg)
          val queryWithAllParams = nested(TestDocument.subDocumentList, matchAll).ignoreUnmappedFalse
            .innerHits(InnerHits.name("innerHitName"))
            .scoreMode(ScoreMode.Max)

          assert(query)(
            equalTo(
              Nested[Any](
                path = "testField",
                query = MatchAll(boost = None),
                scoreMode = None,
                ignoreUnmapped = None,
                innerHitsField = None
              )
            )
          ) &&
          assert(queryTs)(
            equalTo(
              Nested[TestDocument](
                path = "subDocumentList",
                query = MatchAll(boost = None),
                scoreMode = None,
                ignoreUnmapped = None,
                innerHitsField = None
              )
            )
          ) &&
          assert(queryWithIgnoreUnmapped)(
            equalTo(
              Nested[TestDocument](
                path = "subDocumentList",
                query = MatchAll(boost = None),
                scoreMode = None,
                ignoreUnmapped = Some(true),
                innerHitsField = None
              )
            )
          ) &&
          assert(queryWithInnerHits)(
            equalTo(
              Nested[TestDocument](
                path = "subDocumentList",
                query = MatchAll(boost = None),
                scoreMode = None,
                ignoreUnmapped = None,
                innerHitsField = Some(InnerHits(from = Some(0), name = Some("innerHitName"), size = Some(3)))
              )
            )
          ) &&
          assert(queryWithInnerHitsEmpty)(
            equalTo(
              Nested[TestDocument](
                path = "subDocumentList",
                query = MatchAll(boost = None),
                scoreMode = None,
                ignoreUnmapped = None,
                innerHitsField = Some(InnerHits(None, None, None))
              )
            )
          ) &&
          assert(queryWithScoreMode)(
            equalTo(
              Nested[TestDocument](
                path = "subDocumentList",
                query = MatchAll(boost = None),
                scoreMode = Some(ScoreMode.Avg),
                ignoreUnmapped = None,
                innerHitsField = None
              )
            )
          ) &&
          assert(queryWithAllParams)(
            equalTo(
              Nested[TestDocument](
                path = "subDocumentList",
                query = MatchAll(boost = None),
                scoreMode = Some(ScoreMode.Max),
                ignoreUnmapped = Some(false),
                innerHitsField = Some(InnerHits(name = Some("innerHitName")))
              )
            )
          )
        },
        test("range") {
          val query                    = range("testField")
          val queryString              = range(TestDocument.stringField)
          val queryInt                 = range(TestDocument.intField)
          val queryWithSuffix          = range(TestDocument.stringField.suffix("test"))
          val queryLowerBound          = range(TestDocument.doubleField).gt(3.14)
          val queryUpperBound          = range(TestDocument.doubleField).lt(10.21)
          val queryInclusiveLowerBound = range(TestDocument.intField).gte(10)
          val queryInclusiveUpperBound = range(TestDocument.intField).lte(21)
          val queryMixedBounds         = queryLowerBound.lte(21.0)
          val queryWithBoostParam      = queryMixedBounds.boost(2.8)

          assert(query)(
            equalTo(
              Range[Any, Any, Unbounded.type, Unbounded.type](
                field = "testField",
                lower = Unbounded,
                upper = Unbounded,
                boost = None
              )
            )
          ) &&
          assert(queryString)(
            equalTo(
              Range[TestDocument, String, Unbounded.type, Unbounded.type](
                field = "stringField",
                lower = Unbounded,
                upper = Unbounded,
                boost = None
              )
            )
          ) &&
          assert(queryInt)(
            equalTo(
              Range[TestDocument, Int, Unbounded.type, Unbounded.type](
                field = "intField",
                lower = Unbounded,
                upper = Unbounded,
                boost = None
              )
            )
          ) &&
          assert(queryWithSuffix)(
            equalTo(
              Range[TestDocument, String, Unbounded.type, Unbounded.type](
                field = "stringField.test",
                lower = Unbounded,
                upper = Unbounded,
                boost = None
              )
            )
          ) &&
          assert(queryLowerBound)(
            equalTo(
              Range[TestDocument, Double, GreaterThan[Double], Unbounded.type](
                field = "doubleField",
                lower = GreaterThan(3.14),
                upper = Unbounded,
                boost = None
              )
            )
          ) &&
          assert(queryUpperBound)(
            equalTo(
              Range[TestDocument, Double, Unbounded.type, LessThan[Double]](
                field = "doubleField",
                lower = Unbounded,
                upper = LessThan(10.21),
                boost = None
              )
            )
          ) &&
          assert(queryInclusiveLowerBound)(
            equalTo(
              Range[TestDocument, Int, GreaterThanOrEqualTo[Int], Unbounded.type](
                field = "intField",
                lower = GreaterThanOrEqualTo(10),
                upper = Unbounded,
                boost = None
              )
            )
          ) &&
          assert(queryInclusiveUpperBound)(
            equalTo(
              Range[TestDocument, Int, Unbounded.type, LessThanOrEqualTo[Int]](
                field = "intField",
                lower = Unbounded,
                upper = LessThanOrEqualTo(21),
                boost = None
              )
            )
          ) &&
          assert(queryMixedBounds)(
            equalTo(
              Range[TestDocument, Double, GreaterThan[Double], LessThanOrEqualTo[Double]](
                field = "doubleField",
                lower = GreaterThan(3.14),
                upper = LessThanOrEqualTo(21.0),
                boost = None
              )
            )
          ) &&
          assert(queryWithBoostParam)(
            equalTo(
              Range[TestDocument, Double, GreaterThan[Double], LessThanOrEqualTo[Double]](
                field = "doubleField",
                lower = GreaterThan(3.14),
                upper = LessThanOrEqualTo(21),
                boost = Some(2.8)
              )
            )
          )
        },
        test("startsWith") {
          val query                    = startsWith("testField", "test")
          val queryTs                  = startsWith(TestDocument.stringField, "test")
          val queryWithSuffix          = startsWith(TestDocument.stringField.raw, "test")
          val queryWithBoost           = startsWith(TestDocument.stringField, "test").boost(10.21)
          val queryWithCaseInsensitive = startsWith(TestDocument.stringField, "test").caseInsensitiveTrue
          val queryAllParams           = startsWith(TestDocument.stringField, "test").boost(3.14).caseInsensitiveFalse

          assert(query)(
            equalTo(Wildcard[Any](field = "testField", value = "test*", boost = None, caseInsensitive = None))
          ) &&
          assert(queryTs)(
            equalTo(
              Wildcard[TestDocument](field = "stringField", value = "test*", boost = None, caseInsensitive = None)
            )
          ) &&
          assert(queryWithSuffix)(
            equalTo(
              Wildcard[TestDocument](field = "stringField.raw", value = "test*", boost = None, caseInsensitive = None)
            )
          ) &&
          assert(queryWithBoost)(
            equalTo(
              Wildcard[TestDocument](
                field = "stringField",
                value = "test*",
                boost = Some(10.21),
                caseInsensitive = None
              )
            )
          ) &&
          assert(queryWithCaseInsensitive)(
            equalTo(
              Wildcard[TestDocument](field = "stringField", value = "test*", boost = None, caseInsensitive = Some(true))
            )
          ) &&
          assert(queryAllParams)(
            equalTo(
              Wildcard[TestDocument](
                field = "stringField",
                value = "test*",
                boost = Some(3.14),
                caseInsensitive = Some(false)
              )
            )
          )
        },
        test("term") {
          val query                    = term("stringField", "test")
          val queryTs                  = term(TestDocument.stringField, "test")
          val queryWithSuffix          = term(TestDocument.stringField.keyword, "test")
          val queryWithBoost           = term(TestDocument.stringField, "test").boost(10.21)
          val queryWithCaseInsensitive = term(TestDocument.stringField, "test").caseInsensitiveTrue
          val queryAllParams           = term(TestDocument.stringField, "test").boost(3.14).caseInsensitiveFalse

          assert(query)(
            equalTo(Term[Any](field = "stringField", value = "test", boost = None, caseInsensitive = None))
          ) &&
          assert(queryTs)(
            equalTo(Term[TestDocument](field = "stringField", value = "test", boost = None, caseInsensitive = None))
          ) &&
          assert(queryWithSuffix)(
            equalTo(
              Term[TestDocument](field = "stringField.keyword", value = "test", boost = None, caseInsensitive = None)
            )
          ) &&
          assert(queryWithBoost)(
            equalTo(
              Term[TestDocument](field = "stringField", value = "test", boost = Some(10.21), caseInsensitive = None)
            )
          ) &&
          assert(queryWithCaseInsensitive)(
            equalTo(
              Term[TestDocument](field = "stringField", value = "test", boost = None, caseInsensitive = Some(true))
            )
          ) &&
          assert(queryAllParams)(
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
        test("terms") {
          val query           = terms("stringField", "a", "b", "c")
          val queryTs         = terms(TestDocument.stringField, "a", "b", "c")
          val queryWithSuffix = terms(TestDocument.stringField.keyword, "a", "b", "c")
          val queryWithBoost  = terms(TestDocument.stringField, "a", "b", "c").boost(10.21)

          assert(query)(equalTo(Terms[Any](field = "stringField", values = List("a", "b", "c"), boost = None))) &&
          assert(queryTs)(
            equalTo(Terms[TestDocument](field = "stringField", values = List("a", "b", "c"), boost = None))
          ) &&
          assert(queryWithSuffix)(
            equalTo(Terms[TestDocument](field = "stringField.keyword", values = List("a", "b", "c"), boost = None))
          ) &&
          assert(queryWithBoost)(
            equalTo(Terms[TestDocument](field = "stringField", values = List("a", "b", "c"), boost = Some(10.21)))
          )
        },
        test("wildcard") {
          val query                    = wildcard("testField", "test")
          val queryTs                  = wildcard(TestDocument.stringField, "test")
          val queryWithSuffix          = wildcard(TestDocument.stringField.raw, "test")
          val queryWithBoost           = wildcard(TestDocument.stringField, "test").boost(10.21)
          val queryWithCaseInsensitive = wildcard(TestDocument.stringField, "test").caseInsensitiveTrue
          val queryAllParams           = wildcard(TestDocument.stringField, "test").boost(3.14).caseInsensitiveFalse

          assert(query)(
            equalTo(Wildcard[Any](field = "testField", value = "test", boost = None, caseInsensitive = None))
          ) &&
          assert(queryTs)(
            equalTo(Wildcard[TestDocument](field = "stringField", value = "test", boost = None, caseInsensitive = None))
          ) &&
          assert(queryWithSuffix)(
            equalTo(
              Wildcard[TestDocument](field = "stringField.raw", value = "test", boost = None, caseInsensitive = None)
            )
          ) &&
          assert(queryWithBoost)(
            equalTo(
              Wildcard[TestDocument](field = "stringField", value = "test", boost = Some(10.21), caseInsensitive = None)
            )
          ) &&
          assert(queryWithCaseInsensitive)(
            equalTo(
              Wildcard[TestDocument](field = "stringField", value = "test", boost = None, caseInsensitive = Some(true))
            )
          ) &&
          assert(queryAllParams)(
            equalTo(
              Wildcard[TestDocument](
                field = "stringField",
                value = "test",
                boost = Some(3.14),
                caseInsensitive = Some(false)
              )
            )
          )
        }
      ),
      suite("encoding ElasticQuery as JSON")(
        test("hasChild") {
          hasChild("child", matches("field", "value"))
          val queryWithIgnoreUnmapped = hasChild("child", matches("field", "value")).ignoreUnmappedTrue
          val queryWithInnerHits = hasChild("child", matches("field", "value")).innerHits
          val queryWithMaxChildren = hasChild("child", matches("field", "value")).maxChildren(5)
          val queryWithMinChildren = hasChild("child", matches("field", "value")).minChildren(1)
          val queryWithScoreMode = hasChild("child", matches("field", "value")).scoreMode(ScoreMode.Avg)
          val queryWithAllParams = hasChild("child", matches("field", "value"))
            .scoreMode(ScoreMode.Avg)
            .ignoreUnmappedTrue
            .innerHits
            .maxChildren(5)
            .minChildren(1)

          """
            |{
            |  "query": {
            |    "has_child": {
            |      "type": "child",
            |      "query": {
            |        "match": {
            |         "field" : "value"
            |        }
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

          val expectedWithIgnoreUnmapped =
            """
              |{
              |  "query": {
              |    "has_child": {
              |      "type": "child",
              |      "ignore_unmapped": true,
              |      "query": {
              |        "match": {
              |         "field" : "value"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithInnerHits =
            """
              |{
              |  "query": {
              |    "has_child": {
              |      "type": "child",
              |      "inner_hits": {},
              |      "query": {
              |        "match": {
              |         "field" : "value"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMaxChildren =
            """
              |{
              |  "query": {
              |    "has_child": {
              |      "type": "child",
              |      "max_children": 5,
              |      "query": {
              |        "match": {
              |         "field" : "value"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMinChildren =
            """
              |{
              |  "query": {
              |    "has_child": {
              |      "type": "child",
              |      "min_children": 1,
              |      "query": {
              |        "match": {
              |         "field" : "value"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithScoreMode =
            """
              |{
              |  "query": {
              |    "has_child": {
              |      "type": "child",
              |      "score_mode": "avg",
              |      "query": {
              |        "match": {
              |         "field" : "value"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedFullQuery =
            """
              |{
              |  "query": {
              |    "has_child": {
              |      "type": "child",
              |      "score_mode": "avg",
              |      "ignore_unmapped": true,
              |      "inner_hits": {},
              |      "max_children": 5,
              |      "min_children": 1,
              |      "query": {
              |        "match": {
              |         "field" : "value"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(queryWithIgnoreUnmapped.toJson)(equalTo(expectedWithIgnoreUnmapped.toJson)) &&
            assert(queryWithInnerHits.toJson)(equalTo(expectedWithInnerHits.toJson)) &&
            assert(queryWithMaxChildren.toJson)(equalTo(expectedWithMaxChildren.toJson)) &&
            assert(queryWithMinChildren.toJson)(equalTo(expectedWithMinChildren.toJson)) &&
            assert(queryWithScoreMode.toJson)(equalTo(expectedWithScoreMode.toJson)) &&
            assert(queryWithAllParams.toJson)(equalTo(expectedFullQuery.toJson))
        },
        test("hasParent") {
          val query =
            hasParent(parentType = "parent", query = matches("field", "value"))
          val queryWithScore =
            hasParent(parentType = "parent", query = matches("field", "value")).withScoreFalse
          val queryWithIgnoreUnmapped =
            hasParent(parentType = "parent", query = matches("field", "value")).ignoreUnmappedFalse
          val queryWithScoreAndIgnoreUnmapped =
            hasParent(parentType = "parent", query = matches("field", "value")).withScoreTrue.ignoreUnmappedTrue
          val queryWithInnerHits =
            hasParent(parentType = "parent", query = matches("field", "value")).innerHits

          val expected =
            """
              |{
              |  "query": {
              |    "has_parent": {
              |      "parent_type": "parent",
              |      "query": {
              |        "match": {
              |         "field" : "value"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithScore =
            """
              |{
              |  "query": {
              |    "has_parent": {
              |      "parent_type": "parent",
              |      "score": false,
              |      "query": {
              |        "match": {
              |         "field" : "value"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithIgnoreUnmapped =
            """
              |{
              |  "query": {
              |    "has_parent": {
              |      "parent_type": "parent",
              |      "ignore_unmapped": false,
              |      "query": {
              |        "match": {
              |         "field" : "value"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithScoreAndIgnoreUnmapped =
            """
              |{
              |  "query": {
              |    "has_parent": {
              |      "parent_type": "parent",
              |      "score": true,
              |      "ignore_unmapped": true,
              |      "query": {
              |        "match": {
              |         "field" : "value"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithInnerHits =
            """
              |{
              |  "query": {
              |    "has_parent": {
              |      "parent_type": "parent",
              |      "query": {
              |        "match": {
              |         "field" : "value"
              |        }
              |      },
              |      "inner_hits": {}
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson)(equalTo(expected.toJson)) &&
            assert(queryWithScore.toJson)(equalTo(expectedWithScore.toJson)) &&
            assert(queryWithIgnoreUnmapped.toJson)(equalTo(expectedWithIgnoreUnmapped.toJson)) &&
            assert(queryWithScoreAndIgnoreUnmapped.toJson)(equalTo(expectedWithScoreAndIgnoreUnmapped.toJson)) &&
            assert(queryWithInnerHits.toJson)(equalTo(expectedWithInnerHits.toJson))
        },
        test("matchPhrase") {
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

          assert(querySimple.toJson)(equalTo(querySimpleExpectedJson.toJson)) &&
          assert(querySimpleTs.toJson)(equalTo(querySimpleExpectedJson.toJson)) &&
          assert(queryRaw.toJson)(equalTo(queryRawExpectedJson.toJson)) &&
          assert(queryRawTs.toJson)(equalTo(queryRawExpectedJson.toJson)) &&
          assert(queryWithBoost.toJson)(equalTo(queryWithBoostExpectedJson.toJson)) &&
          assert(queryWithBoostTs.toJson)(equalTo(queryWithBoostExpectedJson.toJson))
        },
        test("term") {
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
              |        "boost": 3.14,
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
        test("terms") {
          val query          = terms(field = TestDocument.stringField, values = "a", "b", "c")
          val queryWithBoost = terms(field = TestDocument.stringField, values = "a", "b", "c").boost(10.21)

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

          assert(query.toJson)(equalTo(expected.toJson)) &&
          assert(queryWithBoost.toJson)(equalTo(expectedWithBoost.toJson))
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
        test("properly encode Bool Query with Filter, Must, MustNot and Should containing `Match` leaf query, boost") {
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
          "properly encode Bool Query with Filter, Must, MustNot and Should containing `Match` leaf query and with boost, minimumShouldMatch"
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
