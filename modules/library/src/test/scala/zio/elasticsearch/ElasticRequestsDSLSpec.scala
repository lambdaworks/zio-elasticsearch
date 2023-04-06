package zio.elasticsearch

import zio.elasticsearch.ElasticAggregation.termsAggregation
import zio.elasticsearch.ElasticHighlight.highlight
import zio.elasticsearch.ElasticQuery.range
import zio.elasticsearch.ElasticRequest.search
import zio.elasticsearch.ElasticSort.sortBy
import zio.elasticsearch.domain.TestDocument
import zio.elasticsearch.query.sort.Missing.First
import zio.elasticsearch.utils.RichString
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Str}
import zio.test.Assertion.equalTo
import zio.test._

object ElasticRequestsDSLSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment, Any] =
    suite("Elastic Requests JSON encoding")(
      test("successfully encode search request to JSON") {
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
      test("successfully encode search request to JSON with search after parameter") {
        val jsonRequest: Json = search(IndexName("indexName"), Query).searchAfter(Arr(Str("12345"))) match {
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
      },
      test("successfully encode search request to JSON with size parameter") {
        val jsonRequest: Json = search(IndexName("indexName"), Query).size(20) match {
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
            |  "size" : 20
            |}
            |""".stripMargin

        assert(jsonRequest)(equalTo(expected.toJson))
      },
      test("successfully encode search request to JSON with multiple parameters") {
        val jsonRequest = search(IndexName("indexName"), Query)
          .size(20)
          .sortBy(sortBy(TestDocument.intField).missing(First))
          .from(10) match {
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
            |  "size" : 20,
            |  "from" : 10,
            |  "sort": [
            |    {
            |      "intField": {
            |        "missing": "_first"
            |      }
            |    }
            |  ]
            |}
            |""".stripMargin

        assert(jsonRequest)(equalTo(expected.toJson))
      },
      test("successfully encode search request to JSON with all parameters") {
        val jsonRequest = search(IndexName("indexName"), Query)
          .size(20)
          .highlights(highlight(TestDocument.intField))
          .sortBy(sortBy(TestDocument.intField).missing(First))
          .from(10) match {
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
            |  "size" : 20,
            |  "from" : 10,
            |  "sort": [
            |    {
            |      "intField": {
            |        "missing": "_first"
            |      }
            |    }
            |  ],
            |  "highlight" : {
            |    "fields" : {
            |      "intField" : {}
            |    }
            |  }
            |}
            |""".stripMargin

        assert(jsonRequest)(equalTo(expected.toJson))
      },
      test("successfully encode search and aggregate request to JSON with all parameters") {
        val jsonRequest = search(IndexName("indexName"), Query)
          .aggregate(termsAggregation(name = "aggregation", field = "day_of_week"))
          .size(20)
          .highlights(highlight(TestDocument.intField))
          .sortBy(sortBy(TestDocument.intField).missing(First))
          .from(10) match {
          case r: ElasticRequest.SearchAndAggregate => r.toJson
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
            |  "size" : 20,
            |  "from" : 10,
            |  "sort": [
            |    {
            |      "intField": {
            |        "missing": "_first"
            |      }
            |    }
            |  ],
            |  "highlight" : {
            |    "fields" : {
            |      "intField" : {}
            |    }
            |  },
            |  "aggs": {
            |   "aggregation" : {
            |     "terms" : {
            |       "field" : "day_of_week"
            |     }
            |   }
            |  }
            |}
            |""".stripMargin

        assert(jsonRequest)(equalTo(expected.toJson))
      }
    )

  private val Query = range(TestDocument.intField).gte(10)
}
