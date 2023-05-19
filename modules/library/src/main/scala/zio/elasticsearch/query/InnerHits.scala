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

import zio.Chunk
import zio.elasticsearch.ElasticPrimitive.ElasticPrimitiveOps
import zio.elasticsearch.highlights.Highlights
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Num, Obj, Str}

final case class InnerHits private[elasticsearch] (
  private val excluded: Option[Chunk[String]],
  private val from: Option[Int],
  private val highlights: Option[Highlights],
  private val included: Option[Chunk[String]],
  private val name: Option[String],
  private val size: Option[Int]
) { self =>

  /**
   * Specifies one or more fields to be excluded in the response of a [[zio.elasticsearch.query.InnerHits]].
   *
   * @param field
   *   a field to be excluded
   * @param fields
   *   fields to be excluded
   * @return
   *   an instance of a [[zio.elasticsearch.query.InnerHits]] with specified fields to be excluded.
   */
  def excludes(field: String, fields: String*): InnerHits =
    self.copy(excluded = excluded.map(_ ++ (field +: fields)).orElse(Some(field +: Chunk.fromIterable(fields))))

  def from(value: Int): InnerHits =
    self.copy(from = Some(value))

  def highlights(value: Highlights): InnerHits =
    self.copy(highlights = Some(value))

  /**
   * Specifies one or more fields to be included in the response of a [[zio.elasticsearch.query.InnerHits]].
   *
   * @param field
   *   a field to be included
   * @param fields
   *   fields to be included
   * @return
   *   an instance of a [[zio.elasticsearch.query.InnerHits]] with specified fields to be included.
   */
  def includes(field: String, fields: String*): InnerHits =
    self.copy(included = included.map(_ ++ (field +: fields)).orElse(Some(field +: Chunk.fromIterable(fields))))

  def name(value: String): InnerHits =
    self.copy(name = Some(value))

  def size(value: Int): InnerHits =
    self.copy(size = Some(value))

  private[elasticsearch] def toStringJsonPair: (String, Json) = {
    val sourceJson: Option[Json] =
      (included, excluded) match {
        case (None, None) => None
        case (included, excluded) =>
          val includes = included.fold(Obj())(included => Obj("includes" -> Arr(included.map(_.toJson))))
          val excludes = excluded.fold(Obj())(excluded => Obj("excludes" -> Arr(excluded.map(_.toJson))))
          Some(includes merge excludes)
      }

    "inner_hits" -> Obj(
      Chunk(
        from.map("from" -> Num(_)),
        size.map("size" -> Num(_)),
        name.map("name" -> Str(_)),
        highlights.map("highlight" -> _.toJson),
        sourceJson.map("_source" -> _)
      ).flatten
    )
  }
}

object InnerHits {
  def apply(): InnerHits =
    InnerHits(excluded = None, from = None, highlights = None, included = None, name = None, size = None)
}
