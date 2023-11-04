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
import zio.elasticsearch.ElasticHighlight.highlight
import zio.elasticsearch.ElasticQuery.{script => _, _}
import zio.elasticsearch.data.GeoPoint
import zio.elasticsearch.domain._
import zio.elasticsearch.query.DistanceType.Plane
import zio.elasticsearch.query.DistanceUnit.Kilometers
import zio.elasticsearch.query.FunctionScoreFunction._
import zio.elasticsearch.query.MultiMatchType._
import zio.elasticsearch.query.MultiValueMode.Max
import zio.elasticsearch.query.ValidationMethod.IgnoreMalformed
import zio.elasticsearch.query._
import zio.elasticsearch.script.{Painless, Script}
import zio.elasticsearch.utils._
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
                    Exists(field = "booleanField", boost = None)
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
                    Exists(field = "booleanField", boost = None)
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
                  filter = Chunk(MatchPhrase(field = "stringField", value = "test", boost = None)),
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
                    Exists(field = "anotherTestField", boost = None)
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
                  filter = Chunk(MatchPhrase(field = "stringField", value = "test", boost = None)),
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
                    Exists(field = "anotherTestField", boost = None)
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
        test("constantScore") {
          val query          = constantScore(terms("stringField", "a", "b", "c"))
          val queryTs        = constantScore(terms(TestDocument.stringField, "a", "b", "c"))
          val queryWithBoost = constantScore(terms(TestDocument.stringField, "a", "b", "c")).boost(2.2)

          assert(query)(
            equalTo(
              ConstantScore[Any](
                Terms(field = "stringField", values = Chunk("a", "b", "c"), boost = None),
                boost = None
              )
            )
          ) &&
          assert(queryTs)(
            equalTo(
              ConstantScore[TestDocument](
                Terms(field = "stringField", values = Chunk("a", "b", "c"), boost = None),
                boost = None
              )
            )
          ) &&
          assert(queryWithBoost)(
            equalTo(
              ConstantScore[TestDocument](
                Terms(field = "stringField", values = Chunk("a", "b", "c"), boost = None),
                boost = Some(2.2)
              )
            )
          )
        },
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
          val query          = exists("testField")
          val queryTs        = exists(TestDocument.intField)
          val queryWithBoost = exists(TestDocument.intField).boost(3)

          assert(query)(equalTo(Exists[Any](field = "testField", boost = None))) &&
          assert(queryTs)(equalTo(Exists[TestDocument](field = "intField", boost = None))) &&
          assert(queryWithBoost)(equalTo(Exists[TestDocument](field = "intField", boost = Some(3))))

        },
        test("functionScore") {
          val scriptScore = scriptScoreFunction(Script("params.agg1 + params.agg2 > 10"))
          val weight      = weightFunction(10.0)
          val randomScore = randomScoreFunction()
          val fieldValue  = fieldValueFactor(TestDocument.stringField)
          val decay       = expDecayFunction("field", origin = "11, 12", scale = "2km")
          val typedDecay  = expDecayFunction(TestDocument.intField, origin = "11,12", scale = "2km")

          val fullQuery: FunctionScoreQuery[TestDocument] = functionScore(scriptScore, weight, randomScore)
            .withFunctions(decay)
            .withFunctions(fieldValue)
            .boost(2.0)
            .boostMode(FunctionScoreBoostMode.Avg)
            .maxBoost(42)
            .minScore(32)
            .query(matches("stringField", "value"))
            .scoreMode(FunctionScoreScoreMode.Min)

          val queryWithType: FunctionScoreQuery[TestDocument] =
            functionScore(fieldValue).query(matches(TestDocument.stringField, "value"))
          val queryTypeShrink: FunctionScoreQuery[TestDocument] =
            functionScore(scriptScore).query(matches(TestDocument.stringField, "value"))
          val queryWithoutTypeShrink: FunctionScoreQuery[Any] =
            functionScore(scriptScore).query(matches("stringField", "value"))
          val queryWithNewAnyQuery: FunctionScoreQuery[TestDocument] =
            functionScore(fieldValue).query(matches("stringField", "value"))

          val anyQueryWithNewTypedFunction   = functionScore(scriptScore).withFunctions(fieldValue)
          val anyQueryWithNewAnyFunction     = functionScore(scriptScore).withFunctions(weight)
          val typedQueryWithNewTypedFunction = functionScore(fieldValue).withFunctions(typedDecay)
          val typedQueryWithNewAnyFunction   = functionScore(fieldValue).withFunctions(weight)

          assert(fullQuery)(
            equalTo(
              FunctionScore[TestDocument](
                functionScoreFunctions = Chunk(
                  scriptScore,
                  weight,
                  randomScore,
                  decay,
                  fieldValue
                ),
                boost = Some(2.0),
                boostMode = Some(FunctionScoreBoostMode.Avg),
                maxBoost = Some(42.0),
                minScore = Some(32.0),
                query = Some(Match("stringField", "value")),
                scoreMode = Some(FunctionScoreScoreMode.Min)
              )
            )
          ) &&
          assert(queryTypeShrink)(
            equalTo(
              FunctionScore[TestDocument](
                functionScoreFunctions = Chunk(scriptScore),
                boost = None,
                boostMode = None,
                maxBoost = None,
                minScore = None,
                query = Some(Match("stringField", "value")),
                scoreMode = None
              )
            )
          ) &&
          assert(queryWithType)(
            equalTo(
              FunctionScore[TestDocument](
                functionScoreFunctions = Chunk(fieldValue),
                boost = None,
                boostMode = None,
                maxBoost = None,
                minScore = None,
                query = Some(Match("stringField", "value")),
                scoreMode = None
              )
            )
          ) &&
          assert(queryWithoutTypeShrink)(
            equalTo(
              FunctionScore[Any](
                functionScoreFunctions = Chunk(scriptScore),
                boost = None,
                boostMode = None,
                maxBoost = None,
                minScore = None,
                query = Some(Match("stringField", "value")),
                scoreMode = None
              )
            )
          ) &&
          assert(queryWithNewAnyQuery)(
            equalTo(
              FunctionScore[TestDocument](
                functionScoreFunctions = Chunk(fieldValue),
                boost = None,
                boostMode = None,
                maxBoost = None,
                minScore = None,
                query = Some(Match("stringField", "value")),
                scoreMode = None
              )
            )
          ) &&
          assert(anyQueryWithNewTypedFunction)(
            equalTo(
              FunctionScore[TestDocument](
                functionScoreFunctions = Chunk(scriptScore, fieldValue),
                boost = None,
                boostMode = None,
                maxBoost = None,
                minScore = None,
                query = None,
                scoreMode = None
              )
            )
          ) &&
          assert(anyQueryWithNewAnyFunction)(
            equalTo(
              FunctionScore[Any](
                functionScoreFunctions = Chunk(scriptScore, weight),
                boost = None,
                boostMode = None,
                maxBoost = None,
                minScore = None,
                query = None,
                scoreMode = None
              )
            )
          ) &&
          assert(typedQueryWithNewTypedFunction)(
            equalTo(
              FunctionScore[TestDocument](
                functionScoreFunctions = Chunk(fieldValue, typedDecay),
                boost = None,
                boostMode = None,
                maxBoost = None,
                minScore = None,
                query = None,
                scoreMode = None
              )
            )
          ) &&
          assert(typedQueryWithNewAnyFunction)(
            equalTo(
              FunctionScore[TestDocument](
                functionScoreFunctions = Chunk(fieldValue, weight),
                boost = None,
                boostMode = None,
                maxBoost = None,
                minScore = None,
                query = None,
                scoreMode = None
              )
            )
          )
        },
        test("fuzzy") {
          val query                  = fuzzy("stringField", "test")
          val queryTs                = fuzzy(TestDocument.stringField, "test")
          val queryWithFuzzinessAuto = fuzzy(TestDocument.stringField, "test").fuzziness("AUTO")
          val queryWithMaxExpansions = fuzzy(TestDocument.stringField, "test").maxExpansions(50)
          val queryWithPrefixLength  = fuzzy(TestDocument.stringField, "test").prefixLength(3)
          val queryWithAllParameters =
            fuzzy(TestDocument.stringField, "test").prefixLength(3).fuzziness("AUTO").maxExpansions(50)
          val queryWithSuffix = fuzzy(TestDocument.stringField.raw, "test")

          assert(query)(
            equalTo(
              Fuzzy[Any](
                field = "stringField",
                value = "test",
                fuzziness = None,
                maxExpansions = None,
                prefixLength = None
              )
            )
          ) &&
          assert(queryTs)(
            equalTo(
              Fuzzy[TestDocument](
                field = "stringField",
                value = "test",
                fuzziness = None,
                maxExpansions = None,
                prefixLength = None
              )
            )
          ) &&
          assert(queryWithFuzzinessAuto)(
            equalTo(
              Fuzzy[TestDocument](
                field = "stringField",
                value = "test",
                fuzziness = Some("AUTO"),
                maxExpansions = None,
                prefixLength = None
              )
            )
          ) &&
          assert(queryWithMaxExpansions)(
            equalTo(
              Fuzzy[TestDocument](
                field = "stringField",
                value = "test",
                fuzziness = None,
                maxExpansions = Some(50),
                prefixLength = None
              )
            )
          ) &&
          assert(queryWithPrefixLength)(
            equalTo(
              Fuzzy[TestDocument](
                field = "stringField",
                value = "test",
                fuzziness = None,
                maxExpansions = None,
                prefixLength = Some(3)
              )
            )
          ) &&
          assert(queryWithSuffix)(
            equalTo(
              Fuzzy[TestDocument](
                field = "stringField.raw",
                value = "test",
                fuzziness = None,
                maxExpansions = None,
                prefixLength = None
              )
            )
          ) &&
          assert(queryWithAllParameters)(
            equalTo(
              Fuzzy[TestDocument](
                field = "stringField",
                value = "test",
                fuzziness = Some("AUTO"),
                maxExpansions = Some(50),
                prefixLength = Some(3)
              )
            )
          )
        },
        test("geoDistance") {
          val queryWithHash =
            geoDistance(TestDocument.geoPointField, GeoHash("drm3btev3e86"), Distance(200, Kilometers))
          val queryWithPoint =
            geoDistance(TestDocument.geoPointField, GeoPoint(20.0, 21.1), Distance(200, Kilometers))
          val queryWithDistanceType =
            geoDistance(TestDocument.geoPointField, GeoPoint(20.0, 21.1), Distance(200, Kilometers)).distanceType(Plane)
          val queryWithName =
            geoDistance(TestDocument.geoPointField, GeoPoint(20.0, 21.1), Distance(200, Kilometers)).name("name")
          val queryWithValidationMethod =
            geoDistance(TestDocument.geoPointField, GeoPoint(20.0, 21.1), Distance(200, Kilometers)).validationMethod(
              IgnoreMalformed
            )
          val queryWithAllParams =
            geoDistance(TestDocument.geoPointField, GeoPoint(20.0, 21.1), Distance(200, Kilometers))
              .validationMethod(IgnoreMalformed)
              .distanceType(Plane)
              .name("name")

          assert(queryWithHash)(
            equalTo(
              GeoDistance[TestDocument](
                field = "geoPointField",
                point = "drm3btev3e86",
                distance = Distance(200, Kilometers),
                distanceType = None,
                queryName = None,
                validationMethod = None
              )
            )
          ) &&
          assert(queryWithPoint)(
            equalTo(
              GeoDistance[TestDocument](
                field = "geoPointField",
                point = "20.0,21.1",
                distance = Distance(200, Kilometers),
                distanceType = None,
                queryName = None,
                validationMethod = None
              )
            )
          ) && assert(queryWithDistanceType)(
            equalTo(
              GeoDistance[TestDocument](
                field = "geoPointField",
                point = "20.0,21.1",
                distance = Distance(200, Kilometers),
                distanceType = Some(Plane),
                queryName = None,
                validationMethod = None
              )
            )
          ) && assert(queryWithName)(
            equalTo(
              GeoDistance[TestDocument](
                field = "geoPointField",
                point = "20.0,21.1",
                distance = Distance(200, Kilometers),
                distanceType = None,
                queryName = Some("name"),
                validationMethod = None
              )
            )
          ) && assert(queryWithValidationMethod)(
            equalTo(
              GeoDistance[TestDocument](
                field = "geoPointField",
                point = "20.0,21.1",
                distance = Distance(200, Kilometers),
                distanceType = None,
                queryName = None,
                validationMethod = Some(IgnoreMalformed)
              )
            )
          ) && assert(queryWithAllParams)(
            equalTo(
              GeoDistance[TestDocument](
                field = "geoPointField",
                point = "20.0,21.1",
                distance = Distance(200, Kilometers),
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
          val queryWithBoost               = hasParent("parent", matchAll).boost(3)
          val queryWithScoreTrue           = hasParent("parent", matchAll).withScoreTrue
          val queryWithScoreFalse          = hasParent("parent", matchAll).withScoreFalse
          val queryWithIgnoreUnmappedTrue  = hasParent("parent", matchAll).ignoreUnmappedTrue
          val queryWithIgnoreUnmappedFalse = hasParent("parent", matchAll).ignoreUnmappedFalse
          val queryWithAllParams           = hasParent("parent", matchAll).boost(3).ignoreUnmappedFalse.withScoreTrue

          assert(query)(
            equalTo(
              HasParent[Any](
                parentType = "parent",
                query = matchAll,
                boost = None,
                ignoreUnmapped = None,
                innerHitsField = None,
                score = None
              )
            )
          ) && assert(queryWithBoost)(
            equalTo(
              HasParent[Any](
                parentType = "parent",
                query = matchAll,
                boost = Some(3.0),
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
                boost = None,
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
                boost = None,
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
                boost = None,
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
                boost = None,
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
                boost = Some(3.0),
                ignoreUnmapped = Some(false),
                innerHitsField = None,
                score = Some(true)
              )
            )
          )
        },
        test("ids") {
          val idsQuery = ids("1", "2", "3")

          assert(idsQuery)(
            equalTo(
              Ids[Any](
                values = Chunk("1", "2", "3")
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
        test("matchBooleanPrefix") {
          val queryString                 = matchBooleanPrefix("stringField", "test")
          val queryBool                   = matchBooleanPrefix("booleanField", true)
          val queryInt                    = matchBooleanPrefix("intField", 1)
          val queryStringTs               = matchBooleanPrefix(TestDocument.stringField, "test")
          val queryBoolTs                 = matchBooleanPrefix(TestDocument.booleanField, true)
          val queryIntTs                  = matchBooleanPrefix(TestDocument.intField, 1)
          val queryWithSuffix             = matchBooleanPrefix(TestDocument.stringField.raw, "test")
          val queryWithMinimumShouldMatch = matchBooleanPrefix(TestDocument.stringField, "test").minimumShouldMatch(3)

          assert(queryString)(
            equalTo(MatchBooleanPrefix[Any, String](field = "stringField", value = "test", minimumShouldMatch = None))
          ) &&
          assert(queryBool)(
            equalTo(MatchBooleanPrefix[Any, Boolean](field = "booleanField", value = true, minimumShouldMatch = None))
          ) &&
          assert(queryInt)(
            equalTo(MatchBooleanPrefix[Any, Int](field = "intField", value = 1, minimumShouldMatch = None))
          ) &&
          assert(queryStringTs)(
            equalTo(
              MatchBooleanPrefix[TestDocument, String](field = "stringField", value = "test", minimumShouldMatch = None)
            )
          ) &&
          assert(queryBoolTs)(
            equalTo(
              MatchBooleanPrefix[TestDocument, Boolean](field = "booleanField", value = true, minimumShouldMatch = None)
            )
          ) &&
          assert(queryIntTs)(
            equalTo(MatchBooleanPrefix[TestDocument, Int](field = "intField", value = 1, minimumShouldMatch = None))
          ) &&
          assert(queryWithSuffix)(
            equalTo(
              MatchBooleanPrefix[TestDocument, String](
                field = "stringField.raw",
                value = "test",
                minimumShouldMatch = None
              )
            )
          ) &&
          assert(queryWithMinimumShouldMatch)(
            equalTo(
              MatchBooleanPrefix[TestDocument, String](
                field = "stringField",
                value = "test",
                minimumShouldMatch = Some(3)
              )
            )
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
          val queryWithBoost  = matchPhrase(TestDocument.stringField, "this is a test").boost(3)

          assert(query)(equalTo(MatchPhrase[Any](field = "stringField", value = "this is a test", boost = None))) &&
          assert(queryTs)(
            equalTo(MatchPhrase[TestDocument](field = "stringField", value = "this is a test", boost = None))
          ) &&
          assert(queryWithSuffix)(
            equalTo(MatchPhrase[TestDocument](field = "stringField.raw", value = "this is a test", boost = None))
          ) &&
          assert(queryWithBoost)(
            equalTo(MatchPhrase[TestDocument](field = "stringField", value = "this is a test", boost = Some(3)))
          )
        },
        test("matchPhrasePrefix") {
          val query   = matchPhrasePrefix("stringField", "test")
          val queryTs = matchPhrasePrefix(TestDocument.stringField, "test")

          assert(query)(equalTo(MatchPhrasePrefix[Any](field = "stringField", value = "test"))) &&
          assert(queryTs)(equalTo(MatchPhrasePrefix[TestDocument](field = "stringField", value = "test")))
        },
        test("multiMatch") {
          val query                       = multiMatch("this is a test")
          val queryWithFields             = multiMatch("this is a test").fields("stringField1", "stringField2")
          val queryWithFieldsTs           = multiMatch("this is a test").fields(TestDocument.stringField)
          val queryWithFieldsSuffix       = multiMatch("this is a test").fields(TestDocument.stringField.raw)
          val queryWithType               = multiMatch("this is a test").matchingType(BestFields)
          val queryWithBoost              = multiMatch("this is a test").boost(2.2)
          val queryWithMinimumShouldMatch = multiMatch("this is a test").minimumShouldMatch(2)
          val queryWithAllParams = multiMatch("this is a test")
            .fields(TestDocument.stringField)
            .matchingType(BestFields)
            .boost(2.2)
            .minimumShouldMatch(2)

          assert(query)(
            equalTo(
              MultiMatch[Any](
                fields = Chunk.empty,
                value = "this is a test",
                matchingType = None,
                boost = None,
                minimumShouldMatch = None
              )
            )
          ) &&
          assert(queryWithFields)(
            equalTo(
              MultiMatch[Any](
                fields = Chunk("stringField1", "stringField2"),
                value = "this is a test",
                matchingType = None,
                boost = None,
                minimumShouldMatch = None
              )
            )
          ) &&
          assert(queryWithFieldsTs)(
            equalTo(
              MultiMatch[TestDocument](
                fields = Chunk("stringField"),
                value = "this is a test",
                matchingType = None,
                boost = None,
                minimumShouldMatch = None
              )
            )
          ) &&
          assert(queryWithFieldsSuffix)(
            equalTo(
              MultiMatch[TestDocument](
                fields = Chunk("stringField.raw"),
                value = "this is a test",
                matchingType = None,
                boost = None,
                minimumShouldMatch = None
              )
            )
          ) &&
          assert(queryWithType)(
            equalTo(
              MultiMatch[Any](
                fields = Chunk.empty,
                value = "this is a test",
                matchingType = Some(BestFields),
                boost = None,
                minimumShouldMatch = None
              )
            )
          ) &&
          assert(queryWithBoost)(
            equalTo(
              MultiMatch[Any](
                fields = Chunk.empty,
                value = "this is a test",
                matchingType = None,
                boost = Some(2.2),
                minimumShouldMatch = None
              )
            )
          ) &&
          assert(queryWithMinimumShouldMatch)(
            equalTo(
              MultiMatch[Any](
                fields = Chunk.empty,
                value = "this is a test",
                matchingType = None,
                boost = None,
                minimumShouldMatch = Some(2)
              )
            )
          ) &&
          assert(queryWithAllParams)(
            equalTo(
              MultiMatch[TestDocument](
                fields = Chunk("stringField"),
                value = "this is a test",
                matchingType = Some(BestFields),
                boost = Some(2.2),
                minimumShouldMatch = Some(2)
              )
            )
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
                innerHitsField = Some(
                  InnerHits(
                    excluded = Chunk(),
                    included = Chunk(),
                    from = Some(0),
                    highlights = None,
                    name = Some("innerHitName"),
                    size = Some(3)
                  )
                )
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
                innerHitsField = Some(
                  InnerHits(
                    excluded = Chunk(),
                    included = Chunk(),
                    from = None,
                    highlights = None,
                    name = None,
                    size = None
                  )
                )
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
                innerHitsField = Some(
                  InnerHits(
                    excluded = Chunk(),
                    included = Chunk(),
                    from = None,
                    highlights = None,
                    name = Some("innerHitName"),
                    size = None
                  )
                )
              )
            )
          )
        },
        test("prefix") {
          val query                    = prefix("stringField", "test")
          val queryTs                  = prefix(TestDocument.stringField, "test")
          val queryWithSuffix          = prefix(TestDocument.stringField.keyword, "test")
          val queryWithCaseInsensitive = prefix(TestDocument.stringField, "test").caseInsensitiveTrue

          assert(query)(
            equalTo(Prefix[Any](field = "stringField", value = "test", caseInsensitive = None))
          ) &&
          assert(queryTs)(
            equalTo(Prefix[TestDocument](field = "stringField", value = "test", caseInsensitive = None))
          ) &&
          assert(queryWithSuffix)(
            equalTo(
              Prefix[TestDocument](field = "stringField.keyword", value = "test", caseInsensitive = None)
            )
          ) &&
          assert(queryWithCaseInsensitive)(
            equalTo(
              Prefix[TestDocument](field = "stringField", value = "test", caseInsensitive = Some(true))
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
          val queryWithFormatParam     = range(TestDocument.dateField).gt(LocalDate.of(2023, 5, 11)).format("yyyy-MM-dd")

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
                format = Some("yyyy-MM-dd")
              )
            )
          )
        },
        test("regexp") {
          val query                    = regexp("stringField", "t.*st")
          val queryTs                  = regexp(TestDocument.stringField, "t.*st")
          val queryWithCaseInsensitive = regexp(TestDocument.stringField, "t.*st").caseInsensitiveTrue
          val queryWithSuffix          = regexp(TestDocument.stringField.raw, "t.*st")

          assert(query)(equalTo(Regexp[Any](field = "stringField", value = "t.*st", caseInsensitive = None))) &&
          assert(queryTs)(
            equalTo(Regexp[TestDocument](field = "stringField", value = "t.*st", caseInsensitive = None))
          ) &&
          assert(queryWithCaseInsensitive)(
            equalTo(Regexp[TestDocument](field = "stringField", value = "t.*st", caseInsensitive = Some(true)))
          ) &&
          assert(queryWithSuffix)(
            equalTo(Regexp[TestDocument](field = "stringField.raw", value = "t.*st", caseInsensitive = None))
          )
        },
        test("script") {
          val query =
            ElasticQuery.script(Script("doc['day_of_week'].value > params['day']").params("day" -> 2).lang(Painless))
          val queryWithBoost = ElasticQuery.script(Script("doc['day_of_week'].value > 2")).boost(2.0)

          assert(query)(
            equalTo(
              zio.elasticsearch.query.Script(
                script = Script(
                  source = "doc['day_of_week'].value > params['day']",
                  params = Map("day" -> 2),
                  lang = Some(Painless)
                ),
                boost = None
              )
            )
          ) &&
          assert(queryWithBoost)(
            equalTo(
              zio.elasticsearch.query.Script(
                script = Script(
                  source = "doc['day_of_week'].value > 2",
                  params = Map.empty,
                  lang = None
                ),
                boost = Some(2.0)
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
          val queryString              = term("stringField", "test")
          val queryBool                = term("booleanField", true)
          val queryInt                 = term("intField", 1)
          val queryStringTs            = term(TestDocument.stringField, "test")
          val queryBoolTs              = term(TestDocument.booleanField, true)
          val queryIntTs               = term(TestDocument.intField, 1)
          val queryWithSuffix          = term(TestDocument.stringField.keyword, "test")
          val queryWithBoost           = term(TestDocument.stringField, "test").boost(10.21)
          val queryWithCaseInsensitive = term(TestDocument.stringField, "test").caseInsensitiveTrue
          val queryAllParams           = term(TestDocument.stringField, "test").boost(3.14).caseInsensitiveFalse

          assert(queryString)(
            equalTo(Term[Any, String](field = "stringField", value = "test", boost = None, caseInsensitive = None))
          ) &&
          assert(queryBool)(
            equalTo(Term[Any, Boolean](field = "booleanField", value = true, boost = None, caseInsensitive = None))
          ) &&
          assert(queryInt)(
            equalTo(Term[Any, Int](field = "intField", value = 1, boost = None, caseInsensitive = None))
          ) &&
          assert(queryStringTs)(
            equalTo(
              Term[TestDocument, String](field = "stringField", value = "test", boost = None, caseInsensitive = None)
            )
          ) &&
          assert(queryBoolTs)(
            equalTo(
              Term[TestDocument, Boolean](field = "booleanField", value = true, boost = None, caseInsensitive = None)
            )
          ) &&
          assert(queryIntTs)(
            equalTo(Term[TestDocument, Int](field = "intField", value = 1, boost = None, caseInsensitive = None))
          ) &&
          assert(queryWithSuffix)(
            equalTo(
              Term[TestDocument, String](
                field = "stringField.keyword",
                value = "test",
                boost = None,
                caseInsensitive = None
              )
            )
          ) &&
          assert(queryWithBoost)(
            equalTo(
              Term[TestDocument, String](
                field = "stringField",
                value = "test",
                boost = Some(10.21),
                caseInsensitive = None
              )
            )
          ) &&
          assert(queryWithCaseInsensitive)(
            equalTo(
              Term[TestDocument, String](
                field = "stringField",
                value = "test",
                boost = None,
                caseInsensitive = Some(true)
              )
            )
          ) &&
          assert(queryAllParams)(
            equalTo(
              Term[TestDocument, String](
                field = "stringField",
                value = "test",
                boost = Some(3.14),
                caseInsensitive = Some(false)
              )
            )
          )
        },
        test("terms") {
          val queryString     = terms("stringField", "a", "b", "c")
          val queryBool       = terms("booleanField", true, false)
          val queryInt        = terms("intField", 1, 2, 3)
          val queryStringTs   = terms(TestDocument.stringField, "a", "b", "c")
          val queryBoolTs     = terms(TestDocument.booleanField, true, false)
          val queryIntTs      = terms(TestDocument.intField, 1, 2, 3)
          val queryWithSuffix = terms(TestDocument.stringField.keyword, "a", "b", "c")
          val queryWithBoost  = terms(TestDocument.stringField, "a", "b", "c").boost(10.21)

          assert(queryString)(
            equalTo(Terms[Any, String](field = "stringField", values = Chunk("a", "b", "c"), boost = None))
          ) &&
          assert(queryBool)(
            equalTo(Terms[Any, Boolean](field = "booleanField", values = Chunk(true, false), boost = None))
          ) &&
          assert(queryInt)(
            equalTo(Terms[Any, Int](field = "intField", values = Chunk(1, 2, 3), boost = None))
          ) &&
          assert(queryStringTs)(
            equalTo(Terms[TestDocument, String](field = "stringField", values = Chunk("a", "b", "c"), boost = None))
          ) &&
          assert(queryBoolTs)(
            equalTo(Terms[TestDocument, Boolean](field = "booleanField", values = Chunk(true, false), boost = None))
          ) &&
          assert(queryIntTs)(
            equalTo(Terms[TestDocument, Int](field = "intField", values = Chunk(1, 2, 3), boost = None))
          ) &&
          assert(queryWithSuffix)(
            equalTo(
              Terms[TestDocument, String](field = "stringField.keyword", values = Chunk("a", "b", "c"), boost = None)
            )
          ) &&
          assert(queryWithBoost)(
            equalTo(
              Terms[TestDocument, String](field = "stringField", values = Chunk("a", "b", "c"), boost = Some(10.21))
            )
          )
        },
        test("termsSet") {
          val queryString     = termsSet("stringField", "required_matches", "a", "b", "c")
          val queryBool       = termsSet("booleanField", "required_matches", true, false)
          val queryInt        = termsSet("intField", "required_matches", 1, 2, 3)
          val queryStringTs   = termsSet(TestDocument.stringField, "required_matches", "a", "b", "c")
          val queryBoolTs     = termsSet(TestDocument.booleanField, "required_matches", true, false)
          val queryIntTs      = termsSet(TestDocument.intField, "required_matches", 1, 2, 3)
          val queryWithSuffix = termsSet(TestDocument.stringField.keyword, "required_matches", "a", "b", "c")
          val queryWithBoost  = termsSet("intField", "required_matches", 1, 2, 3).boost(10.0)

          assert(queryString)(
            equalTo(
              TermsSet[Any, String](
                field = "stringField",
                terms = Chunk("a", "b", "c"),
                minimumShouldMatchField = Some("required_matches"),
                minimumShouldMatchScript = None,
                boost = None
              )
            )
          ) &&
          assert(queryBool)(
            equalTo(
              TermsSet[Any, Boolean](
                field = "booleanField",
                terms = Chunk(true, false),
                minimumShouldMatchField = Some("required_matches"),
                minimumShouldMatchScript = None,
                boost = None
              )
            )
          ) &&
          assert(queryInt)(
            equalTo(
              TermsSet[Any, Int](
                field = "intField",
                terms = Chunk(1, 2, 3),
                minimumShouldMatchField = Some("required_matches"),
                minimumShouldMatchScript = None,
                boost = None
              )
            )
          ) &&
          assert(queryStringTs)(
            equalTo(
              TermsSet[TestDocument, String](
                field = "stringField",
                terms = Chunk("a", "b", "c"),
                minimumShouldMatchField = Some("required_matches"),
                minimumShouldMatchScript = None,
                boost = None
              )
            )
          ) &&
          assert(queryBoolTs)(
            equalTo(
              TermsSet[TestDocument, Boolean](
                field = "booleanField",
                terms = Chunk(true, false),
                minimumShouldMatchField = Some("required_matches"),
                minimumShouldMatchScript = None,
                boost = None
              )
            )
          ) &&
          assert(queryIntTs)(
            equalTo(
              TermsSet[TestDocument, Int](
                field = "intField",
                terms = Chunk(1, 2, 3),
                minimumShouldMatchField = Some("required_matches"),
                minimumShouldMatchScript = None,
                boost = None
              )
            )
          ) &&
          assert(queryWithSuffix)(
            equalTo(
              TermsSet[TestDocument, String](
                field = "stringField.keyword",
                terms = Chunk("a", "b", "c"),
                minimumShouldMatchField = Some("required_matches"),
                minimumShouldMatchScript = None,
                boost = None
              )
            )
          ) &&
          assert(queryWithBoost)(
            equalTo(
              TermsSet[Any, Int](
                field = "intField",
                terms = Chunk(1, 2, 3),
                minimumShouldMatchField = Some("required_matches"),
                minimumShouldMatchScript = None,
                boost = Some(10.0)
              )
            )
          )
        },
        test("termsSetScript") {
          val queryString   = termsSetScript("stringField", Script("doc['intField'].value"), "a", "b", "c")
          val queryBool     = termsSetScript("booleanField", Script("doc['intField'].value"), true, false)
          val queryInt      = termsSetScript("intField", Script("doc['intField'].value"), 1, 2, 3)
          val queryStringTs = termsSetScript(TestDocument.stringField, Script("doc['intField'].value"), "a", "b", "c")
          val queryBoolTs   = termsSetScript(TestDocument.booleanField, Script("doc['intField'].value"), true, false)
          val queryIntTs    = termsSetScript(TestDocument.intField, Script("doc['intField'].value"), 1, 2, 3)
          val queryWithSuffix =
            termsSetScript(TestDocument.stringField.keyword, Script("doc['intField'].value"), "a", "b", "c")
          val queryWithBoost = termsSetScript("intField", Script("doc['intField'].value"), 1, 2, 3).boost(10.0)

          assert(queryString)(
            equalTo(
              TermsSet[Any, String](
                field = "stringField",
                terms = Chunk("a", "b", "c"),
                minimumShouldMatchField = None,
                minimumShouldMatchScript = Some(Script("doc['intField'].value")),
                boost = None
              )
            )
          ) &&
          assert(queryBool)(
            equalTo(
              TermsSet[Any, Boolean](
                field = "booleanField",
                terms = Chunk(true, false),
                minimumShouldMatchField = None,
                minimumShouldMatchScript = Some(Script("doc['intField'].value")),
                boost = None
              )
            )
          ) &&
          assert(queryInt)(
            equalTo(
              TermsSet[Any, Int](
                field = "intField",
                terms = Chunk(1, 2, 3),
                minimumShouldMatchField = None,
                minimumShouldMatchScript = Some(Script("doc['intField'].value")),
                boost = None
              )
            )
          ) &&
          assert(queryStringTs)(
            equalTo(
              TermsSet[TestDocument, String](
                field = "stringField",
                terms = Chunk("a", "b", "c"),
                minimumShouldMatchField = None,
                minimumShouldMatchScript = Some(Script("doc['intField'].value")),
                boost = None
              )
            )
          ) &&
          assert(queryBoolTs)(
            equalTo(
              TermsSet[TestDocument, Boolean](
                field = "booleanField",
                terms = Chunk(true, false),
                minimumShouldMatchField = None,
                minimumShouldMatchScript = Some(Script("doc['intField'].value")),
                boost = None
              )
            )
          ) &&
          assert(queryIntTs)(
            equalTo(
              TermsSet[TestDocument, Int](
                field = "intField",
                terms = Chunk(1, 2, 3),
                minimumShouldMatchField = None,
                minimumShouldMatchScript = Some(Script("doc['intField'].value")),
                boost = None
              )
            )
          ) &&
          assert(queryWithSuffix)(
            equalTo(
              TermsSet[TestDocument, String](
                field = "stringField.keyword",
                terms = Chunk("a", "b", "c"),
                minimumShouldMatchField = None,
                minimumShouldMatchScript = Some(Script("doc['intField'].value")),
                boost = None
              )
            )
          ) &&
          assert(queryWithBoost)(
            equalTo(
              TermsSet[Any, Int](
                field = "intField",
                terms = Chunk(1, 2, 3),
                minimumShouldMatchField = None,
                minimumShouldMatchScript = Some(Script("doc['intField'].value")),
                boost = Some(10.0)
              )
            )
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
        test("constantScore") {
          val query          = constantScore(matchPhrase("stringField", "test"))
          val queryTs        = constantScore(matchPhrase(TestDocument.stringField, "test"))
          val queryWithBoost = constantScore(matchPhrase(TestDocument.stringField, "test")).boost(1.5)

          val expected =
            """
              |{
              |  "constant_score": {
              |    "filter": {
              |      "match_phrase": {
              |         "stringField": "test"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin
          val expectedWithBoost =
            """
              |{
              |  "constant_score": {
              |    "filter": {
              |      "match_phrase": {
              |         "stringField": "test"
              |      }
              |    },
              |    "boost": 1.5
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryTs.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson))
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
          val query            = exists("testField")
          val queryTs          = exists(TestDocument.dateField)
          val queryTsWithBoost = exists(TestDocument.dateField).boost(3)

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

          val expectedTsWithBoost =
            """
              |{
              |  "exists": {
              |    "field": "dateField",
              |    "boost": 3.0
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryTs.toJson(fieldPath = None))(equalTo(expectedTs.toJson)) &&
          assert(queryTsWithBoost.toJson(fieldPath = None))(equalTo(expectedTsWithBoost.toJson))
        },
        test("fuzzy") {
          val query                  = fuzzy("stringField", "test")
          val queryTs                = fuzzy(TestDocument.stringField, "test")
          val queryWithFuzzinessAuto = fuzzy(TestDocument.stringField, "test").fuzziness("AUTO")
          val queryWithMaxExpansions = fuzzy(TestDocument.stringField, "test").maxExpansions(50)
          val queryWithPrefixLength  = fuzzy(TestDocument.stringField, "test").prefixLength(3)
          val queryWithAllParameters =
            fuzzy(TestDocument.stringField, "test").prefixLength(3).fuzziness("AUTO").maxExpansions(50)
          val queryWithSuffix = fuzzy(TestDocument.stringField.raw, "test")

          val expected =
            """
              |{
              |  "fuzzy": {
              |    "stringField": {
              |      "value": "test"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithFuzzinessAuto =
            """
              |{
              |  "fuzzy": {
              |    "stringField": {
              |      "value": "test",
              |      "fuzziness": "AUTO"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMaxExpansions =
            """
              |{
              |  "fuzzy": {
              |    "stringField": {
              |      "value": "test",
              |      "max_expansions": 50
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithPrefixLength =
            """
              |{
              |  "fuzzy": {
              |    "stringField": {
              |      "value": "test",
              |      "prefix_length": 3
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParameters =
            """
              |{
              |  "fuzzy": {
              |    "stringField": {
              |      "value": "test",
              |      "fuzziness": "AUTO",
              |      "max_expansions": 50,
              |      "prefix_length": 3
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithSuffix =
            """
              |{
              |  "fuzzy": {
              |    "stringField.raw": {
              |      "value": "test"
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryTs.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryWithFuzzinessAuto.toJson(fieldPath = None))(equalTo(expectedWithFuzzinessAuto.toJson)) &&
          assert(queryWithMaxExpansions.toJson(fieldPath = None))(equalTo(expectedWithMaxExpansions.toJson)) &&
          assert(queryWithPrefixLength.toJson(fieldPath = None))(equalTo(expectedWithPrefixLength.toJson)) &&
          assert(queryWithAllParameters.toJson(fieldPath = None))(equalTo(expectedWithAllParameters.toJson)) &&
          assert(queryWithSuffix.toJson(fieldPath = None))(equalTo(expectedWithSuffix.toJson))
        },
        test("functionScore") {
          val query = functionScore(
            scriptScoreFunction(Script("params.agg1 + params.agg2 > 10")),
            randomScoreFunction().weight(2.0),
            expDecayFunction("field", origin = "2013-09-17", scale = "10d")
              .offset("5d")
              .multiValueMode(Max)
              .weight(10.0)
          )
            .boost(2.0)
            .boostMode(FunctionScoreBoostMode.Avg)
            .maxBoost(42)
            .minScore(32)
            .query(matches("stringField", "string"))
            .scoreMode(FunctionScoreScoreMode.Min)

          val expected =
            """
              |{
              |  "function_score": {
              |    "query" : { "match": { "stringField" : "string" } },
              |    "score_mode": "min",
              |    "boost": 2.0,
              |    "boost_mode": "avg",
              |    "max_boost": 42.0,
              |    "min_score": 32.0,
              |    "functions": [
              |      {
              |        "script_score": {
              |          "script": {
              |            "source": "params.agg1 + params.agg2 > 10"
              |          }
              |        }
              |      },
              |      {
              |        "random_score": {},
              |        "weight": 2.0
              |      },
              |      {
              |        "exp": {
              |          "field": {
              |            "origin": "2013-09-17",
              |            "scale": "10d",
              |            "offset": "5d"
              |          },
              |          "multi_value_mode": "max"
              |        },
              |        "weight": 10.0
              |      }
              |    ]
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson))
        },
        test("geoDistance") {
          val queryWithHash =
            geoDistance(TestDocument.geoPointField, GeoHash("drm3btev3e86"), Distance(200, Kilometers))
          val queryWithPoint =
            geoDistance(TestDocument.geoPointField, GeoPoint(20.0, 21.1), Distance(200, Kilometers))
          val queryWithDistanceType =
            geoDistance(TestDocument.geoPointField, GeoPoint(20.0, 21.1), Distance(200, Kilometers)).distanceType(Plane)
          val queryWithName =
            geoDistance(TestDocument.geoPointField, GeoPoint(20.0, 21.1), Distance(200, Kilometers)).name("name")
          val queryWithValidationMethod =
            geoDistance(TestDocument.geoPointField, GeoPoint(20.0, 21.1), Distance(200, Kilometers)).validationMethod(
              IgnoreMalformed
            )
          val queryWithAllParams =
            geoDistance(TestDocument.geoPointField, GeoPoint(20.0, 21.1), Distance(200, Kilometers))
              .validationMethod(IgnoreMalformed)
              .distanceType(Plane)
              .name("name")

          val expectedWithHash =
            """
              |{
              |  "geo_distance": {
              |    "geoPointField": "drm3btev3e86",
              |    "distance": "200.0km"
              |  }
              |}
              |""".stripMargin

          val expectedWithDistance =
            """
              |{
              |  "geo_distance": {
              |    "distance": "200.0km",
              |    "geoPointField": "20.0,21.1"
              |  }
              |}
              |""".stripMargin

          val expectedWithDistanceType =
            """
              |{
              |  "geo_distance": {
              |    "distance_type" :  "plane",
              |    "geoPointField": "20.0,21.1",
              |    "distance": "200.0km"
              |  }
              |}
              |""".stripMargin

          val expectedWithName =
            """
              |{
              |  "geo_distance": {
              |    "_name": "name",
              |    "geoPointField": "20.0,21.1",
              |    "distance": "200.0km"
              |  }
              |}
              |""".stripMargin

          val expectedWithValidationMethod =
            """
              |{
              |  "geo_distance": {
              |    "validation_method": "IGNORE_MALFORMED",
              |    "geoPointField": "20.0,21.1",
              |    "distance": "200.0km"
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
              |    "geoPointField": "20.0,21.1"
              |  }
              |}
              |""".stripMargin

          assert(queryWithHash.toJson(fieldPath = None))(equalTo(expectedWithHash.toJson)) &&
          assert(queryWithPoint.toJson(fieldPath = None))(equalTo(expectedWithDistance.toJson)) &&
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
          val queryWithBoost          = hasParent("parent", matches(TestDocument.stringField, "test")).boost(3)
          val queryWithScore          = hasParent("parent", matches("field", "test")).withScoreFalse
          val queryWithIgnoreUnmapped = hasParent("parent", matches("field", "test")).ignoreUnmappedFalse
          val queryWithScoreAndIgnoreUnmapped =
            hasParent("parent", matches("field", "test")).withScoreTrue.ignoreUnmappedTrue
          val queryWithInnerHits = hasParent("parent", matches("field", "test")).innerHits
          val queryWithAllParams = hasParent("parent", matches(TestDocument.stringField, "test"))
            .boost(3)
            .withScoreFalse
            .ignoreUnmappedFalse
            .innerHits
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

          val expectedWithBoost =
            """
              |{
              |  "has_parent": {
              |    "parent_type": "parent",
              |    "query": {
              |      "match": {
              |        "stringField" : "test"
              |      }
              |    },
              |    "boost": 3.0
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
              |        "field" : "test"
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
              |        "field" : "test"
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
              |        "field" : "test"
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
              |        "field" : "test"
              |      }
              |    },
              |    "inner_hits": {}
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "has_parent": {
              |    "parent_type": "parent",
              |    "query": {
              |      "match": {
              |        "stringField" : "test"
              |      }
              |    },
              |    "boost": 3.0,
              |    "ignore_unmapped": false,
              |    "score": false,
              |    "inner_hits": {}
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson)) &&
          assert(queryWithScore.toJson(fieldPath = None))(equalTo(expectedWithScore.toJson)) &&
          assert(queryWithIgnoreUnmapped.toJson(fieldPath = None))(equalTo(expectedWithIgnoreUnmapped.toJson)) &&
          assert(queryWithScoreAndIgnoreUnmapped.toJson(fieldPath = None))(
            equalTo(expectedWithScoreAndIgnoreUnmapped.toJson)
          ) &&
          assert(queryWithInnerHits.toJson(fieldPath = None))(equalTo(expectedWithInnerHits.toJson)) &&
          assert(queryWithAllParams.toJson(fieldPath = None))(equalTo(expectedWithAllParams.toJson))
        },
        test("ids") {
          val query = ids("1", "2", "3")

          val expected =
            """
              |{
              |  "ids": {
              |     "values": ["1", "2", "3"]
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson))
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
        test("matchBooleanPrefix") {
          val queryString                 = matchBooleanPrefix("stringField", "test")
          val queryBool                   = matchBooleanPrefix("booleanField", true)
          val queryInt                    = matchBooleanPrefix("intField", 1)
          val queryStringTs               = matchBooleanPrefix(TestDocument.stringField, "test")
          val queryBoolTs                 = matchBooleanPrefix(TestDocument.booleanField, true)
          val queryIntTs                  = matchBooleanPrefix(TestDocument.intField, 1)
          val queryWithSuffix             = matchBooleanPrefix(TestDocument.stringField.raw, "test")
          val queryWithMinimumShouldMatch = matchBooleanPrefix(TestDocument.stringField, "test").minimumShouldMatch(3)

          val expectedString =
            """
              |{
              |  "match_bool_prefix": {
              |    "stringField": "test"
              |  }
              |}
              |""".stripMargin

          val expectedBool =
            """
              |{
              |  "match_bool_prefix": {
              |    "booleanField": true
              |  }
              |}
              |""".stripMargin

          val expectedInt =
            """
              |{
              |  "match_bool_prefix": {
              |    "intField": 1
              |  }
              |}
              |""".stripMargin

          val expectedWithSuffix =
            """
              |{
              |  "match_bool_prefix": {
              |    "stringField.raw": "test"
              |  }
              |}
              |""".stripMargin

          val expectedWithMinimumShouldMatch =
            """
              |{
              |  "match_bool_prefix": {
              |    "stringField": {
              |      "query": "test",
              |      "minimum_should_match": 3
              |    }
              |  }
              |}
              |""".stripMargin

          assert(queryString.toJson(fieldPath = None))(equalTo(expectedString.toJson)) &&
          assert(queryBool.toJson(fieldPath = None))(equalTo(expectedBool.toJson)) &&
          assert(queryInt.toJson(fieldPath = None))(equalTo(expectedInt.toJson)) &&
          assert(queryWithMinimumShouldMatch.toJson(fieldPath = None))(
            equalTo(expectedWithMinimumShouldMatch.toJson)
          ) &&
          assert(queryStringTs.toJson(fieldPath = None))(equalTo(expectedString.toJson)) &&
          assert(queryBoolTs.toJson(fieldPath = None))(equalTo(expectedBool.toJson)) &&
          assert(queryIntTs.toJson(fieldPath = None))(equalTo(expectedInt.toJson)) &&
          assert(queryWithSuffix.toJson(fieldPath = None))(equalTo(expectedWithSuffix.toJson)) &&
          assert(queryWithMinimumShouldMatch.toJson(fieldPath = None))(equalTo(expectedWithMinimumShouldMatch.toJson))
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
          val querySimple            = matchPhrase("stringField", "this is a test")
          val queryRaw               = matchPhrase("stringField.raw", "this is a test")
          val querySimpleTs          = matchPhrase(TestDocument.stringField, "this is a test")
          val queryRawTs             = matchPhrase(TestDocument.stringField.raw, "this is a test")
          val querySimpleTsWithBoost = matchPhrase(TestDocument.stringField, "this is a test").boost(3)

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

          val expectedSimpleTsWithBoost =
            """
              |{
              |  "match_phrase": {
              |    "stringField": {
              |      "query": "this is a test",
              |      "boost": 3.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(querySimple.toJson(fieldPath = None))(equalTo(expectedSimple.toJson)) &&
          assert(querySimpleTs.toJson(fieldPath = None))(equalTo(expectedSimple.toJson)) &&
          assert(queryRaw.toJson(fieldPath = None))(equalTo(expectedRaw.toJson)) &&
          assert(queryRawTs.toJson(fieldPath = None))(equalTo(expectedRaw.toJson)) &&
          assert(querySimpleTsWithBoost.toJson(fieldPath = None))(equalTo(expectedSimpleTsWithBoost.toJson))
        },
        test("matchPhrasePrefix") {
          val query   = matchPhrasePrefix("stringField", "test")
          val queryTs = matchPhrasePrefix(TestDocument.stringField, "test")

          val expected =
            """
              |{
              |  "match_phrase_prefix": {
              |    "stringField": {
              |       "query" : "test"
              |    }
              |   }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryTs.toJson(fieldPath = None))(equalTo(expected.toJson))
        },
        test("multiMatch") {
          val query                       = multiMatch("this is a test")
          val queryWithFields             = multiMatch("this is a test").fields("stringField1", "stringField2")
          val queryWithFieldsTs           = multiMatch("this is a test").fields(TestDocument.stringField)
          val queryWithFieldsSuffix       = multiMatch("this is a test").fields(TestDocument.stringField.raw)
          val queryWithType               = multiMatch("this is a test").matchingType(BestFields)
          val queryWithBoost              = multiMatch("this is a test").boost(2.2)
          val queryWithMinimumShouldMatch = multiMatch("this is a test").minimumShouldMatch(2)
          val queryWithAllParams = multiMatch("this is a test")
            .fields(TestDocument.stringField)
            .matchingType(BestFields)
            .boost(2.2)
            .minimumShouldMatch(2)

          val expected =
            """
              |{
              |  "multi_match": {
              |    "query": "this is a test"
              |  }
              |}
              |""".stripMargin

          val expectedWithFields =
            """
              |{
              |  "multi_match": {
              |    "query": "this is a test",
              |    "fields": [ "stringField1", "stringField2" ]
              |  }
              |}
              |""".stripMargin

          val expectedWithFieldsTs =
            """
              |{
              |  "multi_match": {
              |    "query": "this is a test",
              |    "fields": [ "stringField" ]
              |  }
              |}
              |""".stripMargin

          val expectedWithSuffix =
            """
              |{
              |  "multi_match": {
              |    "query": "this is a test",
              |    "fields": [ "stringField.raw" ]
              |  }
              |}
              |""".stripMargin

          val expectedWithType =
            """
              |{
              |  "multi_match": {
              |    "query": "this is a test",
              |    "type": "best_fields"
              |  }
              |}
              |""".stripMargin

          val expectedWithBoost =
            """
              |{
              |  "multi_match": {
              |    "query": "this is a test",
              |    "boost": 2.2
              |  }
              |}
              |""".stripMargin

          val expectedWithMinimumShouldMatch =
            """
              |{
              |  "multi_match": {
              |    "query": "this is a test",
              |    "minimum_should_match": 2
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "multi_match": {
              |    "query": "this is a test",
              |    "type": "best_fields",
              |    "fields": [ "stringField" ],
              |    "boost": 2.2,
              |    "minimum_should_match": 2
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryWithFields.toJson(fieldPath = None))(equalTo(expectedWithFields.toJson)) &&
          assert(queryWithFieldsTs.toJson(fieldPath = None))(equalTo(expectedWithFieldsTs.toJson)) &&
          assert(queryWithFieldsSuffix.toJson(fieldPath = None))(equalTo(expectedWithSuffix.toJson)) &&
          assert(queryWithType.toJson(fieldPath = None))(equalTo(expectedWithType.toJson)) &&
          assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson)) &&
          assert(queryWithMinimumShouldMatch.toJson(fieldPath = None))(
            equalTo(expectedWithMinimumShouldMatch.toJson)
          ) &&
          assert(queryWithAllParams.toJson(fieldPath = None))(equalTo(expectedWithAllParams.toJson))
        },
        test("nested") {
          val query                   = nested(TestDocument.subDocumentList, matchAll)
          val queryWithNested         = nested(TestDocument.subDocumentList, nested("items", term("testField", "test")))
          val queryWithIgnoreUnmapped = nested(TestDocument.subDocumentList, matchAll).ignoreUnmappedTrue
          val queryWithInnerHits =
            nested(TestDocument.subDocumentList, matchAll).innerHits(
              InnerHits()
                .from(0)
                .size(3)
                .name("innerHitName")
                .highlights(highlight("stringField"))
                .excludes("longField")
                .includes("intField")
            )
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
              |      "name": "innerHitName",
              |      "highlight" : {
              |        "fields" : {
              |          "subDocumentList.stringField" : {}
              |        }
              |      },
              |      "_source" : {
              |        "includes" : [
              |          "intField"
              |        ],
              |        "excludes" : [
              |          "longField"
              |        ]
              |      }
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
        test("prefix") {
          val query                    = prefix(TestDocument.stringField, "test")
          val queryWithCaseInsensitive = prefix(TestDocument.stringField, "test").caseInsensitiveTrue

          val expected =
            """
              |{
              |  "prefix": {
              |    "stringField": {
              |      "value": "test"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithCaseInsensitive =
            """
              |{
              |  "prefix": {
              |    "stringField": {
              |      "value": "test",
              |      "case_insensitive": true
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryWithCaseInsensitive.toJson(fieldPath = None))(equalTo(expectedWithCaseInsensitive.toJson))
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
          val queryWithFormat           = range(TestDocument.dateField).gt(LocalDate.of(2020, 1, 10)).format("yyyy-MM-dd")

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
              |      "format": "yyyy-MM-dd"
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
        test("regexp") {
          val query                    = regexp("stringField", "t.*st")
          val queryTs                  = regexp(TestDocument.stringField, "t.*st")
          val queryWithCaseInsensitive = regexp(TestDocument.stringField, "t.*st").caseInsensitiveTrue
          val queryWithSuffix          = regexp(TestDocument.stringField.raw, "t.*st")

          val expected =
            """
              |{
              |  "regexp": {
              |    "stringField": {
              |      "value": "t.*st"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithCaseInsensitive =
            """
              |{
              |  "regexp": {
              |    "stringField": {
              |      "value": "t.*st",
              |      "case_insensitive": true
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithSuffix =
            """
              |{
              |  "regexp": {
              |    "stringField.raw": {
              |      "value": "t.*st"
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryTs.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryWithCaseInsensitive.toJson(fieldPath = None))(equalTo(expectedWithCaseInsensitive.toJson)) &&
          assert(queryWithSuffix.toJson(fieldPath = None))(equalTo(expectedWithSuffix.toJson))
        },
        test("script") {
          val query =
            ElasticQuery.script(Script("doc['day_of_week'].value > params['day']").params("day" -> 2).lang(Painless))
          val queryWithBoost = ElasticQuery.script(Script("doc['day_of_week'].value > 2")).boost(2.0)

          val expected =
            """
              |{
              |  "script": {
              |    "script": {
              |      "lang": "painless",
              |      "source": "doc['day_of_week'].value > params['day']",
              |      "params": {
              |        "day": 2
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithBoost =
            """
              |{
              |  "script": {
              |    "script": {
              |      "source": "doc['day_of_week'].value > 2"
              |    },
              |    "boost": 2.0
              |  }
              |}
              |""".stripMargin

          assert(query.toJson(fieldPath = None))(equalTo(expected.toJson)) &&
          assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson))
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
          val queryString              = term(TestDocument.stringField, "test")
          val queryBool                = term(TestDocument.booleanField, true)
          val queryInt                 = term(TestDocument.intField, 21)
          val queryWithBoost           = term(TestDocument.stringField, "test").boost(10.21)
          val queryWithCaseInsensitive = term(TestDocument.stringField, "test").caseInsensitiveTrue
          val queryWithAllParams       = term(TestDocument.stringField, "test").boost(3.14).caseInsensitiveFalse

          val expectedString =
            """
              |{
              |  "term": {
              |    "stringField": {
              |      "value": "test"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedBool =
            """
              |{
              |  "term": {
              |    "booleanField": {
              |      "value": true
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedInt =
            """
              |{
              |  "term": {
              |    "intField": {
              |      "value": 21
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

          assert(queryString.toJson(fieldPath = None))(equalTo(expectedString.toJson)) &&
          assert(queryBool.toJson(fieldPath = None))(equalTo(expectedBool.toJson)) &&
          assert(queryInt.toJson(fieldPath = None))(equalTo(expectedInt.toJson)) &&
          assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson)) &&
          assert(queryWithCaseInsensitive.toJson(fieldPath = None))(equalTo(expectedWithCaseInsensitive.toJson)) &&
          assert(queryWithAllParams.toJson(fieldPath = None))(equalTo(expectedWithAllParams.toJson))
        },
        test("terms") {
          val queryString    = terms(TestDocument.stringField, "a", "b", "c")
          val queryBool      = terms(TestDocument.booleanField, true, false)
          val queryInt       = terms(TestDocument.intField, 1, 2, 3, 4)
          val queryWithBoost = terms(TestDocument.stringField, "a", "b", "c").boost(10.21)

          val expectedString =
            """
              |{
              |  "terms": {
              |    "stringField": [ "a", "b", "c" ]
              |  }
              |}
              |""".stripMargin

          val expectedBool =
            """
              |{
              |  "terms": {
              |    "booleanField": [ true, false ]
              |  }
              |}
              |""".stripMargin

          val expectedInt =
            """
              |{
              |  "terms": {
              |    "intField": [ 1, 2, 3, 4 ]
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

          assert(queryString.toJson(fieldPath = None))(equalTo(expectedString.toJson)) &&
          assert(queryBool.toJson(fieldPath = None))(equalTo(expectedBool.toJson)) &&
          assert(queryInt.toJson(fieldPath = None))(equalTo(expectedInt.toJson)) &&
          assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson))
        },
        test("termsSet") {
          val queryString = termsSet(TestDocument.stringField, "required_matches", "a", "b", "c")
          val queryBool   = termsSet(TestDocument.booleanField, "required_matches", true, false)
          val queryInt    = termsSet(TestDocument.intField, "required_matches", 1, 2, 3, 4)
          val queryWithBoost = termsSet(TestDocument.stringField, "required_matches", "a", "b", "c")
            .boost(10.0)

          val expectedString =
            """
              |{
              |  "terms_set": {
              |    "stringField": {
              |      "terms": [ "a", "b", "c" ],
              |      "minimum_should_match_field": "required_matches"
              |     }
              |  }
              |}
              |""".stripMargin

          val expectedBool =
            """
              |{
              |  "terms_set": {
              |    "booleanField": {
              |     "terms": [ true, false ],
              |      "minimum_should_match_field": "required_matches"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedInt =
            """
              |{
              |  "terms_set": {
              |    "intField": {
              |     "terms": [ 1, 2, 3, 4 ],
              |     "minimum_should_match_field": "required_matches"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithBoost =
            """
              |{
              |  "terms_set": {
              |    "stringField": {
              |      "terms": [ "a", "b", "c" ],
              |      "minimum_should_match_field": "required_matches",
              |       "boost": 10.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(queryString.toJson(fieldPath = None))(equalTo(expectedString.toJson)) &&
          assert(queryBool.toJson(fieldPath = None))(equalTo(expectedBool.toJson)) &&
          assert(queryInt.toJson(fieldPath = None))(equalTo(expectedInt.toJson)) &&
          assert(queryWithBoost.toJson(fieldPath = None))(equalTo(expectedWithBoost.toJson))

        },
        test("termsSetScript") {
          val queryString = termsSetScript(TestDocument.stringField, Script("doc['intField'].value"), "a", "b", "c")
          val queryBool   = termsSetScript(TestDocument.booleanField, Script("doc['intField'].value"), true, false)
          val queryInt    = termsSetScript(TestDocument.intField, Script("doc['intField'].value"), 1, 2, 3, 4)
          val queryWithBoost = termsSetScript(TestDocument.intField, Script("doc['intField'].value"), 1, 2, 3, 4)
            .boost(10.0)

          val expectedString =
            """
              |{
              |  "terms_set": {
              |    "stringField": {
              |      "terms": [ "a", "b", "c" ],
              |      "minimum_should_match_script": {
              |          "source": "doc['intField'].value"
              |        }
              |     }
              |  }
              |}
              |""".stripMargin

          val expectedBool =
            """
              |{
              |  "terms_set": {
              |    "booleanField": {
              |     "terms": [ true, false ],
              |      "minimum_should_match_script": {
              |          "source": "doc['intField'].value"
              |        }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedInt =
            """
              |{
              |  "terms_set": {
              |    "intField": {
              |     "terms": [ 1, 2, 3, 4 ],
              |     "minimum_should_match_script": {
              |          "source": "doc['intField'].value"
              |        }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithBoost =
            """
              |{
              |  "terms_set": {
              |    "intField": {
              |      "terms": [ 1, 2, 3, 4 ],
              |      "minimum_should_match_script": {
              |          "source": "doc['intField'].value"
              |        },
              |       "boost": 10.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(queryString.toJson(fieldPath = None))(equalTo(expectedString.toJson)) &&
          assert(queryBool.toJson(fieldPath = None))(equalTo(expectedBool.toJson)) &&
          assert(queryInt.toJson(fieldPath = None))(equalTo(expectedInt.toJson)) &&
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
