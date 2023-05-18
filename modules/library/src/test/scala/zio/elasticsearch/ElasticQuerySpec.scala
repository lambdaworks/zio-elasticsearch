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

import zio.Chunk
import zio.elasticsearch.ElasticQuery._
import zio.elasticsearch.ElasticRequest.Bulk
import zio.elasticsearch.domain._
import zio.elasticsearch.query.DistanceType.Plane
import zio.elasticsearch.query.DistanceUnit.Kilometers
import zio.elasticsearch.query.ValidationMethod.IgnoreMalformed
import zio.elasticsearch.query._
import zio.elasticsearch.utils._
import zio.prelude.Validation
import zio.test.Assertion.equalTo
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assert}

import java.time.LocalDate

object ElasticQuerySpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment, Any] =
    suite("ElasticQuery")(
      suite("constructing")(
        suite("bool")(
          test("filter") {
            val query = filter(matches(TestDocument.stringField, "test"), matches(field = "testField", "test field"))
            val queryWithBoost =
              filter(matches(TestDocument.stringField, "test"), matches(TestDocument.intField, 22))
                .boost(10.21)

            assert(query)(
              equalTo(
                Bool[TestDocument](
                  filter = Chunk(
                    Match(field = "stringField", value = "test"),
                    Match(field = "testField", value = "test field")
                  ),
                  must = Chunk.empty,
                  mustNot = Chunk.empty,
                  should = Chunk.empty,
                  boost = None,
                  minimumShouldMatch = None
                )
              )
            ) && assert(queryWithBoost)(
              equalTo(
                Bool[TestDocument](
                  filter = Chunk(
                    Match(field = "stringField", value = "test"),
                    Match(field = "intField", value = 22)
                  ),
                  must = Chunk.empty,
                  mustNot = Chunk.empty,
                  should = Chunk.empty,
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
                  filter = Chunk.empty,
                  must = Chunk(
                    Match(field = "stringField", value = "test"),
                    Match(field = "testField", value = "test field")
                  ),
                  mustNot = Chunk.empty,
                  should = Chunk.empty,
                  boost = None,
                  minimumShouldMatch = None
                )
              )
            ) && assert(queryWithBoost)(
              equalTo(
                Bool[TestDocument](
                  filter = Chunk.empty,
                  must = Chunk(
                    Match(field = "stringField.keyword", value = "test"),
                    Match(field = "intField", value = 22)
                  ),
                  mustNot = Chunk.empty,
                  should = Chunk.empty,
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
                  filter = Chunk.empty,
                  must = Chunk.empty,
                  mustNot = Chunk(
                    Match(field = "stringField", value = "test"),
                    Match(field = "testField", value = "test field")
                  ),
                  should = Chunk.empty,
                  boost = None,
                  minimumShouldMatch = None
                )
              )
            ) && assert(queryWithBoost)(
              equalTo(
                Bool[TestDocument](
                  filter = Chunk.empty,
                  must = Chunk.empty,
                  mustNot = Chunk(
                    Match(field = "stringField.keyword", value = "test"),
                    Match(field = "intField", value = 22)
                  ),
                  should = Chunk.empty,
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
                  filter = Chunk.empty,
                  must = Chunk.empty,
                  mustNot = Chunk.empty,
                  should = Chunk(
                    Match(field = "stringField", value = "test"),
                    Match(field = "testField", value = "test field")
                  ),
                  boost = None,
                  minimumShouldMatch = None
                )
              )
            ) && assert(queryWithBoost)(
              equalTo(
                Bool[TestDocument](
                  filter = Chunk.empty,
                  must = Chunk.empty,
                  mustNot = Chunk.empty,
                  should = Chunk(
                    Match(field = "stringField.keyword", value = "test"),
                    Match(field = "intField", value = 22)
                  ),
                  boost = Some(10.21),
                  minimumShouldMatch = None
                )
              )
            ) && assert(queryWithMinimumShouldMatch)(
              equalTo(
                Bool[TestDocument](
                  filter = Chunk.empty,
                  must = Chunk.empty,
                  mustNot = Chunk.empty,
                  should = Chunk(
                    Match(field = "stringField.keyword", value = "test"),
                    Match(field = "intField", value = 22),
                    Exists(field = "booleanField")
                  ),
                  boost = None,
                  minimumShouldMatch = Some(2)
                )
              )
            ) && assert(queryWithAllParams)(
              equalTo(
                Bool[TestDocument](
                  filter = Chunk.empty,
                  must = Chunk.empty,
                  mustNot = Chunk.empty,
                  should = Chunk(
                    Match(field = "stringField.keyword", value = "test"),
                    Match(field = "intField", value = 22),
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
                  filter = Chunk(MatchPhrase(field = "stringField", value = "test")),
                  must = Chunk(Match(field = "booleanField", value = true)),
                  mustNot = Chunk.empty,
                  should = Chunk.empty,
                  boost = None,
                  minimumShouldMatch = None
                )
              )
            ) &&
            assert(query2)(
              equalTo(
                Bool[TestDocument](
                  filter = Chunk.empty,
                  must = Chunk(Terms(field = "stringField", values = Chunk("a", "b", "c"), boost = None)),
                  mustNot = Chunk(
                    Match(field = "doubleField", value = 3.14),
                    Match(field = "testField", value = true),
                    Exists("anotherTestField")
                  ),
                  should = Chunk.empty,
                  boost = None,
                  minimumShouldMatch = None
                )
              )
            ) &&
            assert(query3)(
              equalTo(
                Bool[TestDocument](
                  filter = Chunk.empty,
                  must = Chunk(Terms(field = "stringField", values = Chunk("a", "b", "c"), boost = None)),
                  mustNot = Chunk(Match(field = "intField", value = 50)),
                  should = Chunk(
                    Range(
                      field = "intField",
                      lower = GreaterThan(1),
                      upper = LessThanOrEqualTo(100),
                      boost = None,
                      format = None
                    ),
                    Match(field = "stringField", value = "test")
                  ),
                  boost = None,
                  minimumShouldMatch = None
                )
              )
            ) &&
            assert(queryWithBoost)(
              equalTo(
                Bool[TestDocument](
                  filter = Chunk(MatchPhrase(field = "stringField", value = "test")),
                  must = Chunk(Match(field = "booleanField", value = true)),
                  mustNot = Chunk.empty,
                  should = Chunk.empty,
                  boost = Some(3.14),
                  minimumShouldMatch = None
                )
              )
            ) &&
            assert(queryWithMinimumShouldMatch)(
              equalTo(
                Bool[TestDocument](
                  filter = Chunk.empty,
                  must = Chunk(Terms(field = "stringField", values = Chunk("a", "b", "c"), boost = None)),
                  mustNot = Chunk(
                    Match(field = "doubleField", value = 3.14),
                    Match(field = "testField", value = true),
                    Exists("anotherTestField")
                  ),
                  should = Chunk.empty,
                  boost = None,
                  minimumShouldMatch = Some(2)
                )
              )
            ) &&
            assert(queryWithAllParams)(
              equalTo(
                Bool[TestDocument](
                  filter = Chunk.empty,
                  must = Chunk(Terms(field = "stringField", values = Chunk("a", "b", "c"), boost = None)),
                  mustNot = Chunk(Match(field = "intField", value = 50)),
                  should = Chunk(
                    Range(
                      field = "intField",
                      lower = GreaterThan(1),
                      upper = LessThanOrEqualTo(100),
                      boost = None,
                      format = None
                    ),
                    Match(field = "stringField", value = "test")
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
        test("geoDistance") {
          val query =
            geoDistance("testField", 20.0, 21.1)
          val queryString =
            geoDistance(TestDocument.stringField, "drm3btev3e86")
          val queryWithDistance =
            geoDistance(TestDocument.stringField, 20.0, 21.1).distance(200, Kilometers)
          val queryWithDistanceType =
            geoDistance(TestDocument.stringField, 20.0, 21.1).distanceType(Plane)
          val queryWithName =
            geoDistance(TestDocument.stringField, 20.0, 21.1).name("name")
          val queryWithValidationMethod =
            geoDistance(TestDocument.stringField, 20.0, 21.1).validationMethod(IgnoreMalformed)
          val queryWithAllParams = geoDistance(TestDocument.stringField, 20.0, 21.1)
            .validationMethod(IgnoreMalformed)
            .name("name")
            .distanceType(Plane)
            .distance(200, Kilometers)

          assert(query)(
            equalTo(
              GeoDistance[Any](
                field = "testField",
                point = "20.0,21.1",
                distance = None,
                distanceType = None,
                queryName = None,
                validationMethod = None
              )
            )
          ) &&
          assert(queryString)(
            equalTo(
              GeoDistance[TestDocument](
                field = "stringField",
                point = "drm3btev3e86",
                distance = None,
                distanceType = None,
                queryName = None,
                validationMethod = None
              )
            )
          ) &&
          assert(queryWithDistance)(
            equalTo(
              GeoDistance[TestDocument](
                field = "stringField",
                point = "20.0,21.1",
                distance = Some(Distance(200, Kilometers)),
                distanceType = None,
                queryName = None,
                validationMethod = None
              )
            )
          ) && assert(queryWithDistanceType)(
            equalTo(
              GeoDistance[TestDocument](
                field = "stringField",
                point = "20.0,21.1",
                distance = None,
                distanceType = Some(Plane),
                queryName = None,
                validationMethod = None
              )
            )
          ) && assert(queryWithName)(
            equalTo(
              GeoDistance[TestDocument](
                field = "stringField",
                point = "20.0,21.1",
                distance = None,
                distanceType = None,
                queryName = Some("name"),
                validationMethod = None
              )
            )
          ) && assert(queryWithValidationMethod)(
            equalTo(
              GeoDistance[TestDocument](
                field = "stringField",
                point = "20.0,21.1",
                distance = None,
                distanceType = None,
                queryName = None,
                validationMethod = Some(IgnoreMalformed)
              )
            )
          ) && assert(queryWithAllParams)(
            equalTo(
              GeoDistance[TestDocument](
                field = "stringField",
                point = "20.0,21.1",
                distance = Some(Distance(200, Kilometers)),
                distanceType = Some(Plane),
                queryName = Some("name"),
                validationMethod = Some(IgnoreMalformed)
              )
            )
          )
        },
        test("hasChild") {
          val query                   = hasChild("child", matchAll)
          val queryWithIgnoreUnmapped = hasChild("child", matchAll).ignoreUnmappedTrue
          val queryWithInnerHits      = hasChild("child", matchAll).innerHits
          val queryWithMaxChildren    = hasChild("child", matchAll).maxChildren(5)
          val queryWithMinChildren    = hasChild("child", matchAll).minChildren(1)
          val queryWithScoreMode      = hasChild("child", matchAll).scoreMode(ScoreMode.Avg)
          val queryWithAllParams = hasChild("child", matchAll)
            .scoreMode(ScoreMode.Avg)
            .ignoreUnmappedTrue
            .innerHits
            .maxChildren(5)
            .minChildren(1)

          assert(query)(
            equalTo(
              HasChild[Any](
                childType = "child",
                query = matchAll,
                ignoreUnmapped = None,
                innerHitsField = None,
                maxChildren = None,
                minChildren = None,
                scoreMode = None
              )
            )
          ) && assert(queryWithIgnoreUnmapped)(
            equalTo(
              HasChild[Any](
                childType = "child",
                query = matchAll,
                ignoreUnmapped = Some(true),
                innerHitsField = None,
                maxChildren = None,
                minChildren = None,
                scoreMode = None
              )
            )
          ) && assert(queryWithInnerHits)(
            equalTo(
              HasChild[Any](
                childType = "child",
                query = matchAll,
                ignoreUnmapped = None,
                innerHitsField = Some(InnerHits()),
                maxChildren = None,
                minChildren = None,
                scoreMode = None
              )
            )
          ) && assert(queryWithMaxChildren)(
            equalTo(
              HasChild[Any](
                childType = "child",
                query = matchAll,
                ignoreUnmapped = None,
                innerHitsField = None,
                maxChildren = Some(5),
                minChildren = None,
                scoreMode = None
              )
            )
          ) && assert(queryWithMinChildren)(
            equalTo(
              HasChild[Any](
                childType = "child",
                query = matchAll,
                ignoreUnmapped = None,
                innerHitsField = None,
                maxChildren = None,
                minChildren = Some(1),
                scoreMode = None
              )
            )
          ) && assert(queryWithScoreMode)(
            equalTo(
              HasChild[Any](
                childType = "child",
                query = matchAll,
                ignoreUnmapped = None,
                innerHitsField = None,
                maxChildren = None,
                minChildren = None,
                scoreMode = Some(ScoreMode.Avg)
              )
            )
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
            equalTo(
              HasParent[Any](
                parentType = "parent",
                query = matchAll,
                ignoreUnmapped = None,
                innerHitsField = None,
                score = None
              )
            )
          ) && assert(queryWithScoreTrue)(
            equalTo(
              HasParent[Any](
                parentType = "parent",
                query = matchAll,
                ignoreUnmapped = None,
                innerHitsField = None,
                score = Some(true)
              )
            )
          ) && assert(queryWithScoreFalse)(
            equalTo(
              HasParent[Any](
                parentType = "parent",
                query = matchAll,
                ignoreUnmapped = None,
                innerHitsField = None,
                score = Some(false)
              )
            )
          ) && assert(queryWithIgnoreUnmappedTrue)(
            equalTo(
              HasParent[Any](
                parentType = "parent",
                query = matchAll,
                ignoreUnmapped = Some(true),
                innerHitsField = None,
                score = None
              )
            )
          ) && assert(queryWithIgnoreUnmappedFalse)(
            equalTo(
              HasParent[Any](
                parentType = "parent",
                query = matchAll,
                ignoreUnmapped = Some(false),
                innerHitsField = None,
                score = None
              )
            )
          ) && assert(queryWithAllParams)(
            equalTo(
              HasParent[Any](
                parentType = "parent",
                query = matchAll,
                ignoreUnmapped = Some(false),
                innerHitsField = None,
                score = Some(true)
              )
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
          val queryStringTs   = matches(TestDocument.stringField, "test")
          val queryBoolTs     = matches(TestDocument.booleanField, true)
          val queryIntTs      = matches(TestDocument.intField, 1)
          val queryWithSuffix = matches(TestDocument.stringField.raw, "test")
          val queryWithBoost  = matches(TestDocument.doubleField, 3.14)

          assert(queryString)(equalTo(Match[Any, String](field = "stringField", value = "test"))) &&
          assert(queryBool)(equalTo(Match[Any, Boolean](field = "booleanField", value = true))) &&
          assert(queryInt)(equalTo(Match[Any, Int](field = "intField", value = 1))) &&
          assert(queryStringTs)(
            equalTo(Match[TestDocument, String](field = "stringField", value = "test"))
          ) &&
          assert(queryBoolTs)(
            equalTo(Match[TestDocument, Boolean](field = "booleanField", value = true))
          ) &&
          assert(queryIntTs)(equalTo(Match[TestDocument, Int](field = "intField", value = 1))) &&
          assert(queryWithSuffix)(
            equalTo(Match[TestDocument, String](field = "stringField.raw", value = "test"))
          ) &&
          assert(queryWithBoost)(
            equalTo(Match[TestDocument, Double](field = "doubleField", value = 3.14))
          )
        },
        test("matchPhrase") {
          val query           = matchPhrase("stringField", "this is a test")
          val queryTs         = matchPhrase(TestDocument.stringField, "this is a test")
          val queryWithSuffix = matchPhrase(TestDocument.stringField.raw, "this is a test")
          val queryWithBoost  = matchPhrase(TestDocument.stringField, "this is a test")

          assert(query)(equalTo(MatchPhrase[Any](field = "stringField", value = "this is a test"))) &&
          assert(queryTs)(
            equalTo(MatchPhrase[TestDocument](field = "stringField", value = "this is a test"))
          ) &&
          assert(queryWithSuffix)(
            equalTo(MatchPhrase[TestDocument](field = "stringField.raw", value = "this is a test"))
          ) &&
          assert(queryWithBoost)(
            equalTo(MatchPhrase[TestDocument](field = "stringField", value = "this is a test"))
          )
        },
        test("nested") {
          val query                   = nested("testField", matchAll)
          val queryTs                 = nested(TestDocument.subDocumentList, matchAll)
          val queryWithIgnoreUnmapped = nested(TestDocument.subDocumentList, matchAll).ignoreUnmappedTrue
          val queryWithInnerHits =
            nested(TestDocument.subDocumentList, matchAll).innerHits(InnerHits().from(0).name("innerHitName").size(3))
          val queryWithInnerHitsEmpty = nested(TestDocument.subDocumentList, matchAll).innerHits
          val queryWithScoreMode      = nested(TestDocument.subDocumentList, matchAll).scoreMode(ScoreMode.Avg)
          val queryWithAllParams = nested(TestDocument.subDocumentList, matchAll).ignoreUnmappedFalse
            .innerHits(InnerHits().name("innerHitName"))
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
                innerHitsField = Some(InnerHits(None, Some("innerHitName"), None))
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
          val queryWithFormatParam     = range(TestDocument.dateField).gt(LocalDate.of(2023, 5, 11)).format("uuuu-MM-dd")

          assert(query)(
            equalTo(
              Range[Any, Any, Unbounded.type, Unbounded.type](
                field = "testField",
                lower = Unbounded,
                upper = Unbounded,
                boost = None,
                format = None
              )
            )
          ) &&
          assert(queryString)(
            equalTo(
              Range[TestDocument, String, Unbounded.type, Unbounded.type](
                field = "stringField",
                lower = Unbounded,
                upper = Unbounded,
                boost = None,
                format = None
              )
            )
          ) &&
          assert(queryInt)(
            equalTo(
              Range[TestDocument, Int, Unbounded.type, Unbounded.type](
                field = "intField",
                lower = Unbounded,
                upper = Unbounded,
                boost = None,
                format = None
              )
            )
          ) &&
          assert(queryWithSuffix)(
            equalTo(
              Range[TestDocument, String, Unbounded.type, Unbounded.type](
                field = "stringField.test",
                lower = Unbounded,
                upper = Unbounded,
                boost = None,
                format = None
              )
            )
          ) &&
          assert(queryLowerBound)(
            equalTo(
              Range[TestDocument, Double, GreaterThan[Double], Unbounded.type](
                field = "doubleField",
                lower = GreaterThan(3.14),
                upper = Unbounded,
                boost = None,
                format = None
              )
            )
          ) &&
          assert(queryUpperBound)(
            equalTo(
              Range[TestDocument, Double, Unbounded.type, LessThan[Double]](
                field = "doubleField",
                lower = Unbounded,
                upper = LessThan(10.21),
                boost = None,
                format = None
              )
            )
          ) &&
          assert(queryInclusiveLowerBound)(
            equalTo(
              Range[TestDocument, Int, GreaterThanOrEqualTo[Int], Unbounded.type](
                field = "intField",
                lower = GreaterThanOrEqualTo(10),
                upper = Unbounded,
                boost = None,
                format = None
              )
            )
          ) &&
          assert(queryInclusiveUpperBound)(
            equalTo(
              Range[TestDocument, Int, Unbounded.type, LessThanOrEqualTo[Int]](
                field = "intField",
                lower = Unbounded,
                upper = LessThanOrEqualTo(21),
                boost = None,
                format = None
              )
            )
          ) &&
          assert(queryMixedBounds)(
            equalTo(
              Range[TestDocument, Double, GreaterThan[Double], LessThanOrEqualTo[Double]](
                field = "doubleField",
                lower = GreaterThan(3.14),
                upper = LessThanOrEqualTo(21.0),
                boost = None,
                format = None
              )
            )
          ) &&
          assert(queryWithBoostParam)(
            equalTo(
              Range[TestDocument, Double, GreaterThan[Double], LessThanOrEqualTo[Double]](
                field = "doubleField",
                lower = GreaterThan(3.14),
                upper = LessThanOrEqualTo(21),
                boost = Some(2.8),
                format = None
              )
            )
          ) &&
          assert(queryWithFormatParam)(
            equalTo(
              Range[TestDocument, LocalDate, GreaterThan[LocalDate], Unbounded.type](
                field = "dateField",
                lower = GreaterThan(LocalDate.of(2023, 5, 11)),
                upper = Unbounded,
                boost = None,
                format = Some("uuuu-MM-dd")
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

          assert(query)(equalTo(Terms[Any](field = "stringField", values = Chunk("a", "b", "c"), boost = None))) &&
          assert(queryTs)(
            equalTo(Terms[TestDocument](field = "stringField", values = Chunk("a", "b", "c"), boost = None))
          ) &&
          assert(queryWithSuffix)(
            equalTo(Terms[TestDocument](field = "stringField.keyword", values = Chunk("a", "b", "c"), boost = None))
          ) &&
          assert(queryWithBoost)(
            equalTo(Terms[TestDocument](field = "stringField", values = Chunk("a", "b", "c"), boost = Some(10.21)))
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
      suite("encoding as JSON")(
        suite("bool")(
          test("filter") {
            val query          = filter(matches(TestDocument.doubleField, 39.2))
            val queryWithBoost = filter(matches(TestDocument.booleanField, true)).boost(3.14)

            val expected =
              """
                |{
                |  "bool": {
                |    "filter": [
                |      {
                |        "match": {
                |          "doubleField": 39.2
                |        }
                |      }
                |    ]
                |  }
                |}
                |""".stripMargin

            val expectedWithBoost =
              """
                |{
                |  "bool": {
                |    "filter": [
                |      {
                |        "match": {
                |          "booleanField": true
                |        }
                |      }
                |    ],
                |    "boost": 3.14
                |  }
                |}
                |""".stripMargin

            assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
            assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson))
          },
          test("must") {
            val query          = must(matchPhrase(TestDocument.stringField, "test"))
            val queryWithBoost = must(terms(TestDocument.stringField, "a", "b", "c")).boost(3.14)

            val expected =
              """
                |{
                |  "bool": {
                |    "must": [
                |      {
                |        "match_phrase": {
                |          "stringField": "test"
                |        }
                |      }
                |    ]
                |  }
                |}
                |""".stripMargin

            val expectedWithBoost =
              """
                |{
                |  "bool": {
                |    "must": [
                |      {
                |        "terms": {
                |          "stringField": ["a", "b", "c"]
                |        }
                |      }
                |    ],
                |    "boost": 3.14
                |  }
                |}
                |""".stripMargin

            assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
            assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson))
          },
          test("mustNot") {
            val query = mustNot(matches(TestDocument.stringField, "test"), matches("testField", "test field"))
            val queryWithBoost =
              mustNot(matches(TestDocument.stringField.keyword, "test"), matches(TestDocument.intField, 22))
                .boost(10.21)

            val expected =
              """
                |{
                |  "bool": {
                |    "must_not": [
                |      {
                |        "match": {
                |          "stringField": "test"
                |        }
                |      },
                |      {
                |        "match": {
                |          "testField": "test field"
                |        }
                |      }
                |    ]
                |  }
                |}
                |""".stripMargin

            val expectedWithBoost =
              """
                |{
                |  "bool": {
                |    "must_not": [
                |      {
                |        "match": {
                |          "stringField.keyword": "test"
                |        }
                |      },
                |      {
                |        "match": {
                |          "intField": 22
                |        }
                |      }
                |    ],
                |    "boost": 10.21
                |  }
                |}
                |""".stripMargin

            assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
            assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson))
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

            val expected =
              """
                |{
                |  "bool": {
                |    "should": [
                |      {
                |        "match": {
                |          "stringField": "test"
                |        }
                |      },
                |      {
                |        "match": {
                |          "testField": "test field"
                |        }
                |      }
                |    ]
                |  }
                |}
                |""".stripMargin

            val expectedWithBoost =
              """
                |{
                |  "bool": {
                |    "should": [
                |      {
                |        "match": {
                |          "stringField.keyword": "test"
                |        }
                |      },
                |      {
                |        "match": {
                |          "intField": 22
                |        }
                |      }
                |    ],
                |    "boost": 10.21
                |  }
                |}
                |""".stripMargin

            val expectedWithMinimumShouldMatch =
              """
                |{
                |  "bool": {
                |    "should": [
                |      {
                |        "match": {
                |          "stringField.keyword": "test"
                |        }
                |      },
                |      {
                |        "match": {
                |          "intField": 22
                |        }
                |      },
                |      {
                |        "exists": {
                |          "field": "booleanField"
                |        }
                |      }
                |    ],
                |    "minimum_should_match": 2
                |  }
                |}
                |""".stripMargin

            val expectedWithAllParams =
              """
                |{
                |  "bool": {
                |    "should": [
                |      {
                |        "match": {
                |          "stringField.keyword": "test"
                |        }
                |      },
                |      {
                |        "match": {
                |          "intField": 22
                |        }
                |      },
                |      {
                |        "exists": {
                |          "field": "booleanField"
                |        }
                |      }
                |    ],
                |    "boost": 3.14,
                |    "minimum_should_match": 2
                |  }
                |}
                |""".stripMargin

            assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
            assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson)) &&
            assert(queryWithMinimumShouldMatch.toJson(fieldPath = None))(
              equalTo(expectedWithMinimumShouldMatch.toJson)
            ) &&
            assert(queryWithAllParams.toJson(fieldPath = None))(equalTo(expectedWithAllParams.toJson))
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

            val expected1 =
              """
                |{
                |  "bool": {
                |    "filter": [
                |      {
                |        "match_phrase": {
                |          "stringField": "test"
                |        }
                |      }
                |    ],
                |    "must": [
                |      {
                |        "match": {
                |          "booleanField": true
                |        }
                |      }
                |    ]
                |  }
                |}
                |""".stripMargin

            val expected2 =
              """
                |{
                |  "bool": {
                |    "must": [
                |      {
                |        "terms": {
                |          "stringField": ["a", "b", "c"]
                |        }
                |      }
                |    ],
                |    "must_not": [
                |      {
                |        "match": {
                |          "doubleField": 3.14
                |        }
                |      },
                |      {
                |        "match": {
                |          "testField": true
                |        }
                |      },
                |      {
                |        "exists": {
                |          "field": "anotherTestField"
                |        }
                |      }
                |    ]
                |  }
                |}
                |""".stripMargin

            val expected3 =
              """
                |{
                |  "bool": {
                |    "must": [
                |      {
                |        "terms": {
                |          "stringField": ["a", "b", "c"]
                |        }
                |      }
                |    ],
                |    "must_not": [
                |      {
                |        "match": {
                |          "intField": 50
                |        }
                |      }
                |    ],
                |    "should": [
                |      {
                |        "range": {
                |          "intField": {
                |            "gt": 1,
                |            "lte": 100
                |          }
                |        }
                |      },
                |      {
                |        "match": {
                |          "stringField": "test"
                |        }
                |      }
                |    ]
                |  }
                |}
                |""".stripMargin

            val expectedWithBoost =
              """
                |{
                |  "bool": {
                |    "filter": [
                |      {
                |        "match_phrase": {
                |          "stringField": "test"
                |        }
                |      }
                |    ],
                |    "must": [
                |      {
                |        "match": {
                |          "booleanField": true
                |        }
                |      }
                |    ],
                |    "boost": 3.14
                |  }
                |}
                |""".stripMargin

            val expectedWithMinimumShouldMatch =
              """
                |{
                |  "bool": {
                |    "must": [
                |      {
                |        "terms": {
                |          "stringField": ["a", "b", "c"]
                |        }
                |      }
                |    ],
                |    "must_not": [
                |      {
                |        "match": {
                |          "doubleField": 3.14
                |        }
                |      },
                |      {
                |        "match": {
                |          "testField": true
                |        }
                |      },
                |      {
                |        "exists": {
                |          "field": "anotherTestField"
                |        }
                |      }
                |    ],
                |    "minimum_should_match": 2
                |  }
                |}
                |""".stripMargin

            val expectedWithAllParams =
              """
                |{
                |  "bool": {
                |    "must": [
                |      {
                |        "terms": {
                |          "stringField": ["a", "b", "c"]
                |        }
                |      }
                |    ],
                |    "must_not": [
                |      {
                |        "match": {
                |          "intField": 50
                |        }
                |      }
                |    ],
                |    "should": [
                |      {
                |        "range": {
                |          "intField": {
                |            "gt": 1,
                |            "lte": 100
                |          }
                |        }
                |      },
                |      {
                |        "match": {
                |          "stringField": "test"
                |        }
                |      }
                |    ],
                |    "boost": 3.14,
                |    "minimum_should_match": 3
                |  }
                |}
                |""".stripMargin

            assert(query1.toJson(fieldPath = None))(equalTo(expected1.toJson)) &&
            assert(query2.toJson(fieldPath = None))(equalTo(expected2.toJson)) &&
            assert(query3.toJson(fieldPath = None))(equalTo(expected3.toJson)) &&
            assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson)) &&
            assert(queryWithMinimumShouldMatch.toJson(fieldPath = None))(
              equalTo(expectedWithMinimumShouldMatch.toJson)
            ) &&
            assert(queryWithAllParams.toJson(fieldPath = None))(equalTo(expectedWithAllParams.toJson))
          }
        ),
        test("bulk") {
          val query = IndexName.make("users").map { index =>
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

          val expected =
            """|{ "create" : { "_index" : "users", "_id" : "ETux1srpww2ObCx", "routing" : "StringField" } }
               |{"stringField":"StringField","nestedField":{"stringField":"NestedField","longField":1},"intField":65,"intFieldList":[]}
               |{ "create" : { "_index" : "users", "routing" : "StringField" } }
               |{"stringField":"StringField","nestedField":{"stringField":"NestedField","longField":1},"intField":100,"intFieldList":[]}
               |{ "index" : { "_index" : "users", "_id" : "yMyEG8iFL5qx", "routing" : "StringField" } }
               |{"stringField":"StringField2","nestedField":{"stringField":"NestedField","longField":1},"intField":100,"intFieldList":[]}
               |{ "delete" : { "_index" : "users", "_id" : "1VNzFt2XUFZfXZheDc", "routing" : "StringField" } }
               |""".stripMargin

          assert(query)(equalTo(Validation.succeed(Some(expected))))
        },
        test("contains") {
          val query                    = contains(TestDocument.stringField, "test")
          val queryWithBoost           = contains(TestDocument.stringField, "test").boost(3.14)
          val queryWithCaseInsensitive = contains(TestDocument.stringField, "test").caseInsensitiveTrue
          val queryWithAllParams       = contains(TestDocument.stringField, "test").boost(39.2).caseInsensitiveFalse

          val expected =
            """
              |{
              |  "wildcard": {
              |    "stringField": {
              |      "value": "*test*"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithBoost =
            """
              |{
              |  "wildcard": {
              |    "stringField": {
              |      "value": "*test*",
              |      "boost": 3.14
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithCaseInsensitive =
            """
              |{
              |  "wildcard": {
              |    "stringField": {
              |      "value": "*test*",
              |      "case_insensitive": true
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "wildcard": {
              |    "stringField": {
              |      "value": "*test*",
              |      "boost": 39.2,
              |      "case_insensitive": false
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson)) &&
          assert(queryWithCaseInsensitive.toJson(fieldPath = None))(equalTo(expectedWithCaseInsensitive.toJson)) &&
          assert(queryWithAllParams.toJson(fieldPath = None))(equalTo(expectedWithAllParams.toJson))
        },
        test("exists") {
          val query   = exists("testField")
          val queryTs = exists(TestDocument.dateField)

          val expected =
            """
              |{
              |  "exists": {
              |    "field": "testField"
              |  }
              |}
              |""".stripMargin

          val expectedTs =
            """
              |{
              |  "exists": {
              |    "field": "dateField"
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryTs.toJson(fieldPath = None))(equalTo(expectedTs.toJson))
        },
        test("geoDistance") {
          val query =
            geoDistance("testField", 20.0, 21.1)
          val queryString =
            geoDistance(TestDocument.locationField, "drm3btev3e86")
          val queryWithDistance =
            geoDistance(TestDocument.locationField, 20.0, 21.1).distance(200, Kilometers)
          val queryWithDistanceType =
            geoDistance(TestDocument.locationField, 20.0, 21.1).distanceType(Plane)
          val queryWithName =
            geoDistance(TestDocument.locationField, 20.0, 21.1).name("name")
          val queryWithValidationMethod =
            geoDistance(TestDocument.locationField, 20.0, 21.1).validationMethod(IgnoreMalformed)
          val queryWithAllParams = geoDistance(TestDocument.locationField, 20.0, 21.1)
            .validationMethod(IgnoreMalformed)
            .distance(200, Kilometers)
            .distanceType(Plane)
            .name("name")

          val expected =
            """
              |{
              |  "geo_distance": {
              |    "testField": "20.0,21.1"
              |  }
              |}
              |""".stripMargin

          val expectedWithString =
            """
              |{
              |  "geo_distance": {
              |    "locationField": "drm3btev3e86"
              |  }
              |}
              |""".stripMargin

          val expectedWithDistance =
            """
              |{
              |  "geo_distance": {
              |    "distance": "200.0km",
              |    "locationField": "20.0,21.1"
              |  }
              |}
              |""".stripMargin

          val expectedWithDistanceType =
            """
              |{
              |  "geo_distance": {
              |    "distance_type" :  "plane",
              |    "locationField": "20.0,21.1"
              |  }
              |}
              |""".stripMargin

          val expectedWithName =
            """
              |{
              |  "geo_distance": {
              |    "_name": "name",
              |    "locationField": "20.0,21.1"
              |  }
              |}
              |""".stripMargin

          val expectedWithValidationMethod =
            """
              |{
              |  "geo_distance": {
              |    "validation_method": "IGNORE_MALFORMED",
              |    "locationField": "20.0,21.1"
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "geo_distance": {
              |    "validation_method": "IGNORE_MALFORMED",
              |    "distance_type" :  "plane",
              |    "_name": "name",
              |    "distance": "200.0km",
              |    "locationField": "20.0,21.1"
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryString.toJson(fieldPath = None))(equalTo(expectedWithString.toJson)) &&
          assert(queryWithDistance.toJson(fieldPath = None))(equalTo(expectedWithDistance.toJson)) &&
          assert(queryWithDistanceType.toJson(fieldPath = None))(equalTo(expectedWithDistanceType.toJson)) &&
          assert(queryWithName.toJson(fieldPath = None))(equalTo(expectedWithName.toJson)) &&
          assert(queryWithValidationMethod.toJson(fieldPath = None))(equalTo(expectedWithValidationMethod.toJson)) &&
          assert(queryWithAllParams.toJson(fieldPath = None))(equalTo(expectedWithAllParams.toJson))
        },
        test("hasChild") {
          val query                   = hasChild("child", matches(TestDocument.stringField, "test"))
          val queryWithIgnoreUnmapped = hasChild("child", matches("field", "value")).ignoreUnmappedTrue
          val queryWithInnerHits      = hasChild("child", matches("field", "value")).innerHits
          val queryWithMaxChildren    = hasChild("child", matches("field", "value")).maxChildren(5)
          val queryWithMinChildren    = hasChild("child", matches("field", "value")).minChildren(1)
          val queryWithScoreMode      = hasChild("child", matches("field", "value")).scoreMode(ScoreMode.Avg)
          val queryWithAllParams = hasChild("child", matches("field", "value"))
            .scoreMode(ScoreMode.Avg)
            .ignoreUnmappedTrue
            .innerHits
            .maxChildren(5)
            .minChildren(1)

          val expected =
            """
              |{
              |  "has_child": {
              |    "type": "child",
              |    "query": {
              |      "match": {
              |        "stringField" : "test"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithIgnoreUnmapped =
            """
              |{
              |  "has_child": {
              |    "type": "child",
              |    "ignore_unmapped": true,
              |    "query": {
              |      "match": {
              |        "field" : "value"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithInnerHits =
            """
              |{
              |  "has_child": {
              |    "type": "child",
              |    "inner_hits": {},
              |    "query": {
              |      "match": {
              |        "field" : "value"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMaxChildren =
            """
              |{
              |  "has_child": {
              |    "type": "child",
              |    "max_children": 5,
              |    "query": {
              |      "match": {
              |        "field" : "value"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMinChildren =
            """
              |{
              |  "has_child": {
              |    "type": "child",
              |    "min_children": 1,
              |    "query": {
              |      "match": {
              |        "field" : "value"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithScoreMode =
            """
              |{
              |  "has_child": {
              |    "type": "child",
              |    "score_mode": "avg",
              |    "query": {
              |      "match": {
              |        "field" : "value"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "has_child": {
              |    "type": "child",
              |    "score_mode": "avg",
              |    "ignore_unmapped": true,
              |    "inner_hits": {},
              |    "max_children": 5,
              |    "min_children": 1,
              |    "query": {
              |      "match": {
              |        "field" : "value"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryWithIgnoreUnmapped.toJson(fieldPath = None))(equalTo(expectedWithIgnoreUnmapped.toJson)) &&
          assert(queryWithInnerHits.toJson(fieldPath = None))(equalTo(expectedWithInnerHits.toJson)) &&
          assert(queryWithMaxChildren.toJson(fieldPath = None))(equalTo(expectedWithMaxChildren.toJson)) &&
          assert(queryWithMinChildren.toJson(fieldPath = None))(equalTo(expectedWithMinChildren.toJson)) &&
          assert(queryWithScoreMode.toJson(fieldPath = None))(equalTo(expectedWithScoreMode.toJson)) &&
          assert(queryWithAllParams.toJson(fieldPath = None))(equalTo(expectedWithAllParams.toJson))
        },
        test("hasParent") {
          val query                   = hasParent("parent", matches(TestDocument.stringField, "test"))
          val queryWithScore          = hasParent("parent", matches("field", "value")).withScoreFalse
          val queryWithIgnoreUnmapped = hasParent("parent", matches("field", "value")).ignoreUnmappedFalse
          val queryWithScoreAndIgnoreUnmapped =
            hasParent("parent", matches("field", "value")).withScoreTrue.ignoreUnmappedTrue
          val queryWithInnerHits = hasParent("parent", matches("field", "value")).innerHits

          val expected =
            """
              |{
              |  "has_parent": {
              |    "parent_type": "parent",
              |    "query": {
              |      "match": {
              |        "stringField" : "test"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithScore =
            """
              |{
              |  "has_parent": {
              |    "parent_type": "parent",
              |    "score": false,
              |    "query": {
              |      "match": {
              |        "field" : "value"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithIgnoreUnmapped =
            """
              |{
              |  "has_parent": {
              |    "parent_type": "parent",
              |    "ignore_unmapped": false,
              |    "query": {
              |      "match": {
              |        "field" : "value"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithScoreAndIgnoreUnmapped =
            """
              |{
              |  "has_parent": {
              |    "parent_type": "parent",
              |    "score": true,
              |    "ignore_unmapped": true,
              |    "query": {
              |      "match": {
              |        "field" : "value"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithInnerHits =
            """
              |{
              |  "has_parent": {
              |    "parent_type": "parent",
              |    "query": {
              |      "match": {
              |        "field" : "value"
              |      }
              |    },
              |    "inner_hits": {}
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryWithScore.toJson(fieldPath = None))(equalTo(expectedWithScore.toJson)) &&
          assert(queryWithIgnoreUnmapped.toJson(fieldPath = None))(equalTo(expectedWithIgnoreUnmapped.toJson)) &&
          assert(queryWithScoreAndIgnoreUnmapped.toJson(fieldPath = None))(
            equalTo(expectedWithScoreAndIgnoreUnmapped.toJson)
          ) &&
          assert(queryWithInnerHits.toJson(fieldPath = None))(equalTo(expectedWithInnerHits.toJson))
        },
        test("matchAll") {
          val query          = matchAll
          val queryWithBoost = matchAll.boost(3.14)

          val expected =
            """
              |{
              |  "match_all": {}
              |}
              |""".stripMargin

          val expectedWithBoost =
            """
              |{
              |  "match_all": {
              |    "boost": 3.14
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson))
        },
        test("matches") {
          val query         = matches("testField", true)
          val queryTsInt    = matches(TestDocument.intField, 39)
          val queryTsString = matches(TestDocument.stringField, "test")

          val expected =
            """
              |{
              |  "match": {
              |    "testField": true
              |  }
              |}
              |""".stripMargin

          val expectedTsInt =
            """
              |{
              |  "match": {
              |    "intField": 39
              |  }
              |}
              |""".stripMargin

          val expectedTsString =
            """
              |{
              |  "match": {
              |    "stringField": "test"
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryTsInt.toJson(fieldPath = None))(equalTo(expectedTsInt.toJson)) &&
          assert(queryTsString.toJson(fieldPath = None))(equalTo(expectedTsString.toJson))
        },
        test("matchPhrase") {
          val querySimple   = matchPhrase("stringField", "this is a test")
          val queryRaw      = matchPhrase("stringField.raw", "this is a test")
          val querySimpleTs = matchPhrase(TestDocument.stringField, "this is a test")
          val queryRawTs    = matchPhrase(TestDocument.stringField.raw, "this is a test")

          val expectedSimple =
            """
              |{
              |  "match_phrase": {
              |    "stringField": "this is a test"
              |  }
              |}
              |""".stripMargin

          val expectedRaw =
            """
              |{
              |  "match_phrase": {
              |    "stringField.raw": "this is a test"
              |  }
              |}
              |""".stripMargin

          assert(querySimple.toJson(fieldPath = None))(equalTo(expectedSimple.toJson)) &&
          assert(querySimpleTs.toJson(fieldPath = None))(equalTo(expectedSimple.toJson)) &&
          assert(queryRaw.toJson(fieldPath = None))(equalTo(expectedRaw.toJson)) &&
          assert(queryRawTs.toJson(fieldPath = None))(equalTo(expectedRaw.toJson))
        },
        test("nested") {
          val query                   = nested(TestDocument.subDocumentList, matchAll)
          val queryWithNested         = nested(TestDocument.subDocumentList, nested("items", term("testField", "test")))
          val queryWithIgnoreUnmapped = nested(TestDocument.subDocumentList, matchAll).ignoreUnmappedTrue
          val queryWithInnerHits =
            nested(TestDocument.subDocumentList, matchAll).innerHits(InnerHits().from(0).size(3).name("innerHitName"))
          val queryWithInnerHitsEmpty = nested(TestDocument.subDocumentList, matchAll).innerHits
          val queryWithScoreMode      = nested(TestDocument.subDocumentList, matchAll).scoreMode(ScoreMode.Avg)
          val queryWithAllParams = nested(TestDocument.subDocumentList, matchAll).ignoreUnmappedFalse
            .innerHits(InnerHits().from(10).size(20).name("innerHitName"))
            .scoreMode(ScoreMode.Min)

          val expected =
            """
              |{
              |  "nested": {
              |    "path": "subDocumentList",
              |    "query": {
              |      "match_all": {}
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithNested =
            """
              |{
              |  "nested": {
              |    "path": "subDocumentList",
              |    "query": {
              |      "nested": {
              |        "path": "subDocumentList.items",
              |        "query": {
              |          "term": {
              |            "subDocumentList.items.testField": {
              |              "value": "test"
              |            }
              |          }
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithIgnoreUnmapped =
            """
              |{
              |  "nested": {
              |    "path": "subDocumentList",
              |    "query": {
              |      "match_all": {}
              |    },
              |    "ignore_unmapped": true
              |  }
              |}
              |""".stripMargin

          val expectedWithInnerHits =
            """
              |{
              |  "nested": {
              |    "path": "subDocumentList",
              |    "query": {
              |      "match_all": {}
              |    },
              |    "inner_hits": {
              |      "from": 0,
              |      "size": 3,
              |      "name": "innerHitName"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithInnerHitsEmpty =
            """
              |{
              |  "nested": {
              |    "path": "subDocumentList",
              |    "query": {
              |      "match_all": {}
              |    },
              |    "inner_hits": {}
              |  }
              |}
              |""".stripMargin

          val expectedWithScoreMode =
            """
              |{
              |  "nested": {
              |    "path": "subDocumentList",
              |    "query": {
              |      "match_all": {}
              |    },
              |    "score_mode": "avg"
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "nested": {
              |    "path": "subDocumentList",
              |    "query": {
              |      "match_all": {}
              |    },
              |    "ignore_unmapped": false,
              |    "score_mode": "min",
              |    "inner_hits": {
              |      "from": 10,
              |      "size": 20,
              |      "name": "innerHitName"
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryWithNested.toJson(fieldPath = None))(equalTo(expectedWithNested.toJson)) &&
          assert(queryWithIgnoreUnmapped.toJson(fieldPath = None))(equalTo(expectedWithIgnoreUnmapped.toJson)) &&
          assert(queryWithInnerHits.toJson(fieldPath = None))(equalTo(expectedWithInnerHits.toJson)) &&
          assert(queryWithInnerHitsEmpty.toJson(fieldPath = None))(equalTo(expectedWithInnerHitsEmpty.toJson)) &&
          assert(queryWithScoreMode.toJson(fieldPath = None))(equalTo(expectedWithScoreMode.toJson)) &&
          assert(queryWithAllParams.toJson(fieldPath = None))(equalTo(expectedWithAllParams.toJson))
        },
        test("range") {
          val queryEmpty                = range(TestDocument.intField)
          val queryEmptyWithBoost       = range(TestDocument.intField).boost(3.14)
          val queryLowerBound           = range(TestDocument.intField).gt(23)
          val queryUpperBound           = range(TestDocument.intField).lt(45)
          val queryInclusiveLowerBound  = range(TestDocument.intField).gte(23)
          val queryInclusiveUpperBound  = range(TestDocument.intField).lte(45)
          val queryMixedBounds          = range(TestDocument.intField).gt(10).lte(99)
          val queryMixedBoundsWithBoost = range(TestDocument.intField).gt(10).lte(99).boost(3.14)
          val queryWithFormat           = range(TestDocument.dateField).gt(LocalDate.of(2020, 1, 10)).format("uuuu-MM-dd")

          val expectedEmpty =
            """
              |{
              |  "range": {
              |    "intField": {
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithBoost =
            """
              |{
              |  "range": {
              |    "intField": {
              |      "boost": 3.14
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedLowerBound =
            """
              |{
              |  "range": {
              |    "intField": {
              |      "gt": 23
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedUpperBound =
            """
              |{
              |  "range": {
              |    "intField": {
              |      "lt": 45
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedInclusiveLowerBound =
            """
              |{
              |  "range": {
              |    "intField": {
              |      "gte": 23
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedInclusiveUpperBound =
            """
              |{
              |  "range": {
              |    "intField": {
              |      "lte": 45
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedMixedBounds =
            """
              |{
              |  "range": {
              |    "intField": {
              |      "gt": 10,
              |      "lte": 99
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedMixedBoundsWithBoost =
            """
              |{
              |  "range": {
              |    "intField": {
              |      "gt": 10,
              |      "lte": 99,
              |      "boost": 3.14
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithFormat =
            """
              |{
              |  "range": {
              |    "dateField": {
              |      "gt": "2020-01-10",
              |      "format": "uuuu-MM-dd"
              |    }
              |  }
              |}
              |""".stripMargin

          assert(queryEmpty.toJson(fieldPath = None))(equalTo(expectedEmpty.toJson)) &&
          assert(queryEmptyWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson)) &&
          assert(queryLowerBound.toJson(fieldPath = None))(equalTo(expectedLowerBound.toJson)) &&
          assert(queryUpperBound.toJson(fieldPath = None))(equalTo(expectedUpperBound.toJson)) &&
          assert(queryInclusiveLowerBound.toJson(fieldPath = None))(equalTo(expectedInclusiveLowerBound.toJson)) &&
          assert(queryInclusiveUpperBound.toJson(fieldPath = None))(equalTo(expectedInclusiveUpperBound.toJson)) &&
          assert(queryMixedBounds.toJson(fieldPath = None))(equalTo(expectedMixedBounds.toJson)) &&
          assert(queryMixedBoundsWithBoost.toJson(fieldPath = None))(equalTo(expectedMixedBoundsWithBoost.toJson)) &&
          assert(queryWithFormat.toJson(fieldPath = None))(equalTo(expectedWithFormat.toJson))
        },
        test("startsWith") {
          val query                    = startsWith(TestDocument.stringField, "test")
          val queryWithBoost           = startsWith(TestDocument.stringField, "test").boost(3.14)
          val queryWithCaseInsensitive = startsWith(TestDocument.stringField, "test").caseInsensitiveTrue
          val queryWithAllParams       = startsWith(TestDocument.stringField, "test").boost(39.2).caseInsensitiveFalse

          val expected =
            """
              |{
              |  "wildcard": {
              |    "stringField": {
              |      "value": "test*"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithBoost =
            """
              |{
              |  "wildcard": {
              |    "stringField": {
              |      "value": "test*",
              |      "boost": 3.14
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithCaseInsensitive =
            """
              |{
              |  "wildcard": {
              |    "stringField": {
              |      "value": "test*",
              |      "case_insensitive": true
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "wildcard": {
              |    "stringField": {
              |      "value": "test*",
              |      "boost": 39.2,
              |      "case_insensitive": false
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson)) &&
          assert(queryWithCaseInsensitive.toJson(fieldPath = None))(equalTo(expectedWithCaseInsensitive.toJson)) &&
          assert(queryWithAllParams.toJson(fieldPath = None))(equalTo(expectedWithAllParams.toJson))
        },
        test("term") {
          val query                    = term(TestDocument.stringField, "test")
          val queryWithBoost           = term(TestDocument.stringField, "test").boost(10.21)
          val queryWithCaseInsensitive = term(TestDocument.stringField, "test").caseInsensitiveTrue
          val queryWithAllParams       = term(TestDocument.stringField, "test").boost(3.14).caseInsensitiveFalse

          val expected =
            """
              |{
              |  "term": {
              |    "stringField": {
              |      "value": "test"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithBoost =
            """
              |{
              |  "term": {
              |    "stringField": {
              |      "value": "test",
              |      "boost": 10.21
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithCaseInsensitive =
            """
              |{
              |  "term": {
              |    "stringField": {
              |      "value": "test",
              |      "case_insensitive": true
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "term": {
              |    "stringField": {
              |      "value": "test",
              |      "boost": 3.14,
              |      "case_insensitive": false
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson)) &&
          assert(queryWithCaseInsensitive.toJson(fieldPath = None))(equalTo(expectedWithCaseInsensitive.toJson)) &&
          assert(queryWithAllParams.toJson(fieldPath = None))(equalTo(expectedWithAllParams.toJson))
        },
        test("terms") {
          val query          = terms(TestDocument.stringField, "a", "b", "c")
          val queryWithBoost = terms(TestDocument.stringField, "a", "b", "c").boost(10.21)

          val expected =
            """
              |{
              |  "terms": {
              |    "stringField": [ "a", "b", "c" ]
              |  }
              |}
              |""".stripMargin

          val expectedWithBoost =
            """
              |{
              |  "terms": {
              |    "stringField": [ "a", "b", "c" ],
              |    "boost": 10.21
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson))
        },
        test("wildcard") {
          val query                    = wildcard(TestDocument.stringField, "[a-zA-Z]+")
          val queryWithBoost           = wildcard(TestDocument.stringField, "[a-zA-Z]+").boost(3.14)
          val queryWithCaseInsensitive = wildcard(TestDocument.stringField, "[a-zA-Z]+").caseInsensitiveTrue
          val queryWithAllParams       = wildcard(TestDocument.stringField, "[a-zA-Z]+").boost(39.2).caseInsensitiveFalse

          val expected =
            """
              |{
              |  "wildcard": {
              |    "stringField": {
              |      "value": "[a-zA-Z]+"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithBoost =
            """
              |{
              |  "wildcard": {
              |    "stringField": {
              |      "value": "[a-zA-Z]+",
              |      "boost": 3.14
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithCaseInsensitive =
            """
              |{
              |  "wildcard": {
              |    "stringField": {
              |      "value": "[a-zA-Z]+",
              |      "case_insensitive": true
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "wildcard": {
              |    "stringField": {
              |      "value": "[a-zA-Z]+",
              |      "boost": 39.2,
              |      "case_insensitive": false
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson)) &&
          assert(queryWithCaseInsensitive.toJson(fieldPath = None))(equalTo(expectedWithCaseInsensitive.toJson)) &&
          assert(queryWithAllParams.toJson(fieldPath = None))(equalTo(expectedWithAllParams.toJson))
        }
      )
    )
}
