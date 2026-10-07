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
import zio.json.ast.Json.{Arr, Obj, Str}
import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField}
import zio.prelude.Validation

private[elasticsearch] sealed trait AggregationBucket

sealed trait AggregationResponse

object AggregationResponse {
  private[elasticsearch] def decode(field: String, data: Json): Either[String, (String, AggregationResponse)] = {
    val (tpe, name) = field.span(_ != '#') match { case (tpe, rest) => (tpe, rest.drop(1)) }

    def as[A <: AggregationResponse](implicit decoder: JsonDecoder[A]): Either[String, (String, AggregationResponse)] =
      data.as[A].map(name -> _).left.map(error => s"$field$error")

    tpe match {
      case "avg"                               => as[AvgAggregationResponse]
      case "cardinality"                       => as[CardinalityAggregationResponse]
      case "extended_stats"                    => as[ExtendedStatsAggregationResponse]
      case "filter"                            => as[FilterAggregationResponse]
      case "ip_range"                          => as[IpRangeAggregationResponse]
      case "max"                               => as[MaxAggregationResponse]
      case "min"                               => as[MinAggregationResponse]
      case "missing"                           => as[MissingAggregationResponse]
      case t if t.endsWith("percentile_ranks") => as[PercentileRanksAggregationResponse]
      case t if t.endsWith("percentiles")      => as[PercentilesAggregationResponse]
      case "sampler"                           => as[SamplerAggregationResponse]
      case "stats"                             => as[StatsAggregationResponse]
      case "sum"                               => as[SumAggregationResponse]
      case t if t.endsWith("terms")            => as[TermsAggregationResponse]
      case "value_count"                       => as[ValueCountAggregationResponse]
      case "weighted_avg"                      => as[WeightedAvgAggregationResponse]
      case _                                   => Left(s"Unsupported aggregation: $field")
    }
  }

  private[elasticsearch] def decodeAll(
    fields: Chunk[(String, Json)]
  ): Either[String, Map[String, AggregationResponse]] =
    Validation
      .validateAll(fields.map { case (field, data) => Validation.fromEither(decode(field, data)) })
      .map(_.toMap)
      .toEitherWith(_.mkString(", "))

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
      case IpRangeAggregationResponse(buckets) =>
        IpRangeAggregationResult(
          buckets = buckets.map(b =>
            IpRangeAggregationBucketResult(
              key = b.key,
              from = b.from,
              to = b.to,
              docCount = b.docCount,
              subAggregations =
                b.subAggregations.map(_.map { case (key, response) => (key, toResult(response)) }).getOrElse(Map.empty)
            )
          )
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

private[elasticsearch] final case class AvgAggregationResponse(value: Option[Double]) extends AggregationResponse

private[elasticsearch] object AvgAggregationResponse {
  implicit val decoder: JsonDecoder[AvgAggregationResponse] = DeriveJsonDecoder.gen[AvgAggregationResponse]
}

private[elasticsearch] final case class BucketDecoder(fields: Chunk[(String, Json)]) {
  lazy val docCount: Either[String, Int] =
    fields.collectFirst { case ("doc_count", data) => data.as[Int] }.getOrElse(Left("Missing field: doc_count"))

  lazy val key: Either[String, String] =
    fields.collectFirst { case ("key", data) => Right(data.toString.replaceAll("\"", "")) }
      .getOrElse(Left("Missing field: key"))

  lazy val subAggs: Either[String, Map[String, AggregationResponse]] =
    AggregationResponse.decodeAll(fields.filterNot { case (field, _) => BucketDecoder.metadataFields.contains(field) })
}

private[elasticsearch] object BucketDecoder {
  private val metadataFields: Set[String] = Set("doc_count", "doc_count_error_upper_bound", "key", "key_as_string")
}

private[elasticsearch] final case class CardinalityAggregationResponse(value: Int) extends AggregationResponse

private[elasticsearch] object CardinalityAggregationResponse {
  implicit val decoder: JsonDecoder[CardinalityAggregationResponse] =
    DeriveJsonDecoder.gen[CardinalityAggregationResponse]
}

private[elasticsearch] final case class ExtendedStatsAggregationResponse(
  count: Int,
  min: Option[Double],
  max: Option[Double],
  avg: Option[Double],
  sum: Double,
  @jsonField("sum_of_squares")
  sumOfSquares: Option[Double],
  variance: Option[Double],
  @jsonField("variance_population")
  variancePopulation: Option[Double],
  @jsonField("variance_sampling")
  varianceSampling: Option[Double],
  @jsonField("std_deviation")
  stdDeviation: Option[Double],
  @jsonField("std_deviation_population")
  stdDeviationPopulation: Option[Double],
  @jsonField("std_deviation_sampling")
  stdDeviationSampling: Option[Double],
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

    for {
      docCount <- bucketDecoder.docCount
      subAggs  <- bucketDecoder.subAggs
    } yield FilterAggregationResponse(docCount, Some(subAggs).filter(_.nonEmpty))
  }
}

private[elasticsearch] final case class IpRangeAggregationBucket(
  key: String,
  from: Option[String],
  to: Option[String],
  docCount: Int,
  subAggregations: Option[Map[String, AggregationResponse]]
) extends AggregationBucket

private[elasticsearch] object IpRangeAggregationBucket {
  implicit val decoder: JsonDecoder[IpRangeAggregationBucket] = Obj.decoder.mapOrFail { case Obj(fields) =>
    val bucketDecoder = BucketDecoder(fields.filterNot { case (field, _) => field == "from" || field == "to" })

    for {
      key      <- bucketDecoder.key
      docCount <- bucketDecoder.docCount
      subAggs  <- bucketDecoder.subAggs
    } yield IpRangeAggregationBucket(
      key = key,
      from = fields.collectFirst { case ("from", Str(value)) => value },
      to = fields.collectFirst { case ("to", Str(value)) => value },
      docCount = docCount,
      subAggregations = Some(subAggs).filter(_.nonEmpty)
    )
  }
}

private[elasticsearch] final case class IpRangeAggregationResponse(buckets: Chunk[IpRangeAggregationBucket])
    extends AggregationResponse

private[elasticsearch] object IpRangeAggregationResponse {
  implicit val decoder: JsonDecoder[IpRangeAggregationResponse] = Obj.decoder.mapOrFail { case Obj(fields) =>
    val buckets: Chunk[Json] = fields.collectFirst { case ("buckets", buckets) => buckets } match {
      case Some(Arr(buckets)) =>
        buckets
      case Some(Obj(keyedBuckets)) =>
        keyedBuckets.map {
          case (key, Obj(bucketFields)) if !bucketFields.exists(_._1 == "key") =>
            Obj(("key" -> Str(key)) +: bucketFields)
          case (_, bucket) => bucket
        }
      case _ =>
        Chunk.empty
    }

    buckets
      .foldLeft[Either[String, Chunk[IpRangeAggregationBucket]]](Right(Chunk.empty)) { (acc, bucket) =>
        for {
          decoded <- acc
          next    <- bucket.as[IpRangeAggregationBucket]
        } yield decoded :+ next
      }
      .map(IpRangeAggregationResponse(_))
  }
}

private[elasticsearch] final case class MaxAggregationResponse(value: Option[Double]) extends AggregationResponse

private[elasticsearch] object MaxAggregationResponse {
  implicit val decoder: JsonDecoder[MaxAggregationResponse] = DeriveJsonDecoder.gen[MaxAggregationResponse]
}

private[elasticsearch] final case class MinAggregationResponse(value: Option[Double]) extends AggregationResponse

private[elasticsearch] object MinAggregationResponse {
  implicit val decoder: JsonDecoder[MinAggregationResponse] = DeriveJsonDecoder.gen[MinAggregationResponse]
}

private[elasticsearch] final case class MissingAggregationResponse(@jsonField("doc_count") docCount: Int)
    extends AggregationResponse

private[elasticsearch] object MissingAggregationResponse {
  implicit val decoder: JsonDecoder[MissingAggregationResponse] = DeriveJsonDecoder.gen[MissingAggregationResponse]
}

private[elasticsearch] final case class PercentileRanksAggregationResponse(values: Map[String, Option[Double]])
    extends AggregationResponse

private[elasticsearch] object PercentileRanksAggregationResponse {
  implicit val decoder: JsonDecoder[PercentileRanksAggregationResponse] =
    DeriveJsonDecoder.gen[PercentileRanksAggregationResponse]
}

private[elasticsearch] final case class PercentilesAggregationResponse(values: Map[String, Option[Double]])
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

