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

package zio.elasticsearch.result

import zio.Chunk

import scala.util.{Failure, Success, Try}

sealed trait AggregationResult

final case class AvgAggregationResult private[elasticsearch] (value: Double) extends AggregationResult

final case class CardinalityAggregationResult private[elasticsearch] (value: Int) extends AggregationResult

private[elasticsearch] case class StdDeviationBoundsResult(
  upper: Double,
  lower: Double,
  upperPopulation: Double,
  lowerPopulation: Double,
  upperSampling: Double,
  lowerSampling: Double
) extends AggregationResult

final case class ExtendedStatsAggregationResult private[elasticsearch] (
  count: Int,
  min: Double,
  max: Double,
  avg: Double,
  sum: Double,
  sumOfSquares: Double,
  variance: Double,
  variancePopulation: Double,
  varianceSampling: Double,
  stdDeviation: Double,
  stdDeviationPopulation: Double,
  stdDeviationSampling: Double,
  stdDeviationBoundsResult: StdDeviationBoundsResult
) extends AggregationResult

final case class MaxAggregationResult private[elasticsearch] (value: Double) extends AggregationResult

final case class MinAggregationResult private[elasticsearch] (value: Double) extends AggregationResult

final case class MissingAggregationResult private[elasticsearch] (docCount: Int) extends AggregationResult

final case class PercentilesAggregationResult private[elasticsearch] (values: Map[String, Double])
    extends AggregationResult

final case class StatsAggregationResult private[elasticsearch] (
  count: Int,
  min: Double,
  max: Double,
  avg: Double,
  sum: Double
) extends AggregationResult

final case class SumAggregationResult private[elasticsearch] (value: Double) extends AggregationResult

final case class TermsAggregationResult private[elasticsearch] (
  docErrorCount: Int,
  sumOtherDocCount: Int,
  buckets: Chunk[TermsAggregationBucketResult]
) extends AggregationResult

final case class TermsAggregationBucketResult private[elasticsearch] (
  key: String,
  docCount: Int,
  subAggregations: Map[String, AggregationResult]
) extends AggregationResult {

  def subAggregationAs[A <: AggregationResult](aggName: String): Either[DecodingException, Option[A]] =
    subAggregations.get(aggName) match {
      case Some(aggRes) =>
        Try(aggRes.asInstanceOf[A]) match {
          case Failure(_)   => Left(DecodingException(s"Aggregation with name $aggName was not of type you provided."))
          case Success(agg) => Right(Some(agg))
        }
      case None =>
        Right(None)
    }
}

final case class ValueCountAggregationResult private[elasticsearch] (value: Int) extends AggregationResult

final case class WeightedAvgAggregationResult private[elasticsearch] (value: Double) extends AggregationResult
