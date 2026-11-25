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

package zio.elasticsearch.aggregation

import zio.elasticsearch.ElasticAggregation.multipleAggregations
import zio.elasticsearch.ElasticPrimitive.{ElasticPrimitive, ElasticPrimitiveOps}
import zio.elasticsearch.aggregation.options._
import zio.elasticsearch.query.ElasticQuery
import zio.elasticsearch.query.sort.Sort
import zio.elasticsearch.script.Script
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj}
import zio.{Chunk, NonEmptyChunk}

import java.time.{Duration, ZoneId}

/**
 * An aggregation that can be used at the top level of a request: either a
 * [[zio.elasticsearch.aggregation.MultipleAggregations]] or a single
 * [[zio.elasticsearch.aggregation.MetricAggregation]], [[zio.elasticsearch.aggregation.BucketAggregation]] or
 * [[zio.elasticsearch.aggregation.SiblingPipelineAggregation]].
 */
sealed trait ElasticAggregation { self =>

  /**
   * Adds an aggregation on the same level, combining both into [[zio.elasticsearch.aggregation.MultipleAggregations]].
   * Aggregations are kept in the order in which they are added. Aggregations on the same level must have unique names.
   *
   * @param agg
   *   the [[zio.elasticsearch.aggregation.ElasticAggregation]] to add
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.MultipleAggregations]] that contains both aggregations.
   */
  final def withAgg(agg: ElasticAggregation): MultipleAggregations =
    multipleAggregations.aggregations(self, agg)

  /**
   * The single aggregations that this aggregation consists of.
   */
  private[elasticsearch] def singleAggregations: Chunk[SingleElasticAggregation]

  private[elasticsearch] def toJson: Json
}

/**
 * A single named aggregation. Any single aggregation can be used as a sub-aggregation of a
 * [[zio.elasticsearch.aggregation.MultiBucketAggregation]], while only metric, bucket and sibling pipeline aggregations
 * can be used at the top level of a request or as a sub-aggregation of a
 * [[zio.elasticsearch.aggregation.SingleBucketAggregation]].
 *
 * Each aggregation declares its name, type, parameters and optional sub-aggregations, from which the JSON is built.
 */
sealed trait SingleElasticAggregation {
  private[elasticsearch] def name: String

  private[elasticsearch] def aggregationType: String

  private[elasticsearch] def meta: Chunk[(String, Json)]

  private[elasticsearch] def params: Json

  private[elasticsearch] def subAggregations: Chunk[SingleElasticAggregation]

  private[elasticsearch] final def toJson: Json =
    Obj(
      name -> Obj(
        Chunk(
          Some(aggregationType -> params),
          if (meta.nonEmpty) Some("meta" -> Obj(meta)) else None,
          if (subAggregations.nonEmpty) Some("aggs" -> subAggregations.map(_.toJson).foldLeft[Json](Obj())(_.merge(_)))
          else None
        ).flatten
      )
    )
}

/**
 * An aggregation that computes metrics from the values of the aggregated documents.
 */
sealed trait MetricAggregation extends SingleElasticAggregation with ElasticAggregation { self =>
  private[elasticsearch] final def singleAggregations: Chunk[SingleElasticAggregation] = Chunk.single(self)

  private[elasticsearch] final def subAggregations: Chunk[SingleElasticAggregation] = Chunk.empty
}

/**
 * An aggregation that groups documents into buckets, and can have sub-aggregations that are computed for each bucket.
 */
sealed trait BucketAggregation extends SingleElasticAggregation with ElasticAggregation { self =>
  private[elasticsearch] final def singleAggregations: Chunk[SingleElasticAggregation] = Chunk.single(self)
}

/**
 * A bucket aggregation that creates multiple buckets (e.g. `terms` or `range`). Any single aggregation, including a
 * [[zio.elasticsearch.aggregation.ParentPipelineAggregation]], can be used as its sub-aggregation.
 */
sealed trait MultiBucketAggregation extends BucketAggregation

/**
 * A bucket aggregation that creates a single bucket (e.g. `filter` or `missing`). Since a
 * [[zio.elasticsearch.aggregation.ParentPipelineAggregation]] requires a multi-bucket parent, only aggregations that
 * can be used at the top level can be used as its sub-aggregations.
 */
sealed trait SingleBucketAggregation extends BucketAggregation

/**
 * An aggregation that works on the outputs of other aggregations instead of on documents.
 */
sealed trait PipelineAggregation extends SingleElasticAggregation {
  private[elasticsearch] final def subAggregations: Chunk[SingleElasticAggregation] = Chunk.empty
}

/**
 * A pipeline aggregation that works on the buckets of its parent aggregation, so it can only be used as a
 * sub-aggregation of a [[zio.elasticsearch.aggregation.MultiBucketAggregation]].
 */
sealed trait ParentPipelineAggregation extends PipelineAggregation

/**
 * A pipeline aggregation that works on the output of a sibling aggregation, so it can be used on the same level as the
 * aggregation it refers to.
 */
sealed trait SiblingPipelineAggregation extends PipelineAggregation with ElasticAggregation { self =>
  private[elasticsearch] final def singleAggregations: Chunk[SingleElasticAggregation] = Chunk.single(self)
}

sealed trait AvgAggregation
    extends MetricAggregation
    with HasFormat[AvgAggregation]
    with HasMeta[AvgAggregation]
    with HasNumericOrDateMissing[AvgAggregation]
    with HasScript[AvgAggregation]

private[elasticsearch] final case class Avg(
  name: String,
  field: String,
  format: Option[String],
  meta: Chunk[(String, Json)],
  missing: Option[Json],
  script: Option[Script]
) extends AvgAggregation { self =>

  def format(value: String): AvgAggregation =
    self.copy(format = Some(value))

  def meta[B: ElasticPrimitive](key: String, value: B): AvgAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def missing[B: ElasticPrimitive: NumericOrDate](value: B): AvgAggregation =
    self.copy(missing = Some(value.toJson))

  def script(script: Script): AvgAggregation =
    self.copy(script = Some(script))

  private[elasticsearch] def aggregationType: String = "avg"

  private[elasticsearch] def params: Json =
    Params.field(field = field, format = format, missing = missing, script = script)
}

sealed trait AvgBucketAggregation
    extends SiblingPipelineAggregation
    with HasFormat[AvgBucketAggregation]
    with HasGapPolicy[AvgBucketAggregation]
    with HasMeta[AvgBucketAggregation]

private[elasticsearch] final case class AvgBucket(
  name: String,
  bucketsPath: String,
  format: Option[String],
  gapPolicy: Option[GapPolicy],
  meta: Chunk[(String, Json)]
) extends AvgBucketAggregation { self =>

  def format(value: String): AvgBucketAggregation =
    self.copy(format = Some(value))

  def gapPolicy(value: GapPolicy): AvgBucketAggregation =
    self.copy(gapPolicy = Some(value))

  def meta[B: ElasticPrimitive](key: String, value: B): AvgBucketAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  private[elasticsearch] def aggregationType: String = "avg_bucket"

  private[elasticsearch] def params: Json =
    Params.pipeline(bucketsPath = bucketsPath.toJson, format = format, gapPolicy = gapPolicy)
}

