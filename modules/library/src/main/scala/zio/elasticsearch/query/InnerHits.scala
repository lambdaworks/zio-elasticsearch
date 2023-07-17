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
import zio.elasticsearch.Field
import zio.elasticsearch.highlights.Highlights
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Num, Obj, Str}

final case class InnerHits private[elasticsearch] (
  private val excluded: Chunk[String],
  private val included: Chunk[String],
  private val from: Option[Int],
  private val highlights: Option[Highlights],
  private val name: Option[String],
  private val size: Option[Int]
) { self =>

  /**
   * Specifies one or more type-safe fields to be excluded in the response of a [[zio.elasticsearch.query.InnerHits]].
   *
   * @param field
   *   a type-safe field to be excluded
   * @param fields
   *   type-safe fields to be excluded
   * @tparam S
   *   document which fields are excluded
   * @return
   *   an instance of a [[zio.elasticsearch.query.InnerHits]] with specified fields to be excluded.
   */
  def excludes[S](field: Field[S, _], fields: Field[S, _]*): InnerHits =
    self.copy(excluded = excluded ++ (field.toString +: fields.map(_.toString)))

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
    self.copy(excluded = excluded ++ (field +: fields))

  /**
   * Specifies the starting offset of the [[zio.elasticsearch.query.InnerHits]] to be returned.
   *
   * @param value
   *   the starting offset value
   * @return
   *   an instance of a [[zio.elasticsearch.query.InnerHits]] with the specified starting offset.
   */
  def from(value: Int): InnerHits =
    self.copy(from = Some(value))

  /**
   * Specifies the highlighting configuration for the [[zio.elasticsearch.query.InnerHits]].
   *
   * @param value
   *   the [[zio.elasticsearch.highlights.Highlights]] configuration
   * @return
   *   an instance of a [[zio.elasticsearch.query.InnerHits]] with the specified highlighting configuration.
   */
  def highlights(value: Highlights): InnerHits =
    self.copy(highlights = Some(value))

  /**
   * Specifies one or more type-safe fields to be included in the response of a [[zio.elasticsearch.query.InnerHits]].
   *
   * @param field
   *   a type-safe field to be included
   * @param fields
   *   type-safe fields to be included
   * @tparam S
   *   document which fields are included
   * @return
   *   an instance of a [[zio.elasticsearch.query.InnerHits]] with specified fields to be included.
   */
  def includes[S](field: Field[S, _], fields: Field[S, _]*): InnerHits =
    self.copy(included = included ++ (field.toString +: fields.map(_.toString)))

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
    self.copy(included = included ++ (field +: fields))

  /**
   * Specifies the name of the [[zio.elasticsearch.query.InnerHits]].
   *
   * @param value
   *   the name of the [[zio.elasticsearch.query.InnerHits]]
   * @return
   *   an instance of a [[zio.elasticsearch.query.InnerHits]] with the specified name.
   */
  def name(value: String): InnerHits =
    self.copy(name = Some(value))

  /**
   * Specifies the maximum number of [[zio.elasticsearch.query.InnerHits]] to be returned.
   *
   * @param value
   *   the maximum number of [[zio.elasticsearch.query.InnerHits]]
   * @return
   *   an instance of a [[zio.elasticsearch.query.InnerHits]] with the specified size.
   */
  def size(value: Int): InnerHits =
    self.copy(size = Some(value))

  private[elasticsearch] def toStringJsonPair(fieldPath: Option[String]): (String, Json) = {
    val sourceJson: Option[Json] =
      (included, excluded) match {
        case (Chunk(), Chunk()) =>
          None
        case (included, excluded) =>
          val includes = if (included.isEmpty) Obj() else Obj("includes" -> Arr(included.map(_.toJson)))
          val excludes = if (excluded.isEmpty) Obj() else Obj("excludes" -> Arr(excluded.map(_.toJson)))
          Some(includes merge excludes)
      }

    "inner_hits" -> Obj(
      Chunk(
        from.map("from" -> Num(_)),
        size.map("size" -> Num(_)),
        name.map("name" -> Str(_)),
        highlights.map("highlight" -> _.toJson(fieldPath)),
        sourceJson.map("_source" -> _)
      ).flatten
    )
  }
}

object InnerHits {
  def apply(): InnerHits =
    InnerHits(excluded = Chunk(), included = Chunk(), from = None, highlights = None, name = None, size = None)
}
