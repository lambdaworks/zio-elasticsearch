package zio.elasticsearch

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, delete, get, head, post, put, urlEqualTo}
import sttp.model.StatusCode
import zio.ZIO
import zio.elasticsearch.ElasticQuery.matchAll
import zio.test.Assertion._
import zio.test.TestAspect.{afterAll, beforeAll}
import zio.test.{Spec, TestEnvironment, assertZIO}

object HttpElasticExecutorSpec extends WiremockSpec {

  override def spec: Spec[TestEnvironment, Any] =
    suite("HttpElasticExecutor")(
      suite("creating document request") {
        test("return document ID") {
          server.addStubMapping(
            post(urlEqualTo("/organization/_doc?refresh=true&routing=routing"))
              .willReturn(
                aResponse
                  .withBody("""
                              |{
                              |  "_id": "V4x8q4UB3agN0z75fv5r"
                              |}""".stripMargin)
                  .withStatus(StatusCode.Created.code)
              )
              .build
          )

          assertZIO(
            ElasticRequest
              .create[GitHubRepo](index = index, doc = repo)
              .routing(Routing("routing"))
              .refresh(value = true)
              .execute
          )(equalTo(DocumentId("V4x8q4UB3agN0z75fv5r")))
        }
      },
      suite("creating request with given ID") {
        test("return Created outcome") {
          server.addStubMapping(
            post(urlEqualTo("/organization/_create/V4x8q4UB3agN0z75fv5r?refresh=true&routing=routing"))
              .willReturn(aResponse.withStatus(StatusCode.Created.code))
              .build
          )

          assertZIO(
            ElasticRequest
              .create[GitHubRepo](index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"), doc = repo)
              .routing(Routing("routing"))
              .refresh(value = true)
              .execute
          )(equalTo(CreationOutcome.Created))
        }
      },
      suite("creating index request") {
        test("return Created outcome") {
          server.addStubMapping(
            put(urlEqualTo("/organization"))
              .willReturn(aResponse.withStatus(StatusCode.Ok.code))
              .build
          )

          assertZIO(ElasticRequest.createIndex(name = index, definition = None).execute)(
            equalTo(CreationOutcome.Created)
          )
        }
      },
      suite("creating or updating request") {
        test("successfully create or update document") {
          server.addStubMapping(
            put(urlEqualTo("/organization/_doc/V4x8q4UB3agN0z75fv5r?refresh=true&routing=routing"))
              .willReturn(aResponse.withStatus(StatusCode.Created.code))
              .build
          )

          assertZIO(
            ElasticRequest
              .upsert[GitHubRepo](index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"), doc = repo)
              .routing(Routing("routing"))
              .refresh(value = true)
              .execute
          )(isUnit)
        }
      },
      suite("deleting by ID request") {
        test("return Deleted outcome") {
          server.addStubMapping(
            delete(urlEqualTo("/organization/_doc/V4x8q4UB3agN0z75fv5r?refresh=true&routing=routing"))
              .willReturn(aResponse.withStatus(StatusCode.Ok.code))
              .build
          )

          assertZIO(
            ElasticRequest
              .deleteById(index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"))
              .routing(Routing("routing"))
              .refresh(value = true)
              .execute
          )(equalTo(DeletionOutcome.Deleted))
        }
      },
      suite("deleting by query request") {
        test("return Deleted outcome") {
          server.addStubMapping(
            post(urlEqualTo("/organization/_delete_by_query?refresh=true"))
              .willReturn(aResponse.withStatus(StatusCode.Ok.code))
              .build
          )

          assertZIO(
            ElasticRequest.deleteByQuery(index = index, query = matchAll()).refresh(value = true).execute
          )(equalTo(DeletionOutcome.Deleted))
        }
      },
      suite("deleting index request") {
        test("return Deleted outcome") {
          server.addStubMapping(
            delete(urlEqualTo("/organization"))
              .willReturn(aResponse.withStatus(StatusCode.Ok.code))
              .build
          )

          assertZIO(ElasticRequest.deleteIndex(name = index).execute)(equalTo(DeletionOutcome.Deleted))
        }
      },
      suite("exists request") {
        test("return true") {
          server.addStubMapping(
            head(urlEqualTo("/organization/_doc/example-id?routing=routing"))
              .willReturn(aResponse.withStatus(StatusCode.Ok.code))
              .build
          )

          assertZIO(
            ElasticRequest.exists(index = index, id = DocumentId("example-id")).routing(Routing("routing")).execute
          )(isTrue)
        }
      },
      suite("getting by ID request") {
        test("successfully return Document") {
          server.addStubMapping(
            get(urlEqualTo("/organization/_doc/V4x8q4UB3agN0z75fv5r?routing=routing"))
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

          assertZIO(
            ElasticRequest
              .getById[GitHubRepo](index = index, id = DocumentId("V4x8q4UB3agN0z75fv5r"))
              .routing(Routing("routing"))
              .execute
          )(
            isSome(equalTo(repo))
          )
        }
      },
      suite("getting by query request") {
        test("successfully return documents") {
          server.addStubMapping(
            post(urlEqualTo("/organization/_search"))
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
                      |        "_index": "organization",
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

          assertZIO(ElasticRequest.search[GitHubRepo](index = index, query = matchAll()).execute)(
            equalTo(List(repo))
          )
        }
      }
    ).provideShared(elasticsearchWireMockLayer) @@
      beforeAll(ZIO.attempt(server.start())) @@
      afterAll(ZIO.attempt(server.stop()).orDie)
}
