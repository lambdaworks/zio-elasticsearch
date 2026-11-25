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
import zio.json.JsonDecoder
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Num, Obj, Str}
import zio.prelude.Validation

import scala.reflect.ClassTag

/**
 * The result of an aggregation.
 */
sealed trait AggregationResult {

  /**
   * The `meta` object that was set on the aggregation, or an empty map if none was set.
   */
  def meta: Map[String, Json]
}

object AggregationResult {

  /**
   * Decoders of all supported aggregation results, by the typed key prefixes that Elasticsearch returns for them (e.g.
   * `sterms` in `sterms#name`).
   */
  private lazy val decoders: Map[String, JsonDecoder[AggregationResult]] =
    Chunk[AggregationResultDecoder[_ <: AggregationResult]](
      AvgAggregationResult,
      BucketMetricValueAggregationResult,
      CardinalityAggregationResult,
      DateHistogramAggregationResult,
      DerivativeAggregationResult,
      ExtendedStatsAggregationResult,
      FilterAggregationResult,
      FiltersAggregationResult,
      HistogramAggregationResult,
      IpRangeAggregationResult,
      MaxAggregationResult,
      MinAggregationResult,
      MissingAggregationResult,
      PercentileRanksAggregationResult,
      PercentilesAggregationResult,
      RangeAggregationResult,
      SamplerAggregationResult,
      SimpleValueAggregationResult,
      StatsAggregationResult,
      SumAggregationResult,
      TermsAggregationResult,
      ValueCountAggregationResult,
      WeightedAvgAggregationResult
    ).flatMap(result => result.typedKeys.map(_ -> result.widenedDecoder)).toMap

  /**
   * Decodes aggregation results from fields whose names are typed keys (`type#name`), keyed by aggregation name.
   */
  private[elasticsearch] def decodeAll(fields: Chunk[(String, Json)]): Either[String, Map[String, AggregationResult]] =
    Validation
      .validateAll(fields.map { case (field, data) => Validation.fromEither(decode(field, data)) })
      .map(_.toMap)
      .toEitherWith(_.mkString(", "))

  private def decode(field: String, data: Json): Either[String, (String, AggregationResult)] = {
    val (typedKey, name) = field.span(_ != '#') match { case (typedKey, rest) => (typedKey, rest.drop(1)) }

    decoders.get(typedKey) match {
      case Some(decoder) => data.as(decoder).map(name -> _).left.map(error => s"$field$error")
      case None          => Left(s"Unsupported aggregation: $field")
    }
  }
}

/**
 * A result that contains the results of sub-aggregations, such as a single bucket aggregation result or a bucket of a
 * multi bucket aggregation result.
 */
sealed trait HasSubAggregations {

  /**
   * The results of the sub-aggregations, by aggregation name.
   */
  def subAggregations: Map[String, AggregationResult]

  /**
   * Returns the result of the sub-aggregation with the given name as an instance of `A`.
   *
   * @param aggName
   *   the name of the sub-aggregation
   * @tparam A
   *   the expected type of the sub-aggregation result, a subtype of [[AggregationResult]]
   * @return
   *   the sub-aggregation result, `None` if there is no sub-aggregation with the given name, or a [[DecodingException]]
   *   if the sub-aggregation result is not of type `A`.
   */
  final def subAggregationAs[A <: AggregationResult: ClassTag](aggName: String): Either[DecodingException, Option[A]] =
    subAggregations.get(aggName) match {
      case Some(agg: A) => Right(Some(agg))
      case Some(_)      => Left(DecodingException(s"Aggregation with name $aggName was not of type you provided."))
      case None         => Right(None)
    }
}

/**
 * The result of a single bucket aggregation (e.g. `filter`).
 */
sealed trait SingleBucketAggregationResult extends AggregationResult with HasSubAggregations {

  /**
   * The number of documents in the bucket.
   */
  def docCount: Long
}

/**
 * The result of a multi bucket aggregation (e.g. `terms`).
 *
 * @tparam B
 *   the type of the buckets
 */
sealed trait MultiBucketAggregationResult[+B <: AggregationBucketResult[_]] extends AggregationResult {

  /**
   * The buckets, in the order in which Elasticsearch returned them.
   */
  def buckets: Chunk[B]
}

/**
 * A bucket of a multi bucket aggregation result.
 *
 * @tparam K
 *   the type of the bucket key
 */