sealed trait BucketScriptAggregation
    extends ParentPipelineAggregation
    with HasFormat[BucketScriptAggregation]
    with HasGapPolicy[BucketScriptAggregation]
    with HasMeta[BucketScriptAggregation]

private[elasticsearch] final case class BucketScript(
  name: String,
  script: Script,
  bucketsPath: Map[String, String],
  format: Option[String],
  gapPolicy: Option[GapPolicy],
  meta: Chunk[(String, Json)]
) extends BucketScriptAggregation { self =>

  def format(value: String): BucketScriptAggregation =
    self.copy(format = Some(value))

  def gapPolicy(value: GapPolicy): BucketScriptAggregation =
    self.copy(gapPolicy = Some(value))

  def meta[B: ElasticPrimitive](key: String, value: B): BucketScriptAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  private[elasticsearch] def aggregationType: String = "bucket_script"

  private[elasticsearch] def params: Json =
    Params
      .pipeline(bucketsPath = Params.bucketsPath(bucketsPath), format = format, gapPolicy = gapPolicy)
      .merge(Obj("script" -> script.toJson))
}

sealed trait BucketSelectorAggregation
    extends ParentPipelineAggregation
    with HasGapPolicy[BucketSelectorAggregation]
    with HasMeta[BucketSelectorAggregation]

private[elasticsearch] final case class BucketSelector(
  name: String,
  script: Script,
  bucketsPath: Map[String, String],
  gapPolicy: Option[GapPolicy],
  meta: Chunk[(String, Json)]
) extends BucketSelectorAggregation { self =>

  def gapPolicy(value: GapPolicy): BucketSelectorAggregation =
    self.copy(gapPolicy = Some(value))

  def meta[B: ElasticPrimitive](key: String, value: B): BucketSelectorAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  private[elasticsearch] def aggregationType: String = "bucket_selector"

  private[elasticsearch] def params: Json =
    Params(
      Some("buckets_path" -> Params.bucketsPath(bucketsPath)),
      gapPolicy.map(policy => "gap_policy" -> policy.toString.toJson),
      Some("script" -> script.toJson)
    )
}

sealed trait BucketSortAggregation
    extends ParentPipelineAggregation
    with HasGapPolicy[BucketSortAggregation]
    with HasMeta[BucketSortAggregation]
    with HasSize[BucketSortAggregation] {

  /**
   * Sets the starting offset from where the [[zio.elasticsearch.aggregation.BucketSortAggregation]] returns results.
   *
   * @param value
   *   a non-negative number to set the `from` parameter in the [[zio.elasticsearch.aggregation.BucketSortAggregation]]
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.BucketSortAggregation]] enriched with the `from` parameter.
   */
  def from(value: Int): BucketSortAggregation

  /**
   * Sets the sorting criteria for the [[zio.elasticsearch.aggregation.BucketSortAggregation]].
   *
   * @param sort
   *   required [[zio.elasticsearch.query.sort.Sort]] object that define the sorting criteria
   * @param sorts
   *   rest of the [[zio.elasticsearch.query.sort.Sort]] objects that define the sorting criteria
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.BucketSortAggregation]] enriched with the sorting criteria.
   */
  def sort(sort: Sort, sorts: Sort*): BucketSortAggregation
}

private[elasticsearch] final case class BucketSort(
  name: String,
  from: Option[Int],
  gapPolicy: Option[GapPolicy],
  meta: Chunk[(String, Json)],
  size: Option[Int],
  sortBy: Chunk[Sort]
) extends BucketSortAggregation { self =>

  def from(value: Int): BucketSortAggregation =
    self.copy(from = Some(value))

  def gapPolicy(value: GapPolicy): BucketSortAggregation =
    self.copy(gapPolicy = Some(value))

  def meta[B: ElasticPrimitive](key: String, value: B): BucketSortAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def size(value: Int): BucketSortAggregation =
    self.copy(size = Some(value))

  def sort(sort: Sort, sorts: Sort*): BucketSortAggregation =
    self.copy(sortBy = sortBy ++ (sort +: Chunk.fromIterable(sorts)))

  private[elasticsearch] def aggregationType: String = "bucket_sort"

  private[elasticsearch] def params: Json =
    Params(
      self.from.map("from" -> _.toJson),
      gapPolicy.map(policy => "gap_policy" -> policy.toString.toJson),
      size.map("size" -> _.toJson),
      if (sortBy.nonEmpty) Some("sort" -> Arr(sortBy.map(_.toJson))) else None
    )
}

sealed trait CardinalityAggregation
    extends MetricAggregation
    with HasMeta[CardinalityAggregation]
    with HasMissing[CardinalityAggregation]
    with HasScript[CardinalityAggregation]

private[elasticsearch] final case class Cardinality(
  name: String,
  field: String,
  meta: Chunk[(String, Json)],
  missing: Option[Json],
  script: Option[Script]
) extends CardinalityAggregation { self =>

  def meta[B: ElasticPrimitive](key: String, value: B): CardinalityAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def missing[B: ElasticPrimitive](value: B): CardinalityAggregation =
    self.copy(missing = Some(value.toJson))

  def script(script: Script): CardinalityAggregation =
    self.copy(script = Some(script))

  private[elasticsearch] def aggregationType: String = "cardinality"

  private[elasticsearch] def params: Json =
    Params.field(field = field, format = None, missing = missing, script = script)
}

sealed trait CumulativeSumAggregation
    extends ParentPipelineAggregation
    with HasFormat[CumulativeSumAggregation]
    with HasMeta[CumulativeSumAggregation]

private[elasticsearch] final case class CumulativeSum(
  name: String,
  bucketsPath: String,
  format: Option[String],
  meta: Chunk[(String, Json)]
) extends CumulativeSumAggregation { self =>

  def format(value: String): CumulativeSumAggregation =
    self.copy(format = Some(value))

  def meta[B: ElasticPrimitive](key: String, value: B): CumulativeSumAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  private[elasticsearch] def aggregationType: String = "cumulative_sum"

  private[elasticsearch] def params: Json =
    Params.pipeline(bucketsPath = bucketsPath.toJson, format = format, gapPolicy = None)
}

