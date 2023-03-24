package zio.elasticsearch

import zio.Scope
import zio.elasticsearch.ElasticAggregation.{multipleAggregations, termsAggregation}
import zio.elasticsearch.aggregation.Aggregation._
import zio.elasticsearch.utils.{RichString, UserDocument}
import zio.test.Assertion.equalTo
import zio.test._

object AggregationSpec extends ZIOSpecDefault {

  def spec: Spec[Environment with TestEnvironment with Scope, Any] =
    suite("Aggregations")(
      suite("creating ElasticAggregation")(
        test("successfully create Terms aggregation using `terms` method") {
          val aggregation = termsAggregation(name = "aggregation", field = "day_of_week")

          assert(aggregation)(
            equalTo(Terms(name = "aggregation", field = "day_of_week", subAggregations = Nil))
          )
        },
        test("successfully create type-safe Terms aggregation using `terms` method") {
          val aggregation = termsAggregation(name = "aggregation", field = UserDocument.name)

          assert(aggregation)(
            equalTo(Terms(name = "aggregation", field = "name", subAggregations = Nil))
          )
        },
        test("successfully create type-safe Terms aggregation with multi-field using `terms` method") {
          val aggregation =
            termsAggregation(name = "aggregation", field = UserDocument.name, multiField = Some("keyword"))

          assert(aggregation)(
            equalTo(Terms(name = "aggregation", field = "name.keyword", subAggregations = Nil))
          )
        },
        test("successfully create Multiple aggregations using `multipleAggregations` method with two `terms`") {
          val aggregation = multipleAggregations.aggregations(
            termsAggregation(name = "firstAggregation", field = "day_of_week"),
            termsAggregation(name = "secondAggregation", field = "customer_age")
          )

          assert(aggregation)(
            equalTo(
              Multiple(
                List(
                  Terms(name = "firstAggregation", field = "day_of_week", subAggregations = Nil),
                  Terms(name = "secondAggregation", field = "customer_age", subAggregations = Nil)
                )
              )
            )
          )
        },
        test("successfully create Multiple aggregations using `withAgg`") {
          val aggregation1 = termsAggregation(name = "firstAggregation", field = "day_of_week")
            .withAgg(termsAggregation(name = "secondAggregation", field = "customer_age"))
          val aggregation2 = multipleAggregations
            .aggregations(
              termsAggregation(name = "firstAggregation", field = "day_of_week"),
              termsAggregation(name = "secondAggregation", field = "customer_age")
            )
            .withAgg(termsAggregation(name = "thirdAggregation", field = "day_of_month"))

          assert(aggregation1)(
            equalTo(
              Multiple(
                List(
                  Terms(name = "firstAggregation", field = "day_of_week", subAggregations = Nil),
                  Terms(name = "secondAggregation", field = "customer_age", subAggregations = Nil)
                )
              )
            )
          ) && assert(aggregation2)(
            equalTo(
              Multiple(
                List(
                  Terms(name = "thirdAggregation", field = "day_of_month", subAggregations = Nil),
                  Terms(name = "firstAggregation", field = "day_of_week", subAggregations = Nil),
                  Terms(name = "secondAggregation", field = "customer_age", subAggregations = Nil)
                )
              )
            )
          )
        },
        test("successfully create nested aggregation using `withSubAgg`") {
          val aggregation1 = termsAggregation(name = "firstAggregation", field = "day_of_week").withSubAgg(
            termsAggregation(name = "secondAggregation", field = "customer_age")
          )
          val aggregation2 = multipleAggregations
            .aggregations(
              termsAggregation(name = "firstAggregation", field = "day_of_week"),
              termsAggregation(name = "secondAggregation", field = "customer_age").withSubAgg(
                termsAggregation(name = "thirdAggregation", field = "day_of_month")
              )
            )

          assert(aggregation1)(
            equalTo(
              Terms(
                name = "firstAggregation",
                field = "day_of_week",
                subAggregations = List(Terms(name = "secondAggregation", field = "customer_age", subAggregations = Nil))
              )
            )
          ) && assert(aggregation2)(
            equalTo(
              Multiple(
                List(
                  Terms(name = "firstAggregation", field = "day_of_week", subAggregations = Nil),
                  Terms(
                    name = "secondAggregation",
                    field = "customer_age",
                    subAggregations =
                      List(Terms(name = "thirdAggregation", field = "day_of_month", subAggregations = Nil))
                  )
                )
              )
            )
          )
        }
      ),
      suite("encoding ElasticAggregation as JSON")(
        test("properly encode Terms aggregation") {
          val aggregation = termsAggregation(name = "aggregation", field = "day_of_week")
          val expected =
            """
              |{
              |  "aggs": {
              |    "aggregation": {
              |      "terms": {
              |        "field": "day_of_week"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Multiple aggregations with two Terms aggregations") {
          val aggregation = multipleAggregations.aggregations(
            termsAggregation(name = "firstAggregation", field = "day_of_week"),
            termsAggregation(name = "secondAggregation", field = "customer_age")
          )
          val expected =
            """
              |{
              |  "aggs": {
              |    "firstAggregation": {
              |      "terms": {
              |        "field": "day_of_week"
              |      }
              |    },
              |    "secondAggregation": {
              |      "terms": {
              |        "field": "customer_age"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson))
        },
        test("properly encode nested aggregation") {
          val aggregation = termsAggregation(name = "firstAggregation", field = "day_of_week").withSubAgg(
            termsAggregation(name = "secondAggregation", field = "customer_age")
          )
          val expected =
            """
              |{
              |  "aggs": {
              |    "firstAggregation": {
              |      "terms": {
              |        "field": "day_of_week"
              |      },
              |      "aggs": {
              |        "secondAggregation": {
              |          "terms": {
              |            "field": "customer_age"
              |          }
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson))
        },
        test("properly encode multiple aggregation with nested aggregation") {
          val aggregation = termsAggregation(name = "firstAggregation", field = "day_of_week")
            .withSubAgg(
              termsAggregation(name = "secondAggregation", field = "customer_age")
            )
            .withAgg(termsAggregation(name = "thirdAggregation", field = "day_of_month"))
          val expected =
            """
              |{
              |  "aggs": {
              |    "firstAggregation": {
              |      "terms": {
              |        "field": "day_of_week"
              |      },
              |      "aggs": {
              |        "secondAggregation": {
              |          "terms": {
              |            "field": "customer_age"
              |          }
              |        }
              |      }
              |    },
              |    "thirdAggregation": {
              |      "terms": {
              |        "field": "day_of_month"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson))
        }
      )
    )
}