sealed trait AggregationBucketResult[+K] extends HasSubAggregations {

  /**
   * The number of documents in the bucket.
   */
  def docCount: Long

  /**
   * The key of the bucket.
   */
  def key: K
}

final case class AvgAggregationResult private[elasticsearch] (
  value: Option[Double],
  meta: Map[String, Json] = Map.empty
) extends AggregationResult

object AvgAggregationResult extends AggregationResultDecoder[AvgAggregationResult]("avg") {
  private[elasticsearch] implicit val decoder: JsonDecoder[AvgAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        value <- fields.optional[Double]("value")
        meta  <- fields.meta
      } yield AvgAggregationResult(value = value, meta = meta)
    )
}

/**
 * The result of a `min_bucket` or `max_bucket` aggregation.
 *
 * @param value
 *   the minimum or maximum value of the metric, or `None` if there are no buckets with a value
 * @param valueAsString
 *   the formatted value, if a format was set
 * @param keys
 *   the keys of the buckets with that value
 */
final case class BucketMetricValueAggregationResult private[elasticsearch] (
  value: Option[Double],
  valueAsString: Option[String],
  keys: Chunk[String],
  meta: Map[String, Json] = Map.empty
) extends AggregationResult

object BucketMetricValueAggregationResult
    extends AggregationResultDecoder[BucketMetricValueAggregationResult]("bucket_metric_value") {
  private[elasticsearch] implicit val decoder: JsonDecoder[BucketMetricValueAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        value         <- fields.optional[Double]("value")
        valueAsString <- fields.optional[String]("value_as_string")
        keys          <- fields.optional[Chunk[String]]("keys")
        meta          <- fields.meta
      } yield BucketMetricValueAggregationResult(
        value = value,
        valueAsString = valueAsString,
        keys = keys.getOrElse(Chunk.empty),
        meta = meta
      )
    )
}

final case class CardinalityAggregationResult private[elasticsearch] (
  value: Long,
  meta: Map[String, Json] = Map.empty
) extends AggregationResult

object CardinalityAggregationResult extends AggregationResultDecoder[CardinalityAggregationResult]("cardinality") {
  private[elasticsearch] implicit val decoder: JsonDecoder[CardinalityAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        value <- fields.required[Long]("value")
        meta  <- fields.meta
      } yield CardinalityAggregationResult(value = value, meta = meta)
    )
}

final case class DateHistogramAggregationResult private[elasticsearch] (
  buckets: Chunk[DateHistogramAggregationBucketResult],
  meta: Map[String, Json] = Map.empty
) extends MultiBucketAggregationResult[DateHistogramAggregationBucketResult]

object DateHistogramAggregationResult
    extends AggregationResultDecoder[DateHistogramAggregationResult]("date_histogram") {
  private[elasticsearch] implicit val decoder: JsonDecoder[DateHistogramAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        buckets <- fields.buckets[DateHistogramAggregationBucketResult]
        meta    <- fields.meta
      } yield DateHistogramAggregationResult(buckets = buckets, meta = meta)
    )
}

/**
 * A bucket of a [[DateHistogramAggregationResult]].
 *
 * @param key
 *   the start of the bucket, in milliseconds since the epoch
 * @param keyAsString
 *   the formatted start of the bucket (e.g. `"2024-01-01T00:00:00.000Z"`), which Elasticsearch returns for date fields
 */
final case class DateHistogramAggregationBucketResult private[elasticsearch] (
  key: Long,
  keyAsString: Option[String],
  docCount: Long,
  subAggregations: Map[String, AggregationResult]
) extends AggregationBucketResult[Long]

object DateHistogramAggregationBucketResult {
  private[elasticsearch] implicit val decoder: JsonDecoder[DateHistogramAggregationBucketResult] =
    ResultFields.decoder(fields =>
      for {
        key             <- fields.required[Long]("key")
        keyAsString     <- fields.optional[String]("key_as_string")
        docCount        <- fields.docCount
        subAggregations <- fields.subAggregations
      } yield DateHistogramAggregationBucketResult(
        key = key,
        keyAsString = keyAsString,
        docCount = docCount,
        subAggregations = subAggregations
      )
    )
}

