package zio.elasticsearch

import zio.elasticsearch.ElasticAggregation.termsAggregation
import zio.elasticsearch.ElasticHighlight.highlight
import zio.elasticsearch.ElasticQuery.term
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

object ElasticRequestDSLSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment, Any] =
    suite("Elastic Requests JSON encoding")(
      test("successfully encode search request to JSON") {
        val jsonRequest: Json = search(Index, Query) match {
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
        val jsonRequest: Json = search(Index, Query).searchAfter(Arr(Str("12345"))) match {
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
        val jsonRequest: Json = search(Index, Query).size(20) match {
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
        val jsonRequest = search(Index, Query)
          .size(20)
          .sort(sortBy(TestDocument.intField).missing(First))
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
        val jsonRequest = search(Index, Query)
          .size(20)
          .highlights(highlight(TestDocument.intField))
          .sort(sortBy(TestDocument.intField).missing(First))
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
        val jsonRequest = search(Index, Query)
          .aggregate(termsAggregation(name = "aggregation", field = "day_of_week"))
          .size(20)
          .highlights(highlight(TestDocument.intField))
          .sort(sortBy(TestDocument.intField).missing(First))
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
      test("successfully encode update by query request to JSON") {
        val jsonRequest = updateByQuery(
          index = Index,
          query = term(TestDocument.stringField.keyword, "StringField"),
          script = Script("ctx._source['intField']++")
        ) match { case r: UpdateByQuery => r.toJson }

        val expected =
          """
            |{
            |  "script": {
            |    "source": "ctx._source['intField']++"
            |  },
            |  "query": {
            |    "term": {
            |      "stringField.keyword": {
            |        "value": "StringField"
            |      }
            |    }
            |  }
            |}
            |""".stripMargin

        assert(jsonRequest)(equalTo(expected.toJson))
      },
      test("successfully encode update request to JSON with all parameters - script") {
        val jsonRequest = updateByScript(
          index = Index,
          id = DocId,
          script = Script("ctx._source.intField += params['factor']").withParams("factor" -> 2)
        ).orCreate[TestDocument](
          TestDocument(
            stringField = "stringField",
            subDocumentList = Nil,
            dateField = LocalDate.parse("2020-10-10"),
            intField = 1,
            doubleField = 1.0,
            booleanField = true
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
            |    "doubleField": 1.0,
            |    "booleanField": true
            |  }
            |}
            |""".stripMargin

        assert(jsonRequest)(equalTo(expected.toJson))
      },
      test("successfully encode update request to JSON with all parameters - doc") {
        val jsonRequest = update[TestDocument](
          index = Index,
          id = DocId,
          doc = TestDocument(
            stringField = "stringField1",
            subDocumentList = Nil,
            dateField = LocalDate.parse("2020-10-10"),
            intField = 1,
            doubleField = 1.0,
            booleanField = true
          )
        ).orCreate[TestDocument](
          TestDocument(
            stringField = "stringField2",
            subDocumentList = Nil,
            dateField = LocalDate.parse("2020-11-11"),
            intField = 2,
            doubleField = 2.0,
            booleanField = false
          )
        ) match { case r: Update => r.toJson }

        val expected =
          """
            |{
            |  "doc": {
            |    "stringField": "stringField1",
            |    "subDocumentList": [],
            |    "dateField": "2020-10-10",
            |    "intField": 1,
            |    "doubleField": 1.0,
            |    "booleanField": true
            |  },
            |  "upsert": {
            |    "stringField": "stringField2",
            |    "subDocumentList": [],
            |    "dateField": "2020-11-11",
            |    "intField": 2,
            |    "doubleField": 2.0,
            |    "booleanField": false
            |  }
            |}
            |""".stripMargin

        assert(jsonRequest)(equalTo(expected.toJson))
      }
    )

  private val Query = ElasticQuery.range(TestDocument.intField).gte(10)
  private val Index = IndexName("index")
  private val DocId = DocumentId("documentid")
}
