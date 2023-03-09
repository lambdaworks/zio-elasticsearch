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

import zio.elasticsearch.ElasticQuery.NestedQuery
import zio.elasticsearch.ElasticQueryType.Nested

sealed trait ScoreMode

object ScoreMode {

  final case object Avg  extends ScoreMode
  final case object Max  extends ScoreMode
  final case object Min  extends ScoreMode
  final case object None extends ScoreMode
  final case object Sum  extends ScoreMode

  trait WithScoreMode[EQT <: ElasticQueryType] {
    def withScoreMode[S](query: ElasticQuery[S, EQT], scoreMode: ScoreMode): ElasticQuery[S, EQT]
  }

  object WithScoreMode {
    implicit val nestedWithScoreMode: WithScoreMode[Nested] =
      new WithScoreMode[Nested] {
        def withScoreMode[S](query: ElasticQuery[S, Nested], scoreMode: ScoreMode): ElasticQuery[S, Nested] =
          query match {
            case q: NestedQuery[S] => q.copy(scoreMode = Some(scoreMode))
          }
      }
  }
}