/**
 * The result of a `derivative` aggregation, in a bucket of its parent aggregation.
 *
 * @param value
 *   the derivative, or `None` if it cannot be calculated for the bucket (e.g. for the first bucket)
 * @param valueAsString
 *   the formatted derivative, if a format was set
 * @param normalizedValue
 *   the derivative per the `unit` of the aggregation, if a unit was set
 */
final case class DerivativeAggregationResult private[elasticsearch] (
  value: Option[Double],
  valueAsString: Option[String],
  normalizedValue: Option[Double],
  meta: Map[String, Json] = Map.empty
) extends AggregationResult

object DerivativeAggregationResult extends AggregationResultDecoder[DerivativeAggregationResult]("derivative") {
  private[elasticsearch] implicit val decoder: JsonDecoder[DerivativeAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        value           <- fields.optional[Double]("value")
        valueAsString   <- fields.optional[String]("value_as_string")
        normalizedValue <- fields.optional[Double]("normalized_value")
        meta            <- fields.meta
      } yield DerivativeAggregationResult(
        value = value,
        valueAsString = valueAsString,
        normalizedValue = normalizedValue,
        meta = meta
      )
    )
}

final case class ExtendedStatsAggregationResult private[elasticsearch] (
  count: Long,
  min: Option[Double],
  max: Option[Double],
  avg: Option[Double],
  sum: Double,
  sumOfSquares: Option[Double],
  variance: Option[Double],
  variancePopulation: Option[Double],
  varianceSampling: Option[Double],
  stdDeviation: Option[Double],
  stdDeviationPopulation: Option[Double],
  stdDeviationSampling: Option[Double],
  stdDeviationBoundsResult: StdDeviationBoundsResult,
  meta: Map[String, Json] = Map.empty
) extends AggregationResult

object ExtendedStatsAggregationResult
    extends AggregationResultDecoder[ExtendedStatsAggregationResult]("extended_stats") {
  private[elasticsearch] implicit val decoder: JsonDecoder[ExtendedStatsAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        count                  <- fields.required[Long]("count")
        min                    <- fields.optional[Double]("min")
        max                    <- fields.optional[Double]("max")
        avg                    <- fields.optional[Double]("avg")
        sum                    <- fields.required[Double]("sum")
        sumOfSquares           <- fields.optional[Double]("sum_of_squares")
        variance               <- fields.optional[Double]("variance")
        variancePopulation     <- fields.optional[Double]("variance_population")
        varianceSampling       <- fields.optional[Double]("variance_sampling")
        stdDeviation           <- fields.optional[Double]("std_deviation")
        stdDeviationPopulation <- fields.optional[Double]("std_deviation_population")
        stdDeviationSampling   <- fields.optional[Double]("std_deviation_sampling")
        stdDeviationBounds     <- fields.required[StdDeviationBoundsResult]("std_deviation_bounds")
        meta                   <- fields.meta
      } yield ExtendedStatsAggregationResult(
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
        stdDeviationBoundsResult = stdDeviationBounds,
        meta = meta
      )
    )
}

final case class FilterAggregationResult private[elasticsearch] (
  docCount: Long,
  subAggregations: Map[String, AggregationResult],
  meta: Map[String, Json] = Map.empty
) extends SingleBucketAggregationResult

object FilterAggregationResult extends AggregationResultDecoder[FilterAggregationResult]("filter") {
  private[elasticsearch] implicit val decoder: JsonDecoder[FilterAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        docCount        <- fields.docCount
        subAggregations <- fields.subAggregations
        meta            <- fields.meta
      } yield FilterAggregationResult(docCount = docCount, subAggregations = subAggregations, meta = meta)
    )
}

/**
 * The result of a `filters` aggregation.
 *
 * @param buckets
 *   a bucket for each filter, followed by the bucket for the documents that do not match any of the filters, if it was
 *   enabled. For named filters, Elasticsearch returns the buckets ordered by key.
 */
final case class FiltersAggregationResult private[elasticsearch] (
  buckets: Chunk[FiltersAggregationBucketResult],
  meta: Map[String, Json] = Map.empty
) extends MultiBucketAggregationResult[FiltersAggregationBucketResult] {

  /**
   * The buckets of named filters by key, or an empty map for anonymous filters.
   */
  def bucketsByKey: Map[String, FiltersAggregationBucketResult] =
    buckets.flatMap(bucket => bucket.key.map(_ -> bucket)).toMap
}

