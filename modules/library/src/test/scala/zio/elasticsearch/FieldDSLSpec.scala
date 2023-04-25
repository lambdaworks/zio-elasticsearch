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

import zio.elasticsearch.domain.{TestNestedField, TestSubDocument}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object FieldDSLSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment, Any] =
    suite("Field DSL")(
      test("properly encode single field path")(
        assertTrue(Field(None, "name").toString == "name")
      ),
      test("properly encode single field path using accessor")(
        assertTrue(TestSubDocument.stringField.toString == "stringField")
      ),
      test("properly encode nested field path")(
        assertTrue(Field(Some(Field(None, "address")), "number").toString == "address.number")
      ),
      test("properly encode nested field path using accessors")(
        assertTrue((TestSubDocument.nestedField / TestNestedField.longField).toString == "nestedField.longField")
      ),
      test("properly encode single field with suffix")(
        assertTrue(Field[Any, String](None, "name").suffix("keyword").toString == "name.keyword")
      ),
      test("properly encode single field with suffixes")(
        assertTrue(
          Field[Any, String](None, "name")
            .suffix("multi_field")
            .suffix("keyword")
            .toString == "name.multi_field.keyword"
        )
      ),
      test("properly encode single field with suffix using accessor")(
        assertTrue(TestSubDocument.stringField.suffix("keyword").toString == "stringField.keyword")
      ),
      test("properly encode single field with keyword suffix")(
        assertTrue(Field[Any, String](None, "name").keyword.toString == "name.keyword")
      ),
      test("properly encode single field with raw suffix")(
        assertTrue(Field[Any, String](None, "name").raw.toString == "name.raw")
      )
    )
}
