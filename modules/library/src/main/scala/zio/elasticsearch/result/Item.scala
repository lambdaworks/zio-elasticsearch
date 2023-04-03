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

import zio.Chunk
import zio.json.DecoderOps
import zio.json.ast.Json
import zio.prelude.ZValidation
import zio.schema.Schema
import zio.schema.codec.DecodeError
import zio.schema.codec.JsonCodec.JsonDecoder

final case class Item(raw: Json, highlight: Option[Json] = None) {
  def documentAs[A](implicit schema: Schema[A]): Either[DecodeError, A] = JsonDecoder.decode(schema, raw.toString)

  lazy val highlights: Option[Either[DecodingException, Map[String, Chunk[String]]]] = highlight map { json =>
    ZValidation.fromEither(json.toString.fromJson[Map[String, Chunk[String]]]).toEitherWith { error =>
      DecodingException(s"Could not parse all highlights successfully: ${error.mkString(",")})")
    }
  }

  def highlight(field: String): Option[Either[DecodingException, Chunk[String]]] = highlights match {
    case Some(value) =>
      value match {
        case Left(err) => Some(Left(err))
        case Right(highlightsMap) =>
          highlightsMap.get(field) match {
            case Some(value) => Some(Right(value))
            case None        => None
          }
      }
    case None => None
  }
}
