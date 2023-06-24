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

import zio.Chunk
import zio.elasticsearch.ElasticAggregation.multipleAggregations
import zio.elasticsearch.ElasticPrimitive.ElasticPrimitiveOps
import zio.elasticsearch.aggregation.options._
import zio.elasticsearch.query.sort.Sort
import zio.elasticsearch.script.Script
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj}

sealed trait ElasticAggregation { self =>
  private[elasticsearch] def toJson: Json
}

sealed trait SingleElasticAggregation extends ElasticAggregation

sealed trait AvgAggregation extends SingleElasticAggregation with HasMissing[AvgAggregation] with WithAgg

private[elasticsearch] final case class Avg(name: String, field: String, missing: Option[Double])
    extends AvgAggregation { self =>
  def missing(value: Double): AvgAggregation =
    self.copy(missing = Some(value))

  def withAgg(agg: SingleElasticAggregation): MultipleAggregations =
    multipleAggregations.aggregations(self, agg)

  private[elasticsearch] def toJson: Json = {
    val missingJson: Json = missing.fold(Obj())(m => Obj("missing" -> m.toJson))

    Obj(name -> Obj("avg" -> (Obj("field" -> field.toJson) merge missingJson)))
  }
}

sealed trait BucketSelectorAggregation extends SingleElasticAggregation with WithAgg

private[elasticsearch] final case class BucketSelector(name: String, script: Script, bucketsPath: Map[String, String])
    extends BucketSelectorAggregation { self =>

  def withAgg(agg: SingleElasticAggregation): MultipleAggregations =
    multipleAggregations.aggregations(self, agg)

  private[elasticsearch] def toJson: Json = {
    val bucketsPathJson: Json = Obj("buckets_path" -> bucketsPath.collect { case (scriptVal, path) =>
      Obj(scriptVal -> path.toJson)
    }.reduce(_ merge _))

    Obj(name -> Obj("bucket_selector" -> (bucketsPathJson merge Obj("script" -> script.toJson))))
  }
}

sealed trait BucketSortAggregation extends SingleElasticAggregation with HasSize[BucketSortAggregation] with WithAgg {

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
  sortBy: Chunk[Sort],
  from: Option[Int],
  size: Option[Int]
) extends BucketSortAggregation { self =>
  def from(value: Int): BucketSortAggregation =
    self.copy(from = Some(value))

  def size(value: Int): BucketSortAggregation =
    self.copy(size = Some(value))

  def sort(sort: Sort, sorts: Sort*): BucketSortAggregation =
    self.copy(sortBy = sortBy ++ (sort :: sorts.toList))

  def withAgg(agg: SingleElasticAggregation): MultipleAggregations =
    multipleAggregations.aggregations(self, agg)

  private[elasticsearch] def toJson: Json = {
    val fromJson: Json = self.from.fold(Obj())(f => Obj("from" -> f.toJson))

    val sizeJson: Json = size.fold(Obj())(s => Obj("size" -> s.toJson))

    val sortJson: Json =
      if (self.sortBy.nonEmpty) Obj("sort" -> Arr(self.sortBy.map(_.toJson): _*)) else Obj()

    Obj(name -> Obj("bucket_sort" -> (sortJson merge fromJson merge sizeJson)))
  }
}

sealed trait CardinalityAggregation
    extends SingleElasticAggregation
    with HasMissing[CardinalityAggregation]
    with WithAgg

private[elasticsearch] final case class Cardinality(name: String, field: String, missing: Option[Double])
    extends CardinalityAggregation { self =>
  def missing(value: Double): CardinalityAggregation =
    self.copy(missing = Some(value))

  def withAgg(agg: SingleElasticAggregation): MultipleAggregations =
    multipleAggregations.aggregations(self, agg)

  private[elasticsearch] def toJson: Json = {
    val missingJson: Json = missing.fold(Obj())(m => Obj("missing" -> m.toJson))

    Obj(name -> Obj("cardinality" -> (Obj("field" -> field.toJson) merge missingJson)))
  }
}

sealed trait MaxAggregation extends SingleElasticAggregation with HasMissing[MaxAggregation] with WithAgg

private[elasticsearch] final case class Max(name: String, field: String, missing: Option[Double])
    extends MaxAggregation { self =>
  def missing(value: Double): MaxAggregation =
    self.copy(missing = Some(value))

  def withAgg(agg: SingleElasticAggregation): MultipleAggregations =
    multipleAggregations.aggregations(self, agg)

  private[elasticsearch] def toJson: Json = {
    val missingJson: Json = missing.fold(Obj())(m => Obj("missing" -> m.toJson))

    Obj(name -> Obj("max" -> (Obj("field" -> field.toJson) merge missingJson)))
  }
}

