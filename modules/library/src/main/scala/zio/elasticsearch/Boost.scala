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
    def withBoost[S](query: ElasticQuery[S, EQT], value: Double): ElasticQuery[S, EQT]
  }

  object WithBoost {
    implicit val boolWithBoost: WithBoost[Bool] =
      new WithBoost[Bool] {
        def withBoost[S](query: ElasticQuery[S, Bool], value: Double): ElasticQuery[S, Bool] =
          query match {
            case q: BoolQuery[S] => q.copy(boost = Some(value))
          }
      }

    implicit val matchAllWithBoost: WithBoost[MatchAll] =
      new WithBoost[MatchAll] {
        def withBoost[S](query: ElasticQuery[S, MatchAll], value: Double): ElasticQuery[S, MatchAll] =
          query match {
            case q: MatchAllQuery => q.copy(boost = Some(value))
          }
      }

    implicit def rangeWithBoost[A, LB <: LowerBound, UB <: UpperBound]: WithBoost[Range[A, LB, UB]] =
      new WithBoost[Range[A, LB, UB]] {
        def withBoost[S](query: ElasticQuery[S, Range[A, LB, UB]], value: Double): ElasticQuery[S, Range[A, LB, UB]] =
          query match {
            case q: RangeQuery[_, A, LB, UB] => q.copy(boost = Some(value))
          }
      }

    implicit def termWithBoost[A: ElasticPrimitive]: WithBoost[Term[A]] =
      new WithBoost[Term[A]] {
        def withBoost[S](query: ElasticQuery[S, Term[A]], value: Double): ElasticQuery[S, Term[A]] =
          query match {
            case q: TermQuery[_, A] => q.copy(boost = Some(value))
          }
      }

    implicit val wildcardWithBoost: WithBoost[Wildcard] =
      new WithBoost[Wildcard] {
        def withBoost[S](query: ElasticQuery[S, Wildcard], value: Double): ElasticQuery[S, Wildcard] =
          query match {
            case q: WildcardQuery[_] => q.copy(boost = Some(value))
          }
      }
  }
}
