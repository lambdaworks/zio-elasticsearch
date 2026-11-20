package zio.elasticsearch

import zio.elasticsearch.result._
import zio.test.Assertion.{equalTo, isLeft, isNone, isRight, isSome}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assert, assertZIO}
import zio.{Task, ZIO}

object AggregationResultSpec extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment, Any] =
    suite("AggregationResult")(
      suite("aggregationAs")(
        test("return the aggregation when it is of the requested type") {
          assertZIO(aggregateResult.aggregationAs[AvgAggregationResult]("avgAggregation"))(
            isSome(equalTo(avgResult))
          )
        },
        test("return none when the aggregation does not exist") {
          assertZIO(aggregateResult.aggregationAs[AvgAggregationResult]("missingAggregation"))(isNone)
        },
        test("return an error when the aggregation is not of the requested type") {
          assertZIO(aggregateResult.aggregationAs[MaxAggregationResult]("avgAggregation").either)(
            isLeft(equalTo(typeMismatch))
          )
        },
        test("return an error from a typed accessor when the aggregation is not of the requested type") {
          assertZIO(aggregateResult.asTermsAggregation("avgAggregation").either)(isLeft(equalTo(typeMismatch)))
        },
        test("return an error from an effect when the aggregation is not of the requested type") {
          val effect: Task[ResultWithAggregation] = ZIO.succeed(aggregateResult)

          assertZIO(effect.aggregationAs[MaxAggregationResult]("avgAggregation").either)(isLeft(equalTo(typeMismatch)))
        }
      ),
      suite("subAggregationAs")(
        test("return the sub aggregation when it is of the requested type") {
          val expected = isRight(isSome(equalTo(avgResult)))

          assert(filterResult.subAggregationAs[AvgAggregationResult]("avgAggregation"))(expected) &&
          assert(ipRangeBucketResult.subAggregationAs[AvgAggregationResult]("avgAggregation"))(expected) &&
          assert(rangeBucketResult.subAggregationAs[AvgAggregationResult]("avgAggregation"))(expected) &&
          assert(samplerResult.subAggregationAs[AvgAggregationResult]("avgAggregation"))(expected) &&
          assert(termsBucketResult.subAggregationAs[AvgAggregationResult]("avgAggregation"))(expected)
        },
        test("return none when the sub aggregation does not exist") {
          val expected = isRight(isNone)

          assert(filterResult.subAggregationAs[AvgAggregationResult]("missingAggregation"))(expected) &&
          assert(ipRangeBucketResult.subAggregationAs[AvgAggregationResult]("missingAggregation"))(expected) &&
          assert(rangeBucketResult.subAggregationAs[AvgAggregationResult]("missingAggregation"))(expected) &&
          assert(samplerResult.subAggregationAs[AvgAggregationResult]("missingAggregation"))(expected) &&
          assert(termsBucketResult.subAggregationAs[AvgAggregationResult]("missingAggregation"))(expected)
        },
        test("return an error when the sub aggregation is not of the requested type") {
          val expected = isLeft(equalTo(typeMismatch))

          assert(filterResult.subAggregationAs[MaxAggregationResult]("avgAggregation"))(expected) &&
          assert(ipRangeBucketResult.subAggregationAs[MaxAggregationResult]("avgAggregation"))(expected) &&
          assert(rangeBucketResult.subAggregationAs[MaxAggregationResult]("avgAggregation"))(expected) &&
          assert(samplerResult.subAggregationAs[MaxAggregationResult]("avgAggregation"))(expected) &&
          assert(termsBucketResult.subAggregationAs[TermsAggregationResult]("avgAggregation"))(expected)
        }
      )
    )

  private val avgResult: AvgAggregationResult = AvgAggregationResult(value = Some(1.0))

  private val typeMismatch: DecodingException =
    DecodingException("Aggregation with name avgAggregation was not of type you provided.")

  private val subAggregations: Map[String, AggregationResult] = Map("avgAggregation" -> avgResult)

  private val aggregateResult: AggregateResult = new AggregateResult(subAggregations)

  private val filterResult: FilterAggregationResult =
    FilterAggregationResult(docCount = 1, subAggregations = subAggregations)

  private val ipRangeBucketResult: IpRangeAggregationBucketResult =
    IpRangeAggregationBucketResult(key = "key", from = None, to = None, docCount = 1, subAggregations = subAggregations)

  private val rangeBucketResult: RangeAggregationBucketResult =
    RangeAggregationBucketResult(key = "key", from = None, to = None, docCount = 1, subAggregations = subAggregations)

  private val samplerResult: SamplerAggregationResult =
    SamplerAggregationResult(docCount = 1, subAggregations = subAggregations)

  private val termsBucketResult: TermsAggregationBucketResult =
    TermsAggregationBucketResult(key = "key", keyAsString = None, docCount = 1, subAggregations = subAggregations)
}
