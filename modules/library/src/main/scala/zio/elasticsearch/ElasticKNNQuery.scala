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

import zio.Chunk
import zio.elasticsearch.query._
import zio.schema.Schema.Field

object ElasticKNNQuery {
  final def kNN[S](field: Field[S, _], k: Int, numCandidates: Int, queryVector: Chunk[Double]): ElasticKNNQuery[S] =
    KNN(field = field.toString, k = k, numCandidates = numCandidates, queryVector = queryVector)

  final def kNN(field: String, k: Int, numCandidates: Int, queryVector: Chunk[Double]): ElasticKNNQuery[Any] =
    KNN(field = field, k = k, numCandidates = numCandidates, queryVector = queryVector)
}