sealed trait DateHistogramAggregation
    extends MultiBucketAggregation
    with HasFormat[DateHistogramAggregation]
    with HasKeyed[DateHistogramAggregation]
    with HasMeta[DateHistogramAggregation]
    with HasMinDocCount[DateHistogramAggregation]
    with HasNumericOrDateMissing[DateHistogramAggregation]
    with HasOrder[DateHistogramAggregation]
    with WithSubAgg[DateHistogramAggregation, SingleElasticAggregation] {

  /**
   * Sets the `extended_bounds` parameter for the [[zio.elasticsearch.aggregation.DateHistogramAggregation]]. Buckets
   * are created for the whole range of the bounds, even if they contain no documents, which requires `min_doc_count` to
   * be `0` (the default).
   *
   * @param bounds
   *   the [[zio.elasticsearch.aggregation.DateHistogramBounds]] to extend the buckets to
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.DateHistogramAggregation]] enriched with the `extended_bounds`
   *   parameter.
   */
  def extendedBounds(bounds: DateHistogramBounds): DateHistogramAggregation

  /**
   * Sets the `hard_bounds` parameter for the [[zio.elasticsearch.aggregation.DateHistogramAggregation]]. Buckets
   * outside of the bounds are not returned, even if they contain documents.
   *
   * @param bounds
   *   the [[zio.elasticsearch.aggregation.DateHistogramBounds]] to limit the buckets to
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.DateHistogramAggregation]] enriched with the `hard_bounds`
   *   parameter.
   */
  def hardBounds(bounds: DateHistogramBounds): DateHistogramAggregation

  /**
   * Sets the `offset` parameter for the [[zio.elasticsearch.aggregation.DateHistogramAggregation]], which shifts the
   * start of each bucket by the given duration (e.g. to start daily buckets at 6 AM). The offset is sent in
   * milliseconds and can be negative.
   *
   * @param value
   *   the duration by which to shift the buckets
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.DateHistogramAggregation]] enriched with the `offset`
   *   parameter.
   */
  def offset(value: Duration): DateHistogramAggregation

  /**
   * Sets the `time_zone` parameter for the [[zio.elasticsearch.aggregation.DateHistogramAggregation]]. Dates are stored
   * in UTC, and the time zone is used to compute the bucket boundaries and keys.
   *
   * @param value
   *   the time zone, either a region (e.g. `ZoneId.of("Europe/Belgrade")`) or an offset (e.g. `ZoneOffset.ofHours(1)`)
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.DateHistogramAggregation]] enriched with the `time_zone`
   *   parameter.
   */
  def timeZone(value: ZoneId): DateHistogramAggregation
}

private[elasticsearch] final case class DateHistogram(
  name: String,
  field: String,
  interval: DateHistogramInterval,
  extendedBounds: Option[DateHistogramBounds],
  format: Option[String],
  hardBounds: Option[DateHistogramBounds],
  isKeyed: Boolean,
  meta: Chunk[(String, Json)],
  minDocCount: Option[Int],
  missing: Option[Json],
  offset: Option[Duration],
  order: Chunk[AggregationOrder],
  subAggregations: Chunk[SingleElasticAggregation],
  timeZone: Option[ZoneId]
) extends DateHistogramAggregation { self =>

  def extendedBounds(bounds: DateHistogramBounds): DateHistogramAggregation =
    self.copy(extendedBounds = Some(bounds))

  def format(value: String): DateHistogramAggregation =
    self.copy(format = Some(value))

  def hardBounds(bounds: DateHistogramBounds): DateHistogramAggregation =
    self.copy(hardBounds = Some(bounds))

  def keyed: DateHistogramAggregation =
    self.copy(isKeyed = true)

  def meta[B: ElasticPrimitive](key: String, value: B): DateHistogramAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def minDocCount(value: Int): DateHistogramAggregation =
    self.copy(minDocCount = Some(value))

  def missing[B: ElasticPrimitive: NumericOrDate](value: B): DateHistogramAggregation =
    self.copy(missing = Some(value.toJson))

  def offset(value: Duration): DateHistogramAggregation =
    self.copy(offset = Some(value))

  def orderBy(order: AggregationOrder, orders: AggregationOrder*): DateHistogramAggregation =
    self.copy(order = self.order ++ (order +: Chunk.fromIterable(orders)))

  def timeZone(value: ZoneId): DateHistogramAggregation =
    self.copy(timeZone = Some(value))

  def withSubAgg(aggregation: SingleElasticAggregation): DateHistogramAggregation =
    self.copy(subAggregations = subAggregations :+ aggregation)

  private[elasticsearch] def aggregationType: String = "date_histogram"

  private[elasticsearch] def params: Json =
    Params(
      Some("field"            -> field.toJson),
      Some(interval.paramName -> interval.value.toJson),
      extendedBounds.map("extended_bounds" -> _.toJson),
      format.map("format" -> _.toJson),
      hardBounds.map("hard_bounds" -> _.toJson),
      if (isKeyed) Some("keyed" -> Json.Bool(true)) else None,
      minDocCount.map("min_doc_count" -> _.toJson),
      missing.map("missing" -> _),
      offset.map(value => "offset" -> s"${value.toMillis}ms".toJson),
      Params.order(order),
      timeZone.map("time_zone" -> _.getId.toJson)
    )
}

/**
 * The bounds of a [[zio.elasticsearch.aggregation.DateHistogramAggregation]], used for its `extended_bounds` and
 * `hard_bounds`. Each bound is either a date or a number of milliseconds since the epoch, and either of them can be
 * omitted. Dates are sent as strings, so they have to match the `format` of the aggregation, if one is set.
 */
final case class DateHistogramBounds(min: Option[Json] = None, max: Option[Json] = None) { self =>

  def max[A: ElasticPrimitive: NumericOrDate](value: A): DateHistogramBounds =
    self.copy(max = Some(value.toJson))

  def min[A: ElasticPrimitive: NumericOrDate](value: A): DateHistogramBounds =
    self.copy(min = Some(value.toJson))

  private[elasticsearch] def toJson: Json =
    Params(min.map("min" -> _), max.map("max" -> _))
}

sealed trait DerivativeAggregation
    extends ParentPipelineAggregation
    with HasFormat[DerivativeAggregation]
    with HasGapPolicy[DerivativeAggregation]
    with HasMeta[DerivativeAggregation] {

  /**
   * Sets the `unit` parameter for the [[zio.elasticsearch.aggregation.DerivativeAggregation]]. When the parent is a
   * [[zio.elasticsearch.aggregation.DateHistogramAggregation]], the derivative is then also returned per the given unit
   * of time as `normalizedValue` (e.g. per day for monthly buckets).
   *
   * @param value
   *   the [[zio.elasticsearch.aggregation.DateHistogramInterval]] to normalize the derivative to
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.DerivativeAggregation]] enriched with the `unit` parameter.
   */
  def unit(value: DateHistogramInterval): DerivativeAggregation
}

private[elasticsearch] final case class Derivative(
  name: String,
  bucketsPath: String,
  format: Option[String],
  gapPolicy: Option[GapPolicy],
  meta: Chunk[(String, Json)],
  unit: Option[DateHistogramInterval]
) extends DerivativeAggregation { self =>

  def format(value: String): DerivativeAggregation =
    self.copy(format = Some(value))

  def gapPolicy(value: GapPolicy): DerivativeAggregation =
    self.copy(gapPolicy = Some(value))

  def meta[B: ElasticPrimitive](key: String, value: B): DerivativeAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def unit(value: DateHistogramInterval): DerivativeAggregation =
    self.copy(unit = Some(value))

  private[elasticsearch] def aggregationType: String = "derivative"

  private[elasticsearch] def params: Json =
    Params
      .pipeline(bucketsPath = bucketsPath.toJson, format = format, gapPolicy = gapPolicy)
      .merge(Params(unit.map("unit" -> _.value.toJson)))
}

