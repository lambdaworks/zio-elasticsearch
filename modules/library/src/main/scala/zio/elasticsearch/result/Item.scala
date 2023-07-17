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
import zio.elasticsearch.Field
import zio.elasticsearch.executor.response.Hit
import zio.json.DecoderOps
import zio.json.ast.{Json, JsonCursor}
import zio.prelude.Validation
import zio.schema.Schema
import zio.schema.codec.DecodeError
import zio.schema.codec.JsonCodec.JsonDecoder

final case class Item(
  raw: Json,
  highlight: Option[Json] = None,
  private val innerHits: Map[String, Chunk[Hit]] = Map.empty,
  sort: Option[Json] = None
) {
  def documentAs[A](implicit schema: Schema[A]): Either[DecodeError, A] = JsonDecoder.decode(schema, raw.toString)

  lazy val highlights: Option[Map[String, Chunk[String]]] = highlight.flatMap { json =>
    json.toString.fromJson[Map[String, Chunk[String]]].toOption
  }

  def highlight(field: String): Option[Chunk[String]] =
    highlight.flatMap(_.get(JsonCursor.field(field)).toOption).flatMap(_.toString.fromJson[Chunk[String]].toOption)

  def highlight(field: Field[_, _]): Option[Chunk[String]] =
    highlight(field.toString)

  def innerHit(name: String): Option[Chunk[Item]] =
    innerHits.get(name).map(_.map(hit => Item(hit.source, hit.highlight)))

  def innerHitAs[A](name: String)(implicit schema: Schema[A]): Either[DecodingException, Chunk[A]] =
    for {
      innerHitItems <- innerHit(name).toRight(DecodingException(s"Could not find inner hits with name $name"))
      innerHits <-
        Validation
          .validateAll(
            innerHitItems.map(item =>
              Validation.fromEither(JsonDecoder.decode(schema, item.raw.toString)).mapError(_.message)
            )
          )
          .toEitherWith(errors =>
            DecodingException(s"Could not parse all documents successfully: ${errors.mkString(", ")}")
          )
    } yield innerHits
}
