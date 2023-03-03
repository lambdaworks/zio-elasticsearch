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

import zio.prelude.ZValidation
import zio.schema.Schema
import zio.{IO, Task, ZIO}

sealed trait ElasticResult[F[_]] {
  def documentAs[A: Schema]: Task[F[A]]
}

final class GetResult private[elasticsearch] (private val doc: Option[Item]) extends ElasticResult[Option] {
  override def documentAs[A: Schema]: IO[DecodingException, Option[A]] =
    ZIO
      .fromEither(doc match {
        case Some(item) =>
          item.documentAs match {
            case Left(e)    => Left(DecodingException(s"Could not parse the document: ${e.message}"))
            case Right(doc) => Right(Some(doc))
          }
        case None =>
          Right(None)
      })
      .mapError(e => DecodingException(s"Could not parse the document: ${e.message}"))
}

final class SearchResult private[elasticsearch] (private val hits: List[Item]) extends ElasticResult[List] {
  override def documentAs[A: Schema]: IO[DecodingException, List[A]] =
    ZIO.fromEither {
      ZValidation.validateAll(hits.map(item => ZValidation.fromEither(item.documentAs))).toEitherWith { errors =>
        DecodingException(s"Could not parse all documents successfully: ${errors.map(_.message).mkString(",")})")
      }
    }
}
