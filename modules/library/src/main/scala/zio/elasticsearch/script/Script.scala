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

import zio.elasticsearch.ElasticPrimitive.ElasticPrimitiveOps
import zio.json.ast.Json
import zio.json.ast.Json.Obj

private[elasticsearch] final case class Script(
  source: String,
  params: Map[String, Any],
  lang: Option[String]
) extends WithLang[Script]
    with WithParams[Script] { self =>
  def lang(value: String): Script =
    self.copy(lang = Some(value))

  def withParams(values: (String, Any)*): Script =
    self.copy(params = params ++ values.toList)

  def toJson: Json =
    Obj(
      List(
        self.lang.map(lang => "lang" -> lang.toJson),
        Some("source" -> source.toJson),
        if (params.nonEmpty) {
          Some("params" -> Obj(params.map { case (key, value) =>
            value match {
              case value: BigDecimal => key -> value.toJson
              case value: Double     => key -> value.toJson
              case value: Int        => key -> value.toJson
              case value: Long       => key -> value.toJson
              case _                 => key -> value.toString.toJson
            }
          }.toList: _*))
        } else {
          None
        }
      ).flatten: _*
    )
}

object Script {
  def apply(source: String): Script =
    Script(source = source, params = Map.empty, lang = None)
}
