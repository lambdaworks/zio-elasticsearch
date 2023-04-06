package zio.elasticsearch

import zio.elasticsearch.ElasticQuery.range
import zio.elasticsearch.ElasticRequest.{search, searchAfter}
import zio.elasticsearch.domain.TestDocument
import zio.elasticsearch.utils.RichString
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Str}
import zio.test.Assertion.equalTo
import zio.test._

object ElasticRequestsDSLSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment, Any] =
    suite("Elastic Requests JSON encoding")(
      test("successfully encode JSON for search request") {
        val jsonRequest: Json = search(IndexName("indexName"), Query) match {
          case r: ElasticRequest.Search => r.toJson
        }
        val expected =
          """
            |{
            |  "query" : {
            |    "range" : {
            |      "intField" : {
            |       "gte" : 10
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        assert(jsonRequest)(equalTo(expected.toJson))
      },
      test("successfully encode JSON for search after request") {
        val searchAfterJson = Arr(Str("12345"))
        val jsonRequest: Json = searchAfter(IndexName("indexName"), Query, searchAfterJson) match {
          case r: ElasticRequest.Search => r.toJson
        }
        val expected =
          """
            |{
            |  "query" : {
            |    "range" : {
            |      "intField" : {
            |       "gte" : 10
            |      }
            |    }
            |  },
            |  "search_after" : [
            |   "12345"
            |   ]
            |}
            |""".stripMargin

        assert(jsonRequest)(equalTo(expected.toJson))
      }
    )

  private val Query = range(TestDocument.intField).gte(10)
}
