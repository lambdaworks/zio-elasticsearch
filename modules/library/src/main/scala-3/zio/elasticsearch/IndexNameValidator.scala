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

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.{equalsAny, startsWithAny}
import zio.prelude.{AssertionError, Validator}

object IndexNameValidator
    extends Validator[String](string => {
      def containsAny(string: String, params: List[String]): Boolean =
        params.exists(StringUtils.contains(string, _))

      def isValid(string: String): Boolean =
        string.toLowerCase == string &&
          !startsWithAny(string, "+", "-", "_") &&
          !containsAny(string = string, params = List("*", "?", "\"", "<", ">", "|", " ", ",", "#", ":")) &&
          !equalsAny(string, ".", "..") &&
          string.getBytes().length <= 255

      if (isValid(string)) {
        Right(())
      } else {
        Left(
          AssertionError.Failure(
            s"""
               |   - Must be lower case only
               |   - Cannot include \\, /, *, ?, ", <, >, |, ` `(space character), `,`(comma), #.
               |   - Cannot include ":"(since 7.0).
               |   - Cannot be empty
               |   - Cannot start with -, _, +.
               |   - Cannot be `.` or `..`.
               |   - Cannot be longer than 255 bytes (note it is bytes, so multi-byte characters will count towards the 255 limit faster).
               |   - Names starting with . are deprecated, except for hidden indices and internal indices managed by plugins.
               |""".stripMargin
          )
        )
      }
    })
