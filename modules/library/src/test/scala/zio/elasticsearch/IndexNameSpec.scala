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

import zio.elasticsearch.utils._
import zio.prelude.Validation
import zio.test.Assertion.equalTo
import zio.test._

object IndexNameSpec extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment, Any] =
    suite("IndexName validation")(
      test("succeed for valid string") {
        check(genString(1, 255)) { name =>
          assert(IndexName.make(name))(equalTo(Validation.succeed(unsafeWrap(name)(IndexName))))
        }
      },
      test("fail for string containing upper letter") {
        check(genString(0, 127), genString(0, 128)) { (part1, part2) =>
          val invalidName = s"${part1}A$part2"
          assert(IndexName.make(invalidName))(equalTo(Validation.fail(indexNameFailureMessage(invalidName))))
        }
      },
      test("fail for string containing character '*'") {
        check(genString(0, 127), genString(0, 128)) { (part1, part2) =>
          val invalidName = s"$part1*$part2"
          assert(IndexName.make(invalidName))(equalTo(Validation.fail(indexNameFailureMessage(invalidName))))
        }
      },
      test("fail for string containing character ':'") {
        check(genString(0, 127), genString(0, 128)) { (part1, part2) =>
          val invalidName = s"$part1:$part2"
          assert(IndexName.make(invalidName))(equalTo(Validation.fail(indexNameFailureMessage(invalidName))))
        }
      },
      test("fail for empty string") {
        val name = ""
        assert(IndexName.make(name))(equalTo(Validation.succeed(unsafeWrap(name)(IndexName))))
      },
      test("fail for string starting with character '-'") {
        check(genString(1, 255)) { name =>
          val invalidName = s"-$name"
          assert(IndexName.make(invalidName))(equalTo(Validation.fail(indexNameFailureMessage(invalidName))))
        }
      },
      test("fail for string '.'") {
        val name = "."
        assert(IndexName.make(name))(equalTo(Validation.fail(indexNameFailureMessage(name))))
      },
      test("fail for string longer than 255 bytes") {
        check(genString(256, 300)) { name =>
          assert(IndexName.make(name))(
            equalTo(Validation.fail(indexNameFailureMessage(name)))
          )
        }
      }
    )

  private def indexNameFailureMessage(name: String): String =
    s"""$name did not satisfy 
       |   - Must be lower case only
       |   - Cannot include \\, /, *, ?, ", <, >, |, ` `(space character), `,`(comma), #.
       |   - Cannot include ":"(since 7.0).
       |   - Cannot be empty
       |   - Cannot start with -, _, +.
       |   - Cannot be `.` or `..`.
       |   - Cannot be longer than 255 bytes (note it is bytes, so multi-byte characters will count towards the 255 limit faster).
       |   - Names starting with . are deprecated, except for hidden indices and internal indices managed by plugins.
       |""".stripMargin

  private def genString(min: Int, max: Int): Gen[Any, String] =
    Gen.stringBounded(min, max)(Gen.alphaChar).map(_.toLowerCase)
}
