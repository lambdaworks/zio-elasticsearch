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

package zio.elasticsearch

import zio.elasticsearch.ElasticAggregation.MultipleAggregations
import zio.elasticsearch.ElasticPrimitive._
import zio.json.ast.Json
import zio.json.ast.Json.Obj

sealed trait ElasticAggregation { self =>
  def paramsToJson: Json

  final def toJson: Json =
    Obj("aggs" -> paramsToJson)
}

sealed trait WithSubAgg[A <: WithSubAgg[A]] {
  def withSubAgg(subAgg: SingleElasticAggregation): A
}

sealed trait WithAgg {
  def withAgg(agg: SingleElasticAggregation): MultipleAggregations
}

sealed trait SingleElasticAggregation extends ElasticAggregation

object ElasticAggregation {
  def multipleAggregations: Multiple =
    Multiple(aggregations = Nil)

  def termsAggregation(name: String, field: Field[_, String], multiField: Option[String] = None): Terms =
    Terms(name = name, field = field.toString ++ multiField.map("." ++ _).getOrElse(""), subAggregations = Nil)

  def termsAggregation(name: String, field: String): Terms =
    Terms(name = name, field = field, subAggregations = Nil)

  sealed trait MultipleAggregations extends ElasticAggregation with WithAgg

  private[elasticsearch] final case class Multiple(aggregations: List[SingleElasticAggregation])
      extends MultipleAggregations { self =>
    def aggregations(aggregations: SingleElasticAggregation*): MultipleAggregations =
      self.copy(aggregations = self.aggregations ++ aggregations)

    def paramsToJson: Json =
      Obj(aggregations.map { case Terms(name, field, subAggregations) =>
        (
          name,
          Obj(("terms" -> Obj("field" -> field.toJson)) :: subAggregations.map { agg =>
            "aggs" -> agg.paramsToJson
          }: _*)
        )
      }: _*)

    def withAgg(agg: SingleElasticAggregation): MultipleAggregations =
      self.copy(aggregations = agg +: aggregations)
  }

  sealed trait TermsAggregation extends SingleElasticAggregation with WithSubAgg[TermsAggregation] with WithAgg

  private[elasticsearch] final case class Terms(
    name: String,
    field: String,
    subAggregations: List[SingleElasticAggregation]
  ) extends TermsAggregation { self =>

    def paramsToJson: Json =
      Obj(name -> paramsToJsonHelper(field, subAggregations))

    private def paramsToJsonHelper(currField: String, currSubAggs: List[SingleElasticAggregation]): Obj =
      if (currSubAggs.nonEmpty) {
        Obj(
          "terms" -> Obj("field" -> currField.toJson),
          "aggs" -> Obj(currSubAggs.map { case termsAgg: Terms =>
            (termsAgg.name, paramsToJsonHelper(termsAgg.field, termsAgg.subAggregations))
          }: _*)
        )
      } else {
        Obj("terms" -> Obj("field" -> currField.toJson))
      }

    def withAgg(aggregation: SingleElasticAggregation): MultipleAggregations =
      multipleAggregations.aggregations(List(self, aggregation): _*)

    def withSubAgg(aggregation: SingleElasticAggregation): TermsAggregation =
      self.copy(subAggregations = aggregation +: subAggregations)
  }
}
