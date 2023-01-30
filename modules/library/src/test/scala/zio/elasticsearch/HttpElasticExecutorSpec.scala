package zio.elasticsearch

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, delete, get, head, post, put, urlEqualTo}
import sttp.model.StatusCode
import zio.ZIO
import zio.elasticsearch.ElasticQuery.matchAll
import zio.test.Assertion._
import zio.test.{Spec, TestEnvironment, assertZIO}

object HttpElasticExecutorSpec extends WireMockSpec {

  override def spec: Spec[TestEnvironment, Any] =
    suite("HttpElasticExecutor")(
      suite("bulk request") {
        test("successfully execute bulk request") {
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

          assertZIO(addStubMapping *> ElasticRequest.bulk(ElasticRequest.create(index, repo)).refreshTrue.execute)(
            isUnit
          )
        }
      },
      suite("creating document request") {
        test("return document ID") {
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
            addStubMapping *> ElasticRequest
              .create[GitHubRepo](index = index, doc = repo)
              .routing(Routing("routing"))
              .refreshTrue
              .execute
          )(equalTo(DocumentId("V4x8q4UB3agN0z75fv5r")))
        }
      },
      suite("creating request with given ID") {
        test("return Created outcome") {
          val addStubMapping = ZIO.serviceWith[WireMockServer](
            _.addStubMapping(
              post(urlEqualTo("/repositories/_create/V4x8q4UB3agN0z75fv5r?refresh=true&routing=routing"))
                .willReturn(aResponse.withStatus(StatusCode.Created.code))
                .build
            )
          )

          assertZIO(
            addStubMapping *> ElasticRequest
              .create[GitHubRepo](index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"), doc = repo)
              .routing(Routing("routing"))
              .refreshTrue
              .execute
          )(equalTo(CreationOutcome.Created))
        }
      },
      suite("creating index request") {
        test("return Created outcome") {
          val addStubMapping = ZIO.serviceWith[WireMockServer](
            _.addStubMapping(
              put(urlEqualTo("/repositories")).willReturn(aResponse.withStatus(StatusCode.Ok.code)).build
            )
          )

          assertZIO(addStubMapping *> ElasticRequest.createIndex(name = index, definition = None).execute)(
            equalTo(CreationOutcome.Created)
          )
        }
      },
      suite("creating or updating request") {
        test("successfully create or update document") {
          val addStubMapping = ZIO.serviceWith[WireMockServer](
            _.addStubMapping(
              put(urlEqualTo("/repositories/_doc/V4x8q4UB3agN0z75fv5r?refresh=true&routing=routing"))
                .willReturn(aResponse.withStatus(StatusCode.Created.code))
                .build
            )
          )

          assertZIO(
            addStubMapping *> ElasticRequest
              .upsert[GitHubRepo](index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"), doc = repo)
              .routing(Routing("routing"))
              .refreshTrue
              .execute
          )(isUnit)
        }
      },
      suite("deleting by ID request") {
        test("return Deleted outcome") {
          val addStubMapping = ZIO.serviceWith[WireMockServer](
            _.addStubMapping(
              delete(urlEqualTo("/repositories/_doc/V4x8q4UB3agN0z75fv5r?refresh=true&routing=routing"))
                .willReturn(aResponse.withStatus(StatusCode.Ok.code))
                .build
            )
          )

          assertZIO(
            addStubMapping *> ElasticRequest
              .deleteById(index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"))
              .routing(Routing("routing"))
              .refreshTrue
              .execute
          )(equalTo(DeletionOutcome.Deleted))
        }
      },
      suite("deleting by query request") {
        test("return Deleted outcome") {
          val addStubMapping = ZIO.serviceWith[WireMockServer](
            _.addStubMapping(
              post(urlEqualTo("/repositories/_delete_by_query?refresh=true"))
                .willReturn(aResponse.withStatus(StatusCode.Ok.code))
                .build
            )
          )

          assertZIO(
            addStubMapping *> ElasticRequest.deleteByQuery(index = index, query = matchAll()).refreshTrue.execute
          )(
            equalTo(DeletionOutcome.Deleted)
          )
        }
      },
      suite("deleting index request") {
        test("return Deleted outcome") {
          val addStubMapping = ZIO.serviceWith[WireMockServer](
            _.addStubMapping(
              delete(urlEqualTo("/repositories"))
                .willReturn(aResponse.withStatus(StatusCode.Ok.code))
                .build
            )
          )

          assertZIO(addStubMapping *> ElasticRequest.deleteIndex(name = index).execute)(
            equalTo(DeletionOutcome.Deleted)
          )
        }
      },
      suite("exists request") {
        test("return true") {
          val addStubMapping = ZIO.serviceWith[WireMockServer](
            _.addStubMapping(
              head(urlEqualTo("/repositories/_doc/example-id?routing=routing"))
                .willReturn(aResponse.withStatus(StatusCode.Ok.code))
                .build
            )
          )

          assertZIO(
            addStubMapping *> ElasticRequest
              .exists(index = index, id = DocumentId("example-id"))
              .routing(Routing("routing"))
              .execute
          )(isTrue)
        }
      },
      suite("getting by ID request") {
        test("successfully return document") {
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
            addStubMapping *> ElasticRequest
              .getById[GitHubRepo](index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"))
              .routing(Routing("routing"))
              .execute
          )(isSome(equalTo(repo)))
        }
      },
      suite("getting by query request") {
        test("successfully return documents") {
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

          assertZIO(addStubMapping *> ElasticRequest.search[GitHubRepo](index = index, query = matchAll()).execute)(
            equalTo(List(repo))
          )
        }
      }
    ).provideShared(elasticsearchWireMockLayer, wireMockServerLayer)
}
