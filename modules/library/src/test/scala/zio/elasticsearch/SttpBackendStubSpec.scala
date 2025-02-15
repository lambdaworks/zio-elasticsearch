package zio.elasticsearch

import sttp.client4.httpclient.zio.{HttpClientZioBackend, SttpClient}
import sttp.client4.testing.{ResponseStub, StubBody, WebSocketStreamBackendStub}
import sttp.client4.{GenericRequest, Response, StringBody}
import sttp.model.{Method, StatusCode}
import zio.elasticsearch.data.GeoPoint
import zio.elasticsearch.domain._
import zio.elasticsearch.executor.Executor
import zio.test.ZIOSpecDefault
import zio.{Task, TaskLayer, ULayer, ZLayer}

import java.time.LocalDate

trait SttpBackendStubSpec extends ZIOSpecDefault {

  final case class StubMapping(request: GenericRequest[_, _] => Boolean, response: Response[StubBody])

  final implicit class SttpBackendStubOps[A](sttpBackendStub: WebSocketStreamBackendStub[Task, A]) {
    def addStubMapping(stubMapping: StubMapping): WebSocketStreamBackendStub[Task, A] =
      sttpBackendStub
        .whenRequestMatches(stubMapping.request)
        .thenRespond(stubMapping.response)
  }

  val index: IndexName = IndexName("repositories")

  val nestedField: TestNestedField = TestNestedField("StringField", 1)

  val subDoc: TestSubDocument =
    TestSubDocument(
      stringField = "StringField",
      nestedField = nestedField,
      intField = 132,
      intFieldList = Nil
    )

  val doc: TestDocument =
    TestDocument(
      stringField = "StringField",
      subDocumentList = List(subDoc),
      dateField = LocalDate.parse("2020-10-11"),
      intField = 10,
      doubleField = 10.0,
      booleanField = true,
      geoPointField = GeoPoint(1.0, 1.0),
      vectorField = List(1, 5, -20)
    )

  val secondDoc: TestDocument =
    TestDocument(
      stringField = "StringField2",
      subDocumentList = Nil,
      dateField = LocalDate.parse("2020-12-12"),
      intField = 12,
      doubleField = 12.0,
      booleanField = true,
      geoPointField = GeoPoint(1.0, 1.0),
      vectorField = List()
    )

  private val url = "http://localhost:9200"

  private val bulkRequestStub: StubMapping = StubMapping(
    request = r => r.method == Method.POST && r.uri.toString == s"$url/_bulk?refresh=true",
    response = ResponseStub.exact(
      """
        |{
        | "took" : 3,
        | "errors" : false,
        | "items" : [
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
  )

  private val countRequestStub: StubMapping = StubMapping(
    request = r => r.method == Method.GET && r.uri.toString == s"$url/repositories/_count?routing=routing",
    response = ResponseStub.exact(
      """
        |{
        |  "count": 2,
        |  "_shards": {
        |    "total": 2,
        |    "successful": 2,
        |    "failed": 0
        |  }
        |}""".stripMargin
    )
  )

  private val createDocumentRequestStub: StubMapping = StubMapping(
    request = r => r.method == Method.POST && r.uri.toString == s"$url/repositories/_doc?refresh=true&routing=routing",
    response = ResponseStub.exact(
      """
        |{
        |  "_id": "V4x8q4UB3agN0z75fv5r"
        |}""".stripMargin,
      StatusCode.Created
    )
  )

  private val createIndexRequestWithoutMappingStub: StubMapping = StubMapping(
    request = r => r.method == Method.PUT && r.uri.toString == s"$url/repositories",
    response = ResponseStub.exact("Ok")
  )

  private val createIndexRequestWithMappingStub: StubMapping = StubMapping(
    request = r =>
      r.method == Method.PUT && r.uri.toString == s"$url/repositories" && r.body == StringBody(
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
          |""".stripMargin,
        "utf-8"
      ),
    response = ResponseStub.exact("Ok")
  )

  private val createOrUpdateRequestStub: StubMapping = StubMapping(
    request = r =>
      r.method == Method.PUT && r.uri.toString == s"$url/repositories/_doc/V4x8q4UB3agN0z75fv5r?refresh=true&routing=routing",
    response = ResponseStub.exact("Created", StatusCode.Created)
  )

