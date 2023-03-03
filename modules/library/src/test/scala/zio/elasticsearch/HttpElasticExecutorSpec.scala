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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{
  aResponse,
  delete,
  equalToJson,
  get,
  head,
  post,
  put,
  urlEqualTo
}
import sttp.model.StatusCode
import zio.ZIO
import zio.elasticsearch.ElasticQuery.matchAll
import zio.test.Assertion._
import zio.test.{Spec, TestEnvironment, assertZIO}

object HttpElasticExecutorSpec extends WireMockSpec {

  def spec: Spec[TestEnvironment, Any] =
    suite("HttpElasticExecutor")(
      test("bulk request") {
        val addStubMapping = ZIO.serviceWith[WireMockServer](
          _.addStubMapping(
            post(urlEqualTo("/_bulk?refresh=true"))
              .willReturn(
                aResponse
                  .withBody(
                    """
                      |{
                      | "took" = 3,
                      | "errors" = false,
                      | "items" = [
                      |   {
                      |     "create": {
                      |       "_index": "repositories",
                      |       "_type": "_doc",
                      |       "_id": "123",
                      |       "_version": 1,
                      |       "result": "created",
                      |       "_shards": {
                      |         "total": 1,
                      |         "successful": 1,
                      |         "failed": 0
                      |       },
                      |       "_seq_no": 0,
                      |       "_primary_term": 1,
                      |       "status": 201
                      |     }
                      |   }
                      | ]
                      |}""".stripMargin
                  )
                  .withStatus(StatusCode.Ok.code)
              )
              .build
          )
        )

        assertZIO(
          addStubMapping *> ElasticExecutor.execute(ElasticRequest.bulk(ElasticRequest.create(index, repo)).refreshTrue)
        )(
          isUnit
        )
      },
      test("creating document request") {
        val addStubMapping = ZIO.serviceWith[WireMockServer](
          _.addStubMapping(
            post(urlEqualTo("/repositories/_doc?refresh=true&routing=routing"))
              .willReturn(
                aResponse
                  .withBody(
                    """
                      |{
                      |  "_id": "V4x8q4UB3agN0z75fv5r"
                      |}""".stripMargin
                  )
                  .withStatus(StatusCode.Created.code)
              )
              .build
          )
        )

        assertZIO(
          addStubMapping *> ElasticExecutor.execute(
            ElasticRequest
              .create[GitHubRepo](index = index, doc = repo)
              .routing(Routing("routing"))
              .refreshTrue
          )
        )(equalTo(DocumentId("V4x8q4UB3agN0z75fv5r")))
      },
      test("creating request with given ID") {
        val addStubMapping = ZIO.serviceWith[WireMockServer](
          _.addStubMapping(
            post(urlEqualTo("/repositories/_create/V4x8q4UB3agN0z75fv5r?refresh=true&routing=routing"))
              .willReturn(aResponse.withStatus(StatusCode.Created.code))
              .build
          )
        )

        assertZIO(
          addStubMapping *> ElasticExecutor.execute(
            ElasticRequest
              .create[GitHubRepo](index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"), doc = repo)
              .routing(Routing("routing"))
              .refreshTrue
          )
        )(equalTo(Created))
      },
      test("creating index request without mapping") {
        val addStubMapping = ZIO.serviceWith[WireMockServer](
          _.addStubMapping(
            put(urlEqualTo("/repositories")).willReturn(aResponse.withStatus(StatusCode.Ok.code)).build
          )
        )

        assertZIO(
          addStubMapping *> ElasticExecutor.execute(ElasticRequest.createIndex(name = index))
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

        val addStubMapping = ZIO.serviceWith[WireMockServer](
          _.addStubMapping(
            put(urlEqualTo("/repositories"))
              .withRequestBody(
                equalToJson(
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
                )
              )
              .willReturn(aResponse.withStatus(StatusCode.Ok.code))
              .build
          )
        )

        assertZIO(
          addStubMapping *> ElasticExecutor.execute(ElasticRequest.createIndex(name = index, definition = mapping))
        )(
          equalTo(Created)
        )
      },
      test("creating or updating request") {
        val addStubMapping = ZIO.serviceWith[WireMockServer](
          _.addStubMapping(
            put(urlEqualTo("/repositories/_doc/V4x8q4UB3agN0z75fv5r?refresh=true&routing=routing"))
              .willReturn(aResponse.withStatus(StatusCode.Created.code))
              .build
          )
        )

        assertZIO(
          addStubMapping *> ElasticExecutor.execute(
            ElasticRequest
              .upsert[GitHubRepo](index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"), doc = repo)
              .routing(Routing("routing"))
              .refreshTrue
          )
        )(isUnit)
      },
      test("deleting by ID request") {
        val addStubMapping = ZIO.serviceWith[WireMockServer](
          _.addStubMapping(
            delete(urlEqualTo("/repositories/_doc/V4x8q4UB3agN0z75fv5r?refresh=true&routing=routing"))
              .willReturn(aResponse.withStatus(StatusCode.Ok.code))
              .build
          )
        )

        assertZIO(
          addStubMapping *> ElasticExecutor.execute(
            ElasticRequest
              .deleteById(index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"))
              .routing(Routing("routing"))
              .refreshTrue
          )
        )(equalTo(Deleted))
      },
      test("deleting by query request") {
        val addStubMapping = ZIO.serviceWith[WireMockServer](
          _.addStubMapping(
            post(urlEqualTo("/repositories/_delete_by_query?refresh=true&routing=routing"))
              .willReturn(aResponse.withStatus(StatusCode.Ok.code))
              .build
          )
        )

        assertZIO(
          addStubMapping *> ElasticExecutor.execute(
            ElasticRequest.deleteByQuery(index = index, query = matchAll).refreshTrue.routing(Routing("routing"))
          )
        )(
          equalTo(Deleted)
        )
      },
      test("deleting index request") {
        val addStubMapping = ZIO.serviceWith[WireMockServer](
          _.addStubMapping(
            delete(urlEqualTo("/repositories"))
              .willReturn(aResponse.withStatus(StatusCode.Ok.code))
              .build
          )
        )

        assertZIO(addStubMapping *> ElasticExecutor.execute(ElasticRequest.deleteIndex(name = index)))(
          equalTo(Deleted)
        )
      },
      test("exists request") {
        val addStubMapping = ZIO.serviceWith[WireMockServer](
          _.addStubMapping(
            head(urlEqualTo("/repositories/_doc/example-id?routing=routing"))
              .willReturn(aResponse.withStatus(StatusCode.Ok.code))
              .build
          )
        )

        assertZIO(
          addStubMapping *> ElasticExecutor.execute(
            ElasticRequest
              .exists(index = index, id = DocumentId("example-id"))
              .routing(Routing("routing"))
          )
        )(isTrue)
      },
      test("getting by ID request") {
        val addStubMapping = ZIO.serviceWith[WireMockServer](
          _.addStubMapping(
            get(urlEqualTo("/repositories/_doc/V4x8q4UB3agN0z75fv5r?routing=routing"))
              .willReturn(
                aResponse
                  .withStatus(StatusCode.Ok.code)
                  .withBody(
                    """
                      |{
                      |  "_source": {
                      |    "id": "123",
                      |    "organization": "lambdaworks.io",
                      |    "name": "LambdaWorks",
                      |    "stars": 10,
                      |    "forks": 10
                      |  }
                      |}""".stripMargin
                  )
              )
              .build
          )
        )

        assertZIO(
          addStubMapping *> ElasticExecutor
            .execute(
              ElasticRequest
                .getById(index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"))
                .routing(Routing("routing"))
            )
            .documentAs[GitHubRepo]
        )(isSome(equalTo(repo)))
      },
      test("getting by query request") {
        val addStubMapping = ZIO.serviceWith[WireMockServer](
          _.addStubMapping(
            post(urlEqualTo("/repositories/_search"))
              .willReturn(
                aResponse
                  .withStatus(StatusCode.Ok.code)
                  .withBody(
                    """
                      |{
                      |  "took": 5, 
                      |  "timed_out": false,
                      |  "_shards": {
                      |    "total": 8,
                      |    "successful": 5,
                      |    "skipped": 3,
                      |    "failed": 0
                      |  },
                      |  "hits": {
                      |    "total": {
                      |      "value": 2,
                      |      "relation": "relation"
                      |    }, 
                      |    "max_score": 1,
                      |    "hits": [
                      |      {
                      |        "_index": "repositories",
                      |        "_type": "type",
                      |        "_id": "111",
                      |        "_score": 1,
                      |        "_source": {
                      |          "id": "123",
                      |          "organization": "lambdaworks.io",
                      |          "name": "LambdaWorks",
                      |          "stars": 10,
                      |          "forks": 10
                      |        }
                      |      }
                      |    ]
                      |  }
                      |}""".stripMargin
                  )
              )
              .build
          )
        )

        assertZIO(
          addStubMapping *> ElasticExecutor
            .execute(ElasticRequest.search(index = index, query = matchAll))
            .documentAs[GitHubRepo]
        )(
          equalTo(List(repo))
        )
      }
    ).provideShared(elasticsearchWireMockLayer, wireMockServerLayer)
}
