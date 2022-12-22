package zio.elasticsearch

import zio.Scope
import zio.elasticsearch.ElasticQuery._
import zio.elasticsearch.utils._
import zio.test.Assertion.equalTo
import zio.test._

object QueryDSLSpec extends ZIOSpecDefault {
  override def spec: Spec[Environment with TestEnvironment with Scope, Any] =
    suite("Query DSL")(
      suite("creating ElasticQuery")(
        test("successfully create Match query using `matches` method") {
          val queryString = matches(field = "day_of_week", query = "Monday")
          val queryBool   = matches(field = "day_of_week", query = true)
          val queryLong   = matches(field = "day_of_week", query = 1L)

          assert(queryString)(equalTo(Match(field = "day_of_week", query = "Monday")))
          assert(queryBool)(equalTo(Match(field = "day_of_week", query = true)))
          assert(queryLong)(equalTo(Match(field = "day_of_week", query = 1)))
        },
        test("successfully create `Must` query from two Match queries") {
          val query = boolQuery()
            .must(matches(field = "day_of_week", query = "Monday"), matches(field = "customer_gender", query = "MALE"))

          assert(query)(
            equalTo(
              BoolQuery(
                must = List(
                  Match(field = "day_of_week", query = "Monday"),
                  Match(field = "customer_gender", query = "MALE")
                ),
                should = List.empty
              )
            )
          )
        },
        test("successfully create `Should` query from two Match queries") {
          val query = boolQuery()
            .should(
              matches(field = "day_of_week", query = "Monday"),
              matches(field = "customer_gender", query = "MALE")
            )

          assert(query)(
            equalTo(
              BoolQuery(
                must = List.empty,
                should = List(
                  Match(field = "day_of_week", query = "Monday"),
                  Match(field = "customer_gender", query = "MALE")
                )
              )
            )
          )
        },
        test("successfully create `Must/Should` mixed query") {
          val query = boolQuery()
            .must(matches(field = "day_of_week", query = "Monday"), matches(field = "customer_gender", query = "MALE"))
            .should(matches(field = "customer_age", query = 23))

          assert(query)(
            equalTo(
              BoolQuery(
                must = List(
                  Match(field = "day_of_week", query = "Monday"),
                  Match(field = "customer_gender", query = "MALE")
                ),
                should = List(Match(field = "customer_age", query = 23))
              )
            )
          )
        },
        test("successfully create `Should/Must` mixed query") {
          val query = boolQuery()
            .must(matches(field = "customer_age", query = 23))
            .should(
              matches(field = "day_of_week", query = "Monday"),
              matches(field = "customer_gender", query = "MALE")
            )

          assert(query)(
            equalTo(
              BoolQuery(
                must = List(Match(field = "customer_age", query = 23)),
                should =
                  List(Match(field = "day_of_week", query = "Monday"), Match(field = "customer_gender", query = "MALE"))
              )
            )
          )
        },
        test("successfully create empty Range Query") {
          val query = range("field")

          assert(query)(equalTo(Range("field", Unbounded, Unbounded)))
        },
        test("successfully create Range Query with upper bound") {
          val query = range("field").lessThan(23)

          assert(query)(equalTo(Range("field", Unbounded, Less(23))))
        },
        test("successfully create Range Query with lower bound") {
          val query = range("field").greaterThan(23)

          assert(query)(equalTo(Range("field", Greater(23), Unbounded)))
        },
        test("successfully create Range Query with inclusive upper bound") {
          val query = range("field").lessEqual(23)

          assert(query)(equalTo(Range("field", Unbounded, LessEqual(23))))
        },
        test("successfully create Range Query with inclusive lower bound") {
          val query = range("field").greaterEqual(23)

          assert(query)(equalTo(Range("field", GreaterEqual(23), Unbounded)))
        },
        test("successfully create Range Query with both upper and lower bound") {
          val query = range("field").greaterEqual(23).lessThan(50)

          assert(query)(equalTo(Range("field", GreaterEqual(23), Less(50))))
        }
      ),
      suite("encoding ElasticQuery containing `Match` leaf query as JSON")(
        test("properly encode Match query") {
          val query = matches(field = "day_of_week", query = true)
          val expected =
            """
              |{
              |  "query": {
              |    "match": {
              |      "day_of_week": true
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with Must containing `Match` leaf query") {
          val query = boolQuery().must(matches(field = "day_of_week", query = "Monday"))
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
              |      "must": [
              |        {
              |          "match": {
              |            "day_of_week": "Monday"
              |          }
              |        }
              |      ],
              |      "should": []
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with Should containing `Match` leaf query") {
          val query = boolQuery().should(matches(field = "day_of_week", query = "Monday"))
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
              |      "must": [],
              |      "should": [
              |        {
              |          "match": {
              |            "day_of_week": "Monday"
              |          }
              |        }
              |      ]
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Bool Query with both Must and Should containing `Match` leaf query") {
          val query = boolQuery()
            .must(matches(field = "customer_id", query = 1))
            .should(matches(field = "day_of_week", query = "Monday"))
          val expected =
            """
              |{
              |  "query": {
              |    "bool": {
              |      "must": [
              |        {
              |          "match": {
              |            "customer_id": 1
              |          }
              |        }
              |      ],
              |      "should": [
              |        {
              |          "match": {
              |            "day_of_week": "Monday"
              |          }
              |        }
              |      ]
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Unbounded Range Query") {
          val query = range("field")
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "field": {
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Range Query with Lower Bound") {
          val query = range("field").greaterThan(23)
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "field": {
              |        "gt": 23
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Range Query with Upper Bound") {
          val query = range("field").lessThan(23)
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "field": {
              |        "lt": 23
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Range Query with Inclusive Lower Bound") {
          val query = range("field").greaterEqual("now")
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "field": {
              |        "gte": "now"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Range Query with inclusive Upper Bound") {
          val query = range("field").lessEqual(100L)
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "field": {
              |        "lte": 100
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Range Query with both Upper and Lower Bound") {
          val query = range("field").greaterEqual(10).lessThan(100)
          val expected =
            """
              |{
              |  "query": {
              |    "range": {
              |      "field": {
              |        "gte": 10,
              |        "lt": 100
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(query.toJsonBody)(equalTo(expected.toJson))
        }
      )
    )
}
