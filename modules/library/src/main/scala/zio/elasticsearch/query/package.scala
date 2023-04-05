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

package object query {
  private[elasticsearch] trait HasBoost[Q <: HasBoost[Q]] {
    def boost(value: Double): Q
  }

  private[elasticsearch] trait HasCaseInsensitive[Q <: HasCaseInsensitive[Q]] {
    def caseInsensitive(value: Boolean): Q

    def caseInsensitiveFalse: Q

    def caseInsensitiveTrue: Q
  }

  private[elasticsearch] trait HasIgnoreUnmapped[Q <: HasIgnoreUnmapped[Q]] {
    def ignoreUnmapped(value: Boolean): Q

    def ignoreUnmappedFalse: Q

    def ignoreUnmappedTrue: Q
  }

  private[elasticsearch] trait HasInnerHits[Q <: HasInnerHits[Q]] {
    def innerHitsEmpty: Q = innerHits(InnerHits())
    def innerHits(innerHits: InnerHits): Q
  }

  private[elasticsearch] trait HasScoreMode[Q <: HasScoreMode[Q]] {
    def scoreMode(scoreMode: ScoreMode): Q
  }
}