sealed trait MinAggregation extends SingleElasticAggregation with HasMissing[MinAggregation] with WithAgg

private[elasticsearch] final case class Min(name: String, field: String, missing: Option[Double])
    extends MinAggregation { self =>
  def missing(value: Double): MinAggregation =
    self.copy(missing = Some(value))

  def withAgg(agg: SingleElasticAggregation): MultipleAggregations =
    multipleAggregations.aggregations(self, agg)

  private[elasticsearch] def toJson: Json = {
    val missingJson: Json = missing.fold(Obj())(m => Obj("missing" -> m.toJson))

    Obj(name -> Obj("min" -> (Obj("field" -> field.toJson) merge missingJson)))
  }
}

sealed trait MultipleAggregations extends ElasticAggregation with WithAgg {

  /**
   * Sets the aggregations for the [[zio.elasticsearch.aggregation.MultipleAggregations]].
   *
   * @param aggregations
   *   the aggregations to be set
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.MultipleAggregations]] with the specified aggregations.
   */
  def aggregations(aggregations: SingleElasticAggregation*): MultipleAggregations
}

private[elasticsearch] final case class Multiple(aggregations: Chunk[SingleElasticAggregation])
    extends MultipleAggregations { self =>
  def aggregations(aggregations: SingleElasticAggregation*): MultipleAggregations =
    self.copy(aggregations = self.aggregations ++ aggregations)

  def withAgg(agg: SingleElasticAggregation): MultipleAggregations =
    self.copy(aggregations = agg +: aggregations)

  private[elasticsearch] def toJson: Json =
    aggregations.map(_.toJson).reduce(_ merge _)
}

sealed trait SumAggregation extends SingleElasticAggregation with HasMissing[SumAggregation] with WithAgg

private[elasticsearch] final case class Sum(name: String, field: String, missing: Option[Double])
    extends SumAggregation { self =>
  def missing(value: Double): SumAggregation =
    self.copy(missing = Some(value))

  def withAgg(agg: SingleElasticAggregation): MultipleAggregations =
    multipleAggregations.aggregations(self, agg)

  private[elasticsearch] def toJson: Json = {
    val missingJson: Json = missing.fold(Obj())(m => Obj("missing" -> m.toJson))

    Obj(name -> Obj("sum" -> (Obj("field" -> field.toJson) merge missingJson)))
  }
}

sealed trait TermsAggregation
    extends SingleElasticAggregation
    with HasOrder[TermsAggregation]
    with HasSize[TermsAggregation]
    with WithAgg
    with WithSubAgg[TermsAggregation]

private[elasticsearch] final case class Terms(
  name: String,
  field: String,
  order: Chunk[AggregationOrder],
  subAggregations: Chunk[SingleElasticAggregation],
  size: Option[Int]
) extends TermsAggregation { self =>
  def orderBy(order: AggregationOrder, orders: AggregationOrder*): TermsAggregation =
    self.copy(order = self.order ++ (order +: orders))

  def size(value: Int): TermsAggregation =
    self.copy(size = Some(value))

  def withAgg(aggregation: SingleElasticAggregation): MultipleAggregations =
    multipleAggregations.aggregations(self, aggregation)

  def withSubAgg(aggregation: SingleElasticAggregation): TermsAggregation =
    self.copy(subAggregations = aggregation +: subAggregations)

  private[elasticsearch] def toJson: Json = {
    val orderJson: Json =
      order.toList match {
        case Nil =>
          Obj()
        case o :: Nil =>
          Obj("order" -> Obj(o.value -> o.order.toString.toJson))
        case orders =>
          Obj("order" -> Arr(Chunk.fromIterable(orders).collect { case AggregationOrder(value, order) =>
            Obj(value -> order.toString.toJson)
          }))
      }

    val sizeJson = size.fold(Obj())(s => Obj("size" -> s.toJson))

    val subAggsJson =
      if (self.subAggregations.nonEmpty)
        Obj("aggs" -> self.subAggregations.map(_.toJson).reduce(_ merge _))
      else
        Obj()

    Obj(name -> (Obj("terms" -> (Obj("field" -> self.field.toJson) merge orderJson merge sizeJson)) merge subAggsJson))
  }
}