  private val createRequestWithGivenIdStub: StubMapping = StubMapping(
    request = r =>
      r.method == Method.POST && r.uri.toString == s"$url/repositories/_create/V4x8q4UB3agN0z75fv5r?refresh=true&routing=routing",
    response = ResponseStub.exact("Created", StatusCode.Created)
  )

  private val deleteByIdRequestStub: StubMapping = StubMapping(
    request = r =>
      r.method == Method.DELETE && r.uri.toString == s"$url/repositories/_doc/V4x8q4UB3agN0z75fv5r?refresh=true&routing=routing",
    response = ResponseStub.exact("Ok")
  )

  private val deleteByQueryRequestStub: StubMapping = StubMapping(
    request = r =>
      r.method == Method.POST && r.uri.toString == s"$url/repositories/_delete_by_query?refresh=true&routing=routing",
    response = ResponseStub.exact("Ok")
  )

  private val deleteIndexRequestStub: StubMapping = StubMapping(
    request = r => r.method == Method.DELETE && r.uri.toString == s"$url/repositories",
    response = ResponseStub.exact("Ok")
  )

  private val existsRequestStub: StubMapping = StubMapping(
    request = r => r.method == Method.HEAD && r.uri.toString == s"$url/repositories/_doc/example-id?routing=routing",
    response = ResponseStub.exact("Ok")
  )

  private val getByIdRequestStub: StubMapping = StubMapping(
    request =
      r => r.method == Method.GET && r.uri.toString == s"$url/repositories/_doc/V4x8q4UB3agN0z75fv5r?routing=routing",
    response = ResponseStub.exact(
      """
        |{
        |  "_source": {
        |    "stringField": "StringField",
        |    "subDocumentList": [
        |      {
        |        "stringField": "StringField",
        |        "nestedField": {
        |          "stringField": "StringField",
        |          "longField": 1
        |        },
        |        "intField": 132,
        |        "intFieldList": []
        |      }
        |    ],
        |    "dateField": "2020-10-11",
        |    "intField": 10,
        |    "doubleField": 10.0,
        |    "booleanField": true,
        |    "geoPointField": {
        |      "lat": 1.0,
        |      "lon": 1.0
        |    },
        |    "vectorField": [
        |      1,
        |      5,
        |      -20
        |    ]
        |  }
        |}""".stripMargin
    )
  )

  private val knnSearchStub: StubMapping = StubMapping(
    request = r => r.method == Method.POST && r.uri.toString == s"$url/repositories/_knn_search",
    response = ResponseStub.exact(
      """
        |{
        |  "took": 5,
        |  "timed_out": false,
        |  "_shards": {
        |    "total": 8,
        |    "successful": 8,
        |    "failed": 0
        |  },
        |  "hits": {
        |    "total": {
        |      "value": 2,
        |      "relation": "eq"
        |    },
        |    "max_score": 0.008547009,
        |    "hits": [
        |      {
        |        "_index": "repositories",
        |        "_type": "type",
        |        "_id": "111",
        |        "_score": 0.008547009,
        |        "_source": {
        |          "stringField": "StringField",
        |          "subDocumentList": [
        |            {
        |              "stringField": "StringField",
        |              "nestedField": {
        |                "stringField": "StringField",
        |                "longField": 1
        |              },
        |              "intField": 132,
        |              "intFieldList": []
        |            }
        |          ],
        |          "dateField": "2020-10-11",
        |          "intField": 10,
        |          "doubleField": 10.0,
        |          "booleanField": true,
        |          "geoPointField": {
        |            "lat": 1.0,
        |            "lon": 1.0
        |          },
        |          "vectorField": [
        |            1,
        |            5,
        |            -20
        |          ]
        |        }
        |      }
        |    ]
        |  }
        |}""".stripMargin
    )
  )

  private val refreshRequestStub: StubMapping = StubMapping(
    request = r => r.method == Method.GET && r.uri.toString == s"$url/repositories/_refresh",
    response = ResponseStub.exact("OK")
  )

