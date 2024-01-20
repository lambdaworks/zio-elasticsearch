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

  /**
   * Constructs a type-safe instance of [[zio.elasticsearch.query.ElasticKNNQuery]] using the specified parameters.
   * [[zio.elasticsearch.query.ElasticKNNQuery]] is used to perform a k-nearest neighbor (kNN) search and returns the
   * matching documents.
   *
   * @param field
   *   the type-safe field for which query is specified for
   * @param k
   *   number of nearest neighbors to return as top hits (must be less than `numCandidates`)
   * @param numCandidates
   *   number of nearest neighbor candidates to consider per shard
   * @param queryVector
   *   query vector
   * @tparam S
   *   document for which field query is executed
   * @return
   *   an instance of [[zio.elasticsearch.query.ElasticKNNQuery]] that represents the kNN query to be performed.
   */
  final def kNN[S](field: Field[S, _], k: Int, numCandidates: Int, queryVector: Chunk[Double]): ElasticKNNQuery[S] =
    KNN(field = field.toString, k = k, numCandidates = numCandidates, queryVector = queryVector)

  /**
   * Constructs an instance of [[zio.elasticsearch.query.ElasticKNNQuery]] using the specified parameters.
   * [[zio.elasticsearch.query.ElasticKNNQuery]] is used to perform a k-nearest neighbor (kNN) search and returns the
   * matching documents.
   *
   * @param field
   *   the field for which query is specified for
   * @param k
   *   number of nearest neighbors to return as top hits (must be less than `numCandidates`)
   * @param numCandidates
   *   number of nearest neighbor candidates to consider per shard
   * @param queryVector
   *   query vector
   * @return
   *   an instance of [[zio.elasticsearch.query.ElasticKNNQuery]] that represents the kNN query to be performed.
   */
  final def kNN(field: String, k: Int, numCandidates: Int, queryVector: Chunk[Double]): ElasticKNNQuery[Any] =
    KNN(field = field, k = k, numCandidates = numCandidates, queryVector = queryVector)

}