object FiltersAggregationResult extends AggregationResultDecoder[FiltersAggregationResult]("filters") {
  private[elasticsearch] implicit val decoder: JsonDecoder[FiltersAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        buckets <- fields.buckets[FiltersAggregationBucketResult]
        meta    <- fields.meta
      } yield FiltersAggregationResult(buckets = buckets, meta = meta)
    )
}

/**
 * A bucket of a [[FiltersAggregationResult]].
 *
 * @param key
 *   the name of the filter of the bucket, or `None` for anonymous filters
 */
final case class FiltersAggregationBucketResult private[elasticsearch] (
  key: Option[String],
  docCount: Long,
  subAggregations: Map[String, AggregationResult]
) extends AggregationBucketResult[Option[String]]

object FiltersAggregationBucketResult {
  private[elasticsearch] implicit val decoder: JsonDecoder[FiltersAggregationBucketResult] =
    ResultFields.decoder(fields =>
      for {
        key             <- fields.optional[String]("key")
        docCount        <- fields.docCount
        subAggregations <- fields.subAggregations
      } yield FiltersAggregationBucketResult(key = key, docCount = docCount, subAggregations = subAggregations)
    )
}

final case class HistogramAggregationResult private[elasticsearch] (
  buckets: Chunk[HistogramAggregationBucketResult],
  meta: Map[String, Json] = Map.empty
) extends MultiBucketAggregationResult[HistogramAggregationBucketResult]

object HistogramAggregationResult extends AggregationResultDecoder[HistogramAggregationResult]("histogram") {
  private[elasticsearch] implicit val decoder: JsonDecoder[HistogramAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        buckets <- fields.buckets[HistogramAggregationBucketResult]
        meta    <- fields.meta
      } yield HistogramAggregationResult(buckets = buckets, meta = meta)
    )
}

/**
 * A bucket of a [[HistogramAggregationResult]].
 *
 * @param key
 *   the start of the bucket
 * @param keyAsString
 *   the formatted start of the bucket, if Elasticsearch returned one
 */
final case class HistogramAggregationBucketResult private[elasticsearch] (
  key: Double,
  keyAsString: Option[String],
  docCount: Long,
  subAggregations: Map[String, AggregationResult]
) extends AggregationBucketResult[Double]

object HistogramAggregationBucketResult {
  private[elasticsearch] implicit val decoder: JsonDecoder[HistogramAggregationBucketResult] =
    ResultFields.decoder(fields =>
      for {
        key             <- fields.required[Double]("key")
        keyAsString     <- fields.optional[String]("key_as_string")
        docCount        <- fields.docCount
        subAggregations <- fields.subAggregations
      } yield HistogramAggregationBucketResult(
        key = key,
        keyAsString = keyAsString,
        docCount = docCount,
        subAggregations = subAggregations
      )
    )
}

final case class IpRangeAggregationResult private[elasticsearch] (
  buckets: Chunk[IpRangeAggregationBucketResult],
  meta: Map[String, Json] = Map.empty
) extends MultiBucketAggregationResult[IpRangeAggregationBucketResult]

object IpRangeAggregationResult extends AggregationResultDecoder[IpRangeAggregationResult]("ip_range") {
  private[elasticsearch] implicit val decoder: JsonDecoder[IpRangeAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        buckets <- fields.buckets[IpRangeAggregationBucketResult]
        meta    <- fields.meta
      } yield IpRangeAggregationResult(buckets = buckets, meta = meta)
    )
}

final case class IpRangeAggregationBucketResult private[elasticsearch] (
  key: String,
  from: Option[String],
  to: Option[String],
  docCount: Long,
  subAggregations: Map[String, AggregationResult]
) extends AggregationBucketResult[String]

object IpRangeAggregationBucketResult {
  private[elasticsearch] implicit val decoder: JsonDecoder[IpRangeAggregationBucketResult] =
    ResultFields.decoder(fields =>
      for {
        key             <- fields.required[String]("key")
        from            <- fields.optional[String]("from")
        to              <- fields.optional[String]("to")
        docCount        <- fields.docCount
        subAggregations <- fields.subAggregations
      } yield IpRangeAggregationBucketResult(
        key = key,
        from = from,
        to = to,
        docCount = docCount,
        subAggregations = subAggregations
      )
    )
}

