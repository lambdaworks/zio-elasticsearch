package zio.elasticsearch

import zio.Chunk
import zio.elasticsearch.ElasticAggregation._
import zio.elasticsearch.aggregation._
import zio.elasticsearch.domain.{TestDocument, TestSubDocument}
import zio.elasticsearch.query.sort.SortOrder.{Asc, Desc}
import zio.elasticsearch.query.sort.{SortByFieldOptions, SortOrder}
import zio.elasticsearch.script.Script
import zio.elasticsearch.utils._
import zio.test.Assertion.equalTo
import zio.test._

object ElasticAggregationSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment, Any] =
    suite("ElasticAggregation")(
      suite("constructing")(
        test("avg") {
          val aggregation            = avgAggregation("aggregation", "testField")
          val aggregationTs          = avgAggregation("aggregation", TestSubDocument.intField)
          val aggregationTsRaw       = avgAggregation("aggregation", TestSubDocument.intField.raw)
          val aggregationWithMissing = avgAggregation("aggregation", TestSubDocument.intField).missing(20.0)

          assert(aggregation)(equalTo(Avg(name = "aggregation", field = "testField", missing = None))) &&
          assert(aggregationTs)(equalTo(Avg(name = "aggregation", field = "intField", missing = None))) &&
          assert(aggregationTsRaw)(equalTo(Avg(name = "aggregation", field = "intField.raw", missing = None))) &&
          assert(aggregationWithMissing)(
            equalTo(Avg(name = "aggregation", field = "intField", missing = Some(20.0)))
          )
        },
        test("bucketSelector") {
          val aggregation1 = bucketSelectorAggregation(
            name = "aggregation",
            script = Script("params.agg1 > 10"),
            bucketsPath = Map("agg1" -> "aggregation1")
          )
          val aggregation2 = bucketSelectorAggregation(
            name = "aggregation",
            script = Script("params.agg1 + params.agg2 > 10"),
            bucketsPath = Map("agg1" -> "aggregation1", "agg2" -> "aggregation2")
          )

          assert(aggregation1)(
            equalTo(
              BucketSelector(
                name = "aggregation",
                script = Script(source = "params.agg1 > 10", params = Map.empty, lang = None),
                bucketsPath = Map("agg1" -> "aggregation1")
              )
            )
          ) &&
          assert(aggregation2)(
            equalTo(
              BucketSelector(
                name = "aggregation",
                script = Script(source = "params.agg1 + params.agg2 > 10", params = Map.empty, lang = None),
                bucketsPath = Map("agg1" -> "aggregation1", "agg2" -> "aggregation2")
              )
            )
          )
        },
        test("bucketSort") {
          val aggregationWithFrom = bucketSortAggregation("aggregation").from(5)
          val aggregationWithSize = bucketSortAggregation("aggregation").size(5)
          val aggregationWithSort = bucketSortAggregation("aggregation").sort(ElasticSort.sortBy("aggregation2"))
          val aggregationWithAllParams =
            bucketSortAggregation("aggregation").sort(ElasticSort.sortBy("aggregation2")).from(5).size(7)

          assert(aggregationWithFrom)(
            equalTo(BucketSort(name = "aggregation", sortBy = Chunk.empty, from = Some(5), size = None))
          ) &&
          assert(aggregationWithSize)(
            equalTo(BucketSort(name = "aggregation", sortBy = Chunk.empty, from = None, size = Some(5)))
          ) &&
          assert(aggregationWithSort)(
            equalTo(
              BucketSort(
                name = "aggregation",
                sortBy = Chunk(
                  SortByFieldOptions(
                    field = "aggregation2",
                    format = None,
                    missing = None,
                    mode = None,
                    numericType = None,
                    order = None,
                    unmappedType = None
                  )
                ),
                from = None,
                size = None
              )
            )
          ) &&
          assert(aggregationWithAllParams)(
            equalTo(
              BucketSort(
                name = "aggregation",
                sortBy = Chunk(
                  SortByFieldOptions(
                    field = "aggregation2",
                    format = None,
                    missing = None,
                    mode = None,
                    numericType = None,
                    order = None,
                    unmappedType = None
                  )
                ),
                from = Some(5),
                size = Some(7)
              )
            )
          )
        },
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
        test("min") {
          val aggregation            = minAggregation("aggregation", "testField")
          val aggregationTs          = minAggregation("aggregation", TestSubDocument.intField)
          val aggregationTsRaw       = minAggregation("aggregation", TestSubDocument.intField.raw)
          val aggregationWithMissing = minAggregation("aggregation", TestSubDocument.intField).missing(20.0)

          assert(aggregation)(equalTo(Min(name = "aggregation", field = "testField", missing = None))) &&
          assert(aggregationTs)(equalTo(Min(name = "aggregation", field = "intField", missing = None))) &&
          assert(aggregationTsRaw)(equalTo(Min(name = "aggregation", field = "intField.raw", missing = None))) &&
          assert(aggregationWithMissing)(
            equalTo(Min(name = "aggregation", field = "intField", missing = Some(20.0)))
          )
        },
        test("missing") {
          val aggregation      = missingAggregation("aggregation", "testField")
          val aggregationTs    = missingAggregation("aggregation", TestSubDocument.stringField)
          val aggregationTsRaw = missingAggregation("aggregation", TestSubDocument.stringField.raw)

          assert(aggregation)(equalTo(Missing(name = "aggregation", field = "testField"))) &&
          assert(aggregationTs)(equalTo(Missing(name = "aggregation", field = "stringField"))) &&
          assert(aggregationTsRaw)(equalTo(Missing(name = "aggregation", field = "stringField.raw")))
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
                Chunk(
                  Terms(
                    name = "first",
                    field = "stringField",
                    order = Chunk(AggregationOrder(value = "_key", order = Desc)),
                    subAggregations = Chunk.empty,
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
                Chunk(
                  Terms(
                    name = "first",
                    field = "testField",
                    order = Chunk.empty,
                    subAggregations = Chunk(
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
                    subAggregations = Chunk.empty,
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
                subAggregations = Chunk(
                  Terms(
                    name = "second",
                    field = "stringField.raw",
                    order = Chunk.empty,
                    subAggregations = Chunk.empty,
                    size = None
                  )
                ),
                size = None
              )
            )
          ) && assert(aggregation2)(
            equalTo(
              Multiple(
                Chunk(
                  Terms(
                    name = "first",
                    field = "stringField",
                    order = Chunk.empty,
                    subAggregations = Chunk.empty,
                    size = None
                  ),
                  Terms(
                    name = "second",
                    field = "testField",
                    order = Chunk.empty,
                    subAggregations = Chunk(
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
        test("sum") {
          val aggregation            = sumAggregation("aggregation", "testField")
          val aggregationTs          = sumAggregation("aggregation", TestSubDocument.intField)
          val aggregationTsRaw       = sumAggregation("aggregation", TestSubDocument.intField.raw)
          val aggregationWithMissing = sumAggregation("aggregation", TestSubDocument.intField).missing(20.0)

          assert(aggregation)(equalTo(Sum(name = "aggregation", field = "testField", missing = None))) &&
          assert(aggregationTs)(equalTo(Sum(name = "aggregation", field = "intField", missing = None))) &&
          assert(aggregationTsRaw)(equalTo(Sum(name = "aggregation", field = "intField.raw", missing = None))) &&
          assert(aggregationWithMissing)(
            equalTo(Sum(name = "aggregation", field = "intField", missing = Some(20.0)))
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
              Terms(
                name = "aggregation",
                field = "testField",
                order = Chunk.empty,
                subAggregations = Chunk.empty,
                size = None
              )
            )
          ) &&
          assert(aggregationTs)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField",
                order = Chunk.empty,
                subAggregations = Chunk.empty,
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
                subAggregations = Chunk.empty,
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
                subAggregations = Chunk.empty,
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
                subAggregations = Chunk.empty,
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
                subAggregations = Chunk.empty,
                size = Some(5)
              )
            )
          )
        }
      ),
      suite("encoding as JSON")(
        test("avg") {
          val aggregation            = avgAggregation("aggregation", "testField")
          val aggregationTs          = avgAggregation("aggregation", TestDocument.intField)
          val aggregationWithMissing = avgAggregation("aggregation", TestDocument.intField).missing(20.0)

          val expected =
            """
              |{
              |  "aggregation": {
              |    "avg": {
              |      "field": "testField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedTs =
            """
              |{
              |  "aggregation": {
              |    "avg": {
              |      "field": "intField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMissing =
            """
              |{
              |  "aggregation": {
              |    "avg": {
              |      "field": "intField",
              |      "missing": 20.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithMissing.toJson)(equalTo(expectedWithMissing.toJson))
        },
        test("bucketSelector") {
          val aggregation1 = bucketSelectorAggregation(
            name = "aggregation",
            script = Script("params.agg1 > 10"),
            bucketsPath = Map("agg1" -> "aggregation1")
          )
          val aggregation2 = bucketSelectorAggregation(
            name = "aggregation",
            script = Script("params.agg1 + params.agg2 > 10"),
            bucketsPath = Map("agg1" -> "aggregation1", "agg2" -> "aggregation2")
          )

          val expected1 =
            """
              |{
              |  "aggregation": {
              |    "bucket_selector": {
              |      "buckets_path": {
              |        "agg1": "aggregation1"
              |      },
              |      "script": {
              |        "source": "params.agg1 > 10"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expected2 =
            """
              |{
              |  "aggregation": {
              |    "bucket_selector": {
              |      "buckets_path": {
              |        "agg1": "aggregation1",
              |        "agg2": "aggregation2"
              |      },
              |      "script": {
              |        "source": "params.agg1 + params.agg2 > 10"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation1.toJson)(equalTo(expected1.toJson)) &&
          assert(aggregation2.toJson)(equalTo(expected2.toJson))
        },
        test("bucketSort") {
          val aggregationWithFrom = bucketSortAggregation("aggregation").from(5)
          val aggregationWithSize = bucketSortAggregation("aggregation").size(5)
          val aggregationWithSort = bucketSortAggregation("aggregation").sort(ElasticSort.sortBy("aggregation2"))
          val aggregationWithAllParams =
            bucketSortAggregation("aggregation").sort(ElasticSort.sortBy("aggregation2")).from(5).size(7)

          val expectedWithFrom =
            """
              |{
              |  "aggregation": {
              |    "bucket_sort": {
              |      "from": 5
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithSize =
            """
              |{
              |  "aggregation": {
              |    "bucket_sort": {
              |      "size": 5
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithSort =
            """
              |{
              |  "aggregation": {
              |    "bucket_sort": {
              |      "sort": [
              |        "aggregation2"
              |      ]
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "aggregation": {
              |    "bucket_sort": {
              |      "sort": [
              |        "aggregation2"
              |      ],
              |      "from": 5,
              |      "size": 7
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregationWithFrom.toJson)(equalTo(expectedWithFrom.toJson)) &&
          assert(aggregationWithSize.toJson)(equalTo(expectedWithSize.toJson)) &&
          assert(aggregationWithSort.toJson)(equalTo(expectedWithSort.toJson)) &&
          assert(aggregationWithAllParams.toJson)(equalTo(expectedWithAllParams.toJson))
        },
        test("cardinality") {
          val aggregation            = cardinalityAggregation("aggregation", "testField")
          val aggregationTs          = cardinalityAggregation("aggregation", TestDocument.intField)
          val aggregationWithMissing = cardinalityAggregation("aggregation", TestDocument.intField).missing(20)

          val expected =
            """
              |{
              |  "aggregation": {
              |    "cardinality": {
              |      "field": "testField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedTs =
            """
              |{
              |  "aggregation": {
              |    "cardinality": {
              |      "field": "intField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMissing =
            """
              |{
              |  "aggregation": {
              |    "cardinality": {
              |      "field": "intField",
              |      "missing": 20.0
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
              |  "aggregation": {
              |    "max": {
              |      "field": "testField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedTs =
            """
              |{
              |  "aggregation": {
              |    "max": {
              |      "field": "intField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMissing =
            """
              |{
              |  "aggregation": {
              |    "max": {
              |      "field": "intField",
              |      "missing": 20.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithMissing.toJson)(equalTo(expectedWithMissing.toJson))
        },
        test("min") {
          val aggregation            = minAggregation("aggregation", "testField")
          val aggregationTs          = minAggregation("aggregation", TestDocument.intField)
          val aggregationWithMissing = minAggregation("aggregation", TestDocument.intField).missing(20.0)

          val expected =
            """
              |{
              |  "aggregation": {
              |    "min": {
              |      "field": "testField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedTs =
            """
              |{
              |  "aggregation": {
              |    "min": {
              |      "field": "intField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMissing =
            """
              |{
              |  "aggregation": {
              |    "min": {
              |      "field": "intField",
              |      "missing": 20.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithMissing.toJson)(equalTo(expectedWithMissing.toJson))
        },
        test("missing") {
          val aggregation   = missingAggregation("aggregation", "testField")
          val aggregationTs = missingAggregation("aggregation", TestDocument.stringField)

          val expected =
            """
              |{
              |  "aggregation": {
              |    "missing": {
              |      "field": "testField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedTs =
            """
              |{
              |  "aggregation": {
              |    "missing": {
              |      "field": "stringField"
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson))
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
              |  "first": {
              |    "terms": {
              |      "field": "stringField",
              |      "order": {
              |        "_key": "desc"
              |      }
              |    }
              |  },
              |  "second": {
              |    "max": {
              |      "field": "testField",
              |      "missing": 20.0
              |    }
              |  },
              |  "third": {
              |    "cardinality": {
              |      "field": "intField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithSubAggregation =
            """
              |{
              |  "first": {
              |    "terms": {
              |      "field": "testField"
              |    },
              |    "aggs": {
              |      "second": {
              |        "max": {
              |          "field": "intField.raw"
              |        }
              |      }
              |    }
              |  },
              |  "third": {
              |    "terms": {
              |      "field": "stringField"
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
              |  "first": {
              |    "terms": {
              |      "field": "stringField"
              |    },
              |    "aggs": {
              |      "second": {
              |        "max": {
              |          "field": "intField"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson))
        },
        test("sum") {
          val aggregation            = sumAggregation("aggregation", "testField")
          val aggregationTs          = sumAggregation("aggregation", TestDocument.intField)
          val aggregationWithMissing = sumAggregation("aggregation", TestDocument.intField).missing(20.0)

          val expected =
            """
              |{
              |  "aggregation": {
              |    "sum": {
              |      "field": "testField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedTs =
            """
              |{
              |  "aggregation": {
              |    "sum": {
              |      "field": "intField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMissing =
            """
              |{
              |  "aggregation": {
              |    "sum": {
              |      "field": "intField",
              |      "missing": 20.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithMissing.toJson)(equalTo(expectedWithMissing.toJson))
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
              |  "aggregation": {
              |    "terms": {
              |      "field": "testField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedTs =
            """
              |{
              |  "aggregation": {
              |    "terms": {
              |      "field": "stringField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithOrder =
            """
              |{
              |  "aggregation": {
              |    "terms": {
              |      "field": "stringField",
              |      "order": {
              |        "_key": "desc"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithSize =
            """
              |{
              |  "aggregation": {
              |    "terms": {
              |      "field": "stringField",
              |      "size": 10
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "aggregation": {
              |    "terms": {
              |      "field": "stringField",
              |      "order": {
              |        "test": "asc"
              |      },
              |      "size": 20
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
