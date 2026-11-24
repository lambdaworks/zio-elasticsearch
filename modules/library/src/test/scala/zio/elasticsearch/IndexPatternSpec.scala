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

import zio.elasticsearch.utils.unsafeWrap
import zio.prelude.Validation
import zio.test.Assertion.equalTo
import zio.test._

object IndexPatternSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment, Any] =
    suite("IndexPattern")(
      suite("constructing")(
        test("fail for empty string") {
          val pattern = ""

          assert(IndexPattern.make(pattern))(equalTo(Validation.fail(indexPatternFailureMessage(pattern))))
        },
        test("fail for string containing character '\\'") {
          check(genString(0, 127), genString(0, 127)) { (part1, part2) =>
            val invalidPattern = s"$part1\\$part2"

            assert(IndexPattern.make(invalidPattern))(
              equalTo(Validation.fail(indexPatternFailureMessage(invalidPattern)))
            )
          }
        },
        test("fail for string containing character '/'") {
          check(genString(0, 127), genString(0, 127)) { (part1, part2) =>
            val invalidPattern = s"$part1/$part2"

            assert(IndexPattern.make(invalidPattern))(
              equalTo(Validation.fail(indexPatternFailureMessage(invalidPattern)))
            )
          }
        },
        test("fail for multi-byte string longer than 255 bytes in UTF-8") {
          val invalidPattern = "ć" * 128

          assert(IndexPattern.make(invalidPattern))(
            equalTo(Validation.fail(indexPatternFailureMessage(invalidPattern)))
          )
        },
        test("succeed for string containing character '*'") {
          check(genString(0, 127), genString(0, 127)) { (part1, part2) =>
            val pattern = s"$part1*$part2"

            assert(IndexPattern.make(pattern))(equalTo(Validation.succeed(unsafeWrap(pattern)(IndexPattern))))
          }
        },
        test("succeed for multi-byte string of at most 255 bytes in UTF-8") {
          val pattern = "ć" * 127

          assert(IndexPattern.make(pattern))(equalTo(Validation.succeed(unsafeWrap(pattern)(IndexPattern))))
        }
      )
    )

  private def indexPatternFailureMessage(pattern: String): String =
    s"$pattern did not satisfy " +
      s"""
         |   - Must be lower case only
         |   - Cannot include \\, /, ?, ", <, >, |, ` `(space character), `,`(comma), #.
         |   - Cannot include ":"(since 7.0).
         |   - Cannot be empty
         |   - Cannot start with +.
         |   - Cannot be `.` or `..`.
         |   - Cannot be longer than 255 bytes (note it is bytes, so multi-byte characters will count towards the 255 limit faster).
         |   - Patterns starting with . are deprecated, except for hidden indices and internal indices managed by plugins.
         |""".stripMargin

  private def genString(min: Int, max: Int): Gen[Any, String] =
    Gen.stringBounded(min, max)(Gen.alphaChar).map(_.toLowerCase)
}