sealed trait ExtendedStatsAggregation
    extends MetricAggregation
    with HasFormat[ExtendedStatsAggregation]
    with HasMeta[ExtendedStatsAggregation]
    with HasNumericMissing[ExtendedStatsAggregation]
    with HasScript[ExtendedStatsAggregation] {

  /**
   * Sets the `sigma` parameter for the [[zio.elasticsearch.aggregation.ExtendedStatsAggregation]]. The`sigma` parameter
   * controls how many standard deviations plus/minus from the mean should std_deviation_bounds object display.
   *
   * @param value
   *   the value to use for sigma parameter
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.ExtendedStatsAggregation]] enriched with the `sigma`
   *   parameter.
   */
  def sigma(value: Double): ExtendedStatsAggregation
}

private[elasticsearch] final case class ExtendedStats(
  name: String,
  field: String,
  format: Option[String],
  meta: Chunk[(String, Json)],
  missing: Option[Json],
  script: Option[Script],
  sigma: Option[Double]
) extends ExtendedStatsAggregation { self =>

  def format(value: String): ExtendedStatsAggregation =
    self.copy(format = Some(value))

  def meta[B: ElasticPrimitive](key: String, value: B): ExtendedStatsAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def missing[B: ElasticPrimitive: Numeric](value: B): ExtendedStatsAggregation =
    self.copy(missing = Some(value.toJson))

  def script(script: Script): ExtendedStatsAggregation =
    self.copy(script = Some(script))

  def sigma(value: Double): ExtendedStatsAggregation =
    self.copy(sigma = Some(value))

  private[elasticsearch] def aggregationType: String = "extended_stats"

  private[elasticsearch] def params: Json =
    Params
      .field(field = field, format = format, missing = missing, script = script)
      .merge(
        Params(sigma.map("sigma" -> _.toJson))
      )
}

sealed trait FilterAggregation
    extends SingleBucketAggregation
    with HasMeta[FilterAggregation]
    with WithSubAgg[FilterAggregation, ElasticAggregation]

private[elasticsearch] final case class Filter(
  name: String,
  query: ElasticQuery[_],
  meta: Chunk[(String, Json)],
  subAggregations: Chunk[SingleElasticAggregation]
) extends FilterAggregation { self =>

  def meta[B: ElasticPrimitive](key: String, value: B): FilterAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def withSubAgg(aggregation: ElasticAggregation): FilterAggregation =
    self.copy(subAggregations = subAggregations ++ aggregation.singleAggregations)

  private[elasticsearch] def aggregationType: String = "filter"

  private[elasticsearch] def params: Json =
    query.toJson(fieldPath = None)
}

sealed trait FiltersAggregation
    extends MultiBucketAggregation
    with HasMeta[FiltersAggregation]
    with WithSubAgg[FiltersAggregation, SingleElasticAggregation] {

  /**
   * Sets the `other_bucket` parameter to `true` for the [[zio.elasticsearch.aggregation.FiltersAggregation]]. An
   * additional bucket is then returned for the documents that do not match any of the filters. For named filters, the
   * key of the bucket is `_other_`, unless it is changed with `otherBucketKey`.
   *
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.FiltersAggregation]] enriched with the `other_bucket`
   *   parameter.
   */
  def otherBucket: FiltersAggregation

  /**
   * Sets the `other_bucket_key` parameter for the [[zio.elasticsearch.aggregation.FiltersAggregation]], which is the
   * key of the bucket for the documents that do not match any of the named filters. Setting it also enables that
   * bucket.
   *
   * @param value
   *   the key of the bucket for the documents that do not match any of the filters
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.FiltersAggregation]] enriched with the `other_bucket_key`
   *   parameter.
   */
  def otherBucketKey(value: String): FiltersAggregation
}

private[elasticsearch] final case class Filters(
  name: String,
  filters: Filters.Queries,
  hasOtherBucket: Boolean,
  meta: Chunk[(String, Json)],
  otherBucketKey: Option[String],
  subAggregations: Chunk[SingleElasticAggregation]
) extends FiltersAggregation { self =>

  def meta[B: ElasticPrimitive](key: String, value: B): FiltersAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def otherBucket: FiltersAggregation =
    self.copy(hasOtherBucket = true)

  def otherBucketKey(value: String): FiltersAggregation =
    self.copy(otherBucketKey = Some(value))

  def withSubAgg(aggregation: SingleElasticAggregation): FiltersAggregation =
    self.copy(subAggregations = subAggregations :+ aggregation)

  private[elasticsearch] def aggregationType: String = "filters"

  private[elasticsearch] def params: Json =
    Params(
      Some("filters" -> filters.toJson),
      if (hasOtherBucket) Some("other_bucket" -> Json.Bool(true)) else None,
      otherBucketKey.map("other_bucket_key" -> _.toJson)
    )
}

private[elasticsearch] object Filters {

  /**
   * The filters of a [[zio.elasticsearch.aggregation.FiltersAggregation]], which are either all named or all anonymous.
   */
  sealed trait Queries {
    private[elasticsearch] def toJson: Json
  }

  final case class Anonymous(queries: NonEmptyChunk[ElasticQuery[_]]) extends Queries {
    private[elasticsearch] def toJson: Json =
      Arr(queries.map(_.toJson(fieldPath = None)))
  }

  final case class Named(queries: NonEmptyChunk[(String, ElasticQuery[_])]) extends Queries {
    private[elasticsearch] def toJson: Json =
      Obj(queries.map { case (key, query) => key -> query.toJson(fieldPath = None) })
  }
}

sealed trait HistogramAggregation
    extends MultiBucketAggregation
    with HasKeyed[HistogramAggregation]
    with HasMeta[HistogramAggregation]
    with HasMinDocCount[HistogramAggregation]
    with HasNumericMissing[HistogramAggregation]
    with HasOrder[HistogramAggregation]
    with WithSubAgg[HistogramAggregation, SingleElasticAggregation] {

  /**
   * Sets the `extended_bounds` parameter for the [[zio.elasticsearch.aggregation.HistogramAggregation]]. Buckets are
   * created for the whole range of the bounds, even if they contain no documents, which requires `min_doc_count` to be
   * `0` (the default).
   *
   * @param bounds
   *   the [[zio.elasticsearch.aggregation.HistogramBounds]] to extend the buckets to
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.HistogramAggregation]] enriched with the `extended_bounds`
   *   parameter.
   */
  def extendedBounds(bounds: HistogramBounds): HistogramAggregation

  /**
   * Sets the `hard_bounds` parameter for the [[zio.elasticsearch.aggregation.HistogramAggregation]]. Buckets outside of
   * the bounds are not returned, even if they contain documents.
   *
   * @param bounds
   *   the [[zio.elasticsearch.aggregation.HistogramBounds]] to limit the buckets to
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.HistogramAggregation]] enriched with the `hard_bounds`
   *   parameter.
   */
  def hardBounds(bounds: HistogramBounds): HistogramAggregation

  /**
   * Sets the `offset` parameter for the [[zio.elasticsearch.aggregation.HistogramAggregation]], which shifts the bucket
   * boundaries by the given value (e.g. with an interval of `10` and an offset of `5`, buckets start at `5`, `15`,
   * ...).
   *
   * @param value
   *   the value by which to shift the buckets, between `0` (inclusive) and the interval (exclusive)
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.HistogramAggregation]] enriched with the `offset` parameter.
   */
  def offset(value: Double): HistogramAggregation
}

