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

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils._
import zio.prelude.AssertionError.failure
import zio.prelude.Newtype
import zio.schema.Schema

package object elasticsearch {
  private[elasticsearch] class ElasticException(message: String) extends RuntimeException(message)

  private[elasticsearch] final case class DecodingException(message: String) extends ElasticException(message)

  object DocumentId extends Newtype[String]
  type DocumentId = DocumentId.Type

  object IndexName extends Newtype[String] {
    override def assertion = assertCustom { (name: String) => // scalafix:ok
      if (
        name.toLowerCase != name ||
        startsWithAny(name, "+", "-", "_") ||
        containsAny(name, List("*", "?", "\"", "<", ">", "|", " ", ",", "#", ":")) ||
        equalsAny(name, ".", "..") ||
        name.getBytes().length > 255
      )
        Left(
          failure(
            s"""
               |   - Must be lower case only
               |   - Cannot include \\, /, *, ?, ", <, >, |, ` `(space character), `,`(comma), #.
               |   - Cannot include ":"(since 7.0).
               |   - Cannot be empty
               |   - Cannot start with -, _, +.
               |   - Cannot be `.` or `..`.
               |   - Cannot be longer than 255 bytes (note it is bytes, so multi-byte characters will count towards the 255 limit faster).
               |   - Names starting with . are deprecated, except for hidden indices and internal indices managed by plugins.
               |""".stripMargin
          )
        )
      else
        Right(())
    }
  }
  type IndexName = IndexName.Type

  def containsAny(name: String, params: List[String]): Boolean =
    params.exists(StringUtils.contains(name, _))

  final implicit class ZIOAggregationsOps[R](zio: RIO[R, AggregationsResult]) {
    def aggregation(name: String): RIO[R, Option[ElasticAggregationResponse]] =
      zio.flatMap(_.aggregation(name))

    def aggregations: RIO[R, Map[String, ElasticAggregationResponse]] =
      zio.flatMap(_.aggregations)
  }

  final implicit class ZIODocumentOps[R, F[_]](zio: RIO[R, DocumentResult[F]]) {
    def documentAs[A: Schema]: RIO[R, F[A]] =
      zio.flatMap(_.documentAs[A])
  }
}
