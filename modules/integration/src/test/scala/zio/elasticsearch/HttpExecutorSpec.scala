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
import zio.elasticsearch.ElasticAggregation._
import zio.elasticsearch.ElasticHighlight.highlight
import zio.elasticsearch.ElasticQuery.{script => _, _}
import zio.elasticsearch.ElasticSort.sortBy
import zio.elasticsearch.aggregation.AggregationOrder
import zio.elasticsearch.data.GeoPoint
import zio.elasticsearch.domain.{PartialTestDocument, TestDocument, TestSubDocument}
import zio.elasticsearch.executor.Executor
import zio.elasticsearch.query.DistanceUnit.Kilometers
import zio.elasticsearch.query.FunctionScoreFunction.randomScoreFunction
import zio.elasticsearch.query.MultiMatchType._
import zio.elasticsearch.query.sort.SortMode.Max
import zio.elasticsearch.query.sort.SortOrder._
import zio.elasticsearch.query.sort.SourceType.NumberType
import zio.elasticsearch.query.{Distance, FunctionScoreBoostMode, FunctionScoreFunction, InnerHits}
import zio.elasticsearch.request.{CreationOutcome, DeletionOutcome}
import zio.elasticsearch.result.{
  FilterAggregationResult,
  Item,
  MaxAggregationResult,
  SamplerAggregationResult,
  SumAggregationResult,
  TermsAggregationBucketResult,
  TermsAggregationResult,
  UpdateByQueryResult
}
import zio.elasticsearch.script.{Painless, Script}
import zio.json.ast.Json.{Arr, Str}
import zio.schema.codec.JsonCodec
import zio.stream.{Sink, ZSink}
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

import java.time.LocalDate
import scala.util.Random

object HttpExecutorSpec extends IntegrationSpec {

