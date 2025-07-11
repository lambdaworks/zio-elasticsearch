/*
 * Copyright 2022 LambdaWorks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zio.elasticsearch.executor.response

import zio.Chunk
import zio.elasticsearch.result._
import zio.json.ast.Json
import zio.json.ast.Json.Obj
import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}

private[elasticsearch] sealed trait AggregationBucket

sealed trait AggregationResponse

object AggregationResponse {
  private[elasticsearch] def toResult(aggregationResponse: AggregationResponse): AggregationResult =
    aggregationResponse match {
      case AvgAggregationResponse(value) =>
        AvgAggregationResult(value)
      case CardinalityAggregationResponse(value) =>
        CardinalityAggregationResult(value)
      case ExtendedStatsAggregationResponse(
            count,
            min,
            max,
            avg,
            sum,
            sumOfSquares,
            variance,
            variancePopulation,
            varianceSampling,
            stdDeviation,
            stdDeviationPopulation,
            stdDeviationSampling,
            stdDeviationBoundsResponse
          ) =>
        ExtendedStatsAggregationResult(
          count = count,
          min = min,
          max = max,
          avg = avg,
          sum = sum,
          sumOfSquares = sumOfSquares,
          variance = variance,
          variancePopulation = variancePopulation,
          varianceSampling = varianceSampling,
          stdDeviation = stdDeviation,
          stdDeviationPopulation = stdDeviationPopulation,
          stdDeviationSampling = stdDeviationSampling,
          StdDeviationBoundsResult(
            upper = stdDeviationBoundsResponse.upper,
            lower = stdDeviationBoundsResponse.lower,
            upperPopulation = stdDeviationBoundsResponse.upperPopulation,
            lowerPopulation = stdDeviationBoundsResponse.lowerPopulation,
            upperSampling = stdDeviationBoundsResponse.upperSampling,
            lowerSampling = stdDeviationBoundsResponse.lowerSampling
          )
        )
      case FilterAggregationResponse(docCount, subAggregations) =>
        FilterAggregationResult(
          docCount = docCount,
          subAggregations =
            subAggregations.map(_.map { case (key, response) => (key, toResult(response)) }).getOrElse(Map.empty)
        )
      case MaxAggregationResponse(value) =>
        MaxAggregationResult(value)
      case MinAggregationResponse(value) =>
        MinAggregationResult(value)
      case MissingAggregationResponse(value) =>
        MissingAggregationResult(value)
      case PercentileRanksAggregationResponse(values) =>
        PercentileRanksAggregationResult(values)
      case PercentilesAggregationResponse(values) =>
        PercentilesAggregationResult(values)
      case SamplerAggregationResponse(count, aggs) =>
        SamplerAggregationResult(
          docCount = count,
          subAggregations = aggs.map(_.map { case (key, response) => (key, toResult(response)) }).getOrElse(Map.empty)
        )
      case StatsAggregationResponse(count, min, max, avg, sum) =>
        StatsAggregationResult(count, min, max, avg, sum)
      case SumAggregationResponse(value) =>
        SumAggregationResult(value)
      case TermsAggregationResponse(docErrorCount, sumOtherDocCount, buckets) =>
        TermsAggregationResult(
          docErrorCount = docErrorCount,
          sumOtherDocCount = sumOtherDocCount,
          buckets = buckets.map(b =>
            TermsAggregationBucketResult(
              docCount = b.docCount,
              key = b.key,
              subAggregations =
                b.subAggregations.map(_.map { case (key, response) => (key, toResult(response)) }).getOrElse(Map.empty)
            )
          )
        )
      case ValueCountAggregationResponse(value) =>
        ValueCountAggregationResult(value)
      case WeightedAvgAggregationResponse(value) =>
        WeightedAvgAggregationResult(value)
    }
}

private[elasticsearch] final case class AvgAggregationResponse(value: Double) extends AggregationResponse

private[elasticsearch] object AvgAggregationResponse {
  implicit val decoder: JsonDecoder[AvgAggregationResponse] = DeriveJsonDecoder.gen[AvgAggregationResponse]
}

private[elasticsearch] case class BucketDecoder(fields: Chunk[(String, Json)]) extends JsonDecoderOps {
  val allFields: Map[String, Any] = fields.flatMap { case (field, data) =>
    field match {
      case "key" =>
        Some(field -> data.toString.replaceAll("\"", ""))
      case "doc_count" =>
        Some(field -> data.unsafeAs[Int])
      case _ =>
        val objFields = data.unsafeAs[Obj].fields.toMap

        (field: @unchecked) match {
          case str if str.contains("weighted_avg#") =>
            Some(field -> WeightedAvgAggregationResponse(value = objFields("value").unsafeAs[Double]))
          case str if str.contains("avg#") =>
            Some(field -> AvgAggregationResponse(value = objFields("value").unsafeAs[Double]))
          case str if str.contains("cardinality#") =>
            Some(field -> CardinalityAggregationResponse(value = objFields("value").unsafeAs[Int]))
          case str if str.contains("extended_stats#") =>
            Some(
              field -> ExtendedStatsAggregationResponse(
                count = objFields("count").unsafeAs[Int],
                min = objFields("min").unsafeAs[Double],
                max = objFields("max").unsafeAs[Double],
                avg = objFields("avg").unsafeAs[Double],
                sum = objFields("sum").unsafeAs[Double],
                sumOfSquares = objFields("sum_of_squares").unsafeAs[Double],
                variance = objFields("variance").unsafeAs[Double],
                variancePopulation = objFields("variance_population").unsafeAs[Double],
                varianceSampling = objFields("variance_sampling").unsafeAs[Double],
                stdDeviation = objFields("std_deviation").unsafeAs[Double],
                stdDeviationPopulation = objFields("std_deviation_population").unsafeAs[Double],
                stdDeviationSampling = objFields("std_deviation_sampling").unsafeAs[Double],
                stdDeviationBoundsResponse = objFields("std_deviation_sampling").unsafeAs[StdDeviationBoundsResponse](
                  StdDeviationBoundsResponse.decoder
                )
              )
            )
          case str if str.contains("filter#") =>
            Some(field -> data.unsafeAs[FilterAggregationResponse](FilterAggregationResponse.decoder))
          case str if str.contains("max#") =>
            Some(field -> MaxAggregationResponse(value = objFields("value").unsafeAs[Double]))
          case str if str.contains("min#") =>
            Some(field -> MinAggregationResponse(value = objFields("value").unsafeAs[Double]))
          case str if str.contains("missing#") =>
            Some(field -> MissingAggregationResponse(docCount = objFields("doc_count").unsafeAs[Int]))
          case str if str.contains("percentile_ranks#") =>
            Some(
              field -> PercentileRanksAggregationResponse(values = objFields("values").unsafeAs[Map[String, Double]])
            )
          case str if str.contains("percentiles#") =>
            Some(field -> PercentilesAggregationResponse(values = objFields("values").unsafeAs[Map[String, Double]]))
          case str if str.contains("sampler#") =>
            Some(field -> data.unsafeAs[SamplerAggregationResponse](SamplerAggregationResponse.decoder))
          case str if str.contains("stats#") =>
            Some(
              field -> StatsAggregationResponse(
                count = objFields("count").unsafeAs[Int],
                min = objFields("min").unsafeAs[Double],
                max = objFields("max").unsafeAs[Double],
                avg = objFields("avg").unsafeAs[Double],
                sum = objFields("sum").unsafeAs[Double]
              )
            )
          case str if str.contains("sum#") =>
            Some(field -> SumAggregationResponse(value = objFields("value").unsafeAs[Double]))
          case str if str.contains("terms#") =>
            Some(field -> data.unsafeAs[TermsAggregationResponse](TermsAggregationResponse.decoder))
          case str if str.contains("value_count#") =>
            Some(field -> ValueCountAggregationResponse(value = objFields("value").unsafeAs[Int]))
        }
    }
  }.toMap

  val subAggs: Map[String, AggregationResponse] = allFields.collect {
    case (field, data) if field != "doc_count" && field != "key" =>
      (field: @unchecked) match {
        case str if str.contains("weighted_avg#") =>
          (field.split("#")(1), data.asInstanceOf[WeightedAvgAggregationResponse])
        case str if str.contains("avg#") =>
          (field.split("#")(1), data.asInstanceOf[AvgAggregationResponse])
        case str if str.contains("cardinality#") =>
          (field.split("#")(1), data.asInstanceOf[CardinalityAggregationResponse])
        case str if str.contains("extended_stats#") =>
          (field.split("#")(1), data.asInstanceOf[ExtendedStatsAggregationResponse])
        case str if str.contains("filter#") =>
          (field.split("#")(1), data.asInstanceOf[FilterAggregationResponse])
        case str if str.contains("max#") =>
          (field.split("#")(1), data.asInstanceOf[MaxAggregationResponse])
        case str if str.contains("min#") =>
          (field.split("#")(1), data.asInstanceOf[MinAggregationResponse])
        case str if str.contains("missing#") =>
          (field.split("#")(1), data.asInstanceOf[MissingAggregationResponse])
        case str if str.contains("percentile_ranks#") =>
          (field.split("#")(1), data.asInstanceOf[PercentileRanksAggregationResponse])
        case str if str.contains("percentiles#") =>
          (field.split("#")(1), data.asInstanceOf[PercentilesAggregationResponse])
        case str if str.contains("sampler#") =>
          (field.split("#")(1), data.asInstanceOf[SamplerAggregationResponse])
        case str if str.contains("stats#") =>
          (field.split("#")(1), data.asInstanceOf[StatsAggregationResponse])
        case str if str.contains("sum#") =>
          (field.split("#")(1), data.asInstanceOf[SumAggregationResponse])
        case str if str.contains("terms#") =>
          (field.split("#")(1), data.asInstanceOf[TermsAggregationResponse])
        case str if str.contains("value_count#") =>
          (field.split("#")(1), data.asInstanceOf[ValueCountAggregationResponse])
      }
  }
}

private[elasticsearch] final case class CardinalityAggregationResponse(value: Int) extends AggregationResponse

private[elasticsearch] object CardinalityAggregationResponse {
  implicit val decoder: JsonDecoder[CardinalityAggregationResponse] =
    DeriveJsonDecoder.gen[CardinalityAggregationResponse]
}

private[elasticsearch] final case class ExtendedStatsAggregationResponse(
  count: Int,
  min: Double,
  max: Double,
  avg: Double,
  sum: Double,
  @jsonField("sum_of_squares")
  sumOfSquares: Double,
  variance: Double,
  @jsonField("variance_population")
  variancePopulation: Double,
  @jsonField("variance_sampling")
  varianceSampling: Double,
  @jsonField("std_deviation")
  stdDeviation: Double,
  @jsonField("std_deviation_population")
  stdDeviationPopulation: Double,
  @jsonField("std_deviation_sampling")
  stdDeviationSampling: Double,
  @jsonField("std_deviation_bounds")
  stdDeviationBoundsResponse: StdDeviationBoundsResponse
) extends AggregationResponse

private[elasticsearch] object ExtendedStatsAggregationResponse {
  implicit val decoder: JsonDecoder[ExtendedStatsAggregationResponse] =
    DeriveJsonDecoder.gen[ExtendedStatsAggregationResponse]
}

private[elasticsearch] final case class FilterAggregationResponse(
  @jsonField("doc_count")
  docCount: Int,
  subAggregations: Option[Map[String, AggregationResponse]] = None
) extends AggregationResponse

private[elasticsearch] object FilterAggregationResponse {
  implicit val decoder: JsonDecoder[FilterAggregationResponse] = Obj.decoder.mapOrFail { case Obj(fields) =>
    val bucketDecoder = BucketDecoder(fields)
    val allFields     = bucketDecoder.allFields
    val docCount      = allFields("doc_count").asInstanceOf[Int]
    val subAggs       = bucketDecoder.subAggs

    Right(FilterAggregationResponse.apply(docCount, Option(subAggs).filter(_.nonEmpty)))
  }
}

private[elasticsearch] sealed trait JsonDecoderOps {
  implicit class JsonDecoderOps(json: Json) {
    def unsafeAs[A](implicit decoder: JsonDecoder[A]): A =
      (json.as[A]: @unchecked) match {
        case Right(decoded) => decoded
      }
  }
}

private[elasticsearch] final case class MaxAggregationResponse(value: Double) extends AggregationResponse

private[elasticsearch] object MaxAggregationResponse {
  implicit val decoder: JsonDecoder[MaxAggregationResponse] = DeriveJsonDecoder.gen[MaxAggregationResponse]
}

private[elasticsearch] final case class MinAggregationResponse(value: Double) extends AggregationResponse

private[elasticsearch] object MinAggregationResponse {
  implicit val decoder: JsonDecoder[MinAggregationResponse] = DeriveJsonDecoder.gen[MinAggregationResponse]
}

private[elasticsearch] final case class MissingAggregationResponse(@jsonField("doc_count") docCount: Int)
    extends AggregationResponse

private[elasticsearch] object MissingAggregationResponse {
  implicit val decoder: JsonDecoder[MissingAggregationResponse] = DeriveJsonDecoder.gen[MissingAggregationResponse]
}

private[elasticsearch] final case class PercentileRanksAggregationResponse(values: Map[String, Double])
    extends AggregationResponse

private[elasticsearch] object PercentileRanksAggregationResponse {
  implicit val decoder: JsonDecoder[PercentileRanksAggregationResponse] =
    DeriveJsonDecoder.gen[PercentileRanksAggregationResponse]
}

private[elasticsearch] final case class PercentilesAggregationResponse(values: Map[String, Double])
    extends AggregationResponse

private[elasticsearch] object PercentilesAggregationResponse {
  implicit val decoder: JsonDecoder[PercentilesAggregationResponse] =
    DeriveJsonDecoder.gen[PercentilesAggregationResponse]
}

private[elasticsearch] final case class SamplerAggregationResponse(
  @jsonField("doc_count")
  docCount: Int,
  subAggregations: Option[Map[String, AggregationResponse]] = None
) extends AggregationResponse

private[elasticsearch] object SamplerAggregationResponse {
  implicit val decoder: JsonDecoder[SamplerAggregationResponse] = Obj.decoder.mapOrFail { case Obj(fields) =>
    val bucketDecoder = BucketDecoder(fields)
    val allFields     = bucketDecoder.allFields
    val docCount      = allFields("doc_count").asInstanceOf[Int]
    val subAggs       = bucketDecoder.subAggs

    Right(SamplerAggregationResponse.apply(docCount, Option(subAggs).filter(_.nonEmpty)))
  }
}

private[elasticsearch] final case class StatsAggregationResponse(
  count: Int,
  min: Double,
  max: Double,
  avg: Double,
  sum: Double
) extends AggregationResponse

private[elasticsearch] object StatsAggregationResponse {
  implicit val decoder: JsonDecoder[StatsAggregationResponse] = DeriveJsonDecoder.gen[StatsAggregationResponse]
}

private[elasticsearch] case class StdDeviationBoundsResponse(
  upper: Double,
  lower: Double,
  @jsonField("upper_population")
  upperPopulation: Double,
  @jsonField("lower_population")
  lowerPopulation: Double,
  @jsonField("upper_sampling")
  upperSampling: Double,
  @jsonField("lower_sampling")
  lowerSampling: Double
) extends AggregationResponse

private[elasticsearch] object StdDeviationBoundsResponse {
  implicit val decoder: JsonDecoder[StdDeviationBoundsResponse] =
    DeriveJsonDecoder.gen[StdDeviationBoundsResponse]
}

private[elasticsearch] final case class SumAggregationResponse(value: Double) extends AggregationResponse

private[elasticsearch] object SumAggregationResponse {
  implicit val decoder: JsonDecoder[SumAggregationResponse] = DeriveJsonDecoder.gen[SumAggregationResponse]
}

private[elasticsearch] final case class TermsAggregationResponse(
  @jsonField("doc_count_error_upper_bound")
  docErrorCount: Int,
  @jsonField("sum_other_doc_count")
  sumOtherDocCount: Int,
  buckets: Chunk[TermsAggregationBucket]
) extends AggregationResponse

private[elasticsearch] object TermsAggregationResponse {
  implicit val decoder: JsonDecoder[TermsAggregationResponse] = DeriveJsonDecoder.gen[TermsAggregationResponse]
}

private[elasticsearch] final case class TermsAggregationBucket(
  key: String,
  @jsonField("doc_count")
  docCount: Int,
  subAggregations: Option[Map[String, AggregationResponse]] = None
) extends AggregationBucket

private[elasticsearch] object TermsAggregationBucket {
  implicit val decoder: JsonDecoder[TermsAggregationBucket] = Obj.decoder.mapOrFail { case Obj(fields) =>
    val bucketDecoder = BucketDecoder(fields)
    val allFields     = bucketDecoder.allFields
    val docCount      = allFields("doc_count").asInstanceOf[Int]
    val key           = allFields("key").asInstanceOf[String]
    val subAggs       = bucketDecoder.subAggs

    Right(TermsAggregationBucket.apply(key, docCount, Option(subAggs).filter(_.nonEmpty)))
  }
}

private[elasticsearch] final case class ValueCountAggregationResponse(value: Int) extends AggregationResponse

private[elasticsearch] object ValueCountAggregationResponse {
  implicit val decoder: JsonDecoder[ValueCountAggregationResponse] =
    DeriveJsonDecoder.gen[ValueCountAggregationResponse]
}

private[elasticsearch] final case class WeightedAvgAggregationResponse(value: Double) extends AggregationResponse

private[elasticsearch] object WeightedAvgAggregationResponse {
  implicit val decoder: JsonDecoder[WeightedAvgAggregationResponse] =
    DeriveJsonDecoder.gen[WeightedAvgAggregationResponse]
}
