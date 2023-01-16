package zio.elasticsearch

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, delete, get, head, post, put, urlEqualTo}
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.model.StatusCode
import zio.elasticsearch.ElasticQuery.matchAll
import zio.json.{DeriveJsonEncoder, JsonEncoder}
import zio.schema.{DeriveSchema, Schema}
import zio.{ZIO, ZLayer}
import zio.test.Assertion._
import zio.test.TestAspect.{afterAll, beforeAll, nondeterministic}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertZIO}

object WiremockSpec extends ZIOSpecDefault {

  private val index: IndexName = IndexName("organization")

  private val elasticsearchWireMockLayer: ZLayer[Any, Throwable, ElasticExecutor] =
    HttpClientZioBackend.layer() >>> (ZLayer.succeed(ElasticConfig.apply("localhost", 5700)) >>> ElasticExecutor.live)

  private val wireMockPort   = 5700
  private val wireMockServer = new WireMockServer(wireMockPort)

  override def spec: Spec[TestEnvironment, Any] =
    suite("Wiremock")(
      suite("creating document request") {
        test("return document ID") {
          wireMockServer.addStubMapping(
            post(urlEqualTo("/organization/_doc?refresh=true&routing=routing"))
              .willReturn(
                aResponse
                  .withBody("{\"_id\": \"V4x8q4UB3agN0z75fv5r\"}")
                  .withStatus(StatusCode.Created.code)
              )
              .build
          )

          assertZIO(
            ElasticRequest
              .create[GitHubRepo](
                index = index,
                doc = GitHubRepo(
                  id = Some("123"),
                  organization = "lambdaworks.io",
                  name = "zio-elasticsearch",
                  stars = 10,
                  forks = 10
                )
              )
              .routing(Routing("routing"))
              .refresh(value = true)
              .execute
          )(equalTo(DocumentId("V4x8q4UB3agN0z75fv5r")))
        }
      },
      suite("creating request with given ID") {
        test("return Created outcome") {
          wireMockServer.addStubMapping(
            post(urlEqualTo("/organization/_create/V4x8q4UB3agN0z75fv5r?refresh=true&routing=routing"))
              .willReturn(aResponse.withStatus(StatusCode.Created.code))
              .build
          )

          assertZIO(
            ElasticRequest
              .create[GitHubRepo](
                index = index,
                id = DocumentId("V4x8q4UB3agN0z75fv5r"),
                doc = GitHubRepo(
                  id = Some("123"),
                  organization = "lambdaworks.io",
                  name = "zio-elasticsearch",
                  stars = 10,
                  forks = 10
                )
              )
              .routing(Routing("routing"))
              .refresh(value = true)
              .execute
          )(equalTo(CreationOutcome.Created))
        }
      },
      suite("creating index request") {
        test("return Created outcome") {
          wireMockServer.addStubMapping(
            put(urlEqualTo("/organization"))
              .willReturn(aResponse.withStatus(StatusCode.Ok.code))
              .build
          )

          assertZIO(ElasticRequest.createIndex(index, None).execute)(equalTo(CreationOutcome.Created))
        }
      },
      suite("creating or updating request") {
        test("successfully create or update document") {
          wireMockServer.addStubMapping(
            put(urlEqualTo("/organization/_doc/V4x8q4UB3agN0z75fv5r?refresh=true&routing=routing"))
              .willReturn(aResponse.withStatus(StatusCode.Created.code))
              .build
          )

          assertZIO(
            ElasticRequest
              .upsert[GitHubRepo](
                index = index,
                id = DocumentId("V4x8q4UB3agN0z75fv5r"),
                doc = GitHubRepo(
                  id = Some("123"),
                  organization = "lambdaworks.io",
                  name = "zio-elasticsearch",
                  stars = 10,
                  forks = 10
                )
              )
              .routing(Routing("routing"))
              .refresh(value = true)
              .execute
          )(isUnit)
        }
      },
      suite("deleting by id request") {
        test("return Deleted outcome") {
          wireMockServer.addStubMapping(
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
          wireMockServer.addStubMapping(
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
          wireMockServer.addStubMapping(
            delete(urlEqualTo("/organization"))
              .willReturn(aResponse.withStatus(StatusCode.Ok.code))
              .build
          )

          assertZIO(ElasticRequest.deleteIndex(name = index).execute)(equalTo(DeletionOutcome.Deleted))
        }
      },
      suite("exists request") {
        test("Return true") {
          wireMockServer.addStubMapping(
            head(urlEqualTo("/organization/_doc/example-id?routing=routing"))
              .willReturn(aResponse.withStatus(StatusCode.Ok.code))
              .build
          )

          assertZIO(ElasticRequest.exists(index, DocumentId("example-id")).routing(Routing("routing")).execute)(isTrue)
        }
      },
      suite("getting by id request") {
        test("return Document") {
          wireMockServer.addStubMapping(
            get(urlEqualTo("/organization/_doc/V4x8q4UB3agN0z75fv5r?routing=routing"))
              .willReturn(
                aResponse
                  .withStatus(StatusCode.Ok.code)
                  .withBody(
                    "{\"_source\": {\"id\": \"111\", \"organization\": \"organization\", \"name\": \"lambdaworks.io\", \"stars\": 5, \"forks\": 5}}"
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
            isSome(
              equalTo(
                GitHubRepo(
                  id = Some("111"),
                  organization = "organization",
                  name = "lambdaworks.io",
                  stars = 5,
                  forks = 5
                )
              )
            )
          )
        }
      },
      suite("getting by query request") {
        test("return documents") {
          wireMockServer.addStubMapping(
            post(urlEqualTo("/organization/_search"))
              .willReturn(
                aResponse
                  .withStatus(StatusCode.Ok.code)
                  .withBody(
                    "{\"took\": 5, \"timed_out\": false, \"_shards\": {\"total\": 8, \"successful\": 5, \"skipped\": 3, \"failed\": 0}, " +
                      "\"hits\": {\"total\": {\"value\": 2, \"relation\": \"relation\"}, \"max_score\": 1, " +
                      "\"hits\": [{\"_index\": \"organization\", \"_type\": \"type\", \"_id\": \"111\", \"_score\": 1, \"_source\": " +
                      "{\"id\": \"111\", \"organization\": \"organization\", \"name\": \"lambdaworks.io\", \"stars\": 5, \"forks\": 5}}]}}}}"
                  )
              )
              .build
          )

          assertZIO(
            ElasticRequest.search[GitHubRepo](index = index, query = matchAll()).execute
          )(
            equalTo(
              List(
                GitHubRepo(
                  id = Some("111"),
                  organization = "organization",
                  name = "lambdaworks.io",
                  stars = 5,
                  forks = 5
                )
              )
            )
          )
        }
      }
    ).provideShared(elasticsearchWireMockLayer) @@
      beforeAll(ZIO.attempt(wireMockServer.start())) @@
      afterAll(ZIO.attempt(wireMockServer.stop()).orDie) @@
      nondeterministic
}

private final case class GitHubRepo(
  id: Option[String],
  organization: String,
  name: String,
  stars: Int,
  forks: Int
)

private object GitHubRepo {
  implicit val schema: Schema[GitHubRepo] = DeriveSchema.gen[GitHubRepo]

  implicit val encoder: JsonEncoder[GitHubRepo] = DeriveJsonEncoder.gen[GitHubRepo]
}
