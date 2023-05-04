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

package zio.elasticsearch.request.options

import zio.schema.Schema

private[elasticsearch] trait HasSourceFiltering[R <: HasSourceFiltering[R]] {
  def excludes(fields: String*): R

  def includes(fields: String*): R

  final def includes(schema: Schema.Record[_]): R = {
    def loop(schema: Schema.Record[_], prefix: Option[String]): List[String] =
      schema.fields.toList.flatMap { field =>
        Schema.force(field.schema) match {
          case schema: Schema.Record[_] => loop(schema, prefix.map(_ + "." + field.name).orElse(Some(field.name)))
          case schema: Schema.Sequence[_, _, _] =>
            Schema.force(schema.elementSchema) match {
              case schema: Schema.Record[_] => loop(schema, prefix.map(_ + "." + field.name).orElse(Some(field.name)))
              case _                        => List(prefix.fold[String](field.name)(_ + "." + field.name))
            }
          case _ => List(prefix.fold[String](field.name)(_ + "." + field.name))
        }
      }

    includes(loop(schema, None): _*)
  }
}
