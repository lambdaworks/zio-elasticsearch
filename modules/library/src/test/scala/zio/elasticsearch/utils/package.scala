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

import zio.json.DecoderOps
import zio.json.ast.Json
import zio.schema.{DeriveSchema, Schema}

package object utils {

  final implicit class RichString(private val text: String) extends AnyVal {
    def toJson: Json = text.fromJson[Json].toOption.get
  }

  final case class UserDocument(
    id: String,
    name: String,
    address: String,
    balance: Double,
    age: Int,
    items: List[String]
  )

  object UserDocument {

    implicit val schema: Schema.CaseClass6[String, String, String, Double, Int, List[String], UserDocument] =
      DeriveSchema.gen[UserDocument]

    val (id, name, address, balance, age, items) = schema.makeAccessors(FieldAccessorBuilder)
  }
}
