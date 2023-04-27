package zio.elasticsearch

import zio.Scope
import zio.elasticsearch.ElasticAggregation.{multipleAggregations, termsAggregation}
import zio.elasticsearch.aggregation._
import zio.elasticsearch.domain.TestSubDocument
import zio.elasticsearch.query.sort.SortOrder.Desc
import zio.elasticsearch.utils._
import zio.test.Assertion.equalTo
import zio.test._

object AggregationSpec extends ZIOSpecDefault {
  def spec: Spec[Environment with TestEnvironment with Scope, Any] =
    suite("Aggregations")(
      suite("creating ElasticAggregation")(
        test("successfully create Terms aggregation using `terms` method") {
          val aggregation = termsAggregation(name = "aggregation", field = "day_of_week")

          assert(aggregation)(
            equalTo(
              Terms(name = "aggregation", field = "day_of_week", order = Set.empty, subAggregations = Nil, size = None)
            )
          )
        },
        test("successfully create type-safe Terms aggregation using `terms` method") {
          val aggregation = termsAggregation(name = "aggregation", field = TestSubDocument.stringField)

          assert(aggregation)(
            equalTo(
              Terms(name = "aggregation", field = "stringField", order = Set.empty, subAggregations = Nil, size = None)
            )
          )
        },
        test("successfully create type-safe Terms aggregation with multi-field using `terms` method") {
          val aggregation =
            termsAggregation(name = "aggregation", field = TestSubDocument.stringField.keyword)

          assert(aggregation)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField.keyword",
                order = Set.empty,
                subAggregations = Nil,
                size = None
              )
            )
          )
        },
        test(
          "successfully create type-safe Terms aggregation with multi-field using `terms` method and order parameter"
        ) {
          val aggregation =
            termsAggregation(name = "aggregation", field = TestSubDocument.stringField.keyword)
              .order(AggregationOrder("_key", Desc))

          assert(aggregation)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField.keyword",
                order = Set(AggregationOrder("_key", Desc)),
                subAggregations = Nil,
                size = None
              )
            )
          )
        },
        test(
          "successfully create type-safe Terms aggregation with multi-field using `terms` method and size parameter"
        ) {
          val aggregation =
            termsAggregation(name = "aggregation", field = TestSubDocument.stringField.keyword)
              .size(5)

          assert(aggregation)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField.keyword",
                order = Set.empty,
                subAggregations = Nil,
                size = Some(5)
              )
            )
          )
        },
        test(
          "successfully create type-safe Terms aggregation with multi-field with all params"
        ) {
          val aggregation =
            termsAggregation(name = "aggregation", field = TestSubDocument.stringField.keyword)
              .order(AggregationOrder("_key", Desc))
              .size(5)

          assert(aggregation)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField.keyword",
                order = Set(AggregationOrder("_key", Desc)),
                subAggregations = Nil,
                size = Some(5)
              )
            )
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
                  Terms(
                    name = "firstAggregation",
                    field = "day_of_week",
                    order = Set.empty,
                    subAggregations = Nil,
                    size = None
                  ),
                  Terms(
                    name = "secondAggregation",
                    field = "customer_age",
                    order = Set.empty,
                    subAggregations = Nil,
                    size = None
                  )
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
                  Terms(
                    name = "firstAggregation",
                    field = "day_of_week",
                    order = Set.empty,
                    subAggregations = Nil,
                    size = None
                  ),
                  Terms(
                    name = "secondAggregation",
                    field = "customer_age",
                    order = Set.empty,
                    subAggregations = Nil,
                    size = None
                  )
                )
              )
            )
          ) && assert(aggregation2)(
            equalTo(
              Multiple(
                List(
                  Terms(
                    name = "thirdAggregation",
                    field = "day_of_month",
                    order = Set.empty,
                    subAggregations = Nil,
                    size = None
                  ),
                  Terms(
                    name = "firstAggregation",
                    field = "day_of_week",
                    order = Set.empty,
                    subAggregations = Nil,
                    size = None
                  ),
                  Terms(
                    name = "secondAggregation",
                    field = "customer_age",
                    order = Set.empty,
                    subAggregations = Nil,
                    size = None
                  )
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
                order = Set.empty,
                subAggregations = List(
                  Terms(
                    name = "secondAggregation",
                    field = "customer_age",
                    order = Set.empty,
                    subAggregations = Nil,
                    size = None
                  )
                ),
                size = None
              )
            )
          ) && assert(aggregation2)(
            equalTo(
              Multiple(
                List(
                  Terms(
                    name = "firstAggregation",
                    field = "day_of_week",
                    order = Set.empty,
                    subAggregations = Nil,
                    size = None
                  ),
                  Terms(
                    name = "secondAggregation",
                    field = "customer_age",
                    order = Set.empty,
                    subAggregations = List(
                      Terms(
                        name = "thirdAggregation",
                        field = "day_of_month",
                        order = Set.empty,
                        subAggregations = Nil,
                        size = None
                      )
                    ),
                    size = None
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
        test("properly encode Terms aggregation with order") {
          val aggregation =
            termsAggregation(name = "aggregation", field = "day_of_week").order(AggregationOrder("_key", Desc))
          val expected =
            """
              |{
              |  "aggs": {
              |    "aggregation": {
              |      "terms": {
              |        "field": "day_of_week",
              |        "order": {
              |          "_key": "desc"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Terms aggregation with size") {
          val aggregation = termsAggregation(name = "aggregation", field = "day_of_week").size(5)
          val expected =
            """
              |{
              |  "aggs": {
              |    "aggregation": {
              |      "terms": {
              |        "field": "day_of_week",
              |        "size": 5
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