private[elasticsearch] final case class Histogram(
  name: String,
  field: String,
  interval: Double,
  extendedBounds: Option[HistogramBounds],
  hardBounds: Option[HistogramBounds],
  isKeyed: Boolean,
  meta: Chunk[(String, Json)],
  minDocCount: Option[Int],
  missing: Option[Json],
  offset: Option[Double],
  order: Chunk[AggregationOrder],
  subAggregations: Chunk[SingleElasticAggregation]
) extends HistogramAggregation { self =>

  def extendedBounds(bounds: HistogramBounds): HistogramAggregation =
    self.copy(extendedBounds = Some(bounds))

  def hardBounds(bounds: HistogramBounds): HistogramAggregation =
    self.copy(hardBounds = Some(bounds))

  def keyed: HistogramAggregation =
    self.copy(isKeyed = true)

  def meta[B: ElasticPrimitive](key: String, value: B): HistogramAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def minDocCount(value: Int): HistogramAggregation =
    self.copy(minDocCount = Some(value))

  def missing[B: ElasticPrimitive: Numeric](value: B): HistogramAggregation =
    self.copy(missing = Some(value.toJson))

  def offset(value: Double): HistogramAggregation =
    self.copy(offset = Some(value))

  def orderBy(order: AggregationOrder, orders: AggregationOrder*): HistogramAggregation =
    self.copy(order = self.order ++ (order +: Chunk.fromIterable(orders)))

  def withSubAgg(aggregation: SingleElasticAggregation): HistogramAggregation =
    self.copy(subAggregations = subAggregations :+ aggregation)

  private[elasticsearch] def aggregationType: String = "histogram"

  private[elasticsearch] def params: Json =
    Params(
      Some("field"    -> field.toJson),
      Some("interval" -> interval.toJson),
      extendedBounds.map("extended_bounds" -> _.toJson),
      hardBounds.map("hard_bounds" -> _.toJson),
      if (isKeyed) Some("keyed" -> Json.Bool(true)) else None,
      minDocCount.map("min_doc_count" -> _.toJson),
      missing.map("missing" -> _),
      offset.map("offset" -> _.toJson),
      Params.order(order)
    )
}

/**
 * The bounds of a [[zio.elasticsearch.aggregation.HistogramAggregation]], used for its `extended_bounds` and
 * `hard_bounds`. Either of the bounds can be omitted.
 */
final case class HistogramBounds(min: Option[Double] = None, max: Option[Double] = None) { self =>

  def max(value: Double): HistogramBounds =
    self.copy(max = Some(value))

  def min(value: Double): HistogramBounds =
    self.copy(min = Some(value))

  private[elasticsearch] def toJson: Json =
    Params(min.map("min" -> _.toJson), max.map("max" -> _.toJson))
}

sealed trait IpRangeAggregation
    extends MultiBucketAggregation
    with HasKeyed[IpRangeAggregation]
    with HasMeta[IpRangeAggregation]
    with WithSubAgg[IpRangeAggregation, SingleElasticAggregation]

private[elasticsearch] final case class IpRange(
  name: String,
  field: String,
  isKeyed: Boolean,
  meta: Chunk[(String, Json)],
  ranges: NonEmptyChunk[IpRangeBound],
  subAggregations: Chunk[SingleElasticAggregation]
) extends IpRangeAggregation { self =>

  def keyed: IpRangeAggregation =
    self.copy(isKeyed = true)

  def meta[B: ElasticPrimitive](key: String, value: B): IpRangeAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def withSubAgg(aggregation: SingleElasticAggregation): IpRangeAggregation =
    self.copy(subAggregations = subAggregations :+ aggregation)

  private[elasticsearch] def aggregationType: String = "ip_range"

  private[elasticsearch] def params: Json =
    Params.ranges(field = field, ranges = ranges.map(_.toJson), isKeyed = isKeyed)
}

/**
 * A range of an [[zio.elasticsearch.aggregation.IpRangeAggregation]]. A range is defined either with `from` and/or `to`
 * IP addresses, or with a CIDR `mask`.
 */
final case class IpRangeBound(
  from: Option[String] = None,
  key: Option[String] = None,
  mask: Option[String] = None,
  to: Option[String] = None
) { self =>

  def from(value: String): IpRangeBound =
    self.copy(from = Some(value))

  def key(value: String): IpRangeBound =
    self.copy(key = Some(value))

  def mask(value: String): IpRangeBound =
    self.copy(mask = Some(value))

  def to(value: String): IpRangeBound =
    self.copy(to = Some(value))

  private[elasticsearch] def toJson: Json =
    Params(
      from.map("from" -> _.toJson),
      key.map("key" -> _.toJson),
      mask.map("mask" -> _.toJson),
      to.map("to" -> _.toJson)
    )
}

sealed trait MaxAggregation
    extends MetricAggregation
    with HasFormat[MaxAggregation]
    with HasMeta[MaxAggregation]
    with HasNumericOrDateMissing[MaxAggregation]
    with HasScript[MaxAggregation]

private[elasticsearch] final case class Max(
  name: String,
  field: String,
  format: Option[String],
  meta: Chunk[(String, Json)],
  missing: Option[Json],
  script: Option[Script]
) extends MaxAggregation { self =>

  def format(value: String): MaxAggregation =
    self.copy(format = Some(value))

  def meta[B: ElasticPrimitive](key: String, value: B): MaxAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def missing[B: ElasticPrimitive: NumericOrDate](value: B): MaxAggregation =
    self.copy(missing = Some(value.toJson))

  def script(script: Script): MaxAggregation =
    self.copy(script = Some(script))

  private[elasticsearch] def aggregationType: String = "max"

  private[elasticsearch] def params: Json =
    Params.field(field = field, format = format, missing = missing, script = script)
}

sealed trait MaxBucketAggregation
    extends SiblingPipelineAggregation
    with HasFormat[MaxBucketAggregation]
    with HasGapPolicy[MaxBucketAggregation]
    with HasMeta[MaxBucketAggregation]

