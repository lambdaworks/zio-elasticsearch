package zio.elasticsearch

import zio.Chunk
import zio.elasticsearch.ElasticAggregation._
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
        test("cardinality") {
          val aggregation            = cardinalityAggregation("aggregation", "testField")
          val aggregationTs          = cardinalityAggregation("aggregation", TestSubDocument.intField)
          val aggregationTsRaw       = cardinalityAggregation("aggregation", TestSubDocument.intField.raw)
          val aggregationWithMissing = cardinalityAggregation("aggregation", TestSubDocument.intField).missing(20)

          assert(aggregation)(equalTo(Cardinality(name = "aggregation", field = "testField", missing = None))) &&
          assert(aggregationTs)(equalTo(Cardinality(name = "aggregation", field = "intField", missing = None))) &&
          assert(aggregationTsRaw)(
            equalTo(Cardinality(name = "aggregation", field = "intField.raw", missing = None))
          ) &&
          assert(aggregationWithMissing)(
            equalTo(Cardinality(name = "aggregation", field = "intField", missing = Some(20)))
          )
        },
        test("max") {
          val aggregation            = maxAggregation("aggregation", "testField")
          val aggregationTs          = maxAggregation("aggregation", TestSubDocument.intField)
          val aggregationTsRaw       = maxAggregation("aggregation", TestSubDocument.intField.raw)
          val aggregationWithMissing = maxAggregation("aggregation", TestSubDocument.intField).missing(20.0)

          assert(aggregation)(equalTo(Max(name = "aggregation", field = "testField", missing = None))) &&
          assert(aggregationTs)(equalTo(Max(name = "aggregation", field = "intField", missing = None))) &&
          assert(aggregationTsRaw)(equalTo(Max(name = "aggregation", field = "intField.raw", missing = None))) &&
          assert(aggregationWithMissing)(
            equalTo(Max(name = "aggregation", field = "intField", missing = Some(20.0)))
          )
        },
        test("multiple") {
          val aggregation =
            multipleAggregations.aggregations(
              termsAggregation("first", TestDocument.stringField).orderByKeyDesc,
              maxAggregation("second", "testField").missing(20),
              cardinalityAggregation("third", TestDocument.intField)
            )
          val aggregationWithSubAggregation =
            termsAggregation("first", "testField")
              .withSubAgg(maxAggregation("second", TestSubDocument.intField.raw))
              .withAgg(termsAggregation("third", TestDocument.stringField))

          assert(aggregation)(
            equalTo(
              Multiple(
                List(
                  Terms(
                    name = "first",
                    field = "stringField",
                    order = Chunk(AggregationOrder(value = "_key", order = Desc)),
                    subAggregations = Nil,
                    size = None
                  ),
                  Max(name = "second", field = "testField", missing = Some(20)),
                  Cardinality(name = "third", field = "intField", missing = None)
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
                    order = Chunk.empty,
                    subAggregations = List(
                      Max(
                        name = "second",
                        field = "intField.raw",
                        missing = None
                      )
                    ),
                    size = None
                  ),
                  Terms(
                    name = "third",
                    field = "stringField",
                    order = Chunk.empty,
                    subAggregations = Nil,
                    size = None
                  )
                )
              )
            )
          )
        },
        test("subAggregation") {
          val aggregation1 = termsAggregation(name = "first", field = TestDocument.stringField).withSubAgg(
            termsAggregation(name = "second", field = TestSubDocument.stringField.raw)
          )
          val aggregation2 = termsAggregation(name = "first", field = TestDocument.stringField).withAgg(
            termsAggregation(name = "second", field = "testField").withSubAgg(
              maxAggregation("third", "anotherTestField")
            )
          )

          assert(aggregation1)(
            equalTo(
              Terms(
                name = "first",
                field = "stringField",
                order = Chunk.empty,
                subAggregations = List(
                  Terms(
                    name = "second",
                    field = "stringField.raw",
                    order = Chunk.empty,
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
                    order = Chunk.empty,
                    subAggregations = Nil,
                    size = None
                  ),
                  Terms(
                    name = "second",
                    field = "testField",
                    order = Chunk.empty,
                    subAggregations = List(
                      Max(
                        name = "third",
                        field = "anotherTestField",
                        missing = None
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
              Terms(name = "aggregation", field = "testField", order = Chunk.empty, subAggregations = Nil, size = None)
            )
          ) &&
          assert(aggregationTs)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField",
                order = Chunk.empty,
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
                order = Chunk.empty,
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
                order = Chunk(
                  AggregationOrder("_key", Desc),
                  AggregationOrder("test", Desc),
                  AggregationOrder("_count", Asc)
                ),
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
                order = Chunk.empty,
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
                order = Chunk(AggregationOrder("_count", Desc), AggregationOrder("_key", Asc)),
                subAggregations = Nil,
                size = Some(5)
              )
            )
          )
        }
      ),
      suite("encoding as JSON")(
        test("cardinality") {
          val aggregation            = cardinalityAggregation("aggregation", "testField")
          val aggregationTs          = cardinalityAggregation("aggregation", TestDocument.intField)
          val aggregationWithMissing = cardinalityAggregation("aggregation", TestDocument.intField).missing(20)

          val expected =
            """
              |{
              |  "aggs": {
              |    "aggregation": {
              |      "cardinality": {
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
              |      "cardinality": {
              |        "field": "intField"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMissing =
            """
              |{
              |  "aggs": {
              |    "aggregation": {
              |      "cardinality": {
              |        "field": "intField",
              |        "missing": 20.0
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithMissing.toJson)(equalTo(expectedWithMissing.toJson))
        },
        test("max") {
          val aggregation            = maxAggregation("aggregation", "testField")
          val aggregationTs          = maxAggregation("aggregation", TestDocument.intField)
          val aggregationWithMissing = maxAggregation("aggregation", TestDocument.intField).missing(20.0)

          val expected =
            """
              |{
              |  "aggs": {
              |    "aggregation": {
              |      "max": {
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
              |      "max": {
              |        "field": "intField"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMissing =
            """
              |{
              |  "aggs": {
              |    "aggregation": {
              |      "max": {
              |        "field": "intField",
              |        "missing": 20.0
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithMissing.toJson)(equalTo(expectedWithMissing.toJson))
        },
        test("multiple") {
          val aggregation =
            multipleAggregations.aggregations(
              termsAggregation("first", TestDocument.stringField).orderByKeyDesc,
              maxAggregation("second", "testField").missing(20.0),
              cardinalityAggregation("third", TestDocument.intField)
            )
          val aggregationWithSubAggregation =
            termsAggregation("first", "testField")
              .withSubAgg(maxAggregation("second", TestSubDocument.intField.raw))
              .withAgg(termsAggregation("third", TestDocument.stringField))

          val expected =
            """
              |{
              |  "aggs": {
              |    "first": {
              |      "terms": {
              |        "field": "stringField",
              |        "order": {
              |          "_key": "desc"
              |        }
              |      }
              |    },
              |    "second": {
              |      "max": {
              |        "field": "testField",
              |        "missing": 20.0
              |      }
              |    },
              |    "third": {
              |      "cardinality": {
              |        "field": "intField"
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
              |          "max": {
              |            "field": "intField.raw"
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
              .withSubAgg(maxAggregation("second", TestSubDocument.intField))

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
              |          "max": {
              |            "field": "intField"
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
