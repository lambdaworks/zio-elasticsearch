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
      test("aggregation") {
        val executorAgregate =
          Executor
            .execute(
              ElasticRequest
                .aggregate(index, termsAggregation(name = "aggregation1", field = "name"))
            )
            .aggregations

        val expectedTermsAggregationResult =
          Map(
            "aggregation1" -> TermsAggregationResult(
              docErrorCount = 0,
              sumOtherDocCount = 0,
              buckets = Chunk(TermsAggregationBucketResult(docCount = 5, key = "name", subAggregations = Map.empty))
            )
          )

        assertZIO(executorAgregate)(equalTo(expectedTermsAggregationResult))
      },
      test("bulk") {
        val executorBulk =
          Executor
            .execute(
              ElasticRequest
                .bulk(ElasticRequest.create(index, doc))
                .refreshTrue
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
        assertZIO(executorBulk)(
          equalTo(expectedBulkResponse)
        )
      },
      test("count") {
        val executorCount =
          Executor
            .execute(ElasticRequest.count(index, matchAll).routing(Routing("routing")))
        assertZIO(executorCount)(equalTo(2))
      },
      test("create") {
        val executorCreate =
          Executor
            .execute(
              ElasticRequest
                .create[TestDocument](index = index, doc = doc)
                .routing(Routing("routing"))
                .refreshTrue
            )
        assertZIO(executorCreate)(equalTo(DocumentId("V4x8q4UB3agN0z75fv5r")))
      },
      test("create with ID") {
        val executorCreateDocumentId =
          Executor.execute(
            ElasticRequest
              .create[TestDocument](index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"), doc = doc)
              .routing(Routing("routing"))
              .refreshTrue
          )
        assertZIO(executorCreateDocumentId)(equalTo(Created))
      },
      test("createIndex") {
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
        assertZIO(executorCreateIndex)(equalTo(Created)) &&
        assertZIO(executorCreateIndexMapping)(equalTo(Created))
      },
      test("deleteById") {
        val executorDeleteById =
          Executor.execute(
            ElasticRequest
              .deleteById(index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"))
              .routing(Routing("routing"))
              .refreshTrue
          )
        assertZIO(executorDeleteById)(equalTo(Deleted))
      },
      test("deleteByQuery") {
        val executorDeleteByQuery =
          Executor.execute(
            ElasticRequest.deleteByQuery(index = index, query = matchAll).refreshTrue.routing(Routing("routing"))
          )
        assertZIO(executorDeleteByQuery)(equalTo(Deleted))
      },
      test("deleteIndex") {
        val executorDeleteIndex =
          Executor.execute(ElasticRequest.deleteIndex(name = index))
        assertZIO(executorDeleteIndex)(equalTo(Deleted))
      },
      test("exists") {
        val executorExists =
          Executor.execute(
            ElasticRequest
              .exists(index = index, id = DocumentId("example-id"))
              .routing(Routing("routing"))
          )
        assertZIO(executorExists)(isTrue)
      },
      test("getById") {
        val executorGetById =
          Executor
            .execute(
              ElasticRequest
                .getById(index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"))
                .routing(Routing("routing"))
            )
            .documentAs[TestDocument]
        assertZIO(executorGetById)(isSome(equalTo(doc)))
      },
      test("search") {
        val executorSearch =
          Executor
            .execute(ElasticRequest.search(index = index, query = matchAll))
            .documentAs[TestDocument]
        val terms = termsAggregation(name = "aggregation1", field = "name")
        val executorSearchWithTerms = Executor
          .execute(ElasticRequest.search(index = index, query = matchAll, terms))
          .documentAs[TestDocument]
        assertZIO(executorSearch)(equalTo(Chunk(doc))) && assertZIO(executorSearchWithTerms)(equalTo(Chunk(doc)))
      },
      test("search and aggergate") {
        val terms = termsAggregation(name = "aggregation1", field = "name")
        val executorSearchAggregations = Executor
          .execute(ElasticRequest.search(index = index, query = matchAll, terms))
          .aggregations
        val expectedTermsAggregationResult =
          Map(
            "aggregation1" -> TermsAggregationResult(
              docErrorCount = 0,
              sumOtherDocCount = 0,
              buckets = Chunk(TermsAggregationBucketResult(docCount = 5, key = "name", subAggregations = Map.empty))
            )
          )
        assertZIO(executorSearchAggregations)(equalTo(expectedTermsAggregationResult))
      },
      test("update") {
        val executorUpdate =
          Executor.execute(
            ElasticRequest
              .update[TestDocument](index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"), doc = doc)
              .orCreate(doc = secondDoc)
              .routing(Routing("routing"))
              .refreshTrue
          )
        assertZIO(executorUpdate)(equalTo(UpdateOutcome.Updated))
      },
      test("updateAllByQuery") {
        val executorUpdateAllByQuery =
          Executor.execute(
            ElasticRequest
              .updateAllByQuery(index = index, script = Script("ctx._source['intField']++"))
              .conflicts(Proceed)
              .routing(Routing("routing"))
              .refreshTrue
          )
        val expectedUpdateByQueryResult =
          UpdateByQueryResult(took = 1, total = 10, updated = 8, deleted = 0, versionConflicts = 2)
        assertZIO(executorUpdateAllByQuery)(equalTo(expectedUpdateByQueryResult))
      },
      test("updateByQuery") {
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
        val expectedUpdateByQueryResult =
          UpdateByQueryResult(took = 1, total = 10, updated = 8, deleted = 0, versionConflicts = 2)
        assertZIO(executorUpdateByQuery)(equalTo(expectedUpdateByQueryResult))
      },
      test("updateByScript") {
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
        assertZIO(executorUpdateByScript)(equalTo(UpdateOutcome.Updated))
      },
      test("upsert") {
        val executorUpsert =
          Executor.execute(
            ElasticRequest
              .upsert[TestDocument](index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"), doc = doc)
              .routing(Routing("routing"))
              .refreshTrue
          )
        assertZIO(executorUpsert)(isUnit)
      }
    ).provideShared(elasticsearchSttpLayer)
}
