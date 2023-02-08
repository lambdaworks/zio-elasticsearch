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

import zio.elasticsearch.ElasticQuery._
import zio.elasticsearch.ElasticQueryType.{Bool, MatchAll, Range, Term, Wildcard}

object Boost {

  trait WithBoost[EQT <: ElasticQueryType] {
    def withBoost(query: ElasticQuery[EQT], value: Double): ElasticQuery[EQT]
  }

  object WithBoost {
    implicit val boolWithBoost: WithBoost[Bool] = (query: ElasticQuery[Bool], value: Double) =>
      query match {
        case q: BoolQuery => q.copy(boost = Some(value))
      }

    implicit val matchAllWithBoost: WithBoost[MatchAll] = (query: ElasticQuery[MatchAll], value: Double) =>
      query match {
        case q: MatchAllQuery => q.copy(boost = Some(value))
      }

    implicit def rangeWithBoost[A, LB <: LowerBound, UB <: UpperBound]: WithBoost[Range[A, LB, UB]] =
      (query: ElasticQuery[Range[A, LB, UB]], value: Double) =>
        query match {
          case q: RangeQuery[A, LB, UB] => q.copy(boost = Some(value))
        }

    implicit def termWithBoost[A: ElasticPrimitive]: WithBoost[Term[A]] =
      (query: ElasticQuery[Term[A]], value: Double) =>
        query match {
          case q: TermQuery[A] => q.copy(boost = Some(value))
        }

    implicit val wildcardWithBoost: WithBoost[Wildcard] = (query: ElasticQuery[Wildcard], value: Double) =>
      query match {
        case q: WildcardQuery => q.copy(boost = Some(value))
      }
  }
}