private[elasticsearch] final case class MaxBucket(
  name: String,
  bucketsPath: String,
  format: Option[String],
  gapPolicy: Option[GapPolicy],
  meta: Chunk[(String, Json)]
) extends MaxBucketAggregation { self =>

  def format(value: String): MaxBucketAggregation =
    self.copy(format = Some(value))

  def gapPolicy(value: GapPolicy): MaxBucketAggregation =
    self.copy(gapPolicy = Some(value))

  def meta[B: ElasticPrimitive](key: String, value: B): MaxBucketAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  private[elasticsearch] def aggregationType: String = "max_bucket"

  private[elasticsearch] def params: Json =
    Params.pipeline(bucketsPath = bucketsPath.toJson, format = format, gapPolicy = gapPolicy)
}

sealed trait MinAggregation
    extends MetricAggregation
    with HasFormat[MinAggregation]
    with HasMeta[MinAggregation]
    with HasNumericOrDateMissing[MinAggregation]
    with HasScript[MinAggregation]

private[elasticsearch] final case class Min(
  name: String,
  field: String,
  format: Option[String],
  meta: Chunk[(String, Json)],
  missing: Option[Json],
  script: Option[Script]
) extends MinAggregation { self =>

  def format(value: String): MinAggregation =
    self.copy(format = Some(value))

  def meta[B: ElasticPrimitive](key: String, value: B): MinAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def missing[B: ElasticPrimitive: NumericOrDate](value: B): MinAggregation =
    self.copy(missing = Some(value.toJson))

  def script(script: Script): MinAggregation =
    self.copy(script = Some(script))

  private[elasticsearch] def aggregationType: String = "min"

  private[elasticsearch] def params: Json =
    Params.field(field = field, format = format, missing = missing, script = script)
}

sealed trait MinBucketAggregation
    extends SiblingPipelineAggregation
    with HasFormat[MinBucketAggregation]
    with HasGapPolicy[MinBucketAggregation]
    with HasMeta[MinBucketAggregation]

private[elasticsearch] final case class MinBucket(
  name: String,
  bucketsPath: String,
  format: Option[String],
  gapPolicy: Option[GapPolicy],
  meta: Chunk[(String, Json)]
) extends MinBucketAggregation { self =>

  def format(value: String): MinBucketAggregation =
    self.copy(format = Some(value))

  def gapPolicy(value: GapPolicy): MinBucketAggregation =
    self.copy(gapPolicy = Some(value))

  def meta[B: ElasticPrimitive](key: String, value: B): MinBucketAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  private[elasticsearch] def aggregationType: String = "min_bucket"

  private[elasticsearch] def params: Json =
    Params.pipeline(bucketsPath = bucketsPath.toJson, format = format, gapPolicy = gapPolicy)
}

sealed trait MissingAggregation
    extends SingleBucketAggregation
    with HasMeta[MissingAggregation]
    with WithSubAgg[MissingAggregation, ElasticAggregation]

private[elasticsearch] final case class Missing(
  name: String,
  field: String,
  meta: Chunk[(String, Json)],
  subAggregations: Chunk[SingleElasticAggregation]
) extends MissingAggregation { self =>

  def meta[B: ElasticPrimitive](key: String, value: B): MissingAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def withSubAgg(aggregation: ElasticAggregation): MissingAggregation =
    self.copy(subAggregations = subAggregations ++ aggregation.singleAggregations)

  private[elasticsearch] def aggregationType: String = "missing"

  private[elasticsearch] def params: Json =
    Obj("field" -> field.toJson)
}

sealed trait MultipleAggregations extends ElasticAggregation {

  /**
   * Adds the given aggregations to the [[zio.elasticsearch.aggregation.MultipleAggregations]]. Aggregations are kept in
   * the order in which they are added.
   *
   * @param aggregations
   *   the aggregations to be added
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.MultipleAggregations]] with the specified aggregations.
   */
  def aggregations(aggregations: ElasticAggregation*): MultipleAggregations
}

private[elasticsearch] final case class Multiple(aggregations: Chunk[ElasticAggregation]) extends MultipleAggregations {
  self =>

  def aggregations(aggregations: ElasticAggregation*): MultipleAggregations =
    self.copy(aggregations = self.aggregations ++ Chunk.fromIterable(aggregations).flatMap {
      case Multiple(aggs) => aggs
      case agg            => Chunk.single(agg)
    })

  private[elasticsearch] def singleAggregations: Chunk[SingleElasticAggregation] =
    aggregations.flatMap(_.singleAggregations)

  private[elasticsearch] def toJson: Json =
    aggregations.map(_.toJson).foldLeft[Json](Obj())(_.merge(_))
}

sealed trait PercentileRanksAggregation
    extends MetricAggregation
    with HasMeta[PercentileRanksAggregation]
    with HasNumericMissing[PercentileRanksAggregation]

private[elasticsearch] final case class PercentileRanks(
  name: String,
  field: String,
  meta: Chunk[(String, Json)],
  missing: Option[Json],
  values: Chunk[BigDecimal]
) extends PercentileRanksAggregation { self =>

  def meta[B: ElasticPrimitive](key: String, value: B): PercentileRanksAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def missing[B: ElasticPrimitive: Numeric](value: B): PercentileRanksAggregation =
    self.copy(missing = Some(value.toJson))

  private[elasticsearch] def aggregationType: String = "percentile_ranks"

  private[elasticsearch] def params: Json =
    Params(
      Some("field" -> field.toJson),
      missing.map("missing" -> _),
      Some("values" -> Arr(values.map(_.toJson)))
    )
}

sealed trait PercentilesAggregation
    extends MetricAggregation
    with HasMeta[PercentilesAggregation]
    with HasNumericMissing[PercentilesAggregation] {

  /**
   * Sets the `percents` parameter for the [[zio.elasticsearch.aggregation.PercentilesAggregation]].
   *
   * @param percents
   *   an array of percentiles to be calculated for [[zio.elasticsearch.aggregation.PercentilesAggregation]]
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.PercentilesAggregation]] enriched with the `percents`
   *   parameter.
   */
  def percents(percent: Double, percents: Double*): PercentilesAggregation
}

private[elasticsearch] final case class Percentiles(
  name: String,
  field: String,
  meta: Chunk[(String, Json)],
  missing: Option[Json],
  percents: Chunk[Double]
) extends PercentilesAggregation { self =>

  def meta[B: ElasticPrimitive](key: String, value: B): PercentilesAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def missing[B: ElasticPrimitive: Numeric](value: B): PercentilesAggregation =
    self.copy(missing = Some(value.toJson))

  def percents(percent: Double, percents: Double*): PercentilesAggregation =
    self.copy(percents = percent +: Chunk.fromIterable(percents))

  private[elasticsearch] def aggregationType: String = "percentiles"

  private[elasticsearch] def params: Json =
    Params(
      Some("field" -> field.toJson),
      missing.map("missing" -> _),
      if (percents.nonEmpty) Some("percents" -> Arr(percents.map(_.toJson))) else None
    )
}

