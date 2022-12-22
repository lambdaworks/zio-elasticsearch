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
        }
      ),
      suite("encoding ElasticQuery as JSON")(
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
        test("properly encode Bool query with Must") {
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
        test("properly encode Bool query with Should") {
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
        test("properly encode Bool query both with Must and Should") {
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
        }
      )
    )
}
