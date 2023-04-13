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

import zio.elasticsearch.query.sort.Sort

package object request {
  private[elasticsearch] trait HasRefresh[R <: HasRefresh[R]] {
    def refresh(value: Boolean): R

    final def refreshFalse: R = refresh(false)

    final def refreshTrue: R = refresh(true)
  }

  private[elasticsearch] trait HasRouting[R <: HasRouting[R]] {
    def routing(value: Routing): R
  }

  private[elasticsearch] trait HasFrom[R <: HasFrom[R]] {
    def from(value: Int): R
  }

  private[elasticsearch] trait HasSize[R <: HasSize[R]] {
    def size(value: Int): R
  }

  private[elasticsearch] trait WithSort[R <: WithSort[R]] {
    def sort(sorts: Sort*): R
  }
}
