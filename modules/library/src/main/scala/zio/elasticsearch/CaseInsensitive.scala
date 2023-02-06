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

object CaseInsensitive {

  trait WithCaseInsensitive[EQT <: ElasticQueryType] {
    def withCaseInsensitive(query: ElasticQuery[EQT], value: Boolean): ElasticQuery[EQT]
  }

  object WithCaseInsensitive {
    implicit val termWithCaseInsensitiveString: WithCaseInsensitive[Term[String]] =
      (query: ElasticQuery[Term[String]], value: Boolean) =>
        query match {
          case q: TermQuery[String] => q.copy(caseInsensitive = Some(value))
        }

    implicit val wildcardWithCaseInsensitiveString: WithCaseInsensitive[Wildcard] =
      (query: ElasticQuery[Wildcard], value: Boolean) =>
        query match {
          case q: WildcardQuery => q.copy(caseInsensitive = Some(value))
        }
  }
}
