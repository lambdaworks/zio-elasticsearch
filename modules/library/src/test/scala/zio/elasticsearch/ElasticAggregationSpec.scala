package zio.elasticsearch

import zio.elasticsearch.ElasticAggregation._
import zio.elasticsearch.ElasticQuery.term
import zio.elasticsearch.aggregation._
import zio.elasticsearch.domain.{TestDocument, TestSubDocument}
import zio.elasticsearch.query.sort.SortOrder.{Asc, Desc}
import zio.elasticsearch.query.sort.{SortByFieldOptions, SortOrder}
import zio.elasticsearch.script.Script
import zio.elasticsearch.utils._
import zio.json.ast.Json.{Num, Obj, Str}
import zio.test.Assertion.{equalTo, isLeft, isRight}
import zio.test._
import zio.{Chunk, NonEmptyChunk}

import java.time.LocalDate

object ElasticAggregationSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment, Any] =
    suite("ElasticAggregation")(
      suite("constructing")(
        test("avg") {
          val aggregation              = avgAggregation("aggregation", "testField")
          val aggregationTs            = avgAggregation("aggregation", TestSubDocument.intField)
          val aggregationTsRaw         = avgAggregation("aggregation", TestSubDocument.intField.raw)
          val aggregationWithMissing   = avgAggregation("aggregation", TestSubDocument.intField).missing(20.0)
          val aggregationTsDate        = avgAggregation("aggregation", TestDocument.dateField)
          val aggregationWithAllParams = avgAggregation("aggregation", TestDocument.intField)
            .format("0.00")
            .meta("unit", "ms")
            .missing(20)
            .script(Script("_value * 2"))
          val aggregationWithReplacedMeta =
            avgAggregation("aggregation", TestDocument.intField).meta("a", 1).meta("b", "two").meta("a", 3)

          assert(aggregation)(
            equalTo(
              Avg(
                name = "aggregation",
                field = "testField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationTs)(
            equalTo(
              Avg(
                name = "aggregation",
                field = "intField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationTsRaw)(
            equalTo(
              Avg(
                name = "aggregation",
                field = "intField.raw",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationWithMissing)(
            equalTo(
              Avg(
                name = "aggregation",
                field = "intField",
                format = None,
                meta = Chunk.empty,
                missing = Some(Num(20.0)),
                script = None
              )
            )
          ) &&
          assert(aggregationTsDate)(
            equalTo(
              Avg(
                name = "aggregation",
                field = "dateField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationWithAllParams)(
            equalTo(
              Avg(
                name = "aggregation",
                field = "intField",
                format = Some("0.00"),
                meta = Chunk("unit" -> Str("ms")),
                missing = Some(Num(20)),
                script = Some(Script("_value * 2"))
              )
            )
          ) &&
          assert(aggregationWithReplacedMeta)(
            equalTo(
              Avg(
                name = "aggregation",
                field = "intField",
                format = None,
                meta = Chunk("b" -> Str("two"), "a" -> Num(3)),
                missing = None,
                script = None
              )
            )
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
          val aggregationWithAllParams = bucketSelectorAggregation(
            name = "aggregation",
            script = Script("params.agg1 > 10"),
            bucketsPath = Map("agg1" -> "aggregation1")
          ).gapPolicy(GapPolicy.InsertZeros).meta("unit", "ms")

          assert(aggregation1)(
            equalTo(
              BucketSelector(
                name = "aggregation",
                script = Script(source = "params.agg1 > 10", params = Map.empty, lang = None),
                bucketsPath = Map("agg1" -> "aggregation1"),
                gapPolicy = None,
                meta = Chunk.empty
              )
            )
          ) &&
          assert(aggregation2)(
            equalTo(
              BucketSelector(
                name = "aggregation",
                script = Script(source = "params.agg1 + params.agg2 > 10", params = Map.empty, lang = None),
                bucketsPath = Map("agg1" -> "aggregation1", "agg2" -> "aggregation2"),
                gapPolicy = None,
                meta = Chunk.empty
              )
            )
          ) &&
          assert(aggregationWithAllParams)(
            equalTo(
              BucketSelector(
                name = "aggregation",
                script = Script("params.agg1 > 10"),
                bucketsPath = Map("agg1" -> "aggregation1"),
                gapPolicy = Some(GapPolicy.InsertZeros),
                meta = Chunk("unit" -> Str("ms"))
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
          val aggregationWithGapPolicyAndMeta =
            bucketSortAggregation("aggregation").gapPolicy(GapPolicy.Skip).meta("unit", "ms")

          assert(aggregationWithFrom)(
            equalTo(
              BucketSort(
                name = "aggregation",
                from = Some(5),
                gapPolicy = None,
                meta = Chunk.empty,
                size = None,
                sortBy = Chunk.empty
              )
            )
          ) &&
          assert(aggregationWithSize)(
            equalTo(
              BucketSort(
                name = "aggregation",
                from = None,
                gapPolicy = None,
                meta = Chunk.empty,
                size = Some(5),
                sortBy = Chunk.empty
              )
            )
          ) &&
          assert(aggregationWithSort)(
            equalTo(
              BucketSort(
                name = "aggregation",
                from = None,
                gapPolicy = None,
                meta = Chunk.empty,
                size = None,
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
                )
              )
            )
          ) &&
          assert(aggregationWithAllParams)(
            equalTo(
              BucketSort(
                name = "aggregation",
                from = Some(5),
                gapPolicy = None,
                meta = Chunk.empty,
                size = Some(7),
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
                )
              )
            )
          ) &&
          assert(aggregationWithGapPolicyAndMeta)(
            equalTo(
              BucketSort(
                name = "aggregation",
                from = None,
                gapPolicy = Some(GapPolicy.Skip),
                meta = Chunk("unit" -> Str("ms")),
                size = None,
                sortBy = Chunk.empty
              )
            )
          )
        },
        test("cardinality") {
          val aggregation              = cardinalityAggregation("aggregation", "testField")
          val aggregationTs            = cardinalityAggregation("aggregation", TestSubDocument.intField)
          val aggregationTsRaw         = cardinalityAggregation("aggregation", TestSubDocument.intField.raw)
          val aggregationWithMissing   = cardinalityAggregation("aggregation", TestSubDocument.intField).missing(20)
          val aggregationWithAllParams = cardinalityAggregation("aggregation", TestDocument.stringField)
            .meta("unit", "users")
            .missing("N/A")
            .script(Script("_value.toLowerCase()"))

          assert(aggregation)(
            equalTo(
              Cardinality(name = "aggregation", field = "testField", meta = Chunk.empty, missing = None, script = None)
            )
          ) &&
          assert(aggregationTs)(
            equalTo(
              Cardinality(name = "aggregation", field = "intField", meta = Chunk.empty, missing = None, script = None)
            )
          ) &&
          assert(aggregationTsRaw)(
            equalTo(
              Cardinality(
                name = "aggregation",
                field = "intField.raw",
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationWithMissing)(
            equalTo(
              Cardinality(
                name = "aggregation",
                field = "intField",
                meta = Chunk.empty,
                missing = Some(Num(20)),
                script = None
              )
            )
          ) &&
          assert(aggregationWithAllParams)(
            equalTo(
              Cardinality(
                name = "aggregation",
                field = "stringField",
                meta = Chunk("unit" -> Str("users")),
                missing = Some(Str("N/A")),
                script = Some(Script("_value.toLowerCase()"))
              )
            )
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
          val aggregationWithAllParams = extendedStatsAggregation("aggregation", TestDocument.intField)
            .format("0.00")
            .meta("unit", "ms")
            .missing(20)
            .script(Script("_value * 2"))
            .sigma(3.0)

          assert(aggregation)(
            equalTo(
              ExtendedStats(
                name = "aggregation",
                field = "testField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None,
                sigma = None
              )
            )
          ) && assert(aggregationTs)(
            equalTo(
              ExtendedStats(
                name = "aggregation",
                field = "intField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None,
                sigma = None
              )
            )
          ) && assert(aggregationTsRaw)(
            equalTo(
              ExtendedStats(
                name = "aggregation",
                field = "intField.raw",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None,
                sigma = None
              )
            )
          ) && assert(aggregationWithMissing)(
            equalTo(
              ExtendedStats(
                name = "aggregation",
                field = "intField",
                format = None,
                meta = Chunk.empty,
                missing = Some(Num(20.0)),
                script = None,
                sigma = None
              )
            )
          ) && assert(aggregationWithSigma)(
            equalTo(
              ExtendedStats(
                name = "aggregation",
                field = "intField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None,
                sigma = Some(3.0)
              )
            )
          ) && assert(aggregationWithMissingAndSigma)(
            equalTo(
              ExtendedStats(
                name = "aggregation",
                field = "intField",
                format = None,
                meta = Chunk.empty,
                missing = Some(Num(20.0)),
                script = None,
                sigma = Some(3.0)
              )
            )
          ) &&
          assert(aggregationWithAllParams)(
            equalTo(
              ExtendedStats(
                name = "aggregation",
                field = "intField",
                format = Some("0.00"),
                meta = Chunk("unit" -> Str("ms")),
                missing = Some(Num(20)),
                script = Some(Script("_value * 2")),
                sigma = Some(3.0)
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
          val aggregationWithMeta = filterAggregation("aggregation", query).meta("unit", "ms")

          assert(aggregation)(
            equalTo(
              Filter(name = "aggregation", query = query, meta = Chunk.empty, subAggregations = Chunk.empty)
            )
          ) &&
          assert(aggregationWithSubAggregation)(
            equalTo(
              Filter(
                name = "aggregation",
                query = query,
                meta = Chunk.empty,
                subAggregations = Chunk(minAggregation("subAggregation", TestDocument.intField))
              )
            )
          ) &&
          assert(aggregationWithMultipleSubAggregations)(
            equalTo(
              Filter(
                name = "aggregation",
                query = query,
                meta = Chunk.empty,
                subAggregations = Chunk(
                  maxAggregation("maxSubAggregation", TestDocument.intField),
                  minAggregation("minSubAggregation", TestDocument.doubleField)
                )
              )
            )
          ) &&
          assert(aggregationWithMeta)(
            equalTo(
              Filter(
                name = "aggregation",
                query = query,
                meta = Chunk("unit" -> Str("ms")),
                subAggregations = Chunk.empty
              )
            )
          )
        },
        test("ipRange") {
          val aggregation =
            ipRangeAggregation(
              name = "aggregation",
              field = "ipField",
              range = IpRangeBound().to("10.0.0.5"),
              ranges = IpRangeBound().from("10.0.0.5")
            )
          val aggregationTs =
            ipRangeAggregation(
              name = "aggregation",
              field = TestDocument.stringField,
              range = IpRangeBound().to("10.0.0.5"),
              ranges = IpRangeBound().from("10.0.0.5")
            )
          val aggregationWithKeyed =
            ipRangeAggregation(
              name = "aggregation",
              field = "ipField",
              range = IpRangeBound().mask("10.0.0.0/25").key("low")
            ).keyed
          val aggregationWithSubAggregation =
            ipRangeAggregation(
              name = "aggregation",
              field = "ipField",
              range = IpRangeBound().mask("10.0.0.0/25")
            ).withSubAgg(maxAggregation("subAggregation", "intField"))
          val aggregationWithMeta =
            ipRangeAggregation(name = "aggregation", field = "ipField", range = IpRangeBound().mask("10.0.0.0/25"))
              .meta("unit", "ip")

          assert(aggregation)(
            equalTo(
              IpRange(
                name = "aggregation",
                field = "ipField",
                isKeyed = false,
                meta = Chunk.empty,
                ranges = NonEmptyChunk(IpRangeBound().to("10.0.0.5"), IpRangeBound().from("10.0.0.5")),
                subAggregations = Chunk.empty
              )
            )
          ) &&
          assert(aggregationTs)(
            equalTo(
              IpRange(
                name = "aggregation",
                field = "stringField",
                isKeyed = false,
                meta = Chunk.empty,
                ranges = NonEmptyChunk(IpRangeBound().to("10.0.0.5"), IpRangeBound().from("10.0.0.5")),
                subAggregations = Chunk.empty
              )
            )
          ) &&
          assert(aggregationWithKeyed)(
            equalTo(
              IpRange(
                name = "aggregation",
                field = "ipField",
                isKeyed = true,
                meta = Chunk.empty,
                ranges = NonEmptyChunk(IpRangeBound().mask("10.0.0.0/25").key("low")),
                subAggregations = Chunk.empty
              )
            )
          ) &&
          assert(aggregationWithSubAggregation)(
            equalTo(
              IpRange(
                name = "aggregation",
                field = "ipField",
                isKeyed = false,
                meta = Chunk.empty,
                ranges = NonEmptyChunk(IpRangeBound().mask("10.0.0.0/25")),
                subAggregations = Chunk(
                  Max(
                    name = "subAggregation",
                    field = "intField",
                    format = None,
                    meta = Chunk.empty,
                    missing = None,
                    script = None
                  )
                )
              )
            )
          ) &&
          assert(aggregationWithMeta)(
            equalTo(
              IpRange(
                name = "aggregation",
                field = "ipField",
                isKeyed = false,
                meta = Chunk("unit" -> Str("ip")),
                ranges = NonEmptyChunk(IpRangeBound(mask = Some("10.0.0.0/25"))),
                subAggregations = Chunk.empty
              )
            )
          )
        },
        test("max") {
          val aggregation              = maxAggregation("aggregation", "testField")
          val aggregationTs            = maxAggregation("aggregation", TestSubDocument.intField)
          val aggregationTsRaw         = maxAggregation("aggregation", TestSubDocument.intField.raw)
          val aggregationWithMissing   = maxAggregation("aggregation", TestSubDocument.intField).missing(20.0)
          val aggregationTsDate        = maxAggregation("aggregation", TestDocument.dateField)
          val aggregationWithAllParams = maxAggregation("aggregation", TestDocument.dateField)
            .format("yyyy-MM-dd")
            .meta("unit", "day")
            .missing(LocalDate.of(2024, 1, 1))
            .script(Script("_value"))

          assert(aggregation)(
            equalTo(
              Max(
                name = "aggregation",
                field = "testField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationTs)(
            equalTo(
              Max(
                name = "aggregation",
                field = "intField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationTsRaw)(
            equalTo(
              Max(
                name = "aggregation",
                field = "intField.raw",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationWithMissing)(
            equalTo(
              Max(
                name = "aggregation",
                field = "intField",
                format = None,
                meta = Chunk.empty,
                missing = Some(Num(20.0)),
                script = None
              )
            )
          ) &&
          assert(aggregationTsDate)(
            equalTo(
              Max(
                name = "aggregation",
                field = "dateField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationWithAllParams)(
            equalTo(
              Max(
                name = "aggregation",
                field = "dateField",
                format = Some("yyyy-MM-dd"),
                meta = Chunk("unit" -> Str("day")),
                missing = Some(Str("2024-01-01")),
                script = Some(Script("_value"))
              )
            )
          )
        },
        test("min") {
          val aggregation              = minAggregation("aggregation", "testField")
          val aggregationTs            = minAggregation("aggregation", TestSubDocument.intField)
          val aggregationTsRaw         = minAggregation("aggregation", TestSubDocument.intField.raw)
          val aggregationWithMissing   = minAggregation("aggregation", TestSubDocument.intField).missing(20.0)
          val aggregationTsDate        = minAggregation("aggregation", TestDocument.dateField)
          val aggregationWithAllParams = minAggregation("aggregation", TestDocument.dateField)
            .format("yyyy-MM-dd")
            .meta("unit", "day")
            .missing(LocalDate.of(2024, 1, 1))
            .script(Script("_value"))

          assert(aggregation)(
            equalTo(
              Min(
                name = "aggregation",
                field = "testField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationTs)(
            equalTo(
              Min(
                name = "aggregation",
                field = "intField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationTsRaw)(
            equalTo(
              Min(
                name = "aggregation",
                field = "intField.raw",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationWithMissing)(
            equalTo(
              Min(
                name = "aggregation",
                field = "intField",
                format = None,
                meta = Chunk.empty,
                missing = Some(Num(20.0)),
                script = None
              )
            )
          ) &&
          assert(aggregationTsDate)(
            equalTo(
              Min(
                name = "aggregation",
                field = "dateField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationWithAllParams)(
            equalTo(
              Min(
                name = "aggregation",
                field = "dateField",
                format = Some("yyyy-MM-dd"),
                meta = Chunk("unit" -> Str("day")),
                missing = Some(Str("2024-01-01")),
                script = Some(Script("_value"))
              )
            )
          )
        },
        test("missing") {
          val aggregation              = missingAggregation("aggregation", "testField")
          val aggregationTs            = missingAggregation("aggregation", TestSubDocument.stringField)
          val aggregationTsRaw         = missingAggregation("aggregation", TestSubDocument.stringField.raw)
          val aggregationTsInt         = missingAggregation("aggregation", TestDocument.intField)
          val aggregationWithAllParams = missingAggregation("aggregation", "testField")
            .meta("unit", "docs")
            .withSubAgg(maxAggregation("maxAggregation", "intField"))
            .withSubAgg(minAggregation("minAggregation", "intField"))

          assert(aggregation)(
            equalTo(
              Missing(name = "aggregation", field = "testField", meta = Chunk.empty, subAggregations = Chunk.empty)
            )
          ) &&
          assert(aggregationTs)(
            equalTo(
              Missing(name = "aggregation", field = "stringField", meta = Chunk.empty, subAggregations = Chunk.empty)
            )
          ) &&
          assert(aggregationTsRaw)(
            equalTo(
              Missing(
                name = "aggregation",
                field = "stringField.raw",
                meta = Chunk.empty,
                subAggregations = Chunk.empty
              )
            )
          ) &&
          assert(aggregationTsInt)(
            equalTo(
              Missing(name = "aggregation", field = "intField", meta = Chunk.empty, subAggregations = Chunk.empty)
            )
          ) &&
          assert(aggregationWithAllParams)(
            equalTo(
              Missing(
                name = "aggregation",
                field = "testField",
                meta = Chunk("unit" -> Str("docs")),
                subAggregations =
                  Chunk(maxAggregation("maxAggregation", "intField"), minAggregation("minAggregation", "intField"))
              )
            )
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
          val aggregationWithNestedMultiple =
            maxAggregation("first", "testField")
              .withAgg(minAggregation("second", "testField").withAgg(avgAggregation("third", "testField")))
              .withAgg(sumAggregation("fourth", "testField"))

          assert(aggregation)(
            equalTo(
              Multiple(
                Chunk(
                  Terms(
                    name = "first",
                    field = "stringField",
                    meta = Chunk.empty,
                    minDocCount = None,
                    order = Chunk(AggregationOrder(value = "_key", order = Desc)),
                    size = None,
                    subAggregations = Chunk.empty
                  ),
                  Max(
                    name = "second",
                    field = "testField",
                    format = None,
                    meta = Chunk.empty,
                    missing = Some(Num(20)),
                    script = None
                  ),
                  Cardinality(name = "third", field = "intField", meta = Chunk.empty, missing = None, script = None)
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
                    meta = Chunk.empty,
                    minDocCount = None,
                    order = Chunk.empty,
                    size = None,
                    subAggregations = Chunk(
                      Max(
                        name = "second",
                        field = "intField.raw",
                        format = None,
                        meta = Chunk.empty,
                        missing = None,
                        script = None
                      )
                    )
                  ),
                  Terms(
                    name = "third",
                    field = "stringField",
                    meta = Chunk.empty,
                    minDocCount = None,
                    order = Chunk.empty,
                    size = None,
                    subAggregations = Chunk.empty
                  )
                )
              )
            )
          ) &&
          assert(aggregationWithNestedMultiple)(
            equalTo(
              Multiple(
                Chunk(
                  maxAggregation("first", "testField"),
                  minAggregation("second", "testField"),
                  avgAggregation("third", "testField"),
                  sumAggregation("fourth", "testField")
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
          val aggregationWithMeta =
            percentileRanksAggregation("aggregation", TestSubDocument.intField, 5, 6).meta("unit", "ms")

          assert(aggregation)(
            equalTo(
              PercentileRanks(
                name = "aggregation",
                field = "testField",
                meta = Chunk.empty,
                missing = None,
                values = Chunk(5.0, 6.0)
              )
            )
          ) &&
          assert(aggregationTs)(
            equalTo(
              PercentileRanks(
                name = "aggregation",
                field = "intField",
                meta = Chunk.empty,
                missing = None,
                values = Chunk(5.0, 6.0)
              )
            )
          ) &&
          assert(aggregationTsRaw)(
            equalTo(
              PercentileRanks(
                name = "aggregation",
                field = "intField.raw",
                meta = Chunk.empty,
                missing = None,
                values = Chunk(5.0, 6.0)
              )
            )
          ) &&
          assert(aggregationWithMissing)(
            equalTo(
              PercentileRanks(
                name = "aggregation",
                field = "intField",
                meta = Chunk.empty,
                missing = Some(Num(20.0)),
                values = Chunk(5.0, 6.0)
              )
            )
          ) &&
          assert(aggregationWithMeta)(
            equalTo(
              PercentileRanks(
                name = "aggregation",
                field = "intField",
                meta = Chunk("unit" -> Str("ms")),
                missing = None,
                values = Chunk(5.0, 6.0)
              )
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
          val aggregationWithMeta = percentilesAggregation("aggregation", TestSubDocument.intField).meta("unit", "ms")

          assert(aggregation)(
            equalTo(
              Percentiles(
                name = "aggregation",
                field = "testField",
                meta = Chunk.empty,
                missing = None,
                percents = Chunk.empty
              )
            )
          ) &&
          assert(aggregationTs)(
            equalTo(
              Percentiles(
                name = "aggregation",
                field = "intField",
                meta = Chunk.empty,
                missing = None,
                percents = Chunk.empty
              )
            )
          ) &&
          assert(aggregationTsRaw)(
            equalTo(
              Percentiles(
                name = "aggregation",
                field = "intField.raw",
                meta = Chunk.empty,
                missing = None,
                percents = Chunk.empty
              )
            )
          ) &&
          assert(aggregationWithMissing)(
            equalTo(
              Percentiles(
                name = "aggregation",
                field = "intField",
                meta = Chunk.empty,
                missing = Some(Num(20.0)),
                percents = Chunk.empty
              )
            )
          ) &&
          assert(aggregationWithPercents)(
            equalTo(
              Percentiles(
                name = "aggregation",
                field = "intField",
                meta = Chunk.empty,
                missing = None,
                percents = Chunk(75, 90, 99)
              )
            )
          ) &&
          assert(aggregationWithAllParams)(
            equalTo(
              Percentiles(
                name = "aggregation",
                field = "intField",
                meta = Chunk.empty,
                missing = Some(Num(20.0)),
                percents = Chunk(75, 90, 99)
              )
            )
          ) &&
          assert(aggregationWithMeta)(
            equalTo(
              Percentiles(
                name = "aggregation",
                field = "intField",
                meta = Chunk("unit" -> Str("ms")),
                missing = None,
                percents = Chunk.empty
              )
            )
          )
        },
        test("range") {
          val aggregation =
            rangeAggregation(
              "aggregation",
              "testField",
              RangeBound().to(50),
              RangeBound().from(50).to(100),
              RangeBound().from(100)
            )
          val aggregationTs =
            rangeAggregation(
              name = "aggregation",
              field = TestDocument.intField,
              range = RangeBound().to(50),
              ranges = RangeBound().from(50)
            )
          val aggregationTsRaw =
            rangeAggregation(name = "aggregation", field = TestSubDocument.intField.raw, range = RangeBound().to(50))
          val aggregationWithKeyed =
            rangeAggregation(
              name = "aggregation",
              field = TestDocument.doubleField,
              range = RangeBound().to(50).key("low"),
              ranges = RangeBound().from(50).key("high")
            ).keyed
          val aggregationWithSubAggregation =
            rangeAggregation(name = "aggregation", field = TestDocument.intField, range = RangeBound().to(50))
              .withSubAgg(maxAggregation("subAggregation", TestDocument.intField))
          val aggregationWithMeta = rangeAggregation(
            name = "aggregation",
            field = TestDocument.doubleField,
            range = RangeBound().to(23.9),
            ranges = RangeBound().from(23.9)
          ).meta("unit", "ms")

          assert(aggregation)(
            equalTo(
              Range(
                name = "aggregation",
                field = "testField",
                isKeyed = false,
                meta = Chunk.empty,
                ranges = NonEmptyChunk(
                  RangeBound(to = Some(50.0)),
                  RangeBound(from = Some(50.0), to = Some(100.0)),
                  RangeBound(from = Some(100.0))
                ),
                subAggregations = Chunk.empty
              )
            )
          ) &&
          assert(aggregationTs)(
            equalTo(
              Range(
                name = "aggregation",
                field = "intField",
                isKeyed = false,
                meta = Chunk.empty,
                ranges = NonEmptyChunk(RangeBound(to = Some(50.0)), RangeBound(from = Some(50.0))),
                subAggregations = Chunk.empty
              )
            )
          ) &&
          assert(aggregationTsRaw)(
            equalTo(
              Range(
                name = "aggregation",
                field = "intField.raw",
                isKeyed = false,
                meta = Chunk.empty,
                ranges = NonEmptyChunk(RangeBound(to = Some(50.0))),
                subAggregations = Chunk.empty
              )
            )
          ) &&
          assert(aggregationWithKeyed)(
            equalTo(
              Range(
                name = "aggregation",
                field = "doubleField",
                isKeyed = true,
                meta = Chunk.empty,
                ranges = NonEmptyChunk(
                  RangeBound(key = Some("low"), to = Some(50.0)),
                  RangeBound(from = Some(50.0), key = Some("high"))
                ),
                subAggregations = Chunk.empty
              )
            )
          ) &&
          assert(aggregationWithSubAggregation)(
            equalTo(
              Range(
                name = "aggregation",
                field = "intField",
                isKeyed = false,
                meta = Chunk.empty,
                ranges = NonEmptyChunk(RangeBound(to = Some(50.0))),
                subAggregations = Chunk(
                  Max(
                    name = "subAggregation",
                    field = "intField",
                    format = None,
                    meta = Chunk.empty,
                    missing = None,
                    script = None
                  )
                )
              )
            )
          ) &&
          assert(aggregationWithMeta)(
            equalTo(
              Range(
                name = "aggregation",
                field = "doubleField",
                isKeyed = false,
                meta = Chunk("unit" -> Str("ms")),
                ranges = NonEmptyChunk(RangeBound(to = Some(23.9)), RangeBound(from = Some(23.9))),
                subAggregations = Chunk.empty
              )
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
          val aggWithMeta =
            samplerAggregation("aggregation", maxAggregation("maxAggregation", "intField")).meta("unit", "ms")
          val aggWithAgg = samplerAggregation("aggregation", maxAggregation("maxAggregation", "intField")).withAgg(
            minAggregation("minAggregation", "intField")
          )

          assert(aggWithSubAgg)(
            equalTo(
              Sampler(
                name = "aggregation2",
                meta = Chunk.empty,
                shardSize = 100,
                subAggregations = Chunk(
                  Terms(
                    name = "keywords",
                    field = "text",
                    meta = Chunk.empty,
                    minDocCount = None,
                    order = Chunk.empty,
                    size = None,
                    subAggregations = Chunk.empty
                  )
                )
              )
            )
          ) &&
          assert(aggWithSubAggsAndMaxDocumentsPerShardParam)(
            equalTo(
              Sampler(
                name = "aggregation2",
                meta = Chunk.empty,
                shardSize = 50,
                subAggregations = Chunk(
                  Terms(
                    name = "keywords",
                    field = "text",
                    meta = Chunk.empty,
                    minDocCount = None,
                    order = Chunk.empty,
                    size = None,
                    subAggregations = Chunk.empty
                  ),
                  Avg(
                    name = "avg_length",
                    field = "length",
                    format = None,
                    meta = Chunk.empty,
                    missing = None,
                    script = None
                  )
                )
              )
            )
          ) &&
          assert(aggWithMeta)(
            equalTo(
              Sampler(
                name = "aggregation",
                meta = Chunk("unit" -> Str("ms")),
                shardSize = 100,
                subAggregations = Chunk(maxAggregation("maxAggregation", "intField"))
              )
            )
          ) &&
          assert(aggWithAgg)(
            equalTo(
              Multiple(
                Chunk(
                  samplerAggregation("aggregation", maxAggregation("maxAggregation", "intField")),
                  minAggregation("minAggregation", "intField")
                )
              )
            )
          )
        },
        test("stats") {
          val aggregation              = statsAggregation("aggregation", "testField")
          val aggregationTs            = statsAggregation("aggregation", TestDocument.intField)
          val aggregationTsRaw         = statsAggregation("aggregation", TestDocument.intField.raw)
          val aggregationWithMissing   = statsAggregation("aggregation", TestDocument.intField).missing(20.0)
          val aggregationTsDate        = statsAggregation("aggregation", TestDocument.dateField)
          val aggregationWithAllParams = statsAggregation("aggregation", TestDocument.dateField)
            .format("yyyy-MM-dd")
            .meta("unit", "day")
            .missing(LocalDate.of(2024, 1, 1))
            .script(Script("_value"))

          assert(aggregation)(
            equalTo(
              Stats(
                name = "aggregation",
                field = "testField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationTs)(
            equalTo(
              Stats(
                name = "aggregation",
                field = "intField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationTsRaw)(
            equalTo(
              Stats(
                name = "aggregation",
                field = "intField.raw",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationWithMissing)(
            equalTo(
              Stats(
                name = "aggregation",
                field = "intField",
                format = None,
                meta = Chunk.empty,
                missing = Some(Num(20.0)),
                script = None
              )
            )
          ) &&
          assert(aggregationTsDate)(
            equalTo(
              Stats(
                name = "aggregation",
                field = "dateField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationWithAllParams)(
            equalTo(
              Stats(
                name = "aggregation",
                field = "dateField",
                format = Some("yyyy-MM-dd"),
                meta = Chunk("unit" -> Str("day")),
                missing = Some(Str("2024-01-01")),
                script = Some(Script("_value"))
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
          val aggregation3 = termsAggregation(name = "first", field = TestDocument.stringField).withSubAgg(
            percentilesAggregation("second", field = TestDocument.intField).percents(75, 90, 99)
          )

          assert(aggregation1)(
            equalTo(
              Terms(
                name = "first",
                field = "stringField",
                meta = Chunk.empty,
                minDocCount = None,
                order = Chunk.empty,
                size = None,
                subAggregations = Chunk(
                  Terms(
                    name = "second",
                    field = "stringField.raw",
                    meta = Chunk.empty,
                    minDocCount = None,
                    order = Chunk.empty,
                    size = None,
                    subAggregations = Chunk.empty
                  )
                )
              )
            )
          ) && assert(aggregation2)(
            equalTo(
              Multiple(
                Chunk(
                  Terms(
                    name = "first",
                    field = "stringField",
                    meta = Chunk.empty,
                    minDocCount = None,
                    order = Chunk.empty,
                    size = None,
                    subAggregations = Chunk.empty
                  ),
                  Terms(
                    name = "second",
                    field = "testField",
                    meta = Chunk.empty,
                    minDocCount = None,
                    order = Chunk.empty,
                    size = None,
                    subAggregations = Chunk(
                      Max(
                        name = "third",
                        field = "anotherTestField",
                        format = None,
                        meta = Chunk.empty,
                        missing = None,
                        script = None
                      )
                    )
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
                meta = Chunk.empty,
                minDocCount = None,
                order = Chunk.empty,
                size = None,
                subAggregations = Chunk(
                  Percentiles(
                    name = "second",
                    field = "intField",
                    meta = Chunk.empty,
                    missing = None,
                    percents = Chunk(75, 90, 99)
                  )
                )
              )
            )
          )
        },
        test("sum") {
          val aggregation              = sumAggregation("aggregation", "testField")
          val aggregationTs            = sumAggregation("aggregation", TestSubDocument.intField)
          val aggregationTsRaw         = sumAggregation("aggregation", TestSubDocument.intField.raw)
          val aggregationWithMissing   = sumAggregation("aggregation", TestSubDocument.intField).missing(20.0)
          val aggregationTsDate        = sumAggregation("aggregation", TestDocument.dateField)
          val aggregationWithAllParams = sumAggregation("aggregation", TestDocument.dateField)
            .format("yyyy-MM-dd")
            .meta("unit", "day")
            .missing(LocalDate.of(2024, 1, 1))
            .script(Script("_value"))

          assert(aggregation)(
            equalTo(
              Sum(
                name = "aggregation",
                field = "testField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationTs)(
            equalTo(
              Sum(
                name = "aggregation",
                field = "intField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationTsRaw)(
            equalTo(
              Sum(
                name = "aggregation",
                field = "intField.raw",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationWithMissing)(
            equalTo(
              Sum(
                name = "aggregation",
                field = "intField",
                format = None,
                meta = Chunk.empty,
                missing = Some(Num(20.0)),
                script = None
              )
            )
          ) &&
          assert(aggregationTsDate)(
            equalTo(
              Sum(
                name = "aggregation",
                field = "dateField",
                format = None,
                meta = Chunk.empty,
                missing = None,
                script = None
              )
            )
          ) &&
          assert(aggregationWithAllParams)(
            equalTo(
              Sum(
                name = "aggregation",
                field = "dateField",
                format = Some("yyyy-MM-dd"),
                meta = Chunk("unit" -> Str("day")),
                missing = Some(Str("2024-01-01")),
                script = Some(Script("_value"))
              )
            )
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
          val aggregationTsInt                  = termsAggregation("aggregation", TestDocument.intField)
          val aggregationTsBoolean              = termsAggregation("aggregation", TestDocument.booleanField)
          val aggregationWithMinDocCountAndMeta =
            termsAggregation("aggregation", TestDocument.stringField).minDocCount(2).meta("unit", "docs")

          assert(aggregation)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "testField",
                meta = Chunk.empty,
                minDocCount = None,
                order = Chunk.empty,
                size = None,
                subAggregations = Chunk.empty
              )
            )
          ) &&
          assert(aggregationTs)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField",
                meta = Chunk.empty,
                minDocCount = None,
                order = Chunk.empty,
                size = None,
                subAggregations = Chunk.empty
              )
            )
          ) &&
          assert(aggregationTsRaw)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField.raw",
                meta = Chunk.empty,
                minDocCount = None,
                order = Chunk.empty,
                size = None,
                subAggregations = Chunk.empty
              )
            )
          ) &&
          assert(aggregationWithOrders)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField",
                meta = Chunk.empty,
                minDocCount = None,
                order = Chunk(
                  AggregationOrder("_key", Desc),
                  AggregationOrder("test", Desc),
                  AggregationOrder("_count", Asc)
                ),
                size = None,
                subAggregations = Chunk.empty
              )
            )
          ) &&
          assert(aggregationWithSize)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField",
                meta = Chunk.empty,
                minDocCount = None,
                order = Chunk.empty,
                size = Some(10),
                subAggregations = Chunk.empty
              )
            )
          ) &&
          assert(aggregationWithAllParams)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField.test",
                meta = Chunk.empty,
                minDocCount = None,
                order = Chunk(AggregationOrder("_count", Desc), AggregationOrder("_key", Asc)),
                size = Some(5),
                subAggregations = Chunk.empty
              )
            )
          ) &&
          assert(aggregationTsInt)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "intField",
                meta = Chunk.empty,
                minDocCount = None,
                order = Chunk.empty,
                size = None,
                subAggregations = Chunk.empty
              )
            )
          ) &&
          assert(aggregationTsBoolean)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "booleanField",
                meta = Chunk.empty,
                minDocCount = None,
                order = Chunk.empty,
                size = None,
                subAggregations = Chunk.empty
              )
            )
          ) &&
          assert(aggregationWithMinDocCountAndMeta)(
            equalTo(
              Terms(
                name = "aggregation",
                field = "stringField",
                meta = Chunk("unit" -> Str("docs")),
                minDocCount = Some(2),
                order = Chunk.empty,
                size = None,
                subAggregations = Chunk.empty
              )
            )
          )
        },
        test("valueCount") {
          val aggregation              = valueCountAggregation("aggregation", "testField")
          val aggregationTs            = valueCountAggregation("aggregation", TestDocument.stringField)
          val aggregationTsRaw         = valueCountAggregation("aggregation", TestDocument.stringField.raw)
          val aggregationWithAllParams =
            valueCountAggregation("aggregation", TestDocument.stringField).meta("unit", "docs").script(Script("_value"))

          assert(aggregation)(
            equalTo(ValueCount(name = "aggregation", field = "testField", meta = Chunk.empty, script = None))
          ) &&
          assert(aggregationTs)(
            equalTo(ValueCount(name = "aggregation", field = "stringField", meta = Chunk.empty, script = None))
          ) &&
          assert(aggregationTsRaw)(
            equalTo(ValueCount(name = "aggregation", field = "stringField.raw", meta = Chunk.empty, script = None))
          ) &&
          assert(aggregationWithAllParams)(
            equalTo(
              ValueCount(
                name = "aggregation",
                field = "stringField",
                meta = Chunk("unit" -> Str("docs")),
                script = Some(Script("_value"))
              )
            )
          )
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
          val aggregationWithMeta =
            weightedAvgAggregation(name = "aggregation", valueField = "valueField", weightField = "weightField")
              .meta("unit", "ms")

          assert(aggregation)(
            equalTo(
              WeightedAvg(
                name = "aggregation",
                valueField = "valueField",
                weightField = "weightField",
                meta = Chunk.empty,
                valueMissing = None,
                weightMissing = None
              )
            )
          ) &&
          assert(aggregationTs)(
            equalTo(
              WeightedAvg(
                name = "aggregation",
                valueField = "stringField",
                weightField = "intField",
                meta = Chunk.empty,
                valueMissing = None,
                weightMissing = None
              )
            )
          ) &&
          assert(aggregationTsRaw)(
            equalTo(
              WeightedAvg(
                name = "aggregation",
                valueField = "stringField.raw",
                weightField = "intField.raw",
                meta = Chunk.empty,
                valueMissing = None,
                weightMissing = None
              )
            )
          ) &&
          assert(aggregationWithValueMissing)(
            equalTo(
              WeightedAvg(
                name = "aggregation",
                valueField = "stringField",
                weightField = "intField",
                meta = Chunk.empty,
                valueMissing = Some(2.0),
                weightMissing = None
              )
            )
          ) &&
          assert(aggregationWithWeightMissing)(
            equalTo(
              WeightedAvg(
                name = "aggregation",
                valueField = "stringField",
                weightField = "intField",
                meta = Chunk.empty,
                valueMissing = None,
                weightMissing = Some(3.0)
              )
            )
          ) &&
          assert(aggregationWithValueAndWeightMissing)(
            equalTo(
              WeightedAvg(
                name = "aggregation",
                valueField = "stringField",
                weightField = "intField",
                meta = Chunk.empty,
                valueMissing = Some(4.0),
                weightMissing = Some(5.0)
              )
            )
          ) &&
          assert(aggregationWithMeta)(
            equalTo(
              WeightedAvg(
                name = "aggregation",
                valueField = "valueField",
                weightField = "weightField",
                meta = Chunk("unit" -> Str("ms")),
                valueMissing = None,
                weightMissing = None
              )
            )
          )
        }
      ),
      suite("encoding as JSON")(
        test("avg") {
          val aggregation              = avgAggregation("aggregation", "testField")
          val aggregationTs            = avgAggregation("aggregation", TestDocument.intField)
          val aggregationWithMissing   = avgAggregation("aggregation", TestDocument.intField).missing(20.0)
          val aggregationWithAllParams = avgAggregation("aggregation", TestDocument.intField)
            .format("0.00")
            .meta("unit", "ms")
            .missing(20)
            .script(Script("_value * 2"))

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

          val expectedAggregationWithAllParams =
            """
              |{
              |  "aggregation": {
              |    "avg": {
              |      "field": "intField",
              |      "format": "0.00",
              |      "missing": 20,
              |      "script": {
              |        "source": "_value * 2"
              |      }
              |    },
              |    "meta": {
              |      "unit": "ms"
              |    }
              |  }
              |}
              |""".stripMargin
          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithMissing.toJson)(equalTo(expectedWithMissing.toJson)) &&
          assert(aggregationWithAllParams.toJson)(equalTo(expectedAggregationWithAllParams.toJson))
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
          val aggregationWithEmptyBucketsPath = bucketSelectorAggregation(
            name = "aggregation",
            script = Script("1 > 0"),
            bucketsPath = Map.empty
          )
          val aggregationWithAllParams = bucketSelectorAggregation(
            name = "aggregation",
            script = Script("params.agg1 > 10"),
            bucketsPath = Map("agg1" -> "aggregation1")
          ).gapPolicy(GapPolicy.InsertZeros).meta("unit", "ms")

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

          val expectedWithEmptyBucketsPath =
            """
              |{
              |  "aggregation": {
              |    "bucket_selector": {
              |      "buckets_path": {},
              |      "script": {
              |        "source": "1 > 0"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedAggregationWithAllParams =
            """
              |{
              |  "aggregation": {
              |    "bucket_selector": {
              |      "buckets_path": {
              |        "agg1": "aggregation1"
              |      },
              |      "gap_policy": "insert_zeros",
              |      "script": {
              |        "source": "params.agg1 > 10"
              |      }
              |    },
              |    "meta": {
              |      "unit": "ms"
              |    }
              |  }
              |}
              |""".stripMargin
          assert(aggregation1.toJson)(equalTo(expected1.toJson)) &&
          assert(aggregation2.toJson)(equalTo(expected2.toJson)) &&
          assert(aggregationWithEmptyBucketsPath.toJson)(equalTo(expectedWithEmptyBucketsPath.toJson)) &&
          assert(aggregationWithAllParams.toJson)(equalTo(expectedAggregationWithAllParams.toJson))
        },
        test("bucketSort") {
          val aggregationWithFrom      = bucketSortAggregation("aggregation").from(5)
          val aggregationWithSize      = bucketSortAggregation("aggregation").size(5)
          val aggregationWithSort      = bucketSortAggregation("aggregation").sort(ElasticSort.sortBy("aggregation2"))
          val aggregationWithAllParams =
            bucketSortAggregation("aggregation").sort(ElasticSort.sortBy("aggregation2")).from(5).size(7)
          val aggregationWithGapPolicy = bucketSortAggregation("aggregation").gapPolicy(GapPolicy.KeepValues)

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

          val expectedAggregationWithGapPolicy =
            """
              |{
              |  "aggregation": {
              |    "bucket_sort": {
              |      "gap_policy": "keep_values"
              |    }
              |  }
              |}
              |""".stripMargin
          assert(aggregationWithFrom.toJson)(equalTo(expectedWithFrom.toJson)) &&
          assert(aggregationWithSize.toJson)(equalTo(expectedWithSize.toJson)) &&
          assert(aggregationWithSort.toJson)(equalTo(expectedWithSort.toJson)) &&
          assert(aggregationWithAllParams.toJson)(equalTo(expectedWithAllParams.toJson)) &&
          assert(aggregationWithGapPolicy.toJson)(equalTo(expectedAggregationWithGapPolicy.toJson))
        },
        test("cardinality") {
          val aggregation              = cardinalityAggregation("aggregation", "testField")
          val aggregationTs            = cardinalityAggregation("aggregation", TestDocument.intField)
          val aggregationWithMissing   = cardinalityAggregation("aggregation", TestDocument.intField).missing(20)
          val aggregationWithAllParams = cardinalityAggregation("aggregation", TestDocument.stringField)
            .meta("unit", "users")
            .missing("N/A")
            .script(Script("_value.toLowerCase()"))

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
              |      "missing": 20
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedAggregationWithAllParams =
            """
              |{
              |  "aggregation": {
              |    "cardinality": {
              |      "field": "stringField",
              |      "missing": "N/A",
              |      "script": {
              |        "source": "_value.toLowerCase()"
              |      }
              |    },
              |    "meta": {
              |      "unit": "users"
              |    }
              |  }
              |}
              |""".stripMargin
          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithMissing.toJson)(equalTo(expectedWithMissing.toJson)) &&
          assert(aggregationWithAllParams.toJson)(equalTo(expectedAggregationWithAllParams.toJson))
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
          val aggregation =
            ipRangeAggregation(
              name = "aggregation",
              field = "ipField",
              range = IpRangeBound().to("10.0.0.5"),
              ranges = IpRangeBound().from("10.0.0.5")
            )
          val aggregationWithKeyed =
            ipRangeAggregation(
              name = "aggregation",
              field = "ipField",
              range = IpRangeBound().mask("10.0.0.0/25").key("low"),
              ranges = IpRangeBound().mask("10.0.0.128/25").key("high")
            ).keyed
          val aggregationWithSubAggregation =
            ipRangeAggregation(
              name = "aggregation",
              field = "ipField",
              range = IpRangeBound().mask("10.0.0.0/25")
            ).withSubAgg(maxAggregation("subAggregation", "intField"))

          val expected =
            """
              |{
              |  "aggregation": {
              |    "ip_range": {
              |      "field": "ipField",
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

          val expectedWithKeyed =
            """
              |{
              |  "aggregation": {
              |    "ip_range": {
              |      "field": "ipField",
              |      "ranges": [
              |        {
              |          "key": "low",
              |          "mask": "10.0.0.0/25"
              |        },
              |        {
              |          "key": "high",
              |          "mask": "10.0.0.128/25"
              |        }
              |      ],
              |      "keyed": true
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithSubAggregation =
            """
              |{
              |  "aggregation": {
              |    "ip_range": {
              |      "field": "ipField",
              |      "ranges": [
              |        {
              |          "mask": "10.0.0.0/25"
              |        }
              |      ]
              |    },
              |    "aggs": {
              |      "subAggregation": {
              |        "max": {
              |          "field": "intField"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationWithKeyed.toJson)(equalTo(expectedWithKeyed.toJson)) &&
          assert(aggregationWithSubAggregation.toJson)(equalTo(expectedWithSubAggregation.toJson))
        },
        test("max") {
          val aggregation                = maxAggregation("aggregation", "testField")
          val aggregationTs              = maxAggregation("aggregation", TestDocument.intField)
          val aggregationWithMissing     = maxAggregation("aggregation", TestDocument.intField).missing(20.0)
          val aggregationWithDateMissing =
            maxAggregation("aggregation", TestDocument.dateField).format("yyyy-MM-dd").missing(LocalDate.of(2024, 1, 1))

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

          val expectedAggregationWithDateMissing =
            """
              |{
              |  "aggregation": {
              |    "max": {
              |      "field": "dateField",
              |      "format": "yyyy-MM-dd",
              |      "missing": "2024-01-01"
              |    }
              |  }
              |}
              |""".stripMargin
          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithMissing.toJson)(equalTo(expectedWithMissing.toJson)) &&
          assert(aggregationWithDateMissing.toJson)(equalTo(expectedAggregationWithDateMissing.toJson))
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
          val aggregation              = missingAggregation("aggregation", "testField")
          val aggregationTs            = missingAggregation("aggregation", TestDocument.stringField)
          val aggregationWithAllParams = missingAggregation("aggregation", "testField")
            .meta("unit", "docs")
            .withSubAgg(maxAggregation("maxAggregation", "intField"))

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

          val expectedAggregationWithAllParams =
            """
              |{
              |  "aggregation": {
              |    "missing": {
              |      "field": "testField"
              |    },
              |    "meta": {
              |      "unit": "docs"
              |    },
              |    "aggs": {
              |      "maxAggregation": {
              |        "max": {
              |          "field": "intField"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin
          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithAllParams.toJson)(equalTo(expectedAggregationWithAllParams.toJson))
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
          assert(aggregationWithSubAggregation.toJson)(equalTo(expectedWithSubAggregation.toJson)) &&
          assert(multipleAggregations.toJson)(equalTo("{}".toJson))
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
        test("range") {
          val aggregation =
            rangeAggregation(
              "aggregation",
              "testField",
              RangeBound().to(50),
              RangeBound().from(50).to(100),
              RangeBound().from(100)
            )
          val aggregationTs =
            rangeAggregation(name = "aggregation", field = TestDocument.intField, range = RangeBound().to(23.9))
          val aggregationWithKeyed =
            rangeAggregation(
              name = "aggregation",
              field = TestDocument.intField,
              range = RangeBound().to(50).key("low"),
              ranges = RangeBound().from(50).key("high")
            ).keyed
          val aggregationWithSubAggregation =
            rangeAggregation(name = "aggregation", field = TestDocument.intField, range = RangeBound().from(50))
              .withSubAgg(maxAggregation("subAggregation", TestDocument.intField))
          val aggregationWithMixedTypes =
            rangeAggregation(
              name = "aggregation",
              field = TestDocument.doubleField,
              range = RangeBound().from(0).to(99.5),
              ranges = RangeBound().from(99.5f).to(Long.MaxValue),
              RangeBound().from(1.toShort).to(BigDecimal("1.25")),
              RangeBound(key = Some("all"))
            )

          val expected =
            """
              |{
              |  "aggregation": {
              |    "range": {
              |      "field": "testField",
              |      "ranges": [
              |        {
              |          "to": 50
              |        },
              |        {
              |          "from": 50,
              |          "to": 100
              |        },
              |        {
              |          "from": 100
              |        }
              |      ]
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedTs =
            """
              |{
              |  "aggregation": {
              |    "range": {
              |      "field": "intField",
              |      "ranges": [
              |        {
              |          "to": 23.9
              |        }
              |      ]
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithKeyed =
            """
              |{
              |  "aggregation": {
              |    "range": {
              |      "field": "intField",
              |      "ranges": [
              |        {
              |          "key": "low",
              |          "to": 50
              |        },
              |        {
              |          "from": 50,
              |          "key": "high"
              |        }
              |      ],
              |      "keyed": true
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithSubAggregation =
            """
              |{
              |  "aggregation": {
              |    "range": {
              |      "field": "intField",
              |      "ranges": [
              |        {
              |          "from": 50
              |        }
              |      ]
              |    },
              |    "aggs": {
              |      "subAggregation": {
              |        "max": {
              |          "field": "intField"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedWithMixedTypes =
            """
              |{
              |  "aggregation": {
              |    "range": {
              |      "field": "doubleField",
              |      "ranges": [
              |        {
              |          "from": 0,
              |          "to": 99.5
              |        },
              |        {
              |          "from": 99.5,
              |          "to": 9223372036854775807
              |        },
              |        {
              |          "from": 1,
              |          "to": 1.25
              |        },
              |        {
              |          "key": "all"
              |        }
              |      ]
              |    }
              |  }
              |}
              |""".stripMargin

          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithKeyed.toJson)(equalTo(expectedWithKeyed.toJson)) &&
          assert(aggregationWithSubAggregation.toJson)(equalTo(expectedWithSubAggregation.toJson)) &&
          assert(aggregationWithMixedTypes.toJson)(equalTo(expectedWithMixedTypes.toJson))
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
          val aggregationWithMeta =
            samplerAggregation("aggregation", maxAggregation("maxAggregation", "intField")).meta("unit", "ms")

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

          val expectedAggregationWithMeta =
            """
              |{
              |  "aggregation": {
              |    "sampler": {
              |      "shard_size": 100
              |    },
              |    "meta": {
              |      "unit": "ms"
              |    },
              |    "aggs": {
              |      "maxAggregation": {
              |        "max": {
              |          "field": "intField"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin
          assert(aggWithSubAgg.toJson)(equalTo(expectedWithSubAgg.toJson)) &&
          assert(aggWithSubAggsAndMaxDocumentsPerShardParam.toJson)(equalTo(expectedWithMultipleSubAggs.toJson)) &&
          assert(aggregationWithMeta.toJson)(equalTo(expectedAggregationWithMeta.toJson))
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
          val aggregationWithMinDocCountAndMeta =
            termsAggregation("aggregation", TestDocument.intField).minDocCount(2).meta("unit", "docs")

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

          val expectedAggregationWithMinDocCountAndMeta =
            """
              |{
              |  "aggregation": {
              |    "terms": {
              |      "field": "intField",
              |      "min_doc_count": 2
              |    },
              |    "meta": {
              |      "unit": "docs"
              |    }
              |  }
              |}
              |""".stripMargin
          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithOrder.toJson)(equalTo(expectedWithOrder.toJson)) &&
          assert(aggregationWithSize.toJson)(equalTo(expectedWithSize.toJson)) &&
          assert(aggregationWithAllParams.toJson)(equalTo(expectedWithAllParams.toJson)) &&
          assert(aggregationWithMinDocCountAndMeta.toJson)(equalTo(expectedAggregationWithMinDocCountAndMeta.toJson))
        },
        test("valueCount") {
          val aggregation           = valueCountAggregation("aggregation", "testField")
          val aggregationTs         = valueCountAggregation("aggregation", TestDocument.stringField)
          val aggregationWithScript =
            valueCountAggregation("aggregation", TestDocument.stringField).script(Script("_value"))

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

          val expectedAggregationWithScript =
            """
              |{
              |  "aggregation": {
              |    "value_count": {
              |      "field": "stringField",
              |      "script": {
              |        "source": "_value"
              |      }
              |    }
              |  }
              |}
              |""".stripMargin
          assert(aggregation.toJson)(equalTo(expected.toJson)) &&
          assert(aggregationTs.toJson)(equalTo(expectedTs.toJson)) &&
          assert(aggregationWithScript.toJson)(equalTo(expectedAggregationWithScript.toJson))
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
        },
        test("single bucket aggregations with multiple aggregations as sub-aggregations") {
          val aggregations = maxAggregation("max", "intField").withAgg(minAggregation("min", "intField"))
          val filter       = filterAggregation("filter", term("stringField", "test")).withSubAgg(aggregations)
          val missing      = missingAggregation("missing", "stringField").withSubAgg(aggregations)
          val sampler      = samplerAggregation("sampler", aggregations, avgAggregation("avg", "intField"))

          val expectedSubAggregations =
            """
              |{
              |  "max": {
              |    "max": {
              |      "field": "intField"
              |    }
              |  },
              |  "min": {
              |    "min": {
              |      "field": "intField"
              |    }
              |  }
              |}
              |""".stripMargin

          val expectedSampler =
            """
              |{
              |  "sampler": {
              |    "sampler": {
              |      "shard_size": 100
              |    },
              |    "aggs": {
              |      "max": {
              |        "max": {
              |          "field": "intField"
              |        }
              |      },
              |      "min": {
              |        "min": {
              |          "field": "intField"
              |        }
              |      },
              |      "avg": {
              |        "avg": {
              |          "field": "intField"
              |        }
              |      }
              |    }
              |  }
              |}
              |""".stripMargin

          assert(filter.subAggregations.map(_.name))(equalTo(Chunk("max", "min"))) &&
          assert(missing.subAggregations.map(_.name))(equalTo(Chunk("max", "min"))) &&
          assert(filter.toJson)(
            equalTo(
              Obj(
                "filter" -> Obj(
                  "filter" -> term("stringField", "test").toJson(None),
                  "aggs"   -> expectedSubAggregations.toJson
                )
              )
            )
          ) &&
          assert(sampler.toJson)(equalTo(expectedSampler.toJson))
        }
      ),
      suite("type checking")(
        test("parent pipeline aggregations are accepted as sub-aggregations of multi-bucket aggregations") {
          assertZIO(
            typeCheck(
              """termsAggregation("aggregation", "testField").withSubAgg(bucketSortAggregation("bucketSort"))"""
            )
          )(isRight) &&
          assertZIO(
            typeCheck(
              """rangeAggregation("aggregation", "testField", RangeBound().to(1)).withSubAgg(bucketSortAggregation("bucketSort"))"""
            )
          )(isRight) &&
          assertZIO(
            typeCheck(
              """ipRangeAggregation("aggregation", "testField", IpRangeBound().to("10.0.0.5")).withSubAgg(bucketSortAggregation("bucketSort"))"""
            )
          )(isRight)
        },
        test("parent pipeline aggregations are rejected as sub-aggregations of single bucket aggregations") {
          assertZIO(
            typeCheck(
              """filterAggregation("aggregation", term("testField", "a")).withSubAgg(maxAggregation("max", "testField"))"""
            )
          )(isRight) &&
          assertZIO(
            typeCheck(
              """samplerAggregation("aggregation", bucketSelectorAggregation("bucketSelector", Script("params.agg > 10"), Map("agg" -> "agg")))"""
            )
          )(isLeft) &&
          assertZIO(
            typeCheck(
              """samplerAggregation("aggregation", maxAggregation("max", "testField")).withSubAgg(bucketSortAggregation("bucketSort"))"""
            )
          )(isLeft) &&
          assertZIO(
            typeCheck(
              """filterAggregation("aggregation", term("testField", "a")).withSubAgg(bucketSortAggregation("bucketSort"))"""
            )
          )(isLeft) &&
          assertZIO(
            typeCheck(
              """missingAggregation("aggregation", "testField").withSubAgg(bucketSortAggregation("bucketSort"))"""
            )
          )(isLeft)
        },
        test("parent pipeline aggregations are rejected at the top level") {
          assertZIO(
            typeCheck(
              """maxAggregation("aggregation", "testField").withAgg(minAggregation("aggregation2", "testField"))"""
            )
          )(isRight) &&
          assertZIO(
            typeCheck("""ElasticRequest.aggregate(IndexName("index"), maxAggregation("aggregation", "testField"))""")
          )(isRight) &&
          assertZIO(
            typeCheck("""maxAggregation("aggregation", "testField").withAgg(bucketSortAggregation("bucketSort"))""")
          )(isLeft) &&
          assertZIO(
            typeCheck("""bucketSortAggregation("bucketSort").withAgg(maxAggregation("aggregation", "testField"))""")
          )(isLeft) &&
          assertZIO(typeCheck("""multipleAggregations.aggregations(bucketSortAggregation("bucketSort"))"""))(isLeft) &&
          assertZIO(
            typeCheck(
              """ElasticRequest.aggregate(IndexName("index"), bucketSortAggregation("bucketSort"))"""
            )
          )(isLeft)
        },
        test("type-safe constructors accept supported field types") {
          assertZIO(typeCheck("""avgAggregation("aggregation", TestDocument.dateField)"""))(isRight) &&
          assertZIO(typeCheck("""termsAggregation("aggregation", TestDocument.intField)"""))(isRight) &&
          assertZIO(typeCheck("""rangeAggregation("aggregation", "testField", RangeBound().to(1))"""))(isRight)
        },
        test("type-safe constructors reject unsupported field types") {
          assertZIO(typeCheck("""avgAggregation("aggregation", TestDocument.stringField)"""))(isLeft) &&
          assertZIO(typeCheck("""termsAggregation("aggregation", TestDocument.geoPointField)"""))(isLeft) &&
          assertZIO(
            typeCheck("""rangeAggregation("aggregation", "testField", RangeBound().to("a"))""")
          )(isLeft)
        },
        test("missing accepts values supported by the aggregation") {
          assertZIO(typeCheck("""cardinalityAggregation("aggregation", "testField").missing(true)"""))(isRight) &&
          assertZIO(typeCheck("""avgAggregation("aggregation", "testField").missing(1)"""))(isRight) &&
          assertZIO(
            typeCheck("""maxAggregation("aggregation", "testField").missing(java.time.Instant.EPOCH)""")
          )(isRight) &&
          assertZIO(
            typeCheck("""statsAggregation("aggregation", "testField").missing(java.time.LocalDateTime.MIN)""")
          )(isRight) &&
          assertZIO(typeCheck("""percentilesAggregation("aggregation", "testField").missing(1.5)"""))(isRight)
        },
        test("missing rejects values not supported by the aggregation") {
          assertZIO(typeCheck("""avgAggregation("aggregation", "testField").missing(true)"""))(isLeft) &&
          assertZIO(typeCheck("""sumAggregation("aggregation", "testField").missing("a")"""))(isLeft) &&
          assertZIO(
            typeCheck("""percentilesAggregation("aggregation", "testField").missing(java.util.UUID.randomUUID())""")
          )(isLeft) &&
          assertZIO(typeCheck("""percentilesAggregation("aggregation", "testField").missing(true)"""))(isLeft) &&
          assertZIO(
            typeCheck("""percentileRanksAggregation("aggregation", "testField", 1.0).missing(LocalDate.MIN)""")
          )(isLeft) &&
          assertZIO(typeCheck("""extendedStatsAggregation("aggregation", "testField").missing("a")"""))(isLeft)
        }
      )
    )
}