final case class MaxAggregationResult private[elasticsearch] (
  value: Option[Double],
  meta: Map[String, Json] = Map.empty
) extends AggregationResult

object MaxAggregationResult extends AggregationResultDecoder[MaxAggregationResult]("max") {
  private[elasticsearch] implicit val decoder: JsonDecoder[MaxAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        value <- fields.optional[Double]("value")
        meta  <- fields.meta
      } yield MaxAggregationResult(value = value, meta = meta)
    )
}

final case class MinAggregationResult private[elasticsearch] (
  value: Option[Double],
  meta: Map[String, Json] = Map.empty
) extends AggregationResult

object MinAggregationResult extends AggregationResultDecoder[MinAggregationResult]("min") {
  private[elasticsearch] implicit val decoder: JsonDecoder[MinAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        value <- fields.optional[Double]("value")
        meta  <- fields.meta
      } yield MinAggregationResult(value = value, meta = meta)
    )
}

final case class MissingAggregationResult private[elasticsearch] (
  docCount: Long,
  subAggregations: Map[String, AggregationResult],
  meta: Map[String, Json] = Map.empty
) extends SingleBucketAggregationResult

object MissingAggregationResult extends AggregationResultDecoder[MissingAggregationResult]("missing") {
  private[elasticsearch] implicit val decoder: JsonDecoder[MissingAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        docCount        <- fields.docCount
        subAggregations <- fields.subAggregations
        meta            <- fields.meta
      } yield MissingAggregationResult(docCount = docCount, subAggregations = subAggregations, meta = meta)
    )
}

/**
 * The result of a `percentile_ranks` aggregation.
 *
 * @param values
 *   the percentile rank of each requested value, keyed by the value as returned by Elasticsearch (e.g. `"500.0"`)
 */
final case class PercentileRanksAggregationResult private[elasticsearch] (
  values: Map[String, Option[Double]],
  meta: Map[String, Json] = Map.empty
) extends AggregationResult

object PercentileRanksAggregationResult
    extends AggregationResultDecoder[PercentileRanksAggregationResult](
      "tdigest_percentile_ranks",
      "hdr_percentile_ranks"
    ) {
  private[elasticsearch] implicit val decoder: JsonDecoder[PercentileRanksAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        values <- fields.percentileValues
        meta   <- fields.meta
      } yield PercentileRanksAggregationResult(values = values, meta = meta)
    )
}

/**
 * The result of a `percentiles` aggregation.
 *
 * @param values
 *   the value at each requested percentile, keyed by the percentile as returned by Elasticsearch (e.g. `"99.0"`)
 */
final case class PercentilesAggregationResult private[elasticsearch] (
  values: Map[String, Option[Double]],
  meta: Map[String, Json] = Map.empty
) extends AggregationResult

object PercentilesAggregationResult
    extends AggregationResultDecoder[PercentilesAggregationResult]("tdigest_percentiles", "hdr_percentiles") {
  private[elasticsearch] implicit val decoder: JsonDecoder[PercentilesAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        values <- fields.percentileValues
        meta   <- fields.meta
      } yield PercentilesAggregationResult(values = values, meta = meta)
    )
}

final case class RangeAggregationResult private[elasticsearch] (
  buckets: Chunk[RangeAggregationBucketResult],
  meta: Map[String, Json] = Map.empty
) extends MultiBucketAggregationResult[RangeAggregationBucketResult]

object RangeAggregationResult extends AggregationResultDecoder[RangeAggregationResult]("range") {
  private[elasticsearch] implicit val decoder: JsonDecoder[RangeAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        buckets <- fields.buckets[RangeAggregationBucketResult]
        meta    <- fields.meta
      } yield RangeAggregationResult(buckets = buckets, meta = meta)
    )
}

final case class RangeAggregationBucketResult private[elasticsearch] (
  key: String,
  from: Option[Double],
  to: Option[Double],
  docCount: Long,
  subAggregations: Map[String, AggregationResult]
) extends AggregationBucketResult[String]

object RangeAggregationBucketResult {
  private[elasticsearch] implicit val decoder: JsonDecoder[RangeAggregationBucketResult] =
    ResultFields.decoder(fields =>
      for {
        key             <- fields.required[String]("key")
        from            <- fields.optional[Double]("from")
        to              <- fields.optional[Double]("to")
        docCount        <- fields.docCount
        subAggregations <- fields.subAggregations
      } yield RangeAggregationBucketResult(
        key = key,
        from = from,
        to = to,
        docCount = docCount,
        subAggregations = subAggregations
      )
    )
}