    for {
      docCount <- bucketDecoder.docCount
      subAggs  <- bucketDecoder.subAggs
    } yield SamplerAggregationResponse(docCount, Some(subAggs).filter(_.nonEmpty))
  }
}

private[elasticsearch] final case class StatsAggregationResponse(
  count: Int,
  min: Option[Double],
  max: Option[Double],
  avg: Option[Double],
  sum: Double
) extends AggregationResponse

private[elasticsearch] object StatsAggregationResponse {
  implicit val decoder: JsonDecoder[StatsAggregationResponse] = DeriveJsonDecoder.gen[StatsAggregationResponse]
}

private[elasticsearch] case class StdDeviationBoundsResponse(
  upper: Option[Double],
  lower: Option[Double],
  @jsonField("upper_population")
  upperPopulation: Option[Double],
  @jsonField("lower_population")
  lowerPopulation: Option[Double],
  @jsonField("upper_sampling")
  upperSampling: Option[Double],
  @jsonField("lower_sampling")
  lowerSampling: Option[Double]
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

    for {
      key      <- bucketDecoder.key
      docCount <- bucketDecoder.docCount
      subAggs  <- bucketDecoder.subAggs
    } yield TermsAggregationBucket(key, docCount, Some(subAggs).filter(_.nonEmpty))
  }
}

private[elasticsearch] final case class ValueCountAggregationResponse(value: Int) extends AggregationResponse

private[elasticsearch] object ValueCountAggregationResponse {
  implicit val decoder: JsonDecoder[ValueCountAggregationResponse] =
    DeriveJsonDecoder.gen[ValueCountAggregationResponse]
}

private[elasticsearch] final case class WeightedAvgAggregationResponse(value: Option[Double])
    extends AggregationResponse

private[elasticsearch] object WeightedAvgAggregationResponse {
  implicit val decoder: JsonDecoder[WeightedAvgAggregationResponse] =
    DeriveJsonDecoder.gen[WeightedAvgAggregationResponse]
}
