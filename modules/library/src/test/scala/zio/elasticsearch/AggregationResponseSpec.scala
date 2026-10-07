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
