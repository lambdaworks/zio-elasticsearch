package zio.elasticsearch

import zio.Chunk
import zio.elasticsearch.executor.response.AggregationResponse.toResult
import zio.elasticsearch.executor.response.SearchWithAggregationsResponse
import zio.elasticsearch.result._
import zio.test.Assertion.{containsString, equalTo, isLeft, isRight}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assert}

object AggregationResponseSpec extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment, Any] =
    suite("AggregationResponse")(
      suite("decoding null metric values")(
        test("avg, max, min and weighted avg") {
          val aggregations =
            """{
              |  "avg#avgAggregation": { "value": null },
              |  "max#maxAggregation": { "value": null },
              |  "min#minAggregation": { "value": null },
              |  "weighted_avg#weightedAvgAggregation": { "value": null }
              |}""".stripMargin

          assert(results(aggregations))(
            isRight(
              equalTo(
                Map(
                  "avgAggregation"         -> AvgAggregationResult(value = None),
                  "maxAggregation"         -> MaxAggregationResult(value = None),
                  "minAggregation"         -> MinAggregationResult(value = None),
                  "weightedAvgAggregation" -> WeightedAvgAggregationResult(value = None)
                )
              )
            )
          )
        },
        test("stats") {
          val aggregations =
            """{
              |  "stats#statsAggregation": { "count": 0, "min": null, "max": null, "avg": null, "sum": 0.0 }
              |}""".stripMargin

          assert(results(aggregations))(
            isRight(
              equalTo(
                Map(
                  "statsAggregation" -> StatsAggregationResult(count = 0, min = None, max = None, avg = None, sum = 0.0)
                )
              )
            )
          )
        },
        test("extended stats") {
          val aggregations = s"""{ "extended_stats#extendedStatsAggregation": $emptyExtendedStats }"""

          assert(results(aggregations))(
            isRight(equalTo(Map("extendedStatsAggregation" -> emptyExtendedStatsResult)))
          )
        },
        test("percentiles and percentile ranks") {
          val aggregations =
            """{
              |  "tdigest_percentiles#percentilesAggregation": { "values": { "50.0": null, "99.0": null } },
              |  "tdigest_percentile_ranks#percentileRanksAggregation": { "values": { "500.0": null } }
              |}""".stripMargin

          assert(results(aggregations))(
            isRight(
              equalTo(
                Map(
                  "percentilesAggregation" -> PercentilesAggregationResult(values =
                    Map("50.0" -> None, "99.0" -> None)
                  ),
                  "percentileRanksAggregation" -> PercentileRanksAggregationResult(values = Map("500.0" -> None))
                )
              )
            )
          )
        },
        test("sub aggregations inside a bucket") {
          val aggregations =
            s"""{
               |  "sterms#termsAggregation": {
               |    "doc_count_error_upper_bound": 0,
               |    "sum_other_doc_count": 0,
               |    "buckets": [
               |      {
               |        "key": "name",
               |        "doc_count": 1,
               |        "avg#avgAggregation": { "value": null },
               |        "extended_stats#extendedStatsAggregation": $emptyExtendedStats
               |      }
               |    ]
               |  }
               |}""".stripMargin

          assert(results(aggregations))(
            isRight(
              equalTo(
                Map(
                  "termsAggregation" -> TermsAggregationResult(
                    docErrorCount = 0,
                    sumOtherDocCount = 0,
                    buckets = Chunk(
                      TermsAggregationBucketResult(
                        docCount = 1,
                        key = "name",
                        subAggregations = Map(
                          "avgAggregation"           -> AvgAggregationResult(value = None),
                          "extendedStatsAggregation" -> emptyExtendedStatsResult
                        )
                      )
                    )
                  )
                )
              )
            )
          )
        }
      ),
      suite("decoding counts above Int.MaxValue")(
        test("cardinality and value count") {
          val aggregations =
            s"""{
               |  "cardinality#cardinalityAggregation": { "value": $largeCount },
               |  "value_count#valueCountAggregation": { "value": $largeCount }
               |}""".stripMargin

          assert(results(aggregations))(
            isRight(
              equalTo(
                Map(
                  "cardinalityAggregation" -> CardinalityAggregationResult(value = largeCount),
                  "valueCountAggregation"  -> ValueCountAggregationResult(value = largeCount)
                )
              )
            )
          )
        },
        test("stats and extended stats") {
          val extendedStats = emptyExtendedStats.replace(""""count": 0""", s""""count": $largeCount""")
          val aggregations  =
            s"""{
               |  "stats#statsAggregation": { "count": $largeCount, "min": null, "max": null, "avg": null, "sum": 0.0 },
               |  "extended_stats#extendedStatsAggregation": $extendedStats
               |}""".stripMargin

          assert(results(aggregations))(
            isRight(
              equalTo(
                Map(
                  "statsAggregation" -> StatsAggregationResult(
                    count = largeCount,
                    min = None,
                    max = None,
                    avg = None,
                    sum = 0.0
                  ),
                  "extendedStatsAggregation" -> emptyExtendedStatsResult.copy(count = largeCount)
                )
              )
            )
          )
        },
        test("filter, missing and sampler") {
          val aggregations =
            s"""{
               |  "filter#filterAggregation": { "doc_count": $largeCount },
               |  "missing#missingAggregation": { "doc_count": $largeCount },
               |  "sampler#samplerAggregation": { "doc_count": $largeCount }
               |}""".stripMargin

          assert(results(aggregations))(
            isRight(
              equalTo(
                Map(
                  "filterAggregation"  -> FilterAggregationResult(docCount = largeCount, subAggregations = Map.empty),
                  "missingAggregation" -> MissingAggregationResult(docCount = largeCount),
                  "samplerAggregation" -> SamplerAggregationResult(docCount = largeCount, subAggregations = Map.empty)
                )
              )
            )
          )
        },
        test("terms") {
          val aggregations =
            s"""{
               |  "sterms#termsAggregation": {
               |    "doc_count_error_upper_bound": $largeCount,
               |    "sum_other_doc_count": $largeCount,
               |    "buckets": [{ "key": "name", "doc_count": $largeCount }]
               |  }
               |}""".stripMargin

          assert(results(aggregations))(
            isRight(
              equalTo(
                Map(
                  "termsAggregation" -> TermsAggregationResult(
                    docErrorCount = largeCount,
                    sumOtherDocCount = largeCount,
                    buckets = Chunk(
                      TermsAggregationBucketResult(docCount = largeCount, key = "name", subAggregations = Map.empty)
                    )
                  )
                )
              )
            )
          )
        },
        test("ip range") {
          val aggregations =
            s"""{
               |  "ip_range#ipRangeAggregation": {
               |    "buckets": [{ "key": "*-10.0.0.5", "to": "10.0.0.5", "doc_count": $largeCount }]
               |  }
               |}""".stripMargin

          assert(results(aggregations))(
            isRight(
              equalTo(
                Map(
                  "ipRangeAggregation" -> IpRangeAggregationResult(
                    buckets = Chunk(
                      IpRangeAggregationBucketResult(
                        key = "*-10.0.0.5",
                        from = None,
                        to = Some("10.0.0.5"),
                        docCount = largeCount,
                        subAggregations = Map.empty
                      )
                    )
                  )
                )
              )
            )
          )
        }
      ),
      suite("decoding aggregation keys")(
        test("keep everything after the first '#' as the aggregation name") {
          val aggregations = """{ "max#max#aggregation": { "value": 1.0 } }"""

          assert(results(aggregations))(
            isRight(equalTo(Map("max#aggregation" -> MaxAggregationResult(value = Some(1.0)))))
          )
        },
        test("route by aggregation type regardless of the aggregation name") {
          val aggregations = """{ "value_count#avg#aggregation": { "value": 2 } }"""

          assert(results(aggregations))(
            isRight(equalTo(Map("avg#aggregation" -> ValueCountAggregationResult(value = 2))))
          )
        },
        test("prefer key as string and ignore bucket metadata fields") {
          val aggregations =
            """{
              |  "lterms#termsAggregation": {
              |    "doc_count_error_upper_bound": 0,
              |    "sum_other_doc_count": 0,
              |    "buckets": [
              |      {
              |        "key": 1,
              |        "key_as_string": "true",
              |        "doc_count": 1,
              |        "doc_count_error_upper_bound": 0
              |      }
              |    ]
              |  }
              |}""".stripMargin

          assert(results(aggregations))(
            isRight(
              equalTo(
                Map(
                  "termsAggregation" -> TermsAggregationResult(
                    docErrorCount = 0,
                    sumOtherDocCount = 0,
                    buckets =
                      Chunk(TermsAggregationBucketResult(docCount = 1, key = "true", subAggregations = Map.empty))
                  )
                )
              )
            )
          )
        }
      ),
      suite("decoding failures")(
        test("return an error for an invalid aggregation") {
          val aggregations = """{ "avg#avgAggregation": { "value": "invalid" } }"""

          assert(results(aggregations))(isLeft(containsString("avg#avgAggregation")))
        },
        test("return an error for an invalid sub aggregation") {
          val aggregations =
            """{
              |  "filter#filterAggregation": {
              |    "doc_count": 1,
              |    "max#maxAggregation": { "value": "invalid" }
              |  }
              |}""".stripMargin

          assert(results(aggregations))(isLeft(containsString("max#maxAggregation")))
        },
        test("return an error for an unsupported aggregation") {
          val aggregations = """{ "geo_bounds#geoBoundsAggregation": { "bounds": {} } }"""

          assert(results(aggregations))(
            isLeft(equalTo("Unsupported aggregation: geo_bounds#geoBoundsAggregation"))
          )
        }
      )
    )

  private val largeCount: Long = Int.MaxValue.toLong + 1

  private val emptyExtendedStats: String =
    """{
      |  "count": 0,
      |  "min": null,
      |  "max": null,
      |  "avg": null,
      |  "sum": 0.0,
      |  "sum_of_squares": null,
      |  "variance": null,
      |  "variance_population": null,
      |  "variance_sampling": null,
      |  "std_deviation": null,
      |  "std_deviation_population": null,
      |  "std_deviation_sampling": null,
      |  "std_deviation_bounds": {
      |    "upper": null,
      |    "lower": null,
      |    "upper_population": null,
      |    "lower_population": null,
      |    "upper_sampling": null,
      |    "lower_sampling": null
      |  }
      |}""".stripMargin

  private val emptyExtendedStatsResult: ExtendedStatsAggregationResult =
    ExtendedStatsAggregationResult(
      count = 0,
      min = None,
      max = None,
      avg = None,
      sum = 0.0,
      sumOfSquares = None,
      variance = None,
      variancePopulation = None,
      varianceSampling = None,
      stdDeviation = None,
      stdDeviationPopulation = None,
      stdDeviationSampling = None,
      stdDeviationBoundsResult = StdDeviationBoundsResult(
        upper = None,
        lower = None,
        upperPopulation = None,
        lowerPopulation = None,
        upperSampling = None,
        lowerSampling = None
      )
    )

  private def results(aggregations: String): Either[String, Map[String, AggregationResult]] =
    SearchWithAggregationsResponse.decoder
      .decodeJson(
        s"""{
           |  "took": 1,
           |  "timed_out": false,
           |  "_shards": { "total": 1, "successful": 1, "skipped": 0, "failed": 0 },
           |  "hits": { "total": { "value": 0, "relation": "eq" }, "max_score": null, "hits": [] },
           |  "aggregations": $aggregations
           |}""".stripMargin
      )
      .flatMap(_.aggs)
      .map(_.map { case (name, response) => (name, toResult(response)) })
}
