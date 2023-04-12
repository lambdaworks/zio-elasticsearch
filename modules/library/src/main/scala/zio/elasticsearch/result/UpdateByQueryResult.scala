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

package zio.elasticsearch.result

import zio.elasticsearch.executor.response.UpdateByQueryResponse

final case class UpdateByQueryResult(
  took: Int,
  total: Int,
  updated: Int,
  deleted: Int,
  versionConflicts: Int
)

object UpdateByQueryResult {
  def from(response: UpdateByQueryResponse): UpdateByQueryResult =
    UpdateByQueryResult(response.took, response.total, response.updated, response.deleted, response.versionConflicts)
}