sealed trait RangeAggregation
    extends MultiBucketAggregation
    with HasKeyed[RangeAggregation]
    with HasMeta[RangeAggregation]
    with WithSubAgg[RangeAggregation, SingleElasticAggregation]

private[elasticsearch] final case class Range(
  name: String,
  field: String,
  isKeyed: Boolean,
  meta: Chunk[(String, Json)],
  ranges: NonEmptyChunk[RangeBound],
  subAggregations: Chunk[SingleElasticAggregation]
) extends RangeAggregation { self =>

  def keyed: RangeAggregation =
    self.copy(isKeyed = true)

  def meta[B: ElasticPrimitive](key: String, value: B): RangeAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def withSubAgg(aggregation: SingleElasticAggregation): RangeAggregation =
    self.copy(subAggregations = subAggregations :+ aggregation)

  private[elasticsearch] def aggregationType: String = "range"

  private[elasticsearch] def params: Json =
    Params.ranges(field = field, ranges = ranges.map(_.toJson), isKeyed = isKeyed)
}

/**
 * A range of a range aggregation. Each range includes the `from` value and excludes the `to` value. The bounds can be
 * of any numeric type, and the two bounds of a range can be of different numeric types (e.g.
 * `RangeBound().from(0).to(99.5)`).
 */
final case class RangeBound(
  from: Option[BigDecimal] = None,
  key: Option[String] = None,
  to: Option[BigDecimal] = None
) { self =>

  def from[A: Numeric](value: A): RangeBound =
    self.copy(from = Some(RangeBound.toBigDecimal(value)))

  def key(value: String): RangeBound =
    self.copy(key = Some(value))

  def to[A: Numeric](value: A): RangeBound =
    self.copy(to = Some(RangeBound.toBigDecimal(value)))

  private[elasticsearch] def toJson: Json =
    Params(
      from.map("from" -> _.toJson),
      key.map("key" -> _.toJson),
      to.map("to" -> _.toJson)
    )
}

object RangeBound {

  /**
   * Converts a numeric value to `BigDecimal` without losing precision, so that integral values are encoded as integers
   * and large `Long` values are kept exact.
   */
  private def toBigDecimal[A](value: A)(implicit N: Numeric[A]): BigDecimal =
    value match {
      case v: BigDecimal                                   => v
      case v: BigInt                                       => BigDecimal(v)
      case v: Double                                       => BigDecimal(v)
      case v: Float                                        => BigDecimal.decimal(v)
      case _: Byte | _: Short | _: Int | _: Long | _: Char => BigDecimal(N.toLong(value))
      case _                                               => BigDecimal(N.toDouble(value))
    }
}

sealed trait SamplerAggregation
    extends SingleBucketAggregation
    with HasMeta[SamplerAggregation]
    with WithSubAgg[SamplerAggregation, ElasticAggregation] {

  /**
   * Sets the `shard_size` parameter for the [[zio.elasticsearch.aggregation.SamplerAggregation]]. This parameter
   * controls the maximum number of documents to be returned per shard.
   *
   * @param value
   *   the maximum number of documents per shard
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.SamplerAggregation]] enriched with the `shard_size` parameter.
   */
  def maxDocumentsPerShard(value: Int): SamplerAggregation
}

private[elasticsearch] final case class Sampler(
  name: String,
  meta: Chunk[(String, Json)],
  shardSize: Int,
  subAggregations: Chunk[SingleElasticAggregation]
) extends SamplerAggregation { self =>

  def maxDocumentsPerShard(value: Int): SamplerAggregation =
    self.copy(shardSize = value)

  def meta[B: ElasticPrimitive](key: String, value: B): SamplerAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def withSubAgg(aggregation: ElasticAggregation): SamplerAggregation =
    self.copy(subAggregations = subAggregations ++ aggregation.singleAggregations)

  private[elasticsearch] def aggregationType: String = "sampler"

  private[elasticsearch] def params: Json =
    Obj("shard_size" -> shardSize.toJson)
}

sealed trait StatsAggregation
    extends MetricAggregation
    with HasFormat[StatsAggregation]
    with HasMeta[StatsAggregation]
    with HasNumericOrDateMissing[StatsAggregation]
    with HasScript[StatsAggregation]

private[elasticsearch] final case class Stats(
  name: String,
  field: String,
  format: Option[String],
  meta: Chunk[(String, Json)],
  missing: Option[Json],
  script: Option[Script]
) extends StatsAggregation { self =>

  def format(value: String): StatsAggregation =
    self.copy(format = Some(value))

  def meta[B: ElasticPrimitive](key: String, value: B): StatsAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def missing[B: ElasticPrimitive: NumericOrDate](value: B): StatsAggregation =
    self.copy(missing = Some(value.toJson))

  def script(script: Script): StatsAggregation =
    self.copy(script = Some(script))

  private[elasticsearch] def aggregationType: String = "stats"

  private[elasticsearch] def params: Json =
    Params.field(field = field, format = format, missing = missing, script = script)
}

sealed trait StatsBucketAggregation
    extends SiblingPipelineAggregation
    with HasFormat[StatsBucketAggregation]
    with HasGapPolicy[StatsBucketAggregation]
    with HasMeta[StatsBucketAggregation]

private[elasticsearch] final case class StatsBucket(
  name: String,
  bucketsPath: String,
  format: Option[String],
  gapPolicy: Option[GapPolicy],
  meta: Chunk[(String, Json)]
) extends StatsBucketAggregation { self =>

  def format(value: String): StatsBucketAggregation =
    self.copy(format = Some(value))

  def gapPolicy(value: GapPolicy): StatsBucketAggregation =
    self.copy(gapPolicy = Some(value))

  def meta[B: ElasticPrimitive](key: String, value: B): StatsBucketAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  private[elasticsearch] def aggregationType: String = "stats_bucket"

  private[elasticsearch] def params: Json =
    Params.pipeline(bucketsPath = bucketsPath.toJson, format = format, gapPolicy = gapPolicy)
}

sealed trait SumAggregation
    extends MetricAggregation
    with HasFormat[SumAggregation]
    with HasMeta[SumAggregation]
    with HasNumericOrDateMissing[SumAggregation]
    with HasScript[SumAggregation]

private[elasticsearch] final case class Sum(
  name: String,
  field: String,
  format: Option[String],
  meta: Chunk[(String, Json)],
  missing: Option[Json],
  script: Option[Script]
) extends SumAggregation { self =>

  def format(value: String): SumAggregation =
    self.copy(format = Some(value))

  def meta[B: ElasticPrimitive](key: String, value: B): SumAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def missing[B: ElasticPrimitive: NumericOrDate](value: B): SumAggregation =
    self.copy(missing = Some(value.toJson))

  def script(script: Script): SumAggregation =
    self.copy(script = Some(script))

  private[elasticsearch] def aggregationType: String = "sum"

  private[elasticsearch] def params: Json =
    Params.field(field = field, format = format, missing = missing, script = script)
}

