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

import zio.json.ast.Json
import zio.json.ast.Json.{Num, Obj, Str}

private[elasticsearch] final case class InnerHits(
  from: Option[Int] = None,
  name: Option[String] = None,
  size: Option[Int] = None
) { self =>
  def from(value: Int): InnerHits =
    self.copy(from = Some(value))

  def name(value: String): InnerHits =
    self.copy(name = Some(value))

  def size(value: Int): InnerHits =
    self.copy(size = Some(value))

  def toStringJsonPair: (String, Json) =
    "inner_hits" -> Obj(
      List(from.map("from" -> Num(_)), size.map("size" -> Num(_)), name.map("name" -> Str(_))).flatten: _*
    )
}

object InnerHits {
  def from(value: Int): InnerHits =
    InnerHits(from = Some(value))

  def name(value: String): InnerHits =
    InnerHits(name = Some(value))

  def size(value: Int): InnerHits =
    InnerHits(size = Some(value))
}
