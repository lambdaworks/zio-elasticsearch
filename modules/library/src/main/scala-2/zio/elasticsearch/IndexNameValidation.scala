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
import zio.Chunk

object IndexNameValidation {
  def isValid(name: String): Boolean = {
    def containsAny(string: String, params: Chunk[String]): Boolean =
      params.exists(StringUtils.contains(string, _))

    name.toLowerCase == name &&
    name.nonEmpty &&
    !startsWithAny(name, "+", "-", "_") &&
    !containsAny(string = name, params = Chunk("*", "?", "\"", "<", ">", "|", " ", ",", "#", ":")) &&
    !equalsAny(name, ".", "..") &&
    name.getBytes().length <= 255
  }
}