  private val searchRequestStub: StubMapping = StubMapping(
    request = r => r.method == Method.POST && r.uri.toString == s"$url/repositories/_search",
    response = ResponseStub.exact(
      """
        |{
        |  "took": 5,
        |  "timed_out": false,
        |  "_shards": {
        |    "total": 8,
        |    "successful": 8,
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
        |          "stringField": "StringField",
        |          "subDocumentList": [
        |            {
        |              "stringField": "StringField",
        |              "nestedField": {
        |                "stringField": "StringField",
        |                "longField": 1
        |              },
        |              "intField": 132,
        |              "intFieldList": []
        |            }
        |          ],
        |          "dateField": "2020-10-11",
        |          "intField": 10,
        |          "doubleField": 10.0,
        |          "booleanField": true,
        |          "geoPointField": {
        |            "lat": 1.0,
        |            "lon": 1.0
        |          },
        |          "vectorField": [
        |            1,
        |            5,
        |            -20
        |          ]
        |        }
        |      }
        |    ]
        |  }
        |}""".stripMargin
    )
  )

  private val searchWithAggregationRequestStub: StubMapping = StubMapping(
    request = r => r.method == Method.POST && r.uri.toString == s"$url/repositories/_search?typed_keys",
    response = ResponseStub.exact(
      """
        |{
        |  "took": 5,
        |  "timed_out": false,
        |  "_shards": {
        |    "total": 8,
        |    "successful": 8,
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
        |          "stringField": "StringField",
        |          "subDocumentList": [
        |            {
        |              "stringField": "StringField",
        |              "nestedField": {
        |                "stringField": "StringField",
        |                "longField": 1
        |              },
        |              "intField": 132,
        |              "intFieldList": []
        |            }
        |          ],
        |          "dateField": "2020-10-11",
        |          "intField": 10,
        |          "doubleField": 10.0,
        |          "booleanField": true,
        |          "geoPointField": {
        |            "lat": 1.0,
        |            "lon": 1.0
        |          },
        |          "vectorField": [
        |            1,
        |            5,
        |            -20
        |          ]
        |        }
        |      }
        |    ]
        |  }, 
        |  "aggregations": {
        |    "terms#aggregation1": {
        |      "doc_count_error_upper_bound": 0,
        |      "sum_other_doc_count": 0,
        |      "buckets": [
        |        {
        |          "key": "name",
        |          "doc_count": 5
        |        }
        |      ]
        |    }
        |  }
        |}""".stripMargin
    )
  )

  private val updateRequestStub: StubMapping = StubMapping(
    request = r =>
      r.method == Method.POST && r.uri.toString == s"$url/repositories/_update/V4x8q4UB3agN0z75fv5r?refresh=true&routing=routing",
    response = ResponseStub.exact("Updated")
  )

  private val updateByQueryRequestStub: StubMapping = StubMapping(
    request = r =>
      r.method == Method.POST && r.uri.toString == s"$url/repositories/_update_by_query?conflicts=proceed&refresh=true&routing=routing",
    response = ResponseStub.exact(
      """
        |{
        |  "took" : 1,
        |  "total" : 10,
        |  "updated" : 8,
        |  "deleted" : 0,
        |  "version_conflicts" : 2
        |}
        |""".stripMargin
    )
  )

  private val stubs: List[StubMapping] = List(
    bulkRequestStub,
    countRequestStub,
    createDocumentRequestStub,
    createIndexRequestWithMappingStub,
    createIndexRequestWithoutMappingStub,
    createOrUpdateRequestStub,
    createRequestWithGivenIdStub,
    deleteByIdRequestStub,
    deleteByQueryRequestStub,
    deleteIndexRequestStub,
    existsRequestStub,
    getByIdRequestStub,
    knnSearchStub,
    refreshRequestStub,
    searchRequestStub,
    searchWithAggregationRequestStub,
    updateRequestStub,
    updateByQueryRequestStub
  )

  private val sttpBackendStubLayer: ULayer[SttpClient] =
    ZLayer.succeed(stubs.foldLeft(HttpClientZioBackend.stub)(_.addStubMapping(_)))

  val elasticsearchSttpLayer: TaskLayer[Executor] =
    (sttpBackendStubLayer ++ ZLayer.succeed(ElasticConfig.Default)) >>> ElasticExecutor.live

}
