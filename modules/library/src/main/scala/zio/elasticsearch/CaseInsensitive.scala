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

import zio.elasticsearch.ElasticQuery.{TermQuery, WildcardQuery}
import zio.elasticsearch.ElasticQueryType.{Term, Wildcard}

object CaseInsensitive {

  trait WithCaseInsensitive[EQT <: ElasticQueryType] {
    def withCaseInsensitive[S](query: ElasticQuery[S, EQT], value: Boolean): ElasticQuery[S, EQT]
  }

  object WithCaseInsensitive {
    implicit val termWithCaseInsensitiveString: WithCaseInsensitive[Term[String]] =
      new WithCaseInsensitive[Term[String]] {
        def withCaseInsensitive[S](
          query: ElasticQuery[S, Term[String]],
          value: Boolean
        ): ElasticQuery[S, Term[String]] =
          query match {
            case q: TermQuery[S, String] => q.copy(caseInsensitive = Some(value))
          }
      }
    implicit val wildcardWithCaseInsensitiveString: WithCaseInsensitive[Wildcard] =
      new WithCaseInsensitive[Wildcard] {
        def withCaseInsensitive[S](query: ElasticQuery[S, Wildcard], value: Boolean): ElasticQuery[S, Wildcard] =
          query match {
            case q: WildcardQuery[S] => q.copy(caseInsensitive = Some(value))
          }
      }
  }
}
