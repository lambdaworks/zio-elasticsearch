package zio.elasticsearch

import zio.elasticsearch.ElasticAggregation.termsAggregation
import zio.elasticsearch.ElasticHighlight.highlight
import zio.elasticsearch.ElasticRequest._
import zio.elasticsearch.ElasticSort.sortBy
import zio.elasticsearch.domain.TestDocument
import zio.elasticsearch.query.sort.Missing.First
import zio.elasticsearch.script.Script
import zio.elasticsearch.utils.RichString
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Str}
import zio.test.Assertion.equalTo
import zio.test._

import java.time.LocalDate

object ElasticRequestsDSLSpec extends ZIOSpecDefault {

  private val query = ElasticQuery.range(TestDocument.intField).gte(10)
  private val index = IndexName("index")
  private val docId = DocumentId("documentid")

  override def spec: Spec[TestEnvironment, Any] =
    suite("Elastic Requests JSON encoding")(
      test("successfully encode search request to JSON") {
        val jsonRequest: Json = search(index, query) match {
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
        val jsonRequest: Json = search(index, query).searchAfter(Arr(Str("12345"))) match {
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
        val jsonRequest: Json = search(index, query).size(20) match {
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
        val jsonRequest = search(index, query)
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
        val jsonRequest = search(index, query)
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
        val jsonRequest = search(index, query)
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
      },
      test("successfully encode update request to JSON with all parameters - script") {
        val jsonRequest = update(
          index = index,
          id = docId,
          script = Script("ctx._source.intField += params['factor']").withParams("factor" -> 2)
        ).upsert[TestDocument](
          TestDocument(
            stringField = "stringField",
            subDocumentList = Nil,
            dateField = LocalDate.parse("2020-10-10"),
            intField = 1,
            doubleField = 1.0
          )
        ) match { case r: Update => r.toJson }

        val expected =
          """
            |{
            |  "script": {
            |    "source": "ctx._source.intField += params['factor']",
            |    "params": {
            |      "factor": 2
            |    }
            |  },
            |  "upsert": {
            |    "stringField": "stringField",
            |    "subDocumentList": [],
            |    "dateField": "2020-10-10",
            |    "intField": 1,
            |    "doubleField": 1.0
            |  }
            |}
            |""".stripMargin

        assert(jsonRequest)(equalTo(expected.toJson))
      },
      test("successfully encode update request to JSON with all parameters - doc") {
        val jsonRequest = update[TestDocument](
          index = index,
          id = docId,
          doc = TestDocument(
            stringField = "stringField1",
            subDocumentList = Nil,
            dateField = LocalDate.parse("2020-10-10"),
            intField = 1,
            doubleField = 1.0
          )
        ).upsert[TestDocument](
          TestDocument(
            stringField = "stringField2",
            subDocumentList = Nil,
            dateField = LocalDate.parse("2020-11-11"),
            intField = 2,
            doubleField = 2.0
          )
        ).docAsUpsertTrue match { case r: Update => r.toJson }

        val expected =
          """
            |{
            |  "doc": {
            |    "stringField": "stringField1",
            |    "subDocumentList": [],
            |    "dateField": "2020-10-10",
            |    "intField": 1,
            |    "doubleField": 1.0
            |  },
            |  "doc_as_upsert": true,
            |  "upsert": {
            |    "stringField": "stringField2",
            |    "subDocumentList": [],
            |    "dateField": "2020-11-11",
            |    "intField": 2,
            |    "doubleField": 2.0
            |  }
            |}
            |""".stripMargin

        assert(jsonRequest)(equalTo(expected.toJson))
      }
    )
}
