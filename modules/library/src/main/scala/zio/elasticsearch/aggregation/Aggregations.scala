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

/**
 * An aggregation that can be used at the top level of a request: either a
 * [[zio.elasticsearch.aggregation.MultipleAggregations]] or a single
 * [[zio.elasticsearch.aggregation.MetricAggregation]], [[zio.elasticsearch.aggregation.BucketAggregation]] or
 * [[zio.elasticsearch.aggregation.SiblingPipelineAggregation]].
 */
sealed trait ElasticAggregation { self =>

  /**
   * Adds an aggregation on the same level, combining both into [[zio.elasticsearch.aggregation.MultipleAggregations]].
   * Aggregations are kept in the order in which they are added.
   *
   * @param agg
   *   the [[zio.elasticsearch.aggregation.ElasticAggregation]] to add
   * @return
   *   an instance of [[zio.elasticsearch.aggregation.MultipleAggregations]] that contains both aggregations.
   */
  final def withAgg(agg: ElasticAggregation): MultipleAggregations =
    multipleAggregations.aggregations(self, agg)

  private[elasticsearch] def toJson: Json
}

/**
 * A single named aggregation. Any single aggregation can be used as a sub-aggregation of a
 * [[zio.elasticsearch.aggregation.BucketAggregation]], while only metric, bucket and sibling pipeline aggregations can
 * be used at the top level of a request.
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
sealed trait MetricAggregation extends SingleElasticAggregation with ElasticAggregation {
  private[elasticsearch] final def subAggregations: Chunk[SingleElasticAggregation] = Chunk.empty
}

/**
 * An aggregation that groups documents into buckets, and can have sub-aggregations that are computed for each bucket.
 */
sealed trait BucketAggregation extends SingleElasticAggregation with ElasticAggregation

/**
 * An aggregation that works on the outputs of other aggregations instead of on documents.
 */
sealed trait PipelineAggregation extends SingleElasticAggregation {
  private[elasticsearch] final def subAggregations: Chunk[SingleElasticAggregation] = Chunk.empty
}

/**
 * A pipeline aggregation that works on the buckets of its parent aggregation, so it can only be used as a
 * sub-aggregation of a [[zio.elasticsearch.aggregation.BucketAggregation]].
 */
sealed trait ParentPipelineAggregation extends PipelineAggregation

/**
 * A pipeline aggregation that works on the output of a sibling aggregation, so it can be used on the same level as the
 * aggregation it refers to.
 */
sealed trait SiblingPipelineAggregation extends PipelineAggregation with ElasticAggregation

sealed trait AvgAggregation
    extends MetricAggregation
    with HasFormat[AvgAggregation]
    with HasMeta[AvgAggregation]
    with HasMissing[AvgAggregation]
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

  def missing[B: ElasticPrimitive](value: B): AvgAggregation =
    self.copy(missing = Some(value.toJson))

  def script(script: Script): AvgAggregation =
    self.copy(script = Some(script))

  private[elasticsearch] def aggregationType: String = "avg"

  private[elasticsearch] def params: Json =
    Params.field(field = field, format = format, missing = missing, script = script)
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
      Some("buckets_path" -> Obj(Chunk.fromIterable(bucketsPath).map { case (variable, path) =>
        variable -> path.toJson
      })),
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

sealed trait ExtendedStatsAggregation
    extends MetricAggregation
    with HasFormat[ExtendedStatsAggregation]
    with HasMeta[ExtendedStatsAggregation]
    with HasMissing[ExtendedStatsAggregation]
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

  def missing[B: ElasticPrimitive](value: B): ExtendedStatsAggregation =
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
    extends BucketAggregation
    with HasMeta[FilterAggregation]
    with WithSubAgg[FilterAggregation]

private[elasticsearch] final case class Filter(
  name: String,
  query: ElasticQuery[_],
  meta: Chunk[(String, Json)],
  subAggregations: Chunk[SingleElasticAggregation]
) extends FilterAggregation { self =>

  def meta[B: ElasticPrimitive](key: String, value: B): FilterAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def withSubAgg(aggregation: SingleElasticAggregation): FilterAggregation =
    self.copy(subAggregations = subAggregations :+ aggregation)

  private[elasticsearch] def aggregationType: String = "filter"

  private[elasticsearch] def params: Json =
    query.toJson(fieldPath = None)
}

