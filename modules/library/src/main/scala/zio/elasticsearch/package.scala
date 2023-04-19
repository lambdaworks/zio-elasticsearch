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

package zio

import zio.elasticsearch.executor.response.AggregationResponse
import zio.elasticsearch.result.{AggregationsResult, DocumentResult}
import zio.prelude.Newtype
import zio.schema.Schema

package object elasticsearch {
  object DocumentId extends Newtype[String]
  type DocumentId = DocumentId.Type

  object IndexName extends Newtype[String]
  type IndexName = IndexName.Type

  object Routing extends Newtype[String]
  type Routing = Routing.Type

  final implicit class ZIOAggregationsOps[R](zio: RIO[R, AggregationsResult]) {

    /**
     * Executes the [[SearchRequest]] or the [[SearchAndAggregateRequest]] and retrieves the [[AggregationResponse]], if
     * any.
     *
     * @param name
     *   the name of the aggregation to retrieve
     * @return
     *   returns a `RIO` effect that, when executed, will produce an optional [[AggregationResponse]].
     */
    def aggregation(name: String): RIO[R, Option[AggregationResponse]] =
      zio.flatMap(_.aggregation(name))

    /**
     * Executes the [[SearchRequest]] or the [[SearchAndAggregateRequest]] and retrieves the map of aggregation names
     * and [[AggregationResponse]]s.
     *
     * @return
     *   returns a `RIO` effect that, when executed, will produce a Map of the aggregations name and response.
     */
    def aggregations: RIO[R, Map[String, AggregationResponse]] =
      zio.flatMap(_.aggregations)
  }

  final implicit class ZIODocumentOps[R, F[_]](zio: RIO[R, DocumentResult[F]]) {

    /**
     * Fetches and deserializes a document as a specific type.
     *
     * @tparam A
     *   the type to deserialize the document to
     * @return
     *   returnes a `RIO` effect that, when executed, will fetch and deserialize the document as type `A`
     */
    def documentAs[A: Schema]: RIO[R, F[A]] =
      zio.flatMap(_.documentAs[A])
  }
}
