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

import zio.schema.{DeriveSchema, Schema}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object FieldDSLSpec extends ZIOSpecDefault {

  final case class Address(street: String, number: Int)

  object Address {

    implicit val schema: Schema.CaseClass2[String, Int, Address] = DeriveSchema.gen[Address]

    val (street, number) = schema.makeAccessors(FieldAccessorBuilder)
  }

  final case class Student(name: String, address: Address)

  object Student {

    implicit val schema: Schema.CaseClass2[String, Address, Student] = DeriveSchema.gen[Student]

    val (name, address) = schema.makeAccessors(FieldAccessorBuilder)
  }

  def spec: Spec[TestEnvironment, Any] =
    suite("Field DSL")(
      test("properly encode single field path")(
        assertTrue(Field(None, "name").toString == "name")
      ),
      test("properly encode single field path using accessor")(
        assertTrue(Student.name.toString == "name")
      ),
      test("properly encode nested field path")(
        assertTrue(Field[Nothing, Nothing](Some(Field(None, "address")), "number").toString == "address.number")
      ),
      test("properly encode nested field path using accessors")(
        assertTrue((Student.address / Address.number).toString == "address.number")
      )
    )
}