sealed trait IpRangeAggregation
    extends BucketAggregation
    with HasKeyed[IpRangeAggregation]
    with HasMeta[IpRangeAggregation]
    with WithSubAgg[IpRangeAggregation]

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
    with HasMissing[MaxAggregation]
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

  def missing[B: ElasticPrimitive](value: B): MaxAggregation =
    self.copy(missing = Some(value.toJson))

  def script(script: Script): MaxAggregation =
    self.copy(script = Some(script))

  private[elasticsearch] def aggregationType: String = "max"

  private[elasticsearch] def params: Json =
    Params.field(field = field, format = format, missing = missing, script = script)
}

sealed trait MinAggregation
    extends MetricAggregation
    with HasFormat[MinAggregation]
    with HasMeta[MinAggregation]
    with HasMissing[MinAggregation]
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

  def missing[B: ElasticPrimitive](value: B): MinAggregation =
    self.copy(missing = Some(value.toJson))

  def script(script: Script): MinAggregation =
    self.copy(script = Some(script))

  private[elasticsearch] def aggregationType: String = "min"

  private[elasticsearch] def params: Json =
    Params.field(field = field, format = format, missing = missing, script = script)
}

sealed trait MissingAggregation
    extends BucketAggregation
    with HasMeta[MissingAggregation]
    with WithSubAgg[MissingAggregation]

private[elasticsearch] final case class Missing(
  name: String,
  field: String,
  meta: Chunk[(String, Json)],
  subAggregations: Chunk[SingleElasticAggregation]
) extends MissingAggregation { self =>

  def meta[B: ElasticPrimitive](key: String, value: B): MissingAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def withSubAgg(aggregation: SingleElasticAggregation): MissingAggregation =
    self.copy(subAggregations = subAggregations :+ aggregation)

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

  private[elasticsearch] def toJson: Json =
    aggregations.map(_.toJson).foldLeft[Json](Obj())(_.merge(_))
}

sealed trait PercentileRanksAggregation
    extends MetricAggregation
    with HasMeta[PercentileRanksAggregation]
    with HasMissing[PercentileRanksAggregation]

