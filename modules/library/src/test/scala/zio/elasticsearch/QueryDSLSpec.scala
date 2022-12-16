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
          val queryString = matches("day_of_week", "Monday")
          val queryBool   = matches("day_of_week", true)
          val queryLong   = matches("day_of_week", 1L)

          assert(queryString)(equalTo(Match("day_of_week", "Monday")))
          assert(queryBool)(equalTo(Match("day_of_week", true)))
          assert(queryLong)(equalTo(Match("day_of_week", 1)))
        },
        test("successfully create `Must` query from two Match queries") {
          val query = boolQuery()
            .must(matches("day_of_week", "Monday"), matches("customer_gender", "MALE"))

          assert(query)(
            equalTo(
              BoolQuery(List(Match("day_of_week", "Monday"), Match("customer_gender", "MALE")), List.empty)
            )
          )
        },
        test("successfully create `Should` query from two Match queries") {
          val query = boolQuery()
            .should(matches("day_of_week", "Monday"), matches("customer_gender", "MALE"))

          assert(query)(
            equalTo(
              BoolQuery(List.empty, List(Match("day_of_week", "Monday"), Match("customer_gender", "MALE")))
            )
          )
        },
        test("successfully create `Must/Should` mixed query") {
          val query = boolQuery()
            .must(matches("day_of_week", "Monday"), matches("customer_gender", "MALE"))
            .should(matches("customer_age", 23))

          assert(query)(
            equalTo(
              BoolQuery(
                List(Match("day_of_week", "Monday"), Match("customer_gender", "MALE")),
                List(Match("customer_age", 23))
              )
            )
          )
        },
        test("successfully create `Should/Must` mixed query") {
          val query = boolQuery()
            .must(matches("customer_age", 23))
            .should(matches("day_of_week", "Monday"), matches("customer_gender", "MALE"))

          assert(query)(
            equalTo(
              BoolQuery(
                List(Match("customer_age", 23)),
                List(Match("day_of_week", "Monday"), Match("customer_gender", "MALE"))
              )
            )
          )
        }
      ),
      suite("encoding ElasticQuery as JSON")(
        test("properly encode Match query") {
          val query = matches("day_of_week", true)
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

          assert(query.asJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Bool query with Must") {
          val query = boolQuery().must(matches("day_of_week", "Monday"))
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

          assert(query.asJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Bool query with Should") {
          val query = boolQuery().should(matches("day_of_week", "Monday"))
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

          assert(query.asJsonBody)(equalTo(expected.toJson))
        },
        test("properly encode Bool query both with Must and Should") {
          val query = boolQuery().must(matches("customer_id", 1)).should(matches("day_of_week", "Monday"))
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

          assert(query.asJsonBody)(equalTo(expected.toJson))
        }
      )
    )
}
