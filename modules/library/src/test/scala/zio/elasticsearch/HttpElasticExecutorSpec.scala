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

import zio.elasticsearch.ElasticAggregation.termsAggregation
import zio.elasticsearch.ElasticQuery.matchAll
import zio.elasticsearch.executor.Executor
import zio.elasticsearch.executor.response.{TermsAggregationBucket, TermsAggregationResponse}
import zio.test.Assertion._
import zio.test.{Spec, TestEnvironment, assertZIO}
import zio.test.TestResultZIOOps

object HttpElasticExecutorSpec extends SttpBackendStubSpec {

  def spec: Spec[TestEnvironment, Any] =
    suite("HttpExecutor")(
      test("aggregation request") {
        assertZIO(
          Executor
            .execute(
              ElasticRequest.aggregate(index, termsAggregation(name = "aggregation1", field = "name"))
            )
            .aggregations
        )(
          equalTo(Map("aggregation1" -> TermsAggregationResponse(0, 0, List(TermsAggregationBucket("name", 5, None)))))
        )
      },
      test("bulk request") {
        assertZIO(
          Executor.execute(ElasticRequest.bulk(ElasticRequest.create(index, repo)).refreshTrue)
        )(
          isUnit
        )
      },
      test("count request") {
        assertZIO(Executor.execute(ElasticRequest.count(index, matchAll).routing(Routing("routing"))))(
          equalTo(2)
        )
      },
      test("creating document request") {
        assertZIO(
          Executor.execute(
            ElasticRequest
              .create[GitHubRepo](index = index, doc = repo)
              .routing(Routing("routing"))
              .refreshTrue
          )
        )(equalTo(DocumentId("V4x8q4UB3agN0z75fv5r")))
      },
      test("creating request with given ID") {
        assertZIO(
          Executor.execute(
            ElasticRequest
              .create[GitHubRepo](index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"), doc = repo)
              .routing(Routing("routing"))
              .refreshTrue
          )
        )(equalTo(Created))
      },
      test("creating index request without mapping") {
        assertZIO(
          Executor.execute(ElasticRequest.createIndex(name = index))
        )(
          equalTo(Created)
        )
      },
      test("creating index request with mapping") {
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

        assertZIO(
          Executor.execute(ElasticRequest.createIndex(name = index, definition = mapping))
        )(
          equalTo(Created)
        )
      },
      test("creating or updating request") {
        assertZIO(
          Executor.execute(
            ElasticRequest
              .upsert[GitHubRepo](index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"), doc = repo)
              .routing(Routing("routing"))
              .refreshTrue
          )
        )(isUnit)
      },
      test("deleting by ID request") {
        assertZIO(
          Executor.execute(
            ElasticRequest
              .deleteById(index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"))
              .routing(Routing("routing"))
              .refreshTrue
          )
        )(equalTo(Deleted))
      },
      test("deleting by query request") {
        assertZIO(
          Executor.execute(
            ElasticRequest.deleteByQuery(index = index, query = matchAll).refreshTrue.routing(Routing("routing"))
          )
        )(
          equalTo(Deleted)
        )
      },
      test("deleting index request") {
        assertZIO(Executor.execute(ElasticRequest.deleteIndex(name = index)))(
          equalTo(Deleted)
        )
      },
      test("exists request") {
        assertZIO(
          Executor.execute(
            ElasticRequest
              .exists(index = index, id = DocumentId("example-id"))
              .routing(Routing("routing"))
          )
        )(isTrue)
      },
      test("getting by ID request") {
        assertZIO(
          Executor
            .execute(
              ElasticRequest
                .getById(index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"))
                .routing(Routing("routing"))
            )
            .documentAs[GitHubRepo]
        )(isSome(equalTo(repo)))
      },
      test("search request") {
        assertZIO(
          Executor
            .execute(ElasticRequest.search(index = index, query = matchAll))
            .documentAs[GitHubRepo]
        )(equalTo(List(repo)))
      },
      test("search with aggregation request") {
        val terms = termsAggregation(name = "aggregation1", field = "name")
        val req = Executor
          .execute(ElasticRequest.search(index = index, query = matchAll, terms))
        assertZIO(req.documentAs[GitHubRepo])(equalTo(List(repo))) &&
        assertZIO(req.aggregations)(
          equalTo(Map("aggregation1" -> TermsAggregationResponse(0, 0, List(TermsAggregationBucket("name", 5, None)))))
        )

      }
    ).provideShared(elasticsearchSttpLayer)
}