private[elasticsearch] final case class PercentileRanks(
  name: String,
  field: String,
  meta: Chunk[(String, Json)],
  missing: Option[Json],
  values: Chunk[BigDecimal]
) extends PercentileRanksAggregation { self =>

  def meta[B: ElasticPrimitive](key: String, value: B): PercentileRanksAggregation =
    self.copy(meta = Params.addMeta(self.meta, key, value.toJson))

  def missing[B: ElasticPrimitive](value: B): PercentileRanksAggregation =
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
    with HasMissing[PercentilesAggregation] {

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

  def missing[B: ElasticPrimitive](value: B): PercentilesAggregation =
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
    extends BucketAggregation
    with HasKeyed[RangeAggregation]
    with HasMeta[RangeAggregation]
    with WithSubAgg[RangeAggregation]

private[elasticsearch] final case class Range[A](
  name: String,
  field: String,
  isKeyed: Boolean,
  meta: Chunk[(String, Json)],
  ranges: NonEmptyChunk[RangeBound[A]],
  subAggregations: Chunk[SingleElasticAggregation]
)(implicit EP: ElasticPrimitive[A])
    extends RangeAggregation { self =>

  def keyed: RangeAggregation =
    self.copy[A](isKeyed = true)

  def meta[B: ElasticPrimitive](key: String, value: B): RangeAggregation =
    self.copy[A](meta = Params.addMeta(self.meta, key, value.toJson))

  def withSubAgg(aggregation: SingleElasticAggregation): RangeAggregation =
    self.copy[A](subAggregations = subAggregations :+ aggregation)

  private[elasticsearch] def aggregationType: String = "range"

  private[elasticsearch] def params: Json =
    Params.ranges(field = field, ranges = ranges.map(_.toJson[A]), isKeyed = isKeyed)
}

/**
 * A range of a range aggregation. Each range includes the `from` value and excludes the `to` value.
 *
 * @tparam A
 *   the type of the `from` and `to` values (e.g. a number for the `range` aggregation)
 */
final case class RangeBound[+A](
  from: Option[A] = None,
  key: Option[String] = None,
  to: Option[A] = None
) { self =>

  def from[B >: A](value: B): RangeBound[B] =
    self.copy(from = Some(value))

  def key(value: String): RangeBound[A] =
    self.copy(key = Some(value))

  def to[B >: A](value: B): RangeBound[B] =
    self.copy(to = Some(value))

  private[elasticsearch] def toJson[B >: A](implicit EP: ElasticPrimitive[B]): Json =
    Params(
      from.map(value => "from" -> EP.toJson(value)),
      key.map("key" -> _.toJson),
      to.map(value => "to" -> EP.toJson(value))
    )
}

sealed trait SamplerAggregation
    extends BucketAggregation
    with HasMeta[SamplerAggregation]
    with WithSubAgg[SamplerAggregation] {

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

  def withSubAgg(aggregation: SingleElasticAggregation): SamplerAggregation =
    self.copy(subAggregations = subAggregations :+ aggregation)

  private[elasticsearch] def aggregationType: String = "sampler"

  private[elasticsearch] def params: Json =
    Obj("shard_size" -> shardSize.toJson)
}

sealed trait StatsAggregation
    extends MetricAggregation
    with HasFormat[StatsAggregation]
    with HasMeta[StatsAggregation]
    with HasMissing[StatsAggregation]
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

  def missing[B: ElasticPrimitive](value: B): StatsAggregation =
    self.copy(missing = Some(value.toJson))

  def script(script: Script): StatsAggregation =
    self.copy(script = Some(script))

  private[elasticsearch] def aggregationType: String = "stats"

  private[elasticsearch] def params: Json =
    Params.field(field = field, format = format, missing = missing, script = script)
}

sealed trait SumAggregation
    extends MetricAggregation
    with HasFormat[SumAggregation]
    with HasMeta[SumAggregation]
    with HasMissing[SumAggregation]
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

  def missing[B: ElasticPrimitive](value: B): SumAggregation =
    self.copy(missing = Some(value.toJson))

  def script(script: Script): SumAggregation =
    self.copy(script = Some(script))

  private[elasticsearch] def aggregationType: String = "sum"

  private[elasticsearch] def params: Json =
    Params.field(field = field, format = format, missing = missing, script = script)
}

sealed trait TermsAggregation
    extends BucketAggregation
    with HasMeta[TermsAggregation]
    with HasMinDocCount[TermsAggregation]
    with HasOrder[TermsAggregation]
    with HasSize[TermsAggregation]
    with WithSubAgg[TermsAggregation]

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

  private[elasticsearch] def params: Json = {
    val orderJson: Option[(String, Json)] =
      order.toList match {
        case Nil      => None
        case o :: Nil => Some("order" -> Obj(o.value -> o.order.toString.toJson))
        case orders   =>
          Some("order" -> Arr(Chunk.fromIterable(orders).map(o => Obj(o.value -> o.order.toString.toJson))))
      }

    Params(
      Some("field" -> field.toJson),
      minDocCount.map("min_doc_count" -> _.toJson),
      orderJson,
      size.map("size" -> _.toJson)
    )
  }
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

  def field(field: String, format: Option[String], missing: Option[Json], script: Option[Script]): Obj =
    Params(
      Some("field" -> field.toJson),
      format.map("format" -> _.toJson),
      missing.map("missing" -> _),
      script.map("script" -> _.toJson)
    )

  def ranges(field: String, ranges: Chunk[Json], isKeyed: Boolean): Obj =
    Params(
      Some("field" -> field.toJson),
      if (isKeyed) Some("keyed" -> Json.Bool(true)) else None,
      Some("ranges" -> Arr(ranges))
    )
}
