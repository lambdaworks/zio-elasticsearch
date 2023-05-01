package zio.elasticsearch

import zio.elasticsearch.ElasticAggregation.{multipleAggregations, termsAggregation}
import zio.elasticsearch.aggregation._
import zio.elasticsearch.domain.{TestDocument, TestSubDocument}
import zio.elasticsearch.query.sort.SortOrder
import zio.elasticsearch.query.sort.SortOrder.{Asc, Desc}
import zio.elasticsearch.utils._
import zio.test.Assertion.equalTo
import zio.test._

object ElasticAggregationSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment, Any] =
    suite("ElasticAggregation")(
      suite("constructing")(
        test("multiple") {
          val aggregation =
            multipleAggregations.aggregations(
              termsAggregation("first", TestDocument.stringField).orderByKeyDesc,
              termsAggregation("second", "testField").size(5)
            )
          val aggregationWithSubAggregation =
            termsAggregation("first", "testField")
              .withSubAgg(termsAggregation("second", TestSubDocument.stringField.raw))
              .withAgg(termsAggregation("third", TestDocument.stringField))

          assert(aggregation)(
            equalTo(
              Multiple(
                List(
                  Terms(
                    name = "first",
                    field = "stringField",
                    order = Set(AggregationOrder(value = "_key", order = Desc)),
                    subAggregations = Nil,
                    size = None
                  ),
                  Terms(
                    name = "second",
                    field = "testField",
                    order = Set.empty,
                    subAggregations = Nil,
                    size = Some(5)
                  )
                )
              )
            )
          ) &&
          assert(aggregationWithSubAggregation)(
            equalTo(
              Multiple(
                List(
                  Terms(
                    name = "first",
                    field = "testField",
                    order = Set.empty,
                    subAggregations = List(
                      Terms(
                        name = "second",
                        field = "stringField.raw",
                        order = Set.empty,
                        subAggregations = Nil,
                        size = None
                      )
                    ),
                    size = None
                  ),
                  Terms(
                    name = "third",
                    field = "stringField",
                    order = Set.empty,
                    subAggregations = Nil,
                    size = None
                  )
                )
              )
            )
          )
        },
        test("subAggregation") {
          val aggregation1 = termsAggregation("first", TestDocument.stringField).withSubAgg(
            termsAggregation("second", TestSubDocument.stringField.raw)
          )
          val aggregation2 = termsAggregation("first", TestDocument.stringField).withAgg(
            termsAggregation("second", "testField").withSubAgg(termsAggregation("third", "anotherTestField"))
          )

          assert(aggregation1)(
            equalTo(
              Terms(
                name = "first",
                field = "stringField",
                order = Set.empty,
                subAggregations = List(
                  Terms(
                    name = "second",
                    field = "stringField.raw",
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
                    name = "first",
                    field = "stringField",
                    order = Set.empty,
                    subAggregations = Nil,
                    size = None
                  ),
                  Terms(
                    name = "second",
                    field = "testField",
                    order = Set.empty,
                    subAggregations = List(
                      Terms(
                        name = "third",
                        field = "anotherTestField",
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
        },
        test("terms") {
          val aggregation      = termsAggregation("aggregation", "testField")
          val aggregationTs    = termsAggregation("aggregation", TestSubDocument.stringField)
          val aggregationTsRaw = termsAggregation("aggregation", TestSubDocument.stringField.raw)
          val aggregationWithOrders =
            termsAggregation("aggregation", TestSubDocument.stringField).orderByKeyDesc
              .orderBy(AggregationOrder("test", Desc))
              .orderByCountAsc
          val aggregationWithSize = termsAggregation("aggregation", TestSubDocument.stringField).size(10)
          val aggregationWithAllParams =
            termsAggregation("aggregation", TestSubDocument.stringField.suffix("test")).orderByCountDesc.orderByKeyAsc
              .size(5)

          assert(aggregation)(
            equalTo(
              Terms(name = "aggregation", field = "testField", order = Set.empty, subAggregations = Nil, size = None)
            )
          ) &&
          assert(aggregationTs)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField",
                order = Set.empty,
                subAggregations = Nil,
                size = None
              )
            )
          ) &&
          assert(aggregationTsRaw)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField.raw",
                order = Set.empty,
                subAggregations = Nil,
                size = None
              )
            )
          ) &&
          assert(aggregationWithOrders)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField",
                order =
                  Set(AggregationOrder("_key", Desc), AggregationOrder("test", Desc), AggregationOrder("_count", Asc)),
                subAggregations = Nil,
                size = None
              )
            )
          ) &&
          assert(aggregationWithSize)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField",
                order = Set.empty,
                subAggregations = Nil,
                size = Some(10)
              )
            )
          ) &&
          assert(aggregationWithAllParams)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField.test",
                order = Set(AggregationOrder("_count", Desc), AggregationOrder("_key", Asc)),
                subAggregations = Nil,
                size = Some(5)
              )
            )
          )
        }
      ),
      suite("encoding as JSON")(
        test("multiple") {
          val aggregation =
            multipleAggregations.aggregations(
              termsAggregation("first", TestDocument.stringField),
              termsAggregation("second", "testField")
            )
          val aggregationWithSubAggregation =
            termsAggregation("first", "testField")
              .withSubAgg(termsAggregation("second", TestSubDocument.stringField.raw))
              .withAgg(termsAggregation("third", TestDocument.stringField))

          val expected =
            """
              |{
              |  "aggs": {
              |    "first": {
              |      "terms": {
              |        "field": "stringField"
              |      }
              |    },
              |    "second": {
              |      "terms": {
              |        "field": "testField"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithSubAggregation =
            """
              |{
              |  "aggs": {
              |    "first": {
              |      "terms": {
              |        "field": "testField"
              |      },
              |      "aggs": {
              |        "second": {
              |          "terms": {
              |            "field": "stringField.raw"
              |          }
              |        }
              |      }
              |    },
              |    "third": {
              |      "terms": {
              |        "field": "stringField"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationWithSubAggregation.toJson)(equalTo(expectedWithSubAggregation.toJson))
        },
        test("subAggregation") {
          val aggregation =
            termsAggregation("first", TestDocument.stringField)
              .withSubAgg(termsAggregation("second", TestSubDocument.stringField.keyword))

          val expected =
            """
              |{
              |  "aggs": {
              |    "first": {
              |      "terms": {
              |        "field": "stringField"
              |      },
              |      "aggs": {
              |        "second": {
              |          "terms": {
              |            "field": "stringField.keyword"
              |          }
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson))
        },
        test("terms") {
          val aggregation          = termsAggregation("aggregation", "testField")
          val aggregationTs        = termsAggregation("aggregation", TestDocument.stringField)
          val aggregationWithOrder = termsAggregation("aggregation", TestDocument.stringField).orderByKeyDesc
          val aggregationWithSize  = termsAggregation("aggregation", TestDocument.stringField).size(10)
          val aggregationWithAllParams = termsAggregation("aggregation", TestDocument.stringField)
            .orderBy(AggregationOrder("test", SortOrder.Asc))
            .size(20)

          val expected =
            """
              |{
              |  "aggs": {
              |    "aggregation": {
              |      "terms": {
              |        "field": "testField"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedTs =
            """
              |{
              |  "aggs": {
              |    "aggregation": {
              |      "terms": {
              |        "field": "stringField"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithOrder =
            """
              |{
              |  "aggs": {
              |    "aggregation": {
              |      "terms": {
              |        "field": "stringField",
              |        "order": {
              |          "_key": "desc"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithSize =
            """
              |{
              |  "aggs": {
              |    "aggregation": {
              |      "terms": {
              |        "field": "stringField",
              |        "size": 10
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "aggs": {
              |    "aggregation": {
              |      "terms": {
              |        "field": "stringField",
              |        "order": {
              |          "test": "asc"
              |        },
              |        "size": 20
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithOrder.toJson)(equalTo(expectedWithOrder.toJson)) &&
          assert(aggregationWithSize.toJson)(equalTo(expectedWithSize.toJson)) &&
          assert(aggregationWithAllParams.toJson)(equalTo(expectedWithAllParams.toJson))
        }
      )
    )
}