  def spec: Spec[TestEnvironment, Any] = {
    (suite("Executor")(
      suite("aggregation")(
        test("avg aggregation") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <-
                  Executor.execute(
                    ElasticRequest
                      .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument.copy(doubleField = 20))
                  )
                _ <-
                  Executor.execute(
                    ElasticRequest
                      .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument.copy(doubleField = 10))
                      .refreshTrue
                  )
                aggregation = avgAggregation(name = "aggregationDouble", field = TestDocument.doubleField)
                aggsRes    <-
                  Executor
                    .execute(ElasticRequest.aggregate(selectors = firstSearchIndex, aggregation = aggregation))
                    .asAvgAggregation("aggregationDouble")
              } yield assert(aggsRes.head.value)(equalTo(15.0))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("cardinality aggregation") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument.copy(intField = 10))
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument.copy(intField = 20))
                         .refreshTrue
                     )
                aggregation = cardinalityAggregation(name = "aggregationInt", field = TestDocument.intField)
                aggsRes    <-
                  Executor
                    .execute(ElasticRequest.aggregate(selectors = firstSearchIndex, aggregation = aggregation))

                cardinalityAgg <- aggsRes.asCardinalityAggregation("aggregationInt")
              } yield assert(cardinalityAgg.map(_.value))(isSome(equalTo(2)))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("extended stats aggregation") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument.copy(intField = 100))
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument.copy(intField = 50))
                         .refreshTrue
                     )
                aggregation = extendedStatsAggregation(name = "aggregation", field = TestDocument.intField).sigma(3)
                aggsRes    <-
                  Executor
                    .execute(ElasticRequest.aggregate(selectors = firstSearchIndex, aggregation = aggregation))
                    .asExtendedStatsAggregation("aggregation")
              } yield assert(aggsRes.head.count)(equalTo(2)) &&
                assert(aggsRes.head.min)(equalTo(50.0)) &&
                assert(aggsRes.head.max)(equalTo(100.0)) &&
                assert(aggsRes.head.avg)(equalTo(75.0)) &&
                assert(aggsRes.head.sum)(equalTo(150.0)) &&
                assert(aggsRes.head.sumOfSquares)(equalTo(12500.0)) &&
                assert(aggsRes.head.variance)(equalTo(625.0)) &&
                assert(aggsRes.head.variancePopulation)(equalTo(625.0)) &&
                assert(aggsRes.head.varianceSampling)(equalTo(1250.0)) &&
                assert(aggsRes.head.stdDeviation)(equalTo(25.0)) &&
                assert(aggsRes.head.stdDeviationPopulation)(equalTo(25.0)) &&
                assert(aggsRes.head.stdDeviationSampling)(equalTo(35.35533905932738)) &&
                assert(aggsRes.head.stdDeviationBoundsResult.upper)(equalTo(150.0)) &&
                assert(aggsRes.head.stdDeviationBoundsResult.lower)(equalTo(0.0)) &&
                assert(aggsRes.head.stdDeviationBoundsResult.upperPopulation)(equalTo(150.0)) &&
                assert(aggsRes.head.stdDeviationBoundsResult.lowerPopulation)(equalTo(0.0)) &&
                assert(aggsRes.head.stdDeviationBoundsResult.upperSampling)(equalTo(181.06601717798213)) &&
                assert(aggsRes.head.stdDeviationBoundsResult.lowerSampling)(equalTo(-31.066017177982133))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("filter aggregation with max sub aggregation") {
          val expectedResult = (
            "aggregation",
            FilterAggregationResult(
              docCount = 2,
              subAggregations = Map(
                "subAggregation" -> MaxAggregationResult(value = 5.0)
              )
            )
          )
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument, thirdDocumentId, thirdDocument) =>
              for {
                _                    <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                firstDocumentUpdated  = firstDocument.copy(stringField = "test", intField = 7)
                secondDocumentUpdated =
                  secondDocument.copy(stringField = "filterAggregation", intField = 3)
                thirdDocumentUpdated =
                  thirdDocument.copy(stringField = "filterAggregation", intField = 5)
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](
                         firstSearchIndex,
                         firstDocumentId,
                         firstDocumentUpdated
                       )
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](
                           firstSearchIndex,
                           secondDocumentId,
                           secondDocumentUpdated
                         )
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](
                           firstSearchIndex,
                           thirdDocumentId,
                           thirdDocumentUpdated
                         )
                         .refreshTrue
                     )
                query       = term(field = TestDocument.stringField, value = secondDocumentUpdated.stringField.toLowerCase)
                aggregation =
                  filterAggregation(name = "aggregation", query = query).withSubAgg(
                    maxAggregation("subAggregation", TestDocument.intField)
                  )
                aggsRes <-
                  Executor
                    .execute(ElasticRequest.aggregate(selectors = firstSearchIndex, aggregation = aggregation))
                    .aggregations

              } yield assert(aggsRes.head)(equalTo(expectedResult))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("max aggregation") {
          val expectedResponse = ("aggregationInt", MaxAggregationResult(value = 20.0))
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument.copy(intField = 20))
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument.copy(intField = 10))
                         .refreshTrue
                     )
                aggregation = maxAggregation(name = "aggregationInt", field = TestDocument.intField)
                aggsRes    <-
                  Executor
                    .execute(ElasticRequest.aggregate(selectors = firstSearchIndex, aggregation = aggregation))
                    .aggregations
              } yield assert(aggsRes.head)(equalTo(expectedResponse))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("min aggregation") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument.copy(intField = 200))
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument.copy(intField = 23))
                         .refreshTrue
                     )
                aggregation = minAggregation(name = "aggregationInt", field = TestDocument.intField)
                aggsRes    <-
                  Executor
                    .execute(ElasticRequest.aggregate(selectors = firstSearchIndex, aggregation = aggregation))
                    .asMinAggregation("aggregationInt")
              } yield assert(aggsRes.head.value)(equalTo(23.0))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("percentile ranks aggregation") {
          val expectedResult = Map("500.0" -> 55.55555555555555, "600.0" -> 100.0)
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument, thirdDocumentId, thirdDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument.copy(intField = 400))
                     )
                _ <-
                  Executor.execute(
                    ElasticRequest
                      .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument.copy(intField = 500))
                  )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, thirdDocumentId, thirdDocument.copy(intField = 550))
                         .refreshTrue
                     )
                aggregation =
                  percentileRanksAggregation(name = "aggregation", field = "intField", value = 500.0, values = 600.0)
                aggsRes <-
                  Executor
                    .execute(ElasticRequest.aggregate(selectors = firstSearchIndex, aggregation = aggregation))
                    .asPercentileRanksAggregation("aggregation")
              } yield assert(aggsRes.head.values)(equalTo(expectedResult))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("percentiles aggregation") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                aggregation =
                  percentilesAggregation(name = "aggregationInt", field = TestDocument.intField).percents(25, 50, 90)
                aggsRes <-
                  Executor
                    .execute(ElasticRequest.aggregate(selectors = firstSearchIndex, aggregation = aggregation))
                    .asPercentilesAggregation("aggregationInt")
              } yield assert(aggsRes.head.values.size)(equalTo(3))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("percentiles aggregation with multi index") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](secondSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                aggregation =
                  percentilesAggregation(name = "aggregationInt", field = TestDocument.intField).percents(25, 50, 90)
                aggsRes <- Executor
                             .execute(
                               ElasticRequest.aggregate(
                                 selectors = MultiIndex.names(firstSearchIndex, secondSearchIndex),
                                 aggregation = aggregation
                               )
                             )
                             .asPercentilesAggregation("aggregationInt")
              } yield assert(aggsRes.head.values.size)(equalTo(3))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ) @@ around(
          Executor.execute(ElasticRequest.createIndex(secondSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
        ),
        test("percentiles aggregation with index pattern") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](secondSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                aggregation =
                  percentilesAggregation(name = "aggregationInt", field = TestDocument.intField).percents(25, 50, 90)
                aggsRes <- Executor
                             .execute(
                               ElasticRequest.aggregate(
                                 selectors = IndexPatternAll,
                                 aggregation = aggregation
                               )
                             )
                             .asPercentilesAggregation("aggregationInt")
              } yield assert(aggsRes.head.values.size)(equalTo(3))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ) @@ around(
          Executor.execute(ElasticRequest.createIndex(secondSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
        ),
        test("percentiles aggregation as sub aggregation") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                aggregation =
                  termsAggregation(name = "first", field = TestDocument.stringField.keyword)
                    .withSubAgg(percentilesAggregation(name = "second", field = TestSubDocument.intField))
                aggsRes <-
                  Executor
                    .execute(ElasticRequest.aggregate(selectors = firstSearchIndex, aggregation = aggregation))
                    .aggregations
              } yield assert(aggsRes)(isNonEmpty)
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("sampler aggregation with sum and terms sub aggregations") {
          (
            "sampler_agg",
            SamplerAggregationResult(
              docCount = 4,
              subAggregations = Map(
                "total_sum_field"   -> SumAggregationResult(value = 50.0),
                "string_categories" -> TermsAggregationResult(
                  docErrorCount = 0,
                  sumOtherDocCount = 0,
                  buckets = Chunk(
                    TermsAggregationBucketResult(key = "abc", docCount = 1, subAggregations = Map.empty),
                    TermsAggregationBucketResult(key = "def", docCount = 1, subAggregations = Map.empty),
                    TermsAggregationBucketResult(key = "ghi", docCount = 1, subAggregations = Map.empty),
                    TermsAggregationBucketResult(key = "jkl", docCount = 1, subAggregations = Map.empty)
                  )
                )
              )
            )
          )
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (docIdA, docA, docIdB, docB, docIdC, docC) =>
              for {
                _        <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                documentA = docA.copy(stringField = "abc", intField = 10)
                documentB = docB.copy(stringField = "def", intField = 20)
                documentC = docC.copy(stringField = "ghi", intField = 15)
                _        <- Executor.execute(ElasticRequest.upsert[TestDocument](firstSearchIndex, docIdA, documentA))
                _        <- Executor.execute(ElasticRequest.upsert[TestDocument](firstSearchIndex, docIdB, documentB))
                _        <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, docIdC, documentC).refreshTrue
                     )
                aggregation = samplerAggregation(
                                "sampler_agg",
                                sumAggregation("total_sum_field", TestDocument.intField)
                              ).withSubAgg(termsAggregation("string_categories", TestDocument.stringField.keyword))
                                .maxDocumentsPerShard(100)
                aggsRes <-
                  Executor
                    .execute(ElasticRequest.aggregate(selectors = firstSearchIndex, aggregation = aggregation))
                    .aggregations
                    .map(_.head)

                expectedResult =
                  (
                    "sampler_agg",
                    SamplerAggregationResult(
                      docCount = 3,
                      subAggregations = Map(
                        "total_sum_field"   -> SumAggregationResult(value = 45.0),
                        "string_categories" -> TermsAggregationResult(
                          docErrorCount = 0,
                          sumOtherDocCount = 0,
                          buckets = Chunk(
                            TermsAggregationBucketResult(key = "abc", docCount = 1, subAggregations = Map.empty),
                            TermsAggregationBucketResult(key = "def", docCount = 1, subAggregations = Map.empty),
                            TermsAggregationBucketResult(key = "ghi", docCount = 1, subAggregations = Map.empty)
                          )
                        )
                      )
                    )
                  )
              } yield assert(aggsRes)(equalTo(expectedResult))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("stats aggregation") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument, thirdDocumentId, thirdDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument.copy(intField = 7))
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument.copy(intField = 6))
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, thirdDocumentId, thirdDocument.copy(intField = 10))
                         .refreshTrue
                     )
                aggregation = statsAggregation(name = "aggregation", field = TestDocument.intField)
                aggsRes    <-
                  Executor
                    .execute(ElasticRequest.aggregate(selectors = firstSearchIndex, aggregation = aggregation))
                    .asStatsAggregation("aggregation")
              } yield assert(aggsRes.head.count)(equalTo(3)) &&
                assert(aggsRes.head.min)(equalTo(6.0)) &&
                assert(aggsRes.head.max)(equalTo(10.0)) &&
                assert(aggsRes.head.avg)(equalTo(7.666666666666667)) &&
                assert(aggsRes.head.sum)(equalTo(23.0))

          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("sum aggregation") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument.copy(intField = 200))
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument.copy(intField = 23))
                         .refreshTrue
                     )
                aggregation = sumAggregation(name = "aggregationInt", field = TestDocument.intField)
                aggsRes    <-
                  Executor
                    .execute(ElasticRequest.aggregate(selectors = firstSearchIndex, aggregation = aggregation))
                    .asSumAggregation("aggregationInt")
              } yield assert(aggsRes.head.value)(equalTo(223.0))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("terms aggregation with max sub aggregation") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                aggregation =
                  termsAggregation(name = "aggregationString", field = TestDocument.stringField.keyword).withSubAgg(
                    maxAggregation("subAggregation", TestDocument.intField)
                  )
                aggsRes <-
                  Executor
                    .execute(ElasticRequest.aggregate(selectors = firstSearchIndex, aggregation = aggregation))
                    .aggregations
              } yield assert(aggsRes)(isNonEmpty)
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("missing aggregations") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                aggregation = multipleAggregations.aggregations(
                                missingAggregation(
                                  name = "aggregationString",
                                  field = TestDocument.stringField.keyword
                                ),
                                missingAggregation(name = "aggregationString", field = "stringField.keyword")
                              )
                aggsRes <- Executor
                             .execute(
                               ElasticRequest
                                 .aggregate(selectors = firstSearchIndex, aggregation = aggregation)
                             )
                             .aggregations
              } yield assert(aggsRes)(isNonEmpty)
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("multiple terms aggregations") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                aggregation = multipleAggregations.aggregations(
                                termsAggregation(
                                  name = "aggregationString",
                                  field = TestDocument.stringField.keyword
                                ),
                                termsAggregation(name = "aggregationInt", field = "intField.keyword")
                              )
                aggsRes <- Executor
                             .execute(
                               ElasticRequest
                                 .aggregate(selectors = firstSearchIndex, aggregation = aggregation)
                             )
                             .aggregations
              } yield assert(aggsRes)(isNonEmpty)
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("terms aggregation with nested max and bucket sort") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument.copy(intField = 5))
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument.copy(intField = 2))
                         .refreshTrue
                     )
                aggregation =
                  termsAggregation(
                    name = "aggregationString",
                    field = TestDocument.stringField.keyword
                  ).orderBy(AggregationOrder("aggregationInt", Desc))
                    .withSubAgg(maxAggregation(name = "aggregationInt", field = "intField"))
                    .withSubAgg(
                      bucketSortAggregation("aggregationBucket").sort(
                        ElasticSort.sortBy("aggregationInt").order(Desc)
                      )
                    )
                    .size(1)
                aggsRes <- Executor
                             .execute(
                               ElasticRequest
                                 .aggregate(selectors = firstSearchIndex, aggregation = aggregation)
                             )
                agg <- aggsRes.asTermsAggregation("aggregationString")
              } yield assert(agg.map(_.buckets.size))(isSome(equalTo(1)))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("value count aggregation") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <-
                  Executor.execute(
                    ElasticRequest
                      .upsert[TestDocument](
                        firstSearchIndex,
                        firstDocumentId,
                        firstDocument.copy(stringField = "test")
                      )
                  )
                _ <-
                  Executor.execute(
                    ElasticRequest
                      .upsert[TestDocument](
                        firstSearchIndex,
                        secondDocumentId,
                        secondDocument.copy(stringField = "test")
                      )
                      .refreshTrue
                  )
                aggregation = valueCountAggregation(name = "aggregation", field = TestDocument.stringField.keyword)
                aggsRes    <-
                  Executor
                    .execute(ElasticRequest.aggregate(selectors = firstSearchIndex, aggregation = aggregation))
                    .asValueCountAggregation("aggregation")

              } yield assert(aggsRes.head.value)(equalTo(2))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("weighted avg aggregation") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <-
                  Executor.execute(
                    ElasticRequest
                      .upsert[TestDocument](
                        firstSearchIndex,
                        firstDocumentId,
                        firstDocument.copy(doubleField = 5, intField = 2)
                      )
                  )
                _ <-
                  Executor.execute(
                    ElasticRequest
                      .upsert[TestDocument](
                        firstSearchIndex,
                        secondDocumentId,
                        secondDocument.copy(doubleField = 10, intField = 3)
                      )
                      .refreshTrue
                  )
                aggregation = weightedAvgAggregation(
                                name = "weightedAggregation",
                                valueField = TestDocument.doubleField,
                                weightField = TestDocument.intField
                              )
                aggsRes <-
                  Executor
                    .execute(ElasticRequest.aggregate(selectors = firstSearchIndex, aggregation = aggregation))
                    .asWeightedAvgAggregation("weightedAggregation")
              } yield assert(aggsRes.head.value)(equalTo(8.0))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        )
      ),
      suite("search with aggregation")(
        test("match all with multiple terms aggregations") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query       = matchAll
                aggregation = termsAggregation(
                                name = "aggregationString",
                                field = TestDocument.stringField.keyword
                              ).withAgg(termsAggregation("aggregationInt", "intField"))
                res <- Executor.execute(
                         ElasticRequest
                           .search(
                             selectors = firstSearchIndex,
                             query = query,
                             aggregation = aggregation
                           )
                           .from(0)
                           .size(1)
                       )
                docs <- res.documentAs[TestDocument]
                aggs <- res.aggregations
              } yield assert(docs.length)(equalTo(1)) && assert(aggs)(isNonEmpty)
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("match all with terms aggregations and search after") {
          checkOnce(genTestDocument) { firstDocument =>
            for {
              _   <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
              reqs = (0 to 20).map { i =>
                       ElasticRequest.create[TestDocument](
                         firstSearchIndex,
                         firstDocument.copy(stringField = Random.alphanumeric.take(5).mkString, intField = i)
                       )
                     }
              _          <- Executor.execute(ElasticRequest.bulk(reqs: _*).refreshTrue)
              query       = matchAll
              aggregation = termsAggregation(
                              name = "aggregationString",
                              field = TestDocument.stringField.keyword
                            ).withAgg(termsAggregation("aggregationInt", "intField"))
              res <- Executor
                       .execute(
                         ElasticRequest
                           .search(selectors = firstSearchIndex, query = query, aggregation = aggregation)
                           .size(10)
                           .sort(
                             sortBy(TestDocument.intField).order(Asc)
                           )
                       )
              sa   <- res.lastSortValue
              res2 <- Executor
                        .execute(
                          ElasticRequest
                            .search(selectors = firstSearchIndex, query = query, aggregation = aggregation)
                            .searchAfter(sa.get)
                            .size(10)
                            .sort(
                              sortBy(TestDocument.intField).order(Asc)
                            )
                        )
              docs <- res2.documentAs[TestDocument]
              aggs <- res2.aggregations
            } yield assert(docs.length)(equalTo(10)) && assert(aggs)(isNonEmpty)
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("match all with terms aggregations and descending sort") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              val firstDocumentWithFixedIntField  = firstDocument.copy(intField = 25)
              val secondDocumentWithFixedIntField = secondDocument.copy(intField = 32)
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocumentWithFixedIntField)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocumentWithFixedIntField)
                         .refreshTrue
                     )
                query       = matchAll
                aggregation =
                  termsAggregation(
                    name = "aggregationString",
                    field = TestDocument.stringField.keyword
                  ).withAgg(termsAggregation("aggregationInt", "intField.keyword"))
                res <- Executor.execute(
                         ElasticRequest
                           .search(
                             selectors = firstSearchIndex,
                             query = query,
                             aggregation = aggregation
                           )
                           .sort(sortBy(field = TestDocument.intField).order(Desc))
                       )
                docs <- res.documentAs[TestDocument]
                aggs <- res.aggregations
              } yield assert(docs)(equalTo(Chunk(secondDocumentWithFixedIntField, firstDocumentWithFixedIntField))) &&
                assert(aggs)(isNonEmpty)
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("sampler aggregation") {
          val expectedAggResult = SamplerAggregationResult(
            docCount = 2,
            subAggregations = Map(
              "sampled_strings" -> TermsAggregationResult(
                docErrorCount = 0,
                sumOtherDocCount = 0,
                buckets = Chunk(
                  TermsAggregationBucketResult(key = "zio", docCount = 1, subAggregations = Map.empty),
                  TermsAggregationBucketResult(key = "zio-elasticsearch", docCount = 1, subAggregations = Map.empty)
                )
              )
            )
          )
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (docIdA, docA, docIdB, docB, docIdC, docC) =>
              val documentA          = docA.copy(stringField = "zio")
              val documentB          = docB.copy(stringField = "elasticsearch")
              val documentC          = docC.copy(stringField = "zio-elasticsearch")
              val expectedSearchDocs = Chunk(documentA, documentC)
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(ElasticRequest.upsert[TestDocument](firstSearchIndex, docIdA, documentA))
                _ <- Executor.execute(ElasticRequest.upsert[TestDocument](firstSearchIndex, docIdB, documentB))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, docIdC, documentC).refreshTrue
                     )
                searchQuery = matches(TestDocument.stringField, "zio")
                aggregation = samplerAggregation(
                                "sampler_agg",
                                termsAggregation("sampled_strings", TestDocument.stringField.keyword)
                              )
                                .maxDocumentsPerShard(2)
                res <- Executor.execute(
                         ElasticRequest
                           .search(
                             selectors = firstSearchIndex,
                             query = searchQuery,
                             aggregation = aggregation
                           )
                       )
                docs       <- res.documentAs[TestDocument]
                samplerAgg <- res.aggregation("sampler_agg")
              } yield assert(docs.length)(equalTo(2)) &&
                assert(docs.toSet)(equalTo(expectedSearchDocs.toSet)) &&
                assert(samplerAgg)(isSome(equalTo(expectedAggResult)))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("terms aggregations with nested max and bucketSelector") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument.copy(intField = 5))
                     )
                _ <-
                  Executor.execute(
                    ElasticRequest
                      .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument.copy(intField = 100))
                      .refreshTrue
                  )
                query       = matchAll
                aggregation =
                  termsAggregation(
                    name = "aggregationString",
                    field = TestDocument.stringField.keyword
                  ).withSubAgg(maxAggregation(name = "aggregationInt", field = TestDocument.intField))
                    .withSubAgg(
                      bucketSelectorAggregation(
                        name = "aggregationSelector",
                        script = Script("params.aggregation_int > 10"),
                        bucketsPath = Map("aggregation_int" -> "aggregationInt")
                      )
                    )
                res <- Executor.execute(
                         ElasticRequest
                           .search(
                             selectors = firstSearchIndex,
                             query = query,
                             aggregation = aggregation
                           )
                       )
                docs     <- res.documentAs[TestDocument]
                termsAgg <- res.asTermsAggregation("aggregationString")
              } yield assert(docs)(isNonEmpty) && assert(
                termsAgg.map(_.buckets.size)
              )(isSome(equalTo(1)))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        )
      ) @@ shrinks(0),
      suite("counting")(
        test("count with query") {
          checkOnce(genTestDocument) { document =>
            for {
              _ <- Executor.execute(ElasticRequest.deleteByQuery(firstCountIndex, matchAll))
              _ <- Executor.execute(
                     ElasticRequest.create[TestDocument](firstCountIndex, document).refreshTrue
                   )
              res <- Executor.execute(ElasticRequest.count(firstCountIndex, matchAll))
            } yield assert(res)(equalTo(1))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstCountIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstCountIndex)).orDie
        ),
        test("count without query") {
          checkOnce(genTestDocument) { document =>
            for {
              _ <- Executor.execute(ElasticRequest.deleteByQuery(secondCountIndex, matchAll))
              _ <- Executor.execute(
                     ElasticRequest.create[TestDocument](secondCountIndex, document).refreshTrue
                   )
              res <- Executor.execute(ElasticRequest.count(secondCountIndex))
            } yield assert(res)(equalTo(1))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(secondCountIndex)),
          Executor.execute(ElasticRequest.deleteIndex(secondCountIndex)).orDie
        )
      ) @@ shrinks(0),
      suite("creating document")(
        test("create document") {
          checkOnce(genTestDocument) { document =>
            for {
              docId <- Executor.execute(ElasticRequest.create[TestDocument](index, document))
              res   <- Executor.execute(ElasticRequest.getById(index, docId)).documentAs[TestDocument]
            } yield assert(res)(isSome(equalTo(document)))
          }
        },
        test("create document with ID") {
          checkOnce(genDocumentId, genTestDocument) { (documentId, document) =>
            assertZIO(Executor.execute(ElasticRequest.create[TestDocument](index, documentId, document)))(
              equalTo(CreationOutcome.Created)
            )
          }
        },
        test("return AlreadyExists for existing document") {
          checkOnce(genDocumentId, genTestDocument, genTestDocument) { (documentId, firstDocument, secondDocument) =>
            for {
              _   <- Executor.execute(ElasticRequest.upsert[TestDocument](index, documentId, firstDocument))
              res <- Executor.execute(ElasticRequest.create[TestDocument](index, documentId, secondDocument))
            } yield assert(res)(equalTo(CreationOutcome.AlreadyExists))
          }
        }
      ),
      suite("creating index")(
        test("create index and return AlreadyExists for existing") {
          for {
            res1 <- Executor.execute(ElasticRequest.createIndex(createIndexTestName))
            res2 <- Executor.execute(ElasticRequest.createIndex(createIndexTestName))
          } yield assert(res1)(equalTo(CreationOutcome.Created)) &&
            assert(res2)(equalTo(CreationOutcome.AlreadyExists))
        }
      ) @@ after(Executor.execute(ElasticRequest.deleteIndex(createIndexTestName)).orDie),
      suite("upserting document")(
        test("upsert create and update") {
          checkOnce(genDocumentId, genTestDocument, genTestDocument) { (documentId, firstDocument, secondDocument) =>
            for {
              _    <- Executor.execute(ElasticRequest.upsert[TestDocument](index, documentId, firstDocument))
              doc1 <- Executor.execute(ElasticRequest.getById(index, documentId)).documentAs[TestDocument]
              _    <- Executor.execute(ElasticRequest.upsert[TestDocument](index, documentId, secondDocument))
              doc2 <- Executor.execute(ElasticRequest.getById(index, documentId)).documentAs[TestDocument]
            } yield assert(doc1)(isSome(equalTo(firstDocument))) &&
              assert(doc2)(isSome(equalTo(secondDocument)))
          }
        }
      ),
      suite("deleting by ID")(
        test("delete existing and return NotFound for missing") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId) { (documentId, document, missingId) =>
            for {
              _    <- Executor.execute(ElasticRequest.upsert[TestDocument](index, documentId, document))
              res1 <- Executor.execute(ElasticRequest.deleteById(index, documentId))
              res2 <- Executor.execute(ElasticRequest.deleteById(index, missingId))
            } yield assert(res1)(equalTo(DeletionOutcome.Deleted)) &&
              assert(res2)(equalTo(DeletionOutcome.NotFound))
          }
        }
      ),
      suite("deleting index")(
        test("delete existing and return NotFound for missing") {
          checkOnce(genIndexName, genIndexName) { (name, missingName) =>
            for {
              _    <- Executor.execute(ElasticRequest.createIndex(name))
              res1 <- Executor.execute(ElasticRequest.deleteIndex(name))
              res2 <- Executor.execute(ElasticRequest.deleteIndex(missingName))
            } yield assert(res1)(equalTo(DeletionOutcome.Deleted)) &&
              assert(res2)(equalTo(DeletionOutcome.NotFound))
          }
        }
      ),
      suite("finding document")(
        test("exists and not exists") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId) { (documentId, document, missingId) =>
            for {
              _    <- Executor.execute(ElasticRequest.upsert[TestDocument](index, documentId, document))
              res1 <- Executor.execute(ElasticRequest.exists(index, documentId))
              res2 <- Executor.execute(ElasticRequest.exists(index, missingId))
            } yield assert(res1)(isTrue) &&
              assert(res2)(isFalse)
          }
        }
      ),
      suite("retrieving by ID")(
        test("return document") {
          checkOnce(genDocumentId, genTestDocument) { (documentId, document) =>
            for {
              _   <- Executor.execute(ElasticRequest.upsert[TestDocument](index, documentId, document))
              res <- Executor.execute(ElasticRequest.getById(index, documentId)).documentAs[TestDocument]
            } yield assert(res)(isSome(equalTo(document)))
          }
        },
        test("return None for missing document") {
          checkOnce(genDocumentId) { documentId =>
            assertZIO(Executor.execute(ElasticRequest.getById(index, documentId)).documentAs[TestDocument])(isNone)
          }
        },
        test("fail on decoding error") {
          checkOnce(genDocumentId, genTestDocument) { (documentId, document) =>
            val result = for {
              _   <- Executor.execute(ElasticRequest.upsert[TestDocument](index, documentId, document))
              res <- Executor.execute(ElasticRequest.getById(index, documentId)).documentAs[TestSubDocument]
            } yield res

            assertZIO(result.exit)(
              fails(isSubtype[Exception](assertException("Could not parse the document: .nestedField(missing)")))
            )
          }
        }
      ),
      suite("refreshing")(
        test("refresh single, multiple, all indices and return false for missing") {
          for {
            res1 <- Executor.execute(ElasticRequest.refresh(index))
            _    <- Executor.execute(ElasticRequest.createIndex(createIndexTestName))
            res2 <- Executor.execute(ElasticRequest.refresh(MultiIndex.names(index, createIndexTestName)))
            res3 <- Executor.execute(ElasticRequest.refresh(IndexPatternAll))
            res4 <- Executor.execute(ElasticRequest.refresh(refreshFailIndex))
          } yield assert(res1)(isTrue) &&
            assert(res2)(isTrue) &&
            assert(res3)(isTrue) &&
            assert(res4)(isFalse)
        }
      ) @@ after(Executor.execute(ElasticRequest.deleteIndex(createIndexTestName)).orDie),
      suite("retrieving by IDs")(
        test("find by IDs") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = ids(firstDocumentId.toString, secondDocumentId.toString)
                res  <-
                  Executor.execute(
                    ElasticRequest.search(firstSearchIndex, query)
                  )
                items <- res.items
              } yield assert(items != null)(isTrue)
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        )
      ),
      suite("kNN search")(
        test("top two results") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument, thirdDocumentId, thirdDocument) =>
              for {
                _                    <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                firstDocumentUpdated  = firstDocument.copy(vectorField = List(1, 5, -20))
                secondDocumentUpdated = secondDocument.copy(vectorField = List(42, 8, -15))
                thirdDocumentUpdated  = thirdDocument.copy(vectorField = List(15, 11, 23))
                req1                  = ElasticRequest.create(firstSearchIndex, firstDocumentId, firstDocumentUpdated)
                req2                  = ElasticRequest.create(firstSearchIndex, secondDocumentId, secondDocumentUpdated)
                req3                  = ElasticRequest.create(firstSearchIndex, thirdDocumentId, thirdDocumentUpdated)
                _                    <- Executor.execute(ElasticRequest.bulk(req1, req2, req3).refreshTrue)
                query                 = ElasticQuery.kNN(TestDocument.vectorField, 2, 3, Chunk(-5.0, 9.0, -12.0))
                res                  <- Executor.execute(ElasticRequest.knnSearch(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(equalTo(Chunk(firstDocumentUpdated, thirdDocumentUpdated)))
          }
        } @@ around(
          Executor.execute(
            ElasticRequest.createIndex(
              firstSearchIndex,
              """{ "mappings": { "properties": { "vectorField": { "type": "dense_vector", "dims": 3, "similarity": "l2_norm", "index": true } } } }"""
            )
          ),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("top two results with filters") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument, thirdDocumentId, thirdDocument) =>
              for {
                _                    <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                firstDocumentUpdated  = firstDocument.copy(intField = 15, vectorField = List(1, 5, -20))
                secondDocumentUpdated = secondDocument.copy(intField = 21, vectorField = List(42, 8, -15))
                thirdDocumentUpdated  = thirdDocument.copy(intField = 4, vectorField = List(15, 11, 23))
                req1                  = ElasticRequest.create(firstSearchIndex, firstDocumentId, firstDocumentUpdated)
                req2                  = ElasticRequest.create(firstSearchIndex, secondDocumentId, secondDocumentUpdated)
                req3                  = ElasticRequest.create(firstSearchIndex, thirdDocumentId, thirdDocumentUpdated)
                _                    <- Executor.execute(ElasticRequest.bulk(req1, req2, req3).refreshTrue)
                query                 = ElasticQuery.kNN(TestDocument.vectorField, 2, 3, Chunk(-5.0, 9.0, -12.0))
                filter                = ElasticQuery.range(TestDocument.intField).gt(10)
                res                  <- Executor
                         .execute(ElasticRequest.knnSearch(firstSearchIndex, query).filter(filter))
                         .documentAs[TestDocument]
              } yield assert(res)(equalTo(Chunk(firstDocumentUpdated, secondDocumentUpdated)))
          }
        } @@ around(
          Executor.execute(
            ElasticRequest.createIndex(
              firstSearchIndex,
              """{ "mappings": { "properties": { "vectorField": { "type": "dense_vector", "dims": 3, "similarity": "l2_norm", "index": true } } } }"""
            )
          ),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        )
      ) @@ shrinks(0),
      suite("search")(
        test("boosting query") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _                   <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                firstDocumentUpdated =
                  firstDocument.copy(stringField = s"this is a ${firstDocument.stringField} test", intField = 7)
                secondDocumentUpdated =
                  secondDocument.copy(
                    stringField = s"this is another ${secondDocument.stringField} test",
                    intField = 5
                  )
                _ <-
                  Executor.execute(
                    ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocumentUpdated)
                  )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocumentUpdated)
                         .refreshTrue
                     )
                query = boosting(
                          negativeBoost = 0.1f,
                          negativeQuery =
                            term(field = TestDocument.stringField, value = firstDocument.stringField.toLowerCase),
                          positiveQuery = matchPhrase(
                            field = TestDocument.stringField,
                            value = "test"
                          )
                        )
                res <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(equalTo(Chunk(secondDocumentUpdated, firstDocumentUpdated)))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("constant score query") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _       <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                document = firstDocument.copy(stringField = "this is a test")
                _       <-
                  Executor.execute(ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, document))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = constantScore(
                          matchPhrase(
                            field = TestDocument.stringField,
                            value = "test"
                          )
                        ).boost(2.1)
                res <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(Assertion.contains(document)) && assert(res)(!Assertion.contains(secondDocument))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("range query") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument, thirdDocumentId, thirdDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, thirdDocumentId, thirdDocument)
                         .refreshTrue
                     )
                query = range(TestDocument.doubleField).gte(100.0)
                res  <- Executor
                         .execute(ElasticRequest.search(firstSearchIndex, query).from(0).size(2))
                         .documentAs[TestDocument]
              } yield assert(res.length)(equalTo(2))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("range query with date format") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument, thirdDocumentId, thirdDocument) =>
              val firstDocumentUpdated  = firstDocument.copy(dateField = LocalDate.now.minusDays(2))
              val secondDocumentUpdated = secondDocument.copy(dateField = LocalDate.now)
              val thirdDocumentUpdated  = thirdDocument.copy(dateField = LocalDate.now.plusDays(2))
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocumentUpdated)
                     )
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocumentUpdated)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, thirdDocumentId, thirdDocumentUpdated)
                         .refreshTrue
                     )
                query = range(TestDocument.dateField).gte(LocalDate.now).format("yyyy-MM-dd").boost(1.0)
                res  <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(equalTo(Chunk(secondDocumentUpdated, thirdDocumentUpdated)))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("source filtering") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument, thirdDocumentId, thirdDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, thirdDocumentId, thirdDocument)
                         .refreshTrue
                     )
                query = range(TestDocument.doubleField).gte(100.0)
                res  <- Executor
                         .execute(ElasticRequest.search(firstSearchIndex, query).includes[PartialTestDocument])
                items <- res.items
              } yield assert(items.map(item => Right(item.raw)))(
                hasSameElements(
                  List(firstDocument, secondDocument, thirdDocument).map(document =>
                    TestDocument.schema.migrate(PartialTestDocument.schema).flatMap(_(document)).flatMap {
                      partialDocument =>
                        JsonCodec.jsonEncoder(PartialTestDocument.schema).toJsonAST(partialDocument)
                    }
                  )
                )
              )
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("fail on excluded source field decode") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument, thirdDocumentId, thirdDocument) =>
              val result =
                for {
                  _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                  _ <- Executor.execute(
                         ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                       )
                  _ <- Executor.execute(
                         ElasticRequest.upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                       )
                  _ <- Executor.execute(
                         ElasticRequest
                           .upsert[TestDocument](firstSearchIndex, thirdDocumentId, thirdDocument)
                           .refreshTrue
                       )
                  query = range(TestDocument.doubleField).gte(100.0)
                  _    <- Executor
                         .execute(ElasticRequest.search(firstSearchIndex, query).excludes("intField"))
                         .documentAs[TestDocument]
                } yield ()

              assertZIO(result.exit)(
                fails(
                  isSubtype[Exception](
                    assertException("Could not parse all documents successfully: .intField(missing)")
                  )
                )
              )
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("fail on decode error") {
          checkOnce(genDocumentId, genDocumentId, genTestDocument, genTestSubDocument) {
            (documentId, subDocumentId, document, subDocument) =>
              val result =
                for {
                  _ <- Executor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
                  _ <-
                    Executor.execute(ElasticRequest.upsert[TestDocument](secondSearchIndex, documentId, document))
                  _ <- Executor.execute(
                         ElasticRequest
                           .upsert[TestSubDocument](secondSearchIndex, subDocumentId, subDocument)
                           .refreshTrue
                       )
                  query = range(TestDocument.intField).gte(0)
                  res  <- Executor.execute(ElasticRequest.search(secondSearchIndex, query)).documentAs[TestDocument]
                } yield res

              assertZIO(result.exit)(
                fails(
                  isSubtype[Exception](
                    assertException("Could not parse all documents successfully: .dateField(missing)")
                  )
                )
              )
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(secondSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
        ),
        test("prefix query") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = ElasticQuery.prefix(
                          field = TestDocument.stringField.keyword,
                          value = firstDocument.stringField.take(3)
                        )
                res <- Executor
                         .execute(ElasticRequest.search(firstSearchIndex, query))
                         .documentAs[TestDocument]
              } yield assert(res)(Assertion.contains(firstDocument))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("disjunction max query") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _                   <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                firstDocumentUpdated =
                  firstDocument.copy(stringField = s"This is a ${firstDocument.stringField} test.")
                secondDocumentUpdated =
                  secondDocument.copy(stringField =
                    s"This is a ${secondDocument.stringField} test. It should be in the list before ${firstDocument.stringField}, because it has higher relevance score than ${firstDocument.stringField}"
                  )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocumentUpdated)
                     )
                _ <-
                  Executor.execute(
                    ElasticRequest
                      .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocumentUpdated)
                      .refreshTrue
                  )
                query = disjunctionMax(
                          term(
                            field = TestDocument.stringField,
                            value = firstDocument.stringField.toLowerCase
                          ),
                          matchPhrase(
                            field = TestDocument.stringField,
                            value = secondDocument.stringField
                          )
                        )
                res <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(equalTo(Chunk(secondDocumentUpdated, firstDocumentUpdated)))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("fuzzy query") {
          checkOnce(genDocumentId, genTestDocument) { (firstDocumentId, firstDocument) =>
            for {
              _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
              _ <-
                Executor.execute(
                  ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument).refreshTrue
                )
              query = ElasticQuery.fuzzy(
                        field = TestDocument.stringField.keyword,
                        value = firstDocument.stringField.substring(1)
                      )
              res <- Executor
                       .execute(ElasticRequest.search(firstSearchIndex, query))
                       .documentAs[TestDocument]
            } yield {
              assert(res)(Assertion.contains(firstDocument))
            }
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("simple query string query") {
          checkOnce(genDocumentId, genTestDocument, genMultiWordString(), genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, multiWordString, secondDocumentId, secondDocument) =>
              val firstDoc = firstDocument.copy(stringField = multiWordString)

              for {
                _ <- Executor.execute(ElasticRequest.upsert(firstSearchIndex, firstDocumentId, firstDoc))
                _ <- Executor.execute(
                       ElasticRequest.upsert(firstSearchIndex, secondDocumentId, secondDocument).refreshTrue
                     )
                searchTerm = multiWordString.split("\\s+").head
                query      = simpleQueryString(searchTerm).fields(Chunk(TestDocument.stringField))
                res       <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(Assertion.contains(firstDoc))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("simple query string with empty fields") {
          checkOnce(genDocumentId, genTestDocument, genMultiWordString(), genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, multiWordString, secondDocumentId, secondDocument) =>
              val firstDoc = firstDocument.copy(stringField = multiWordString)

              for {
                _ <- Executor.execute(ElasticRequest.upsert(firstSearchIndex, firstDocumentId, firstDoc))
                _ <- Executor.execute(
                       ElasticRequest.upsert(firstSearchIndex, secondDocumentId, secondDocument).refreshTrue
                     )
                searchTerm = multiWordString.split("\\s+").head
                query      = simpleQueryString(searchTerm)
                res       <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(Assertion.contains(firstDoc))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("simple query string with non-existent field") {
          checkOnce(genDocumentId, genTestDocument, genMultiWordString()) { (docId, doc, multiWordString) =>
            val docWithMultiWord = doc.copy(stringField = multiWordString)

            for {
              _         <- Executor.execute(ElasticRequest.upsert(firstSearchIndex, docId, docWithMultiWord).refreshTrue)
              searchTerm = multiWordString.split("\\s+").head
              query      = simpleQueryString(searchTerm).fields("nonExistentField")
              res       <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
            } yield assert(res)(Assertion.isEmpty)
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("wildcard contains query") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = ElasticQuery.contains(
                          field = TestDocument.stringField.keyword,
                          value = firstDocument.stringField.take(3)
                        )
                res <- Executor
                         .execute(ElasticRequest.search(firstSearchIndex, query))
                         .documentAs[TestDocument]
              } yield assert(res)(Assertion.contains(firstDocument))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("wildcard startsWith query") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = ElasticQuery.startsWith(
                          field = TestDocument.stringField.keyword,
                          value = firstDocument.stringField.take(3)
                        )
                res <- Executor
                         .execute(ElasticRequest.search(firstSearchIndex, query))
                         .documentAs[TestDocument]
              } yield assert(res)(Assertion.contains(firstDocument))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("wildcard pattern query") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <-
                  Executor.execute(
                    ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                  )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = wildcard(
                          field = TestDocument.stringField.keyword,
                          value = s"${firstDocument.stringField.take(2)}*${firstDocument.stringField.takeRight(2)}"
                        )
                res <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(Assertion.contains(firstDocument))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("match all with index pattern") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _                <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _                <- Executor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
                firstDocumentCopy = firstDocument.copy(stringField = "this is test")
                _                <-
                  Executor.execute(
                    ElasticRequest
                      .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocumentCopy)
                      .refreshTrue
                  )
                secondDocumentCopy = secondDocument.copy(stringField = "this is test")
                _                 <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](secondSearchIndex, secondDocumentId, secondDocumentCopy)
                         .refreshTrue
                     )
                query = matchAll
                res  <- Executor
                         .execute(ElasticRequest.search(IndexPattern("search-index*"), query))
                         .documentAs[TestDocument]
              } yield assert(res)(Assertion.contains(firstDocumentCopy)) && assert(res)(
                Assertion.contains(secondDocumentCopy)
              )
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ) @@ around(
          Executor.execute(ElasticRequest.createIndex(secondSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
        ),
        test("match boolean prefix query") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _       <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                document = firstDocument.copy(stringField = "test this is boolean")
                _       <-
                  Executor.execute(ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, document))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = matchBooleanPrefix(TestDocument.stringField, "this is test bo")
                res  <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(Assertion.contains(document)) && assert(res)(!Assertion.contains(secondDocument))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("match phrase query with multi index") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _                <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _                <- Executor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
                firstDocumentCopy = firstDocument.copy(stringField = "this is test")
                _                <-
                  Executor.execute(
                    ElasticRequest
                      .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocumentCopy)
                      .refreshTrue
                  )
                secondDocumentCopy = secondDocument.copy(stringField = "this is test")
                _                 <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](secondSearchIndex, secondDocumentId, secondDocumentCopy)
                         .refreshTrue
                     )
                query = matchPhrase(
                          field = TestDocument.stringField,
                          value = firstDocumentCopy.stringField
                        )

                res <- Executor
                         .execute(ElasticRequest.search(MultiIndex.names(firstSearchIndex, secondSearchIndex), query))
                         .documentAs[TestDocument]
              } yield assert(res)(Assertion.contains(firstDocumentCopy)) && assert(res)(
                Assertion.contains(secondDocumentCopy)
              )
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ) @@ around(
          Executor.execute(ElasticRequest.createIndex(secondSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
        ),
        test("match phrase query") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _       <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                document = firstDocument.copy(stringField = s"this is ${firstDocument.stringField} test")
                _       <-
                  Executor.execute(ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, document))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = matchPhrase(
                          field = TestDocument.stringField,
                          value = firstDocument.stringField
                        )
                res <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(Assertion.contains(document))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("match phrase prefix query") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _       <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                document = firstDocument.copy(stringField = s"${firstDocument.stringField} test")
                _       <-
                  Executor.execute(ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, document))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = matchPhrasePrefix(
                          field = TestDocument.stringField,
                          value = s"${firstDocument.stringField} te"
                        )
                res <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(Assertion.contains(document)) && assert(res)(!Assertion.contains(secondDocument))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("multi match query") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _       <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                document = firstDocument.copy(stringField = "test")
                _       <-
                  Executor.execute(ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, document))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )

                query =
                  multiMatch(value = "test").fields(TestDocument.stringField).matchingType(BestFields)
                res <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(Assertion.contains(document)) && assert(res)(!Assertion.contains(secondDocument))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("terms query") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument, thirdDocumentId, thirdDocument) =>
              for {
                _                    <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                firstDocumentUpdated  = firstDocument.copy(stringField = s"this is ${firstDocument.stringField} test")
                secondDocumentUpdated =
                  secondDocument.copy(stringField = s"this is ${secondDocument.stringField} another test")
                _ <-
                  Executor.execute(
                    ElasticRequest
                      .bulk(
                        ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocumentUpdated),
                        ElasticRequest
                          .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocumentUpdated),
                        ElasticRequest
                          .upsert[TestDocument](firstSearchIndex, thirdDocumentId, thirdDocument)
                      )
                      .refreshTrue
                  )
                query = terms(
                          field = TestDocument.stringField,
                          values = firstDocument.stringField.toLowerCase,
                          secondDocument.stringField.toLowerCase
                        )
                res <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(hasSameElements(List(firstDocumentUpdated, secondDocumentUpdated)))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("terms set query with minimumShouldMatchField") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _                   <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                firstDocumentUpdated =
                  firstDocument.copy(stringField = s"this is ${firstDocument.stringField} test", intField = 2)
                secondDocumentUpdated =
                  secondDocument.copy(
                    stringField =
                      s"this is ${secondDocument.stringField} another test, not ${firstDocument.stringField}",
                    intField = 2
                  )
                _ <-
                  Executor.execute(
                    ElasticRequest
                      .bulk(
                        ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocumentUpdated),
                        ElasticRequest
                          .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocumentUpdated)
                      )
                      .refreshTrue
                  )
                query = termsSet(
                          field = "stringField",
                          minimumShouldMatchField = "intField",
                          terms = secondDocument.stringField.toLowerCase,
                          firstDocument.stringField.toLowerCase
                        )
                res <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(hasSameElements(Chunk(secondDocumentUpdated)))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("terms set query with minimumShouldMatchScript") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _                   <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                firstDocumentUpdated =
                  firstDocument.copy(stringField = s"this is ${firstDocument.stringField} test", intField = 2)
                secondDocumentUpdated =
                  secondDocument.copy(
                    stringField = s"this is ${secondDocument.stringField} test, not ${firstDocument.stringField}",
                    intField = 2
                  )
                _ <-
                  Executor.execute(
                    ElasticRequest
                      .bulk(
                        ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocumentUpdated),
                        ElasticRequest
                          .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocumentUpdated)
                      )
                      .refreshTrue
                  )
                query = termsSetScript(
                          field = TestDocument.stringField,
                          minimumShouldMatchScript = Script("doc['intField'].value"),
                          terms = firstDocument.stringField.toLowerCase,
                          secondDocument.stringField.toLowerCase
                        )
                res <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(hasSameElements(Chunk(secondDocumentUpdated)))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("nested query") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <-
                  Executor.execute(
                    ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                  )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query =
                  nested(path = TestDocument.subDocumentList, query = matchAll)
                res <-
                  Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(hasSameElements(List(firstDocument, secondDocument)))
          }
        } @@ around(
          Executor.execute(
            ElasticRequest.createIndex(
              firstSearchIndex,
              """{ "mappings": { "properties": { "subDocumentList": { "type": "nested" } } } }"""
            )
          ),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("should with satisfying minimumShouldMatch") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <-
                  Executor.execute(
                    ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                  )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = should(
                          matches(TestDocument.stringField, firstDocument.stringField),
                          matches(TestDocument.intField, firstDocument.intField),
                          matches(TestDocument.doubleField, firstDocument.doubleField + 1)
                        ).minimumShouldMatch(2)
                res <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(Assertion.contains(firstDocument))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("script query") {
          checkN(4)(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <-
                  Executor.execute(
                    ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                  )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = ElasticQuery.script(Script("doc['booleanField'].value == true"))
                res  <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(hasSameElements(List(firstDocument, secondDocument).filter(_.booleanField == true)))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("regexp query without case insensitive") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query =
                  ElasticQuery.regexp(
                    field = TestDocument.stringField,
                    value =
                      s"${firstDocument.stringField.take(1)}.*${firstDocument.stringField.takeRight(1)}".toUpperCase
                  )
                res <- Executor
                         .execute(ElasticRequest.search(firstSearchIndex, query))
                         .documentAs[TestDocument]
              } yield assert(res)(!Assertion.contains(firstDocument))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("regexp query with case insensitive") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = ElasticQuery
                          .regexp(
                            field = TestDocument.stringField,
                            value = s"${firstDocument.stringField.take(1)}.*${firstDocument.stringField.takeRight(1)}"
                          )
                          .caseInsensitiveTrue
                res <- Executor
                         .execute(ElasticRequest.search(firstSearchIndex, query))
                         .documentAs[TestDocument]
              } yield assert(res)(Assertion.contains(firstDocument)) && assert(res)(
                !Assertion.contains(secondDocument)
              )
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("should with unsatisfying minimumShouldMatch") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <-
                  Executor.execute(
                    ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                  )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = should(
                          matches(TestDocument.stringField, firstDocument.stringField),
                          matches(TestDocument.intField, firstDocument.intField + 1),
                          matches(TestDocument.doubleField, firstDocument.doubleField + 1)
                        ).minimumShouldMatch(2)
                res <- Executor.execute(ElasticRequest.search(firstSearchIndex, query)).documentAs[TestDocument]
              } yield assert(res)(isEmpty)
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        )
      ) @@ shrinks(0),
      suite("search with inner hits")(
        test("nested query with inner hits") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <-
                  Executor.execute(
                    ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                  )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query =
                  nested(path = TestDocument.subDocumentList, query = matchAll).innerHits
                result <- Executor.execute(ElasticRequest.search(firstSearchIndex, query))
                items  <- result.items
                res     =
                  items.map(_.innerHitAs[TestSubDocument]("subDocumentList")).collect { case Right(value) => value }
              } yield assert(res)(
                hasSameElements(List(firstDocument.subDocumentList, secondDocument.subDocumentList))
              )
          }
        } @@ around(
          Executor.execute(
            ElasticRequest.createIndex(
              firstSearchIndex,
              """{ "mappings": { "properties": { "subDocumentList": { "type": "nested" } } } }"""
            )
          ),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        )
      ) @@ shrinks(0),
      suite("search with highlights")(
        test("highlight") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = should(matches("stringField", firstDocument.stringField))
                res  <-
                  Executor.execute(
                    ElasticRequest.search(firstSearchIndex, query).highlights(highlight("stringField"))
                  )
                items <- res.items
              } yield assert(items.map(_.highlight("stringField")))(
                hasSameElements(List(Some(Chunk(s"<em>${firstDocument.stringField}</em>"))))
              )
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("inner hit highlight") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <-
                  Executor.execute(
                    ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                  )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = nested(
                          path = TestDocument.subDocumentList,
                          query = must(
                            matches(
                              TestSubDocument.stringField,
                              secondDocument.subDocumentList.headOption.map(_.stringField).getOrElse("foo")
                            )
                          )
                        ).innerHits(
                          InnerHits().highlights(highlight(TestSubDocument.stringField))
                        )
                result <- Executor.execute(ElasticRequest.search(firstSearchIndex, query))
                items  <- result.items
                res     = items
                        .flatMap(_.innerHit("subDocumentList"))
                        .flatten
                        .flatMap(_.highlight("subDocumentList.stringField"))
                        .flatten
              } yield assert(res)(
                Assertion.contains(
                  secondDocument.subDocumentList.headOption
                    .map(doc => s"<em>${doc.stringField}</em>")
                    .getOrElse("<em>foo</em>")
                )
              )
          }
        } @@ around(
          Executor.execute(
            ElasticRequest.createIndex(
              firstSearchIndex,
              """{ "mappings": { "properties": { "subDocumentList": { "type": "nested" } } } }"""
            )
          ),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("highlight with field accessor") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = should(matches("stringField", firstDocument.stringField))
                res  <-
                  Executor.execute(
                    ElasticRequest.search(firstSearchIndex, query).highlights(highlight(TestDocument.stringField))
                  )
                items <- res.items
              } yield assert(items.map(_.highlight(TestDocument.stringField)))(
                hasSameElements(List(Some(Chunk(s"<em>${firstDocument.stringField}</em>"))))
              )
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("highlights map") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = should(matches("stringField", firstDocument.stringField))
                res  <-
                  Executor.execute(
                    ElasticRequest.search(firstSearchIndex, query).highlights(highlight("stringField"))
                  )
                items <- res.items
              } yield assert(items.map(_.highlights))(
                hasSameElements(List(Some(Map("stringField" -> Chunk(s"<em>${firstDocument.stringField}</em>")))))
              )
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("highlight with global config") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = should(matches("stringField", firstDocument.stringField))
                res  <-
                  Executor.execute(
                    ElasticRequest
                      .search(firstSearchIndex, query)
                      .highlights(
                        highlight(TestDocument.stringField)
                          .withGlobalConfig("pre_tags", Arr(Str("<ul>")))
                          .withGlobalConfig("post_tags", Arr(Str("</ul>")))
                      )
                  )
                items <- res.items
              } yield assert(items.map(_.highlight(TestDocument.stringField)))(
                hasSameElements(List(Some(Chunk(s"<ul>${firstDocument.stringField}</ul>"))))
              )
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("highlight with local config overwriting global") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = should(matches("stringField", firstDocument.stringField))
                res  <-
                  Executor.execute(
                    ElasticRequest
                      .search(firstSearchIndex, query)
                      .highlights(
                        highlight(
                          TestDocument.stringField,
                          config = Map("pre_tags" -> Arr(Str("<ol>")), "post_tags" -> Arr(Str("</ol>")))
                        )
                          .withGlobalConfig("pre_tags", Arr(Str("<ul>")))
                          .withGlobalConfig("post_tags", Arr(Str("</ul>")))
                      )
                  )
                items <- res.items
              } yield assert(items.map(_.highlight(TestDocument.stringField)))(
                hasSameElements(List(Some(Chunk(s"<ol>${firstDocument.stringField}</ol>"))))
              )
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        )
      ),
      suite("search with sorting")(
        test("sort by descending age and ascending date") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              val firstDocumentWithFixedIntField =
                firstDocument.copy(intField = 30, dateField = LocalDate.parse("1993-12-05"))
              val secondDocumentWithFixedIntField =
                secondDocument.copy(intField = 36, dateField = LocalDate.parse("1987-12-05"))
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocumentWithFixedIntField)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](
                           firstSearchIndex,
                           secondDocumentId,
                           secondDocumentWithFixedIntField
                         )
                         .refreshTrue
                     )
                query = range(TestDocument.intField).gte(20)
                res  <- Executor
                         .execute(
                           ElasticRequest
                             .search(firstSearchIndex, query)
                             .sort(
                               sortBy(TestDocument.intField).order(Desc),
                               sortBy(TestDocument.dateField).order(Asc).format("strict_date_optional_time_nanos")
                             )
                         )
                         .documentAs[TestDocument]
              } yield assert(res)(
                equalTo(Chunk(secondDocumentWithFixedIntField, firstDocumentWithFixedIntField))
              )
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("sort by script ascending") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstEmployee, secondDocumentId, secondEmployee) =>
              val firstDocumentWithFixedIntField =
                firstEmployee.copy(intField = 30, dateField = LocalDate.parse("1993-12-05"))
              val secondDocumentWithFixedIntField =
                secondEmployee.copy(intField = 36, dateField = LocalDate.parse("1987-12-05"))
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocumentWithFixedIntField)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](
                           firstSearchIndex,
                           secondDocumentId,
                           secondDocumentWithFixedIntField
                         )
                         .refreshTrue
                     )
                query = range(TestDocument.intField).gte(20)
                res  <-
                  Executor
                    .execute(
                      ElasticRequest
                        .search(firstSearchIndex, query)
                        .sort(sortBy(Script("doc['intField'].value").lang(Painless), NumberType).order(Asc))
                    )
                    .documentAs[TestDocument]
              } yield assert(res)(
                equalTo(Chunk(firstDocumentWithFixedIntField, secondDocumentWithFixedIntField))
              )
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("sort descending with max mode") {
          checkOnce(genDocumentId, genTestSubDocument, genDocumentId, genTestSubDocument) {
            (firstDocumentId, firstSubDocument, secondDocumentId, secondSubDocument) =>
              val firstSubDocumentWithFixedIntList  = firstSubDocument.copy(intFieldList = List(11, 4, 37))
              val secondSubDocumentWithFixedIntList = secondSubDocument.copy(intFieldList = List(30, 29, 35))
              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestSubDocument](firstSearchIndex, firstDocumentId, firstSubDocumentWithFixedIntList)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestSubDocument](
                           firstSearchIndex,
                           secondDocumentId,
                           secondSubDocumentWithFixedIntList
                         )
                         .refreshTrue
                     )
                query = matchAll
                res  <- Executor
                         .execute(
                           ElasticRequest
                             .search(firstSearchIndex, query)
                             .sort(sortBy(TestSubDocument.intFieldList).mode(Max).order(Desc))
                         )
                         .documentAs[TestSubDocument]
              } yield assert(res)(
                equalTo(Chunk(firstSubDocumentWithFixedIntList, secondSubDocumentWithFixedIntList))
              )
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        )
      ) @@ shrinks(0),
      suite("search with scroll API")(
        test("range query") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument) =>
              val sink: Sink[Throwable, Item, Nothing, Chunk[Item]] = ZSink.collectAll[Item]

              for {
                _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
                _ <- Executor.execute(
                       ElasticRequest.upsert[TestDocument](firstSearchIndex, firstDocumentId, firstDocument)
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](firstSearchIndex, secondDocumentId, secondDocument)
                         .refreshTrue
                     )
                query = range(TestDocument.doubleField).gte(100.0)
                res  <- Executor.stream(ElasticRequest.search(firstSearchIndex, query)).run(sink)
              } yield assert(res)(isNonEmpty)
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("range query with multiple pages") {
          checkOnce(genTestDocument) { document =>
            def sink: Sink[Throwable, Item, Nothing, Chunk[Item]] = ZSink.collectAll[Item]

            for {
              _   <- Executor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
              reqs = (0 to 203).map { _ =>
                       ElasticRequest.create[TestDocument](
                         secondSearchIndex,
                         document.copy(stringField = Random.alphanumeric.take(5).mkString, doubleField = 150)
                       )
                     }
              _    <- Executor.execute(ElasticRequest.bulk(reqs: _*).refreshTrue)
              query = range(TestDocument.doubleField).gte(100.0)
              res  <- Executor
                       .stream(
                         ElasticRequest.search(secondSearchIndex, query)
                       )
                       .run(sink)
            } yield assert(res)(hasSize(equalTo(204)))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(secondSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
        ),
        test("range query with multiple pages and return type") {
          checkOnce(genTestDocument) { document =>
            def sink: Sink[Throwable, TestDocument, Nothing, Chunk[TestDocument]] =
              ZSink.collectAll[TestDocument]

            for {
              _   <- Executor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
              reqs = (0 to 200).map { _ =>
                       ElasticRequest.create[TestDocument](
                         secondSearchIndex,
                         document.copy(stringField = Random.alphanumeric.take(5).mkString, doubleField = 150)
                       )
                     }
              _    <- Executor.execute(ElasticRequest.bulk(reqs: _*).refreshTrue)
              query = range(TestDocument.doubleField).gte(100.0)
              res  <- Executor
                       .streamAs[TestDocument](ElasticRequest.search(secondSearchIndex, query))
                       .run(sink)
            } yield assert(res)(hasSize(equalTo(201)))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(secondSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
        ),
        test("empty stream") {
          val sink: Sink[Throwable, Item, Nothing, Chunk[Item]] = ZSink.collectAll[Item]

          for {
            _    <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
            query = range(TestDocument.doubleField).gte(100.0)
            res  <- Executor.stream(ElasticRequest.search(firstSearchIndex, query)).run(sink)
          } yield assert(res)(hasSize(equalTo(0)))
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        )
      ) @@ shrinks(0),
      suite("search with PIT")(
        test("PIT stream results") {
          checkOnce(genTestDocument) { document =>
            def sink: Sink[Throwable, Item, Nothing, Chunk[Item]] =
              ZSink.collectAll[Item]

            for {
              _   <- Executor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
              reqs = (0 to 200).map { _ =>
                       ElasticRequest.create[TestDocument](
                         secondSearchIndex,
                         document.copy(stringField = Random.alphanumeric.take(5).mkString, doubleField = 150)
                       )
                     }
              _    <- Executor.execute(ElasticRequest.bulk(reqs: _*).refreshTrue)
              query = range(TestDocument.doubleField).gte(100.0)
              res  <- Executor
                       .stream(ElasticRequest.search(secondSearchIndex, query), StreamConfig.SearchAfter)
                       .run(sink)
            } yield assert(res)(hasSize(equalTo(201)))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(secondSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
        ),
        test("PIT stream with custom page size and keep alive") {
          checkOnce(genTestDocument) { document =>
            def sink: Sink[Throwable, Item, Nothing, Chunk[Item]] =
              ZSink.collectAll[Item]

            for {
              _   <- Executor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
              reqs = (0 to 200).map { _ =>
                       ElasticRequest.create[TestDocument](
                         secondSearchIndex,
                         document.copy(stringField = Random.alphanumeric.take(5).mkString, doubleField = 150)
                       )
                     }
              _    <- Executor.execute(ElasticRequest.bulk(reqs: _*).refreshTrue)
              query = range(TestDocument.doubleField).gte(100.0)
              res  <- Executor
                       .stream(
                         ElasticRequest.search(secondSearchIndex, query),
                         StreamConfig.SearchAfter.withPageSize(40).keepAliveFor("2m")
                       )
                       .run(sink)
            } yield assert(res)(hasSize(equalTo(201)))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(secondSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
        ),
        test("PIT stream as specific type") {
          checkOnce(genTestDocument) { document =>
            def sink: Sink[Throwable, TestDocument, Nothing, Chunk[TestDocument]] =
              ZSink.collectAll[TestDocument]

            for {
              _   <- Executor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
              reqs = (0 to 200).map { _ =>
                       ElasticRequest.create[TestDocument](
                         secondSearchIndex,
                         document.copy(stringField = Random.alphanumeric.take(5).mkString, doubleField = 150)
                       )
                     }
              _    <- Executor.execute(ElasticRequest.bulk(reqs: _*).refreshTrue)
              query = range(TestDocument.doubleField).gte(100.0)
              res  <- Executor
                       .streamAs[TestDocument](
                         ElasticRequest.search(secondSearchIndex, query),
                         StreamConfig.SearchAfter
                       )
                       .run(sink)
            } yield assert(res)(hasSize(equalTo(201)))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(secondSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
        ),
        test("PIT empty stream") {
          checkOnce(genTestDocument) { document =>
            def sink: Sink[Throwable, Item, Nothing, Chunk[Item]] =
              ZSink.collectAll[Item]

            for {
              _ <- Executor.execute(ElasticRequest.deleteByQuery(secondSearchIndex, matchAll))
              _ <- Executor.execute(
                     ElasticRequest
                       .create[TestDocument](
                         secondSearchIndex,
                         document.copy(stringField = Random.alphanumeric.take(5).mkString, doubleField = 150)
                       )
                       .refreshTrue
                   )
              query = range(TestDocument.doubleField).gte(200.0)
              res  <- Executor
                       .stream(ElasticRequest.search(secondSearchIndex, query), StreamConfig.SearchAfter)
                       .run(sink)
            } yield assert(res)(isEmpty)
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(secondSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(secondSearchIndex)).orDie
        )
      ) @@ shrinks(0),
      suite("search with SearchAfter")(
        test("search after with ascending sort") {
          checkOnce(genTestDocument) { firstDocument =>
            for {
              _   <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
              reqs = (0 to 100).map { i =>
                       ElasticRequest.create[TestDocument](
                         firstSearchIndex,
                         firstDocument.copy(stringField = Random.alphanumeric.take(5).mkString, intField = i)
                       )
                     }
              _    <- Executor.execute(ElasticRequest.bulk(reqs: _*).refreshTrue)
              query = range(TestDocument.intField).gte(10)
              res  <- Executor
                       .execute(
                         ElasticRequest
                           .search(firstSearchIndex, query)
                           .size(10)
                           .sort(
                             sortBy(TestDocument.intField).order(Asc)
                           )
                       )
              sa   <- res.lastSortValue
              res2 <- Executor
                        .execute(
                          ElasticRequest
                            .search(firstSearchIndex, query)
                            .searchAfter(sa.get)
                            .size(10)
                            .sort(
                              sortBy(TestDocument.intField).order(Asc)
                            )
                        )
                        .documentAs[TestDocument]
            } yield assert(res2.map(_.intField))(
              equalTo(Chunk.fromIterable(20 to 29))
            )
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(firstSearchIndex)),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ) @@ shrinks(0)
      ),
      suite("deleting by query")(
        test("delete all matched documents") {
          checkOnce(genDocumentId, genTestDocument, genDocumentId, genTestDocument, genDocumentId, genTestDocument) {
            (firstDocumentId, firstDocument, secondDocumentId, secondDocument, thirdDocumentId, thirdDocument) =>
              for {
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](
                           deleteByQueryIndex,
                           firstDocumentId,
                           firstDocument.copy(doubleField = 150)
                         )
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](
                           deleteByQueryIndex,
                           secondDocumentId,
                           secondDocument.copy(doubleField = 350)
                         )
                     )
                _ <- Executor.execute(
                       ElasticRequest
                         .upsert[TestDocument](
                           deleteByQueryIndex,
                           thirdDocumentId,
                           thirdDocument.copy(doubleField = 400)
                         )
                         .refreshTrue
                     )
                deleteQuery = range(TestDocument.doubleField).gte(300.0)
                _          <- Executor
                       .execute(ElasticRequest.deleteByQuery(deleteByQueryIndex, deleteQuery).refreshTrue)
                res <- Executor
                         .execute(ElasticRequest.search(deleteByQueryIndex, matchAll))
                         .documentAs[TestDocument]
              } yield assert(res)(hasSameElements(List(firstDocument.copy(doubleField = 150))))
          }
        } @@ around(
          Executor.execute(ElasticRequest.createIndex(deleteByQueryIndex)),
          Executor.execute(ElasticRequest.deleteIndex(deleteByQueryIndex)).orDie
        ),
        test("return NotFound for missing index") {
          checkOnce(genIndexName) { missingIndex =>
            assertZIO(Executor.execute(ElasticRequest.deleteByQuery(missingIndex, matchAll)))(
              equalTo(DeletionOutcome.NotFound)
            )
          }
        }
      ),
      suite("bulk")(
        test("execute bulk query") {
          checkOnce(genDocumentId, genDocumentId, genDocumentId, genTestDocument) {
            (firstDocumentId, secondDocumentId, thirdDocumentId, document) =>
              for {
                _ <- Executor.execute(
                       ElasticRequest
                         .create[TestDocument](index, firstDocumentId, document.copy(stringField = "randomIdString"))
                     )
                _ <-
                  Executor.execute(
                    ElasticRequest
                      .create[TestDocument](index, secondDocumentId, document.copy(stringField = "randomIdString2"))
                      .refreshTrue
                  )
                req1 = ElasticRequest.create[TestDocument](index, thirdDocumentId, document)
                req2 = ElasticRequest.create[TestDocument](index, document.copy(stringField = "randomIdString3"))
                req3 = ElasticRequest.upsert[TestDocument](index, firstDocumentId, document.copy(doubleField = 3000))
                req4 = ElasticRequest.deleteById(index, secondDocumentId)
                req5 = ElasticRequest.update[TestDocument](index, thirdDocumentId, document.copy(intField = 100))
                req6 = ElasticRequest.updateByScript(
                         index,
                         firstDocumentId,
                         Script("ctx._source.intField = params['factor']").params("factor" -> 100)
                       )
                req7 =
                  ElasticRequest
                    .update[TestDocument](index, DocumentId("invalid-document-id"), document.copy(intField = 100))
                res <-
                  Executor.execute(ElasticRequest.bulk(req1, req2, req3, req4, req5, req6, req7).refreshTrue)
                doc1 <- Executor.execute(ElasticRequest.getById(index, firstDocumentId)).documentAs[TestDocument]
                doc2 <- Executor.execute(ElasticRequest.getById(index, secondDocumentId)).documentAs[TestDocument]
                doc3 <- Executor.execute(ElasticRequest.getById(index, thirdDocumentId)).documentAs[TestDocument]
              } yield assert(res.items.size)(equalTo(7)) &&
                assert(res.items.map(_.error.isDefined))(
                  equalTo(Chunk(false, false, false, false, false, false, true))
                ) &&
                assert(res.items(6).status)(equalTo(Some(404))) &&
                assert(res.items(6).error.map(_.`type`))(equalTo(Some("document_missing_exception"))) &&
                assert(doc3)(isSome(equalTo(document.copy(intField = 100)))) &&
                assert(doc2)(isNone) && assert(doc1)(
                  isSome(equalTo(document.copy(doubleField = 3000, intField = 100)))
                )
          }
        }
      ),
      suite("updating document")(
        test("update with script") {
          checkOnce(genDocumentId, genTestDocument) { (documentId, document) =>
            val intField = document.intField
            val factor   = 2
            for {
              _ <- Executor.execute(ElasticRequest.upsert[TestDocument](index, documentId, document))
              _ <- Executor.execute(
                     ElasticRequest.updateByScript(
                       index,
                       documentId,
                       Script("ctx._source.intField += params['factor']").params("factor" -> factor)
                     )
                   )
              doc <- Executor.execute(ElasticRequest.getById(index, documentId)).documentAs[TestDocument]
            } yield assert(doc)(isSome(equalTo(document.copy(intField = intField + factor))))
          }
        },
        test("create if not exists") {
          checkOnce(genDocumentId, genTestDocument) { (documentId, document) =>
            for {
              _ <- Executor.execute(
                     ElasticRequest
                       .updateByScript(
                         index,
                         documentId,
                         Script("ctx._source.intField += params['factor']").params("factor" -> 2)
                       )
                       .orCreate(document)
                   )
              doc <- Executor.execute(ElasticRequest.getById(index, documentId)).documentAs[TestDocument]
            } yield assert(doc)(isSome(equalTo(document)))
          }
        },
        test("update with doc") {
          checkOnce(genDocumentId, genTestDocument, genTestDocument) { (documentId, firstDocument, secondDocument) =>
            for {
              _   <- Executor.execute(ElasticRequest.upsert[TestDocument](index, documentId, firstDocument))
              _   <- Executor.execute(ElasticRequest.update[TestDocument](index, documentId, secondDocument))
              doc <- Executor.execute(ElasticRequest.getById(index, documentId)).documentAs[TestDocument]
            } yield assert(doc)(isSome(equalTo(secondDocument)))
          }
        }
      ),
      suite("updating by query")(
        test("update with script only") {
          checkOnce(genDocumentId, genTestDocument) { (documentId, document) =>
            val stringField = "StringField"
            for {
              _ <- Executor.execute(ElasticRequest.deleteByQuery(updateByQueryIndex, matchAll).refreshTrue)
              _ <- Executor.execute(
                     ElasticRequest.upsert[TestDocument](updateByQueryIndex, documentId, document).refreshTrue
                   )
              updateRes <-
                Executor.execute(
                  ElasticRequest
                    .updateAllByQuery(
                      updateByQueryIndex,
                      Script("ctx._source['stringField'] = params['str']").params("str" -> stringField)
                    )
                    .refreshTrue
                )
              doc <- Executor.execute(ElasticRequest.getById(updateByQueryIndex, documentId)).documentAs[TestDocument]
            } yield assert(updateRes)(
              equalTo(
                UpdateByQueryResult(took = updateRes.took, total = 1, updated = 1, deleted = 0, versionConflicts = 0)
              )
            ) && assert(doc)(isSome(equalTo(document.copy(stringField = stringField))))
          }
        },
        test("update with script and query") {
          checkOnce(genDocumentId, genTestDocument) { (documentId, document) =>
            val newDocument = document.copy(stringField = "StringField")
            for {
              _ <- Executor.execute(ElasticRequest.deleteByQuery(updateByQueryIndex, matchAll).refreshTrue)
              _ <- Executor.execute(
                     ElasticRequest.upsert[TestDocument](updateByQueryIndex, documentId, newDocument).refreshTrue
                   )
              updateRes <-
                Executor.execute(
                  ElasticRequest
                    .updateByQuery(
                      index = updateByQueryIndex,
                      query = term(field = TestDocument.stringField.keyword, value = "StringField"),
                      script = Script("ctx._source['intField']++")
                    )
                    .refreshTrue
                )
              doc <- Executor.execute(ElasticRequest.getById(updateByQueryIndex, documentId)).documentAs[TestDocument]
            } yield assert(updateRes)(
              equalTo(
                UpdateByQueryResult(took = updateRes.took, total = 1, updated = 1, deleted = 0, versionConflicts = 0)
              )
            ) && assert(doc)(isSome(equalTo(newDocument.copy(intField = newDocument.intField + 1))))
          }
        }
      ),
      suite("geo-distance")(
        test("geo-distance query") {
          checkOnce(genTestDocument) { document =>
            val indexDefinition =
              """
                |{
                |  "mappings": {
                |      "properties": {
                |        "geoPointField": {
                |          "type": "geo_point"
                |      }
                |    }
                |  }
                |}
                |""".stripMargin

            for {
              _ <- Executor.execute(ElasticRequest.createIndex(geoDistanceIndex, indexDefinition))
              _ <- Executor.execute(ElasticRequest.deleteByQuery(geoDistanceIndex, matchAll))
              _ <- Executor.execute(
                     ElasticRequest.create[TestDocument](geoDistanceIndex, document).refreshTrue
                   )
              result <- Executor
                          .execute(
                            ElasticRequest.search(
                              geoDistanceIndex,
                              ElasticQuery
                                .geoDistance(
                                  "geoPointField",
                                  GeoPoint(document.geoPointField.lat, document.geoPointField.lon),
                                  Distance(300, Kilometers)
                                )
                            )
                          )
                          .documentAs[TestDocument]
            } yield assert(result)(equalTo(Chunk(document)))
          }
        } @@ after(Executor.execute(ElasticRequest.deleteIndex(geoDistanceIndex)).orDie)
      ),
      suite("geo-polygon")(
        test("geo-polygon query") {
          checkOnce(genTestDocument) { document =>
            val indexDefinition =
              """
                |{
                |  "mappings": {
                |      "properties": {
                |        "geoPointField": {
                |          "type": "geo_point"
                |      }
                |    }
                |  }
                |}
                |""".stripMargin

            for {
              _ <- Executor.execute(ElasticRequest.createIndex(geoPolygonIndex, indexDefinition))
              _ <- Executor.execute(ElasticRequest.deleteByQuery(geoPolygonIndex, matchAll))
              _ <- Executor.execute(
                     ElasticRequest.create[TestDocument](geoPolygonIndex, document).refreshTrue
                   )

              r1 <- Executor
                      .execute(
                        ElasticRequest.search(
                          geoPolygonIndex,
                          ElasticQuery
                            .geoPolygon("geoPointField", Chunk("0, 0", "0, 90", "90, 90", "90, 0"))
                        )
                      )
                      .documentAs[TestDocument]
            } yield assert(r1)(equalTo(Chunk(document)))
          }
        } @@ after(Executor.execute(ElasticRequest.deleteIndex(geoPolygonIndex)).orDie)
      ),
      suite("search with FunctionScore")(
        test("randomScore function") {
          checkOnce(genTestDocument, genTestDocument) { (firstDocument, secondDocument) =>
            val secondDocumentUpdated = secondDocument.copy(stringField = firstDocument.stringField)
            for {
              _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
              _ <- Executor.execute(
                     ElasticRequest.create[TestDocument](firstSearchIndex, firstDocument).refreshTrue
                   )
              _ <- Executor.execute(
                     ElasticRequest
                       .create[TestDocument](
                         firstSearchIndex,
                         secondDocumentUpdated
                       )
                       .refreshTrue
                   )
              r1 <- Executor
                      .execute(
                        ElasticRequest.search(
                          firstSearchIndex,
                          ElasticQuery
                            .functionScore(randomScoreFunction())
                            .query(matches("stringField", firstDocument.stringField))
                        )
                      )
                      .documentAs[TestDocument]
            } yield assert(r1)(
              hasSameElements(Chunk(firstDocument, secondDocumentUpdated))
            )
          }
        } @@ around(
          Executor.execute(
            ElasticRequest.createIndex(
              firstSearchIndex,
              """{ "mappings": { "properties": { "subDocumentList": { "type": "nested" } } } }"""
            )
          ),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        ),
        test("randomScore and weight functions") {
          checkOnce(genTestDocument, genTestDocument) { (firstDocument, secondDocument) =>
            val secondDocumentUpdated = secondDocument.copy(stringField = firstDocument.stringField)
            for {
              _ <- Executor.execute(ElasticRequest.deleteByQuery(firstSearchIndex, matchAll))
              _ <- Executor.execute(
                     ElasticRequest.create[TestDocument](firstSearchIndex, firstDocument).refreshTrue
                   )
              _ <- Executor.execute(
                     ElasticRequest
                       .create[TestDocument](
                         firstSearchIndex,
                         secondDocumentUpdated
                       )
                       .refreshTrue
                   )
              r1 <- Executor
                      .execute(
                        ElasticRequest.search(
                          firstSearchIndex,
                          ElasticQuery
                            .functionScore(
                              FunctionScoreFunction.randomScoreFunction(),
                              FunctionScoreFunction.weightFunction(2)
                            )
                            .query(matches("stringField", firstDocument.stringField))
                            .boost(2.0)
                            .boostMode(FunctionScoreBoostMode.Max)
                        )
                      )
                      .documentAs[TestDocument]
            } yield assert(r1)(
              hasSameElements(Chunk(firstDocument, secondDocumentUpdated))
            )
          }
        } @@ around(
          Executor.execute(
            ElasticRequest.createIndex(
              firstSearchIndex,
              """{ "mappings": { "properties": { "subDocumentList": { "type": "nested" } } } }"""
            )
          ),
          Executor.execute(ElasticRequest.deleteIndex(firstSearchIndex)).orDie
        )
      )
    ) @@ nondeterministic @@ sequential @@ prepareElasticsearchIndexForTests @@ afterAll(
      Executor.execute(ElasticRequest.deleteIndex(index)).orDie
    )).provideShared(
      elasticsearchLayer
    )
  }
}
