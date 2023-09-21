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

package zio.elasticsearch.script

import zio.Chunk
import zio.elasticsearch.ElasticPrimitive.ElasticPrimitiveOps
import zio.elasticsearch.script.options._
import zio.json.ast.Json
import zio.json.ast.Json.{Obj, Str}

final case class Script private[elasticsearch] (
  private val source: String,
  private val params: Map[String, Any],
  private val lang: Option[ScriptLang]
) extends HasLang[Script]
    with HasParams[Script] { self =>

  def lang(value: ScriptLang): Script =
    self.copy(lang = Some(value))

  def params(values: (String, Any)*): Script =
    self.copy(params = params ++ values.toMap)

  private[elasticsearch] def toJson: Json =
    Obj(
      Chunk(
        self.lang.map(lang => "lang" -> Str(lang.toString.toLowerCase)),
        Some("source" -> source.toJson),
        if (params.nonEmpty) {
          Some("params" -> Obj(Chunk.fromIterable(params).map { case (key, value) =>
            value match {
              case value: BigDecimal => key -> value.toJson
              case value: Double     => key -> value.toJson
              case value: Int        => key -> value.toJson
              case value: Long       => key -> value.toJson
              case _                 => key -> value.toString.toJson
            }
          }))
        } else {
          None
        }
      ).flatten
    )
}

object Script {
  def apply(source: String): Script =
    Script(source = source, params = Map.empty, lang = None)
}

sealed trait ScriptLang

case object Painless   extends ScriptLang
case object Expression extends ScriptLang
case object Mustache   extends ScriptLang
case object Java       extends ScriptLang