final case class SamplerAggregationResult private[elasticsearch] (
  docCount: Long,
  subAggregations: Map[String, AggregationResult],
  meta: Map[String, Json] = Map.empty
) extends SingleBucketAggregationResult

object SamplerAggregationResult extends AggregationResultDecoder[SamplerAggregationResult]("sampler") {
  private[elasticsearch] implicit val decoder: JsonDecoder[SamplerAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        docCount        <- fields.docCount
        subAggregations <- fields.subAggregations
        meta            <- fields.meta
      } yield SamplerAggregationResult(docCount = docCount, subAggregations = subAggregations, meta = meta)
    )
}

/**
 * The result of a pipeline aggregation that returns a single value: `avg_bucket`, `sum_bucket`, `cumulative_sum` or
 * `bucket_script`.
 *
 * @param value
 *   the value, or `None` if it cannot be calculated (e.g. the average of no buckets)
 * @param valueAsString
 *   the formatted value, if a format was set
 */
final case class SimpleValueAggregationResult private[elasticsearch] (
  value: Option[Double],
  valueAsString: Option[String],
  meta: Map[String, Json] = Map.empty
) extends AggregationResult

object SimpleValueAggregationResult extends AggregationResultDecoder[SimpleValueAggregationResult]("simple_value") {
  private[elasticsearch] implicit val decoder: JsonDecoder[SimpleValueAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        value         <- fields.optional[Double]("value")
        valueAsString <- fields.optional[String]("value_as_string")
        meta          <- fields.meta
      } yield SimpleValueAggregationResult(value = value, valueAsString = valueAsString, meta = meta)
    )
}

/**
 * The result of a `stats` or `stats_bucket` aggregation.
 */
final case class StatsAggregationResult private[elasticsearch] (
  count: Long,
  min: Option[Double],
  max: Option[Double],
  avg: Option[Double],
  sum: Double,
  meta: Map[String, Json] = Map.empty
) extends AggregationResult

object StatsAggregationResult extends AggregationResultDecoder[StatsAggregationResult]("stats", "stats_bucket") {
  private[elasticsearch] implicit val decoder: JsonDecoder[StatsAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        count <- fields.required[Long]("count")
        min   <- fields.optional[Double]("min")
        max   <- fields.optional[Double]("max")
        avg   <- fields.optional[Double]("avg")
        sum   <- fields.required[Double]("sum")
        meta  <- fields.meta
      } yield StatsAggregationResult(count = count, min = min, max = max, avg = avg, sum = sum, meta = meta)
    )
}

final case class StdDeviationBoundsResult private[elasticsearch] (
  upper: Option[Double],
  lower: Option[Double],
  upperPopulation: Option[Double],
  lowerPopulation: Option[Double],
  upperSampling: Option[Double],
  lowerSampling: Option[Double]
)

object StdDeviationBoundsResult {
  private[elasticsearch] implicit val decoder: JsonDecoder[StdDeviationBoundsResult] =
    ResultFields.decoder(fields =>
      for {
        upper           <- fields.optional[Double]("upper")
        lower           <- fields.optional[Double]("lower")
        upperPopulation <- fields.optional[Double]("upper_population")
        lowerPopulation <- fields.optional[Double]("lower_population")
        upperSampling   <- fields.optional[Double]("upper_sampling")
        lowerSampling   <- fields.optional[Double]("lower_sampling")
      } yield StdDeviationBoundsResult(
        upper = upper,
        lower = lower,
        upperPopulation = upperPopulation,
        lowerPopulation = lowerPopulation,
        upperSampling = upperSampling,
        lowerSampling = lowerSampling
      )
    )
}

final case class SumAggregationResult private[elasticsearch] (
  value: Double,
  meta: Map[String, Json] = Map.empty
) extends AggregationResult

object SumAggregationResult extends AggregationResultDecoder[SumAggregationResult]("sum") {
  private[elasticsearch] implicit val decoder: JsonDecoder[SumAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        value <- fields.required[Double]("value")
        meta  <- fields.meta
      } yield SumAggregationResult(value = value, meta = meta)
    )
}

