package zio.elasticsearch

import zio.Scope
import zio.elasticsearch.ElasticAggregation.{maxAggregation, multipleAggregations, termsAggregation}
import zio.elasticsearch.aggregation._
import zio.elasticsearch.domain.TestSubDocument
import zio.elasticsearch.query.sort.SortOrder.{Asc, Desc}
import zio.elasticsearch.utils._
import zio.test.Assertion.equalTo
import zio.test._

object AggregationSpec extends ZIOSpecDefault {
  def spec: Spec[Environment with TestEnvironment with Scope, Any] =
    suite("Aggregations")(
      suite("creating ElasticAggregation")(
        test("successfully create Max aggregation using `maxAggregation` method") {
          val aggregation = maxAggregation(name = "aggregation", field = "day_of_month")

          assert(aggregation)(
            equalTo(
              Max(name = "aggregation", field = "day_of_month", missing = None)
            )
          )
        },
        test("successfully create type-safe Max aggregation using `maxAggregation` method") {
          val aggregation = maxAggregation(name = "aggregation", field = TestSubDocument.intField)

          assert(aggregation)(
            equalTo(
              Max(name = "aggregation", field = "intField", missing = None)
            )
          )
        },
        test("successfully create type-safe Max aggregation using `maxAggregation` method with `missing` parameter") {
          val aggregation = maxAggregation(name = "aggregation", field = TestSubDocument.intField).missing(20)

          assert(aggregation)(
            equalTo(
              Max(name = "aggregation", field = "intField", missing = Some(20))
            )
          )
        },
        test("successfully create Multiple Aggregations using two `maxAggregation` methods") {
          val aggregation = maxAggregation(name = "aggregation1", field = TestSubDocument.intField)
            .withAgg(maxAggregation("aggregation2", TestSubDocument.intFieldList))

          assert(aggregation)(
            equalTo(
              Multiple(aggregations =
                List(
                  Max(name = "aggregation1", field = "intField", None),
                  Max(name = "aggregation2", field = "intFieldList", None)
                )
              )
            )
          )
        },
        test("successfully create Terms aggregation using `termsAggregation` method") {
          val aggregation = termsAggregation(name = "aggregation", field = "day_of_week")

          assert(aggregation)(
            equalTo(
              Terms(name = "aggregation", field = "day_of_week", order = Set.empty, subAggregations = Nil, size = None)
            )
          )
        },
        test("successfully create type-safe Terms aggregation using `termsAggregation` method") {
          val aggregation = termsAggregation(name = "aggregation", field = TestSubDocument.stringField)

          assert(aggregation)(
            equalTo(
              Terms(name = "aggregation", field = "stringField", order = Set.empty, subAggregations = Nil, size = None)
            )
          )
        },
        test("successfully create type-safe Terms aggregation with multi-field using `termsAggregation` method") {
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
          "successfully create type-safe Terms aggregation with multi-field using `termsAggregation` method and `order` parameter"
        ) {
          val aggregation =
            termsAggregation(
              name = "aggregation",
              field = TestSubDocument.stringField.keyword
            ).orderByKeyDesc.orderByCountAsc

          assert(aggregation)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField.keyword",
                order = Set(AggregationOrder("_key", Desc), AggregationOrder("_count", Asc)),
                subAggregations = Nil,
                size = None
              )
            )
          )
        },
        test(
          "successfully create type-safe Terms aggregation with multi-field using `termsAggregation` method and `size` parameter"
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
            termsAggregation(
              name = "aggregation",
              field = TestSubDocument.stringField.keyword
            ).orderByCountDesc.orderByKeyAsc
              .size(5)

          assert(aggregation)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField.keyword",
                order = Set(AggregationOrder("_count", Desc), AggregationOrder("_key", Asc)),
                subAggregations = Nil,
                size = Some(5)
              )
            )
          )
        },
        test(
          "successfully create Multiple aggregations using `multipleAggregations` method with two `termsAggregation`"
        ) {
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
              termsAggregation(name = "firstAggregation", field = "day_of_week")
                .withSubAgg(maxAggregation(name = "fourthAggregation", field = "age")),
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
                    subAggregations = List(Max(name = "fourthAggregation", field = "age", missing = None)),
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
        test("properly encode Max aggregation") {
          val aggregation = maxAggregation(name = "aggregation", field = "day_of_month")
          val expected =
            """
              |{
              |  "aggs": {
              |    "aggregation": {
              |      "max": {
              |        "field": "day_of_month"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson))
        },
        test("properly encode Max aggregation with `missing") {
          val aggregation = maxAggregation(name = "aggregation", field = "day_of_month").missing(20)
          val expected =
            """
              |{
              |  "aggs": {
              |    "aggregation": {
              |      "max": {
              |        "field": "day_of_month",
              |        "missing": 20.0
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson))
        },
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
        test("properly encode Terms aggregation with `order`") {
          val aggregation =
            termsAggregation(name = "aggregation", field = "day_of_week").orderBy(AggregationOrder("_key", Desc))
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
        test("properly encode Terms aggregation with `size`") {
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
        test("properly encode Multiple aggregations with Terms aggregation and Max aggregation") {
          val aggregation = multipleAggregations.aggregations(
            termsAggregation(name = "firstAggregation", field = "day_of_week"),
            maxAggregation(name = "secondAggregation", field = "customer_age")
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
              |      "max": {
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
            maxAggregation(name = "secondAggregation", field = "customer_age")
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
              |          "max": {
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
