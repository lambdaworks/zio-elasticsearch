package zio.elasticsearch

import zio.elasticsearch.ElasticAggregation._
import zio.elasticsearch.ElasticQuery.term
import zio.elasticsearch.aggregation.IpRange.IpRangeBound
import zio.elasticsearch.aggregation._
import zio.elasticsearch.domain.{TestDocument, TestSubDocument}
import zio.elasticsearch.query.sort.SortOrder.{Asc, Desc}
import zio.elasticsearch.query.sort.{SortByFieldOptions, SortOrder}
import zio.elasticsearch.script.Script
import zio.elasticsearch.utils._
import zio.test.Assertion.equalTo
import zio.test._
import zio.{Chunk, NonEmptyChunk}

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
          assert(aggregationWithMissing)(equalTo(Avg(name = "aggregation", field = "intField", missing = Some(20.0))))
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
          val aggregationWithFrom      = bucketSortAggregation("aggregation").from(5)
          val aggregationWithSize      = bucketSortAggregation("aggregation").size(5)
          val aggregationWithSort      = bucketSortAggregation("aggregation").sort(ElasticSort.sortBy("aggregation2"))
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
        test("extendedStats") {
          val aggregation                    = extendedStatsAggregation("aggregation", "testField")
          val aggregationTs                  = extendedStatsAggregation("aggregation", TestSubDocument.intField)
          val aggregationTsRaw               = extendedStatsAggregation("aggregation", TestSubDocument.intField.raw)
          val aggregationWithMissing         = extendedStatsAggregation("aggregation", TestSubDocument.intField).missing(20.0)
          val aggregationWithSigma           = extendedStatsAggregation("aggregation", TestSubDocument.intField).sigma(3.0)
          val aggregationWithMissingAndSigma =
            extendedStatsAggregation("aggregation", TestSubDocument.intField).missing(20.0).sigma(3.0)

          assert(aggregation)(
            equalTo(ExtendedStats(name = "aggregation", field = "testField", missing = None, sigma = None))
          ) && assert(aggregationTs)(
            equalTo(ExtendedStats(name = "aggregation", field = "intField", missing = None, sigma = None))
          ) && assert(aggregationTsRaw)(
            equalTo(ExtendedStats(name = "aggregation", field = "intField.raw", missing = None, sigma = None))
          ) && assert(aggregationWithMissing)(
            equalTo(ExtendedStats(name = "aggregation", field = "intField", missing = Some(20.0), sigma = None))
          ) && assert(aggregationWithSigma)(
            equalTo(ExtendedStats(name = "aggregation", field = "intField", missing = None, sigma = Some(3.0)))
          ) && assert(aggregationWithMissingAndSigma)(
            equalTo(ExtendedStats(name = "aggregation", field = "intField", missing = Some(20.0), sigma = Some(3.0)))
          )
        },
        test("ipRange") {
          val aggregation =
            ipRangeAggregation(
              name = "ip_range_agg",
              field = "ipField",
              ranges = NonEmptyChunk(
                IpRangeBound(to = Some("10.0.0.5")),
                IpRangeBound(from = Some("10.0.0.5"))
              )
            )

          assert(aggregation)(
            equalTo(
              IpRange(
                name = "ip_range_agg",
                field = "ipField",
                ranges = NonEmptyChunk(
                  IpRangeBound(to = Some("10.0.0.5")),
                  IpRangeBound(from = Some("10.0.0.5"))
                ),
                keyed = None,
                subAggregations = None
              )
            )
          )
        },
        test("filter") {
          val query                         = term(TestDocument.stringField, "test")
          val aggregation                   = filterAggregation("aggregation", query)
          val aggregationWithSubAggregation =
            filterAggregation("aggregation", query).withSubAgg(minAggregation("subAggregation", TestDocument.intField))
          val aggregationWithMultipleSubAggregations = filterAggregation("aggregation", query)
            .withSubAgg(maxAggregation("maxSubAggregation", TestDocument.intField))
            .withSubAgg(minAggregation("minSubAggregation", TestDocument.doubleField))

          assert(aggregation)(
            equalTo(
              Filter(
                name = "aggregation",
                query = query,
                subAggregations = Chunk.empty
              )
            )
          ) &&
          assert(aggregationWithSubAggregation)(
            equalTo(
              Filter(
                name = "aggregation",
                query = query,
                subAggregations = Chunk(minAggregation("subAggregation", TestDocument.intField))
              )
            )
          ) &&
          assert(aggregationWithMultipleSubAggregations)(
            equalTo(
              Filter(
                name = "aggregation",
                query = query,
                subAggregations = Chunk(
                  minAggregation("minSubAggregation", TestDocument.doubleField),
                  maxAggregation("maxSubAggregation", TestDocument.intField)
                )
              )
            )
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
        test("percentileRanks") {
          val aggregation            = percentileRanksAggregation("aggregation", "testField", 5, 6)
          val aggregationTs          = percentileRanksAggregation("aggregation", TestSubDocument.intField, 5, 6)
          val aggregationTsRaw       = percentileRanksAggregation("aggregation", TestSubDocument.intField.raw, 5, 6)
          val aggregationWithMissing =
            percentileRanksAggregation("aggregation", TestSubDocument.intField, 5, 6).missing(20.0)

          assert(aggregation)(
            equalTo(
              PercentileRanks(name = "aggregation", field = "testField", values = Chunk(5.0, 6.0), missing = None)
            )
          ) &&
          assert(aggregationTs)(
            equalTo(PercentileRanks(name = "aggregation", field = "intField", values = Chunk(5.0, 6.0), missing = None))
          ) &&
          assert(aggregationTsRaw)(
            equalTo(
              PercentileRanks(name = "aggregation", field = "intField.raw", values = Chunk(5.0, 6.0), missing = None)
            )
          ) &&
          assert(aggregationWithMissing)(
            equalTo(
              PercentileRanks(name = "aggregation", field = "intField", values = Chunk(5.0, 6.0), missing = Some(20.0))
            )
          )
        },
        test("percentiles") {
          val aggregation             = percentilesAggregation("aggregation", "testField")
          val aggregationTs           = percentilesAggregation("aggregation", TestSubDocument.intField)
          val aggregationTsRaw        = percentilesAggregation("aggregation", TestSubDocument.intField.raw)
          val aggregationWithMissing  = percentilesAggregation("aggregation", TestSubDocument.intField).missing(20.0)
          val aggregationWithPercents =
            percentilesAggregation("aggregation", TestSubDocument.intField).percents(75, 90, 99)
          val aggregationWithAllParams =
            percentilesAggregation("aggregation", TestDocument.intField).percents(75, 90, 99).missing(20.0)

          assert(aggregation)(
            equalTo(Percentiles(name = "aggregation", field = "testField", percents = Chunk.empty, missing = None))
          ) &&
          assert(aggregationTs)(
            equalTo(Percentiles(name = "aggregation", field = "intField", percents = Chunk.empty, missing = None))
          ) &&
          assert(aggregationTsRaw)(
            equalTo(Percentiles(name = "aggregation", field = "intField.raw", percents = Chunk.empty, missing = None))
          ) &&
          assert(aggregationWithMissing)(
            equalTo(Percentiles(name = "aggregation", field = "intField", percents = Chunk.empty, missing = Some(20.0)))
          ) &&
          assert(aggregationWithPercents)(
            equalTo(Percentiles(name = "aggregation", field = "intField", percents = Chunk(75, 90, 99), missing = None))
          ) &&
          assert(aggregationWithAllParams)(
            equalTo(
              Percentiles(name = "aggregation", field = "intField", percents = Chunk(75, 90, 99), missing = Some(20.0))
            )
          )
        },
        test("sampler") {
          val aggWithSubAgg =
            samplerAggregation("aggregation2", ElasticAggregation.termsAggregation("keywords", "text"))
          val aggWithSubAggsAndMaxDocumentsPerShardParam =
            samplerAggregation("aggregation2", ElasticAggregation.termsAggregation("keywords", "text"))
              .maxDocumentsPerShard(50)
              .withSubAgg(ElasticAggregation.avgAggregation("avg_length", "length"))

          assert(aggWithSubAgg)(
            equalTo(
              Sampler(
                name = "aggregation2",
                shardSizeValue = 100,
                subAggregations = Seq(
                  Terms(
                    name = "keywords",
                    field = "text",
                    order = Chunk.empty,
                    subAggregations = Chunk.empty,
                    size = None
                  )
                )
              )
            )
          ) &&
          assert(aggWithSubAggsAndMaxDocumentsPerShardParam)(
            equalTo(
              Sampler(
                name = "aggregation2",
                shardSizeValue = 50,
                subAggregations = Seq(
                  Avg(name = "avg_length", field = "length", missing = None),
                  Terms(
                    name = "keywords",
                    field = "text",
                    order = Chunk.empty,
                    subAggregations = Chunk.empty,
                    size = None
                  )
                )
              )
            )
          )
        },
        test("stats") {
          val aggregation            = statsAggregation("aggregation", "testField")
          val aggregationTs          = statsAggregation("aggregation", TestDocument.intField)
          val aggregationTsRaw       = statsAggregation("aggregation", TestDocument.intField.raw)
          val aggregationWithMissing = statsAggregation("aggregation", TestDocument.intField).missing(20.0)

          assert(aggregation)(equalTo(Stats(name = "aggregation", field = "testField", missing = None))) &&
          assert(aggregationTs)(equalTo(Stats(name = "aggregation", field = "intField", missing = None))) &&
          assert(aggregationTsRaw)(equalTo(Stats(name = "aggregation", field = "intField.raw", missing = None))) &&
          assert(aggregationWithMissing)(
            equalTo(Stats(name = "aggregation", field = "intField", missing = Some(20.0)))
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
          val aggregation3 = termsAggregation(name = "first", field = TestDocument.stringField).withSubAgg(
            percentilesAggregation("second", field = TestDocument.intField).percents(75, 90, 99)
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
          ) &&
          assert(aggregation3)(
            equalTo(
              Terms(
                name = "first",
                field = "stringField",
                order = Chunk.empty,
                subAggregations = Chunk(
                  Percentiles(
                    name = "second",
                    field = "intField",
                    percents = Chunk(75, 90, 99),
                    missing = None
                  )
                ),
                size = None
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
          val aggregation           = termsAggregation("aggregation", "testField")
          val aggregationTs         = termsAggregation("aggregation", TestSubDocument.stringField)
          val aggregationTsRaw      = termsAggregation("aggregation", TestSubDocument.stringField.raw)
          val aggregationWithOrders =
            termsAggregation("aggregation", TestSubDocument.stringField).orderByKeyDesc
              .orderBy(AggregationOrder("test", Desc))
              .orderByCountAsc
          val aggregationWithSize      = termsAggregation("aggregation", TestSubDocument.stringField).size(10)
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
        },
        test("valueCount") {
          val aggregation      = valueCountAggregation("aggregation", "testField")
          val aggregationTs    = valueCountAggregation("aggregation", TestDocument.stringField)
          val aggregationTsRaw = valueCountAggregation("aggregation", TestDocument.stringField.raw)

          assert(aggregation)(equalTo(ValueCount(name = "aggregation", field = "testField"))) &&
          assert(aggregationTs)(equalTo(ValueCount(name = "aggregation", field = "stringField"))) &&
          assert(aggregationTsRaw)(equalTo(ValueCount(name = "aggregation", field = "stringField.raw")))
        },
        test("weightedAvg") {
          val aggregation      = weightedAvgAggregation("aggregation", "valueField", "weightField")
          val aggregationTs    = weightedAvgAggregation("aggregation", TestDocument.stringField, TestDocument.intField)
          val aggregationTsRaw =
            weightedAvgAggregation("aggregation", TestDocument.stringField.raw, TestDocument.intField.raw)
          val aggregationWithValueMissing =
            weightedAvgAggregation("aggregation", TestDocument.stringField, TestDocument.intField).valueMissing(2.0)
          val aggregationWithWeightMissing =
            weightedAvgAggregation("aggregation", TestDocument.stringField, TestDocument.intField).weightMissing(3.0)
          val aggregationWithValueAndWeightMissing = weightedAvgAggregation(
            "aggregation",
            TestDocument.stringField,
            TestDocument.intField
          ).valueMissing(4.0).weightMissing(5.0)

          assert(aggregation)(
            equalTo(
              WeightedAvg(
                name = "aggregation",
                valueField = "valueField",
                valueMissing = None,
                weightField = "weightField",
                weightMissing = None
              )
            )
          ) &&
          assert(aggregationTs)(
            equalTo(
              WeightedAvg(
                name = "aggregation",
                valueField = "stringField",
                valueMissing = None,
                weightField = "intField",
                weightMissing = None
              )
            )
          ) &&
          assert(aggregationTsRaw)(
            equalTo(
              WeightedAvg(
                name = "aggregation",
                valueField = "stringField.raw",
                valueMissing = None,
                weightField = "intField.raw",
                weightMissing = None
              )
            )
          ) &&
          assert(aggregationWithValueMissing)(
            equalTo(
              WeightedAvg(
                name = "aggregation",
                valueField = "stringField",
                valueMissing = Some(2.0),
                weightField = "intField",
                weightMissing = None
              )
            )
          ) &&
          assert(aggregationWithWeightMissing)(
            equalTo(
              WeightedAvg(
                name = "aggregation",
                valueField = "stringField",
                valueMissing = None,
                weightField = "intField",
                weightMissing = Some(3.0)
              )
            )
          ) &&
          assert(aggregationWithValueAndWeightMissing)(
            equalTo(
              WeightedAvg(
                name = "aggregation",
                valueField = "stringField",
                valueMissing = Some(4.0),
                weightField = "intField",
                weightMissing = Some(5.0)
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
          val aggregationWithFrom      = bucketSortAggregation("aggregation").from(5)
          val aggregationWithSize      = bucketSortAggregation("aggregation").size(5)
          val aggregationWithSort      = bucketSortAggregation("aggregation").sort(ElasticSort.sortBy("aggregation2"))
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
        test("extendedStats") {
          val aggregation                    = extendedStatsAggregation("aggregation", "testField")
          val aggregationTs                  = extendedStatsAggregation("aggregation", TestSubDocument.intField)
          val aggregationWithMissing         = extendedStatsAggregation("aggregation", TestSubDocument.intField).missing(20.0)
          val aggregationWithSigma           = extendedStatsAggregation("aggregation", TestSubDocument.intField).sigma(3.0)
          val aggregationWithMissingAndSigma =
            extendedStatsAggregation("aggregation", TestSubDocument.intField).missing(20.0).sigma(3.0)

          val expected =
            """
              |{
              |  "aggregation": {
              |    "extended_stats": {
              |      "field": "testField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedTs =
            """
              |{
              |  "aggregation": {
              |    "extended_stats": {
              |      "field": "intField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMissing =
            """
              |{
              |  "aggregation": {
              |    "extended_stats": {
              |      "field": "intField",
              |      "missing": 20.0
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithSigma =
            """
              |{
              |  "aggregation": {
              |    "extended_stats": {
              |      "field": "intField",
              |       "sigma": 3.0
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMissingAndSigma =
            """
              |{
              |  "aggregation": {
              |    "extended_stats": {
              |      "field": "intField",
              |       "missing": 20.0,
              |       "sigma": 3.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithMissing.toJson)(equalTo(expectedWithMissing.toJson)) &&
          assert(aggregationWithSigma.toJson)(equalTo(expectedWithSigma.toJson)) &&
          assert(aggregationWithMissingAndSigma.toJson)(equalTo(expectedWithMissingAndSigma.toJson))
        },
        test("filter") {
          val query                         = term(TestDocument.stringField, "test")
          val aggregation                   = filterAggregation("aggregation", query)
          val aggregationWithSubAggregation =
            filterAggregation("aggregation", query).withSubAgg(minAggregation("subAggregation", TestDocument.intField))
          val aggregationWithMultipleSubAggregations = filterAggregation("aggregation", query)
            .withSubAgg(maxAggregation("maxSubAggregation", TestDocument.intField))
            .withSubAgg(minAggregation("minSubAggregation", TestDocument.doubleField))

          val expected =
            """
              |{
              |  "aggregation": {
              |    "filter": {
              |      "term": {
              |        "stringField": {
              |          "value": "test"
              |         }
              |       }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithSubAggregation =
            """
              |{
              |  "aggregation": {
              |    "filter": {
              |      "term": {
              |        "stringField": {
              |          "value": "test"
              |         }
              |       }
              |    },
              |     "aggs": {
              |       "subAggregation": {
              |         "min": {
              |           "field": "intField"
              |         }
              |       }
              |     }
              |  }
              |}
              |""".stripMargin

          val expectedWithMultipleSubAggregations =
            """
              |{
              |  "aggregation": {
              |    "filter": {
              |      "term": {
              |        "stringField": {
              |          "value": "test"
              |         }
              |       }
              |    },
              |     "aggs": {
              |       "maxSubAggregation": {
              |         "max": {
              |           "field": "intField"
              |         }
              |       },
              |       "minSubAggregation": {
              |         "min": {
              |           "field": "doubleField"
              |         }
              |       }
              |     }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationWithSubAggregation.toJson)(equalTo(expectedWithSubAggregation.toJson)) &&
          assert(aggregationWithMultipleSubAggregations.toJson)(equalTo(expectedWithMultipleSubAggregations.toJson))
        },
        test("ipRange") {
          val aggFromTo = IpRange(
            name = "ip_range_agg",
            field = "ip",
            ranges = NonEmptyChunk(
              IpRangeBound(to = Some("10.0.0.5")),
              IpRangeBound(from = Some("10.0.0.5"))
            ),
            keyed = None,
            subAggregations = None
          )

          val expectedJsonFromTo =
            """
              |{
              |  "ip_range_agg": {
              |    "ip_range": {
              |      "field": "ip",
              |      "ranges": [
              |        {
              |          "to": "10.0.0.5"
              |        },
              |        {
              |          "from": "10.0.0.5"
              |        }
              |      ]
              |    }
              |  }
              |}
              |""".stripMargin

          val aggMaskKeyed = IpRange(
            name = "ip_range_agg",
            field = "ip",
            ranges = NonEmptyChunk(
              IpRangeBound(mask = Some("10.0.0.0/25")),
              IpRangeBound(mask = Some("10.0.0.127/25"))
            ),
            keyed = Some(true),
            subAggregations = None
          )

          val expectedJsonMaskKeyed =
            """
              |{
              |  "ip_range_agg": {
              |    "ip_range": {
              |      "field": "ip",
              |      "ranges": [
              |        {
              |          "mask": "10.0.0.0/25"
              |        },
              |        {
              |          "mask": "10.0.0.127/25"
              |        }
              |      ],
              |      "keyed": true
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggFromTo.toJson)(equalTo(expectedJsonFromTo.toJson)) &&
          assert(aggMaskKeyed.toJson)(equalTo(expectedJsonMaskKeyed.toJson))
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
        test("percentileRanks") {
          val aggregation            = percentileRanksAggregation("aggregation", "testField", 5, 6)
          val aggregationTs          = percentileRanksAggregation("aggregation", TestSubDocument.intField, 5, 6)
          val aggregationWithMissing =
            percentileRanksAggregation("aggregation", TestSubDocument.intField, 5, 6).missing(20.0)

          val expected =
            """
              |{
              |  "aggregation": {
              |    "percentile_ranks": {
              |      "field": "testField",
              |      "values": [5, 6]
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedTs =
            """
              |{
              |  "aggregation": {
              |    "percentile_ranks": {
              |      "field": "intField",
              |      "values": [5, 6]
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMissing =
            """
              |{
              |  "aggregation": {
              |    "percentile_ranks": {
              |      "field": "intField",
              |      "values": [5, 6],
              |      "missing": 20.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithMissing.toJson)(equalTo(expectedWithMissing.toJson))
        },
        test("percentiles") {
          val aggregation             = percentilesAggregation("aggregation", "testField")
          val aggregationTs           = percentilesAggregation("aggregation", TestDocument.intField)
          val aggregationWithPercents =
            percentilesAggregation("aggregation", TestDocument.intField).percents(75, 90, 99)
          val aggregationWithMissing   = percentilesAggregation("aggregation", TestDocument.intField).missing(20.0)
          val aggregationWithAllParams =
            percentilesAggregation("aggregation", TestDocument.intField).percents(75, 90, 99).missing(20.0)

          val expected =
            """
              |{
              |  "aggregation": {
              |    "percentiles": {
              |      "field": "testField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedTs =
            """
              |{
              |  "aggregation": {
              |    "percentiles": {
              |      "field": "intField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithPercents =
            """
              |{
              |  "aggregation": {
              |    "percentiles": {
              |      "field": "intField",
              |      "percents": [75.0, 90.0, 99.0]
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMissing =
            """
              |{
              |  "aggregation": {
              |    "percentiles": {
              |      "field": "intField",
              |      "missing": 20.0
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithAllParams =
            """
              |{
              |  "aggregation": {
              |    "percentiles": {
              |      "field": "intField",
              |      "percents": [75.0, 90.0, 99.0],
              |      "missing": 20.0
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithPercents.toJson)(equalTo(expectedWithPercents.toJson)) &&
          assert(aggregationWithMissing.toJson)(equalTo(expectedWithMissing.toJson)) &&
          assert(aggregationWithAllParams.toJson)(equalTo(expectedWithAllParams.toJson))
        },
        test("sampler") {
          val aggWithSubAgg = ElasticAggregation.samplerAggregation(
            "sample_with_sub_agg",
            ElasticAggregation.termsAggregation("keywords", "text")
          )
          val aggWithSubAggsAndMaxDocumentsPerShardParam = ElasticAggregation
            .samplerAggregation("sample_with_multiple_aggs", ElasticAggregation.avgAggregation("avg_length", "length"))
            .maxDocumentsPerShard(50)
            .withSubAgg(ElasticAggregation.termsAggregation("keywords", "text"))

          val expectedWithSubAgg =
            """
              |{
              |  "sample_with_sub_agg": {
              |    "sampler": {
              |      "shard_size": 100
              |    },
              |    "aggs": {
              |      "keywords": {
              |        "terms": {
              |          "field": "text"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMultipleSubAggs =
            """
              |{
              |  "sample_with_multiple_aggs": {
              |    "sampler": {
              |      "shard_size": 50
              |    },
              |    "aggs": {
              |      "avg_length": {
              |        "avg": {
              |          "field": "length"
              |        }
              |      },
              |      "keywords": {
              |        "terms": {
              |          "field": "text"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggWithSubAgg.toJson)(equalTo(expectedWithSubAgg.toJson)) &&
          assert(aggWithSubAggsAndMaxDocumentsPerShardParam.toJson)(equalTo(expectedWithMultipleSubAggs.toJson))
        },
        test("stats") {
          val aggregation            = statsAggregation("aggregation", "testField")
          val aggregationTs          = statsAggregation("aggregation", TestDocument.intField)
          val aggregationWithMissing = statsAggregation("aggregation", TestDocument.intField).missing(20.0)

          val expected =
            """
              |{
              |  "aggregation": {
              |    "stats": {
              |      "field": "testField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedTs =
            """
              |{
              |  "aggregation": {
              |    "stats": {
              |      "field": "intField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMissing =
            """
              |{
              |  "aggregation": {
              |    "stats": {
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
        test("subAggregation") {
          val aggregation =
            termsAggregation("first", TestDocument.stringField)
              .withSubAgg(maxAggregation("second", TestSubDocument.intField))

          val aggregationWithPercentilesAgg = termsAggregation("first", TestDocument.stringField)
            .withSubAgg(percentilesAggregation("second", TestSubDocument.intField).percents(75, 90, 99))

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

          val expectedWithPercentilesAgg =
            """
              |{
              |  "first": {
              |    "terms": {
              |      "field": "stringField"
              |    },
              |    "aggs": {
              |      "second": {
              |        "percentiles": {
              |          "field": "intField",
              |          "percents" : [75.0, 90.0, 99.0]
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationWithPercentilesAgg.toJson)(equalTo(expectedWithPercentilesAgg.toJson))
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
          val aggregation              = termsAggregation("aggregation", "testField")
          val aggregationTs            = termsAggregation("aggregation", TestDocument.stringField)
          val aggregationWithOrder     = termsAggregation("aggregation", TestDocument.stringField).orderByKeyDesc
          val aggregationWithSize      = termsAggregation("aggregation", TestDocument.stringField).size(10)
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
        },
        test("valueCount") {
          val aggregation   = valueCountAggregation("aggregation", "testField")
          val aggregationTs = valueCountAggregation("aggregation", TestDocument.stringField)

          val expected =
            """
              |{
              |  "aggregation": {
              |    "value_count": {
              |      "field": "testField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedTs =
            """
              |{
              |  "aggregation": {
              |    "value_count": {
              |      "field": "stringField"
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson))
        },
        test("weightedAvg") {
          val aggregation                 = weightedAvgAggregation("aggregation", "valueField", "weightField")
          val aggregationTs               = weightedAvgAggregation("aggregation", TestDocument.stringField, TestDocument.stringField)
          val aggregationWithValueMissing =
            weightedAvgAggregation("aggregation", TestDocument.stringField, TestDocument.intField).valueMissing(2.0)
          val aggregationWithWeightMissing =
            weightedAvgAggregation("aggregation", TestDocument.stringField, TestDocument.intField).weightMissing(3.0)
          val aggregationWithValueAndWeightMissing = weightedAvgAggregation(
            "aggregation",
            TestDocument.stringField,
            TestDocument.intField
          ).valueMissing(4.0).weightMissing(5.0)

          val expected =
            """
              |{
              |  "aggregation": {
              |    "weighted_avg": {
              |      "value": {
              |       "field": "valueField"
              |      },
              |      "weight": {
              |       "field": "weightField"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedTs =
            """
              |{
              |  "aggregation": {
              |    "weighted_avg": {
              |      "value": {
              |       "field": "stringField"
              |      },
              |      "weight": {
              |       "field": "stringField"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithValueMissing =
            """
              |{
              |  "aggregation": {
              |    "weighted_avg": {
              |      "value": {
              |       "field": "stringField",
              |       "missing": 2.0
              |      },
              |      "weight": {
              |       "field": "intField"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithWeightMissing =
            """
              |{
              |  "aggregation": {
              |    "weighted_avg": {
              |      "value": {
              |       "field": "stringField"
              |      },
              |      "weight": {
              |       "field": "intField",
              |       "missing": 3.0
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithValueAndWeightMissing =
            """
              |{
              |  "aggregation": {
              |    "weighted_avg": {
              |      "value": {
              |       "field": "stringField",
              |       "missing": 4.0
              |      },
              |      "weight": {
              |       "field": "intField",
              |       "missing": 5.0
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithValueMissing.toJson)(equalTo(expectedWithValueMissing.toJson)) &&
          assert(aggregationWithWeightMissing.toJson)(equalTo(expectedWithWeightMissing.toJson)) &&
          assert(aggregationWithValueAndWeightMissing.toJson)(equalTo(expectedWithValueAndWeightMissing.toJson))
        }
      )
    )
}