final case class TermsAggregationResult private[elasticsearch] (
  docErrorCount: Long,
  sumOtherDocCount: Long,
  buckets: Chunk[TermsAggregationBucketResult],
  meta: Map[String, Json] = Map.empty
) extends MultiBucketAggregationResult[TermsAggregationBucketResult]

object TermsAggregationResult
    extends AggregationResultDecoder[TermsAggregationResult]("sterms", "lterms", "ulterms", "dterms", "umterms") {
  private[elasticsearch] implicit val decoder: JsonDecoder[TermsAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        docErrorCount    <- fields.required[Long]("doc_count_error_upper_bound")
        sumOtherDocCount <- fields.required[Long]("sum_other_doc_count")
        buckets          <- fields.buckets[TermsAggregationBucketResult]
        meta             <- fields.meta
      } yield TermsAggregationResult(
        docErrorCount = docErrorCount,
        sumOtherDocCount = sumOtherDocCount,
        buckets = buckets,
        meta = meta
      )
    )
}

/**
 * A bucket of a [[TermsAggregationResult]].
 *
 * @param key
 *   the term of the bucket. Numeric terms (e.g. of `long`, `double` or `boolean` fields) are given as their JSON
 *   representation (e.g. `"1"`).
 * @param keyAsString
 *   the formatted term (e.g. `"true"` for a `boolean` field, or the formatted date for a `date` field), if
 *   Elasticsearch returned one
 */
final case class TermsAggregationBucketResult private[elasticsearch] (
  key: String,
  keyAsString: Option[String],
  docCount: Long,
  subAggregations: Map[String, AggregationResult]
) extends AggregationBucketResult[String]

object TermsAggregationBucketResult {
  private[elasticsearch] implicit val decoder: JsonDecoder[TermsAggregationBucketResult] =
    ResultFields.decoder(fields =>
      for {
        key             <- fields.required("key")(ResultFields.termDecoder)
        keyAsString     <- fields.optional[String]("key_as_string")
        docCount        <- fields.docCount
        subAggregations <- fields.subAggregations
      } yield TermsAggregationBucketResult(
        key = key,
        keyAsString = keyAsString,
        docCount = docCount,
        subAggregations = subAggregations
      )
    )
}

final case class ValueCountAggregationResult private[elasticsearch] (
  value: Long,
  meta: Map[String, Json] = Map.empty
) extends AggregationResult

object ValueCountAggregationResult extends AggregationResultDecoder[ValueCountAggregationResult]("value_count") {
  private[elasticsearch] implicit val decoder: JsonDecoder[ValueCountAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        value <- fields.required[Long]("value")
        meta  <- fields.meta
      } yield ValueCountAggregationResult(value = value, meta = meta)
    )
}

final case class WeightedAvgAggregationResult private[elasticsearch] (
  value: Option[Double],
  meta: Map[String, Json] = Map.empty
) extends AggregationResult

object WeightedAvgAggregationResult extends AggregationResultDecoder[WeightedAvgAggregationResult]("weighted_avg") {
  private[elasticsearch] implicit val decoder: JsonDecoder[WeightedAvgAggregationResult] =
    ResultFields.decoder(fields =>
      for {
        value <- fields.optional[Double]("value")
        meta  <- fields.meta
      } yield WeightedAvgAggregationResult(value = value, meta = meta)
    )
}

/**
 * The decoder of an aggregation result, together with the typed key prefixes that Elasticsearch returns for the
 * aggregation (e.g. `sterms` and `lterms` for a `terms` aggregation). To support a new aggregation result, extend this
 * class in its companion object and register the companion object in `AggregationResult.decoders`.
 */
private[result] abstract class AggregationResultDecoder[A <: AggregationResult](val typedKeys: String*) {
  private[elasticsearch] def decoder: JsonDecoder[A]

  private[result] final def widenedDecoder: JsonDecoder[AggregationResult] = decoder.widen[AggregationResult]
}

/**
 * The fields of a JSON object returned for an aggregation or a bucket, with helpers for decoding them.
 */
