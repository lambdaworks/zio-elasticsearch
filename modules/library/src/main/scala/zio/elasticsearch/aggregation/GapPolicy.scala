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

sealed trait GapPolicy

object GapPolicy {

  /**
   * Treats missing data as if the bucket does not exist. The bucket is skipped and the calculation continues with the
   * next available value.
   */
  case object Skip extends GapPolicy {
    override def toString: String = "skip"
  }

  /**
   * Replaces missing values with a zero and continues the calculation as normal.
   */
  case object InsertZeros extends GapPolicy {
    override def toString: String = "insert_zeros"
  }

  /**
   * Uses the value as-is if it is not missing, and otherwise skips the bucket (like `skip`).
   */
  case object KeepValues extends GapPolicy {
    override def toString: String = "keep_values"
  }
}
