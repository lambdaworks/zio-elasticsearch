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

package zio.elasticsearch.query

package object sort {
  private[elasticsearch] trait WithFormat[S <: WithFormat[S]] {
    def format(value: String): S
  }

  private[elasticsearch] trait WithMode[S <: WithMode[S]] {
    def mode(value: SortMode): S
  }

  private[elasticsearch] trait WithMissing[S <: WithMissing[S]] {
    def missing(value: Missing): S
  }

  private[elasticsearch] trait WithNumericType[S <: WithNumericType[S]] {
    def numericType(value: NumericType): S
  }

  private[elasticsearch] trait WithOrder[S <: WithOrder[S]] {
    def order(value: SortOrder): S
  }

  private[elasticsearch] trait WithUnmappedType[S <: WithUnmappedType[S]] {
    def unmappedType(value: String): S
  }
}