private[result] final class ResultFields(fields: Chunk[(String, Json)]) {

  private def get(name: String): Option[Json] =
    fields.collectFirst { case (`name`, value) => value }

  def required[A](name: String)(implicit decoder: JsonDecoder[A]): Either[String, A] =
    get(name) match {
      case Some(value) => value.as[A].left.map(error => s".$name$error")
      case None        => Left(s".$name(missing)")
    }

  def optional[A](name: String)(implicit decoder: JsonDecoder[A]): Either[String, Option[A]] =
    get(name) match {
      case None | Some(Json.Null) => Right(None)
      case Some(value)            => value.as[A].map(Some(_)).left.map(error => s".$name$error")
    }

  /**
   * Decodes the `buckets` field, which is an array of buckets, or an object of buckets by key when the aggregation is
   * `keyed`.
   */
  def buckets[A](implicit decoder: JsonDecoder[A]): Either[String, Chunk[A]] =
    get("buckets") match {
      case Some(buckets) => ResultFields.entries("buckets", buckets).flatMap(ResultFields.decodeEach[A]("buckets", _))
      case None          => Right(Chunk.empty)
    }

  lazy val docCount: Either[String, Long] = required[Long]("doc_count")

  lazy val meta: Either[String, Map[String, Json]] =
    optional[Map[String, Json]]("meta").map(_.getOrElse(Map.empty))

  /**
   * Decodes the `values` field of `percentiles` and `percentile_ranks`, which is an object of values by key, or an
   * array of `key`/`value` objects when `keyed` is `false`. The `<key>_as_string` entries of a keyed object, which are
   * returned when a format applies, are skipped.
   */
  lazy val percentileValues: Either[String, Map[String, Option[Double]]] =
    get("values") match {
      case Some(values) =>
        val withoutFormatted = values match {
          case Obj(keyed) =>
            Obj(keyed.filterNot { case (key, value) => key.endsWith("_as_string") && value.isInstanceOf[Str] })
          case other =>
            other
        }

        ResultFields
          .entries("values", withoutFormatted)
          .flatMap(ResultFields.decodeEach("values", _)(ResultFields.percentileDecoder))
          .map(_.toMap)
      case None =>
        Left(".values(missing)")
    }

  lazy val subAggregations: Either[String, Map[String, AggregationResult]] =
    AggregationResult.decodeAll(fields.filter { case (field, _) => field.contains('#') })
}

private[result] object ResultFields {

  def decoder[A](decode: ResultFields => Either[String, A]): JsonDecoder[A] =
    Obj.decoder.mapOrFail { case Obj(fields) => decode(new ResultFields(fields)) }

  /**
   * Decodes a term of a `terms` bucket. String terms are kept as they are, and other terms are given as their JSON
   * representation.
   */
  val termDecoder: JsonDecoder[String] =
    Json.decoder.map {
      case Str(value) => value
      case other      => other.toString
    }

  private val percentileDecoder: JsonDecoder[(String, Option[Double])] =
    decoder(fields =>
      for {
        key <- fields.required[Json]("key").flatMap {
                 case Str(value) => Right(value)
                 case Num(value) => Right(value.doubleValue.toString)
                 case other      => Left(s".key(expected a string or a number, got $other)")
               }
        value <- fields.optional[Double]("value")
      } yield key -> value
    )

  private def decodeEach[A](name: String, entries: Chunk[Json])(implicit
    decoder: JsonDecoder[A]
  ): Either[String, Chunk[A]] =
    entries.zipWithIndex.foldLeft[Either[String, Chunk[A]]](Right(Chunk.empty)) { case (acc, (entry, index)) =>
      for {
        decoded <- acc
        next    <- entry.as[A].left.map(error => s".$name[$index]$error")
      } yield decoded :+ next
    }

  /**
   * Returns the entries of an array, or of an object of entries by key, which is what Elasticsearch returns for `keyed`
   * aggregations. An object entry gets its key from the object (unless it contains one), and a value that is not an
   * object is wrapped as `{"key": <key>, "value": <value>}`.
   */
  private def entries(name: String, json: Json): Either[String, Chunk[Json]] =
    json match {
      case Arr(entries) =>
        Right(entries)
      case Obj(keyed) =>
        Right(keyed.map {
          case (key, Obj(fields)) if !fields.exists { case (field, _) => field == "key" } =>
            Obj(("key" -> Str(key)) +: fields)
          case (_, entry: Obj) =>
            entry
          case (key, value) =>
            Obj("key" -> Str(key), "value" -> value)
        })
      case other =>
        Left(s".$name(expected an array or an object, got $other)")
    }
}
