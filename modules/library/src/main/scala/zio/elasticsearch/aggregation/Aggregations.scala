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
import zio.elasticsearch.ElasticPrimitive.ElasticPrimitiveOps
import zio.elasticsearch.aggregation.options._
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj}

sealed trait ElasticAggregation { self =>
  private[elasticsearch] def paramsToJson: Json

  private[elasticsearch] final def toJson: Json =
    Obj("aggs" -> paramsToJson)
}

sealed trait SingleElasticAggregation extends ElasticAggregation

sealed trait MultipleAggregations extends ElasticAggregation with WithAgg {
  def aggregations(aggregations: SingleElasticAggregation*): MultipleAggregations
}

private[elasticsearch] final case class Multiple(aggregations: List[SingleElasticAggregation])
    extends MultipleAggregations { self =>
  def aggregations(aggregations: SingleElasticAggregation*): MultipleAggregations =
    self.copy(aggregations = self.aggregations ++ aggregations)

  def paramsToJson: Json =
    aggregations.map(_.paramsToJson).reduce(_ merge _)

  def withAgg(agg: SingleElasticAggregation): MultipleAggregations =
    self.copy(aggregations = agg +: aggregations)
}

sealed trait TermsAggregation
    extends SingleElasticAggregation
    with HasOrder[TermsAggregation]
    with WithSubAgg[TermsAggregation]
    with WithAgg {

  /**
   * Sets the maximum number of terms to be returned by the aggregation. By default, the [[TermsAggregation]] returns
   * the top ten terms with the most documents.
   *
   * @param value
   *   a non-negative number to set the `size` parameter in the [[zio.elasticsearch.aggregation.TermsAggregation]]
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.TermsAggregation]] enriched with the `size` parameter.
   */
  def size(value: Int): TermsAggregation
}

private[elasticsearch] final case class Terms(
  name: String,
  field: String,
  order: Set[AggregationOrder],
  subAggregations: List[SingleElasticAggregation],
  size: Option[Int]
) extends TermsAggregation { self =>
  def orderBy(requiredOrder: AggregationOrder, orders: AggregationOrder*): TermsAggregation =
    self.copy(order = order + requiredOrder ++ orders.toSet)

  def paramsToJson: Json =
    Obj(name -> paramsToJsonHelper)

  def size(value: Int): TermsAggregation =
    self.copy(size = Some(value))

  def withAgg(aggregation: SingleElasticAggregation): MultipleAggregations =
    multipleAggregations.aggregations(self, aggregation)

  def withSubAgg(aggregation: SingleElasticAggregation): TermsAggregation =
    self.copy(subAggregations = aggregation +: subAggregations)

  private def paramsToJsonHelper: Obj = {
    val orderJson: Json =
      order.toList match {
        case Nil =>
          Obj()
        case ::(o, Nil) =>
          Obj("order" -> Obj(o.value -> o.order.toString.toJson))
        case orders =>
          Obj("order" -> Arr(orders.collect { case AggregationOrder(value, order) =>
            Obj(value -> order.toString.toJson)
          }: _*))
      }

    val sizeJson = size.fold(Obj())(s => Obj("size" -> s.toJson))

    val subAggsJson =
      if (self.subAggregations.nonEmpty)
        Obj("aggs" -> self.subAggregations.map(_.paramsToJson).reduce(_ merge _))
      else
        Obj()

    Obj("terms" -> (Obj("field" -> self.field.toJson) merge orderJson merge sizeJson)) merge subAggsJson
  }
}
