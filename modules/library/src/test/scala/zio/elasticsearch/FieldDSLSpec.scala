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
      suite("constructing")(
        test("field") {
          val encodeName = "name"

          val encodeNestedNumber      = "number"
          val encodeNestedAddress     = "address"
          val encodeNestedExpectedVal = "address.number"

          val encodeSuffixName        = "name"
          val encodeSuffixKeyword     = "keyword"
          val encodeSuffixExpectedVal = "name.keyword"

          val encodeSingleFieldSuffixName        = "name"
          val encodeSingleFieldSuffixMultiField  = "multi_field"
          val encodeSingleFieldSuffixKeyword     = "keyword"
          val encodeSingleFieldSuffixExpectedVal = "name.multi_field.keyword"

          val encodeSingleFieldKeywordSuffixName        = "name"
          val encodeSingleFieldKeywordSuffixExpectedVal = "name.keyword"

          val encodeSingleFieldRawSuffixKeyword     = "name"
          val encodeSingleFieldRawSuffixExpectedVal = "name.raw"

          assertTrue(Field(None, encodeName).toString == encodeName) && assertTrue(
            Field(Some(Field(None, encodeNestedAddress)), encodeNestedNumber).toString == encodeNestedExpectedVal
          ) && assertTrue(
            Field[Any, String](None, encodeSuffixName).suffix(encodeSuffixKeyword).toString == encodeSuffixExpectedVal
          ) && assertTrue(
            Field[Any, String](None, encodeSingleFieldSuffixName)
              .suffix(encodeSingleFieldSuffixMultiField)
              .suffix(encodeSingleFieldSuffixKeyword)
              .toString == encodeSingleFieldSuffixExpectedVal
          ) && assertTrue(
            Field[Any, String](
              None,
              encodeSingleFieldKeywordSuffixName
            ).keyword.toString == encodeSingleFieldKeywordSuffixExpectedVal
          ) && assertTrue(
            Field[Any, String](
              None,
              encodeSingleFieldRawSuffixKeyword
            ).raw.toString == encodeSingleFieldRawSuffixExpectedVal
          )
        },
        test("path") {
          val singleFieldExpectedVal = "stringField"

          val nestedFieldAccessorsExpectedVal = "nestedField.longField"

          val singleFieldSuffixAccessorKeyword     = "keyword"
          val singleFieldSuffixAccessorExpectedVal = "stringField.keyword"

          assertTrue(TestSubDocument.stringField.toString == singleFieldExpectedVal) && assertTrue(
            (TestSubDocument.nestedField / TestNestedField.longField).toString == nestedFieldAccessorsExpectedVal
          ) && assertTrue(
            TestSubDocument.stringField
              .suffix(singleFieldSuffixAccessorKeyword)
              .toString == singleFieldSuffixAccessorExpectedVal
          )
        }
      )
    )
}
