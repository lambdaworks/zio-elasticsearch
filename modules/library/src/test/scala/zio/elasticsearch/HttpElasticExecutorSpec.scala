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
import zio.elasticsearch.ElasticAggregation.termsAggregation
import zio.elasticsearch.ElasticQuery.{matchAll, term}
import zio.elasticsearch.domain.TestDocument
import zio.elasticsearch.executor.Executor
import zio.elasticsearch.executor.response.{BulkResponse, CreateBulkResponse, Shards}
import zio.elasticsearch.request.CreationOutcome.Created
import zio.elasticsearch.request.DeletionOutcome.Deleted
import zio.elasticsearch.request.UpdateConflicts.Proceed
import zio.elasticsearch.request.UpdateOutcome
import zio.elasticsearch.result.{TermsAggregationBucketResult, TermsAggregationResult, UpdateByQueryResult}
import zio.elasticsearch.script.Script
import zio.test.Assertion._
import zio.test.{Spec, TestEnvironment, TestResultZIOOps, assertZIO}

object HttpElasticExecutorSpec extends SttpBackendStubSpec {
  def spec: Spec[TestEnvironment, Any] =
    suite("HttpElasticExecutor")(
      test("aggregation request") {

        val executorAgregate =
          Executor
            .execute(
              ElasticRequest
                .aggregate(index, termsAggregation(name = "aggregation1", field = "name"))
            )
            .aggregations
        val executorBulk =
          Executor
            .execute(
              ElasticRequest
                .bulk(ElasticRequest.create(index, doc))
                .refreshTrue
            )
        val executorCount =
          Executor
            .execute(ElasticRequest.count(index, matchAll).routing(Routing("routing")))
        val executorCreate =
          Executor
            .execute(
              ElasticRequest
                .create[TestDocument](index = index, doc = doc)
                .routing(Routing("routing"))
                .refreshTrue
            )
        val executorCreateDocumentId =
          Executor.execute(
            ElasticRequest
              .create[TestDocument](index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"), doc = doc)
              .routing(Routing("routing"))
              .refreshTrue
          )
        val executorCreateIndex =
          Executor.execute(ElasticRequest.createIndex(name = index))
        val mapping =
          """
            |{
            |  "settings": {
            |    "index": {
            |      "number_of_shards": 1
            |    }
            |  },
            |  "mappings": {
            |    "_routing": {
            |      "required": true
            |    },
            |    "properties": {
            |      "id": {
            |        "type": "keyword"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin
        val executorCreateIndexMapping =
          Executor.execute(ElasticRequest.createIndex(name = index, definition = mapping))
        val executorUpsert =
          Executor.execute(
            ElasticRequest
              .upsert[TestDocument](index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"), doc = doc)
              .routing(Routing("routing"))
              .refreshTrue
          )
        val executorDeleteById =
          Executor.execute(
            ElasticRequest
              .deleteById(index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"))
              .routing(Routing("routing"))
              .refreshTrue
          )
        val executorDeleteByQuery =
          Executor.execute(
            ElasticRequest.deleteByQuery(index = index, query = matchAll).refreshTrue.routing(Routing("routing"))
          )
        val executorDeleteIndex =
          Executor.execute(ElasticRequest.deleteIndex(name = index))
        val executorExists =
          Executor.execute(
            ElasticRequest
              .exists(index = index, id = DocumentId("example-id"))
              .routing(Routing("routing"))
          )
        val executorGetById =
          Executor
            .execute(
              ElasticRequest
                .getById(index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"))
                .routing(Routing("routing"))
            )
            .documentAs[TestDocument]
        val executorSearch =
          Executor
            .execute(ElasticRequest.search(index = index, query = matchAll))
            .documentAs[TestDocument]
        val terms = termsAggregation(name = "aggregation1", field = "name")
        val executorSearchWithTerms = Executor
          .execute(ElasticRequest.search(index = index, query = matchAll, terms))
          .documentAs[TestDocument]
        val executorSearchAggregations = Executor
          .execute(ElasticRequest.search(index = index, query = matchAll, terms))
          .aggregations
        val executorUpdateByScript =
          Executor.execute(
            ElasticRequest
              .updateByScript(
                index = index,
                id = DocumentId("V4x8q4UB3agN0z75fv5r"),
                script = Script("ctx._source.intField += params['factor']").params("factor" -> 2)
              )
              .orCreate(doc = secondDoc)
              .routing(Routing("routing"))
              .refreshTrue
          )
        val executorUpdate =
          Executor.execute(
            ElasticRequest
              .update[TestDocument](index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"), doc = doc)
              .orCreate(doc = secondDoc)
              .routing(Routing("routing"))
              .refreshTrue
          )
        val executorUpdateAllByQuery =
          Executor.execute(
            ElasticRequest
              .updateAllByQuery(index = index, script = Script("ctx._source['intField']++"))
              .conflicts(Proceed)
              .routing(Routing("routing"))
              .refreshTrue
          )
        val executorUpdateByQuery =
          Executor.execute(
            ElasticRequest
              .updateByQuery(
                index = index,
                query = term(field = TestDocument.stringField.keyword, value = "StringField"),
                script = Script("ctx._source['intField']++")
              )
              .conflicts(Proceed)
              .routing(Routing("routing"))
              .refreshTrue
          )

        val expectedTermsAggregationResult =
          Map(
            "aggregation1" -> TermsAggregationResult(
              docErrorCount = 0,
              sumOtherDocCount = 0,
              buckets = Chunk(TermsAggregationBucketResult(docCount = 5, key = "name", subAggregations = Map.empty))
            )
          )
        val expectedBulkResponse =
          BulkResponse(
            took = 3,
            errors = false,
            items = Chunk(
              CreateBulkResponse(
                index = "repositories",
                id = "123",
                version = Some(1),
                result = Some("created"),
                shards = Some(Shards(total = 1, successful = 1, failed = 0)),
                status = Some(201),
                error = None
              )
            )
          )
        val expectedUpdateByQueryResult =
          UpdateByQueryResult(took = 1, total = 10, updated = 8, deleted = 0, versionConflicts = 2)

        assertZIO(
          executorAgregate
        )(
          equalTo(
            expectedTermsAggregationResult
          )
        ) && assertZIO(
          executorBulk
        )(
          equalTo(
            expectedBulkResponse
          )
        ) && assertZIO(
          executorCount
        )(
          equalTo(
            2
          )
        ) && assertZIO(
          executorCreate
        )(
          equalTo(
            DocumentId("V4x8q4UB3agN0z75fv5r")
          )
        ) && assertZIO(
          executorCreateDocumentId
        )(
          equalTo(
            Created
          )
        ) && assertZIO(
          executorCreateIndex
        )(
          equalTo(
            Created
          )
        ) && assertZIO(
          executorCreateIndexMapping
        )(
          equalTo(
            Created
          )
        ) && assertZIO(
          executorUpsert
        )(
          isUnit
        ) && assertZIO(
          executorDeleteById
        )(
          equalTo(
            Deleted
          )
        ) && assertZIO(
          executorDeleteByQuery
        )(
          equalTo(
            Deleted
          )
        ) && assertZIO(
          executorDeleteIndex
        )(
          equalTo(
            Deleted
          )
        ) && assertZIO(
          executorExists
        )(isTrue) && assertZIO(
          executorGetById
        )(
          isSome(equalTo(doc))
        ) && assertZIO(
          executorSearch
        )(
          equalTo(
            Chunk(doc)
          )
        ) && assertZIO(
          executorSearchWithTerms
        )(
          equalTo(Chunk(doc))
        ) && assertZIO(
          executorSearchAggregations
        )(
          equalTo(
            expectedTermsAggregationResult
          )
        ) && assertZIO(
          executorUpdateByScript
        )(
          equalTo(
            UpdateOutcome.Updated
          )
        ) && assertZIO(
          executorUpdate
        )(
          equalTo(
            UpdateOutcome.Updated
          )
        ) && assertZIO(
          executorUpdateAllByQuery
        )(
          equalTo(
            expectedUpdateByQueryResult
          )
        ) && assertZIO(
          executorUpdateByQuery
        )(
          equalTo(
            expectedUpdateByQueryResult
          )
        )
      }
    ).provideShared(elasticsearchSttpLayer)
}