sealed trait SumBucketAggregation
    extends SiblingPipelineAggregation
    with HasFormat[SumBucketAggregation]
    with HasGapPolicy[SumBucketAggregation]
    with HasMeta[SumBucketAggregation]

private[elasticsearch] final case class SumBucket(
  name: String,
  bucketsPath: String,
  format: Option[String],
  gapPolicy: Option[GapPolicy],
  meta: Chunk[(String, Json)]
) extends SumBucketAggregation { self =>

  def format(value: String): SumBucketAggregation =
    self.copy(format = Some(value))

  def gapPolicy(value: GapPolicy): SumBucketAggregation =
    self.copy(gapPolicy = Some(value))

  def meta[B: ElasticPrimitive](key: String, value: B): SumBucketAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  private[elasticsearch] def aggregationType: String = "sum_bucket"

  private[elasticsearch] def params: Json =
    Params.pipeline(bucketsPath = bucketsPath.toJson, format = format, gapPolicy = gapPolicy)
}

sealed trait TermsAggregation
    extends MultiBucketAggregation
    with HasMeta[TermsAggregation]
    with HasMinDocCount[TermsAggregation]
    with HasOrder[TermsAggregation]
    with HasSize[TermsAggregation]
    with WithSubAgg[TermsAggregation, SingleElasticAggregation]

private[elasticsearch] final case class Terms(
  name: String,
  field: String,
  meta: Chunk[(String, Json)],
  minDocCount: Option[Int],
  order: Chunk[AggregationOrder],
  size: Option[Int],
  subAggregations: Chunk[SingleElasticAggregation]
) extends TermsAggregation { self =>

  def meta[B: ElasticPrimitive](key: String, value: B): TermsAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def minDocCount(value: Int): TermsAggregation =
    self.copy(minDocCount = Some(value))

  def orderBy(order: AggregationOrder, orders: AggregationOrder*): TermsAggregation =
    self.copy(order = self.order ++ (order +: Chunk.fromIterable(orders)))

  def size(value: Int): TermsAggregation =
    self.copy(size = Some(value))

  def withSubAgg(aggregation: SingleElasticAggregation): TermsAggregation =
    self.copy(subAggregations = subAggregations :+ aggregation)

  private[elasticsearch] def aggregationType: String = "terms"

  private[elasticsearch] def params: Json =
    Params(
      Some("field" -> field.toJson),
      minDocCount.map("min_doc_count" -> _.toJson),
      Params.order(order),
      size.map("size" -> _.toJson)
    )
}

sealed trait ValueCountAggregation
    extends MetricAggregation
    with HasMeta[ValueCountAggregation]
    with HasScript[ValueCountAggregation]

private[elasticsearch] final case class ValueCount(
  name: String,
  field: String,
  meta: Chunk[(String, Json)],
  script: Option[Script]
) extends ValueCountAggregation { self =>

  def meta[B: ElasticPrimitive](key: String, value: B): ValueCountAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def script(script: Script): ValueCountAggregation =
    self.copy(script = Some(script))

  private[elasticsearch] def aggregationType: String = "value_count"

  private[elasticsearch] def params: Json =
    Params.field(field = field, format = None, missing = None, script = script)
}

sealed trait WeightedAvgAggregation extends MetricAggregation with HasMeta[WeightedAvgAggregation] {

  /**
   * Sets the `valueMissing` parameter for the [[zio.elasticsearch.aggregation.WeightedAvgAggregation]].
   * The`valueMissing` parameter provides a value to use when a document is missing the value field that the aggregation
   * is running on.
   *
   * @param value
   *   the value to use for missing documents
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.WeightedAvgAggregation]] enriched with the `valueMissing`
   *   parameter.
   */
  def valueMissing(value: Double): WeightedAvgAggregation

  /**
   * Sets the `weightMissing` parameter for the [[zio.elasticsearch.aggregation.WeightedAvgAggregation]].
   * The`weightMissing` parameter provides a value to use when a document is missing the weight field that the
   * aggregation is running on.
   *
   * @param value
   *   the value to use for missing documents
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.WeightedAvgAggregation]] enriched with the `weightMissing`
   *   parameter.
   */
  def weightMissing(value: Double): WeightedAvgAggregation
}

private[elasticsearch] final case class WeightedAvg(
  name: String,
  valueField: String,
  weightField: String,
  meta: Chunk[(String, Json)],
  valueMissing: Option[Double],
  weightMissing: Option[Double]
) extends WeightedAvgAggregation { self =>

  def meta[B: ElasticPrimitive](key: String, value: B): WeightedAvgAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def valueMissing(value: Double): WeightedAvgAggregation =
    self.copy(valueMissing = Some(value))

  def weightMissing(value: Double): WeightedAvgAggregation =
    self.copy(weightMissing = Some(value))

  private[elasticsearch] def aggregationType: String = "weighted_avg"

  private[elasticsearch] def params: Json =
    Obj(
      "value"  -> Params(Some("field" -> valueField.toJson), valueMissing.map("missing" -> _.toJson)),
      "weight" -> Params(Some("field" -> weightField.toJson), weightMissing.map("missing" -> _.toJson))
    )
}

private[aggregation] object Params {

  def apply(params: Option[(String, Json)]*): Obj =
    Obj(Chunk.fromIterable(params.flatten))

  def addMeta(meta: Chunk[(String, Json)], key: String, value: Json): Chunk[(String, Json)] =
    meta.filterNot { case (k, _) => k == key } :+ (key -> value)

  def bucketsPath(paths: Map[String, String]): Obj =
    Obj(Chunk.fromIterable(paths).map { case (variable, path) => variable -> path.toJson })

  def field(field: String, format: Option[String], missing: Option[Json], script: Option[Script]): Obj =
    Params(
      Some("field" -> field.toJson),
      format.map("format" -> _.toJson),
      missing.map("missing" -> _),
      script.map("script" -> _.toJson)
    )

  def order(order: Chunk[AggregationOrder]): Option[(String, Json)] = {
    def toJson(o: AggregationOrder): Json = Obj(o.value -> o.order.toString.toJson)

    order.toList match {
      case Nil      => None
      case o :: Nil => Some("order" -> toJson(o))
      case orders   => Some("order" -> Arr(Chunk.fromIterable(orders).map(toJson)))
    }
  }

  def pipeline(bucketsPath: Json, format: Option[String], gapPolicy: Option[GapPolicy]): Obj =
    Params(
      Some("buckets_path" -> bucketsPath),
      format.map("format" -> _.toJson),
      gapPolicy.map(policy => "gap_policy" -> policy.toString.toJson)
    )

  def ranges(field: String, ranges: Chunk[Json], isKeyed: Boolean): Obj =
    Params(
      Some("field" -> field.toJson),
      if (isKeyed) Some("keyed" -> Json.Bool(true)) else None,
      Some("ranges" -> Arr(ranges))
    )
}
