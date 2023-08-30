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

import zio.Chunk
import zio.prelude.AssertionError.failure
import zio.prelude.Newtype

trait IndexSelector[A] {
  def toSelector(a: A): String
}

object IndexSelector {

  implicit object IndexNameSelector extends IndexSelector[IndexName] {
    def toSelector(name: IndexName): String = IndexName.unwrap(name)
  }

  implicit object IndexPatternSelector extends IndexSelector[IndexPattern] {
    def toSelector(pattern: IndexPattern): String = IndexPattern.unwrap(pattern)
  }

  implicit object MultiIndexSelector extends IndexSelector[MultiIndex] {
    def toSelector(multi: MultiIndex): String = multi.indices.mkString(",")
  }

  implicit class IndexNameSyntax[A](a: A)(implicit IS: IndexSelector[A]) {
    def toSelector: String = IS.toSelector(a)
  }

}

trait IndexNameNewtype {
  object IndexName extends Newtype[String] {
    override def assertion = assertCustom { (name: String) => // scalafix:ok
      if (IndexNameValidation.isValid(name)) {
        Right(())
      } else {
        Left(
          failure(
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
    }
  }
  type IndexName = IndexName.Type
}

trait IndexPatternNewType {
  object IndexPattern extends Newtype[String] {
    override def assertion = assertCustom { (pattern: String) => // scalafix:ok
      if (IndexPatternValidation.isValid(pattern)) {
        Right(())
      } else {
        Left(
          failure(
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
          )
        )
      }
    }
    def All: IndexPattern = this("_all")
  }
  type IndexPattern = IndexPattern.Type

}

final case class MultiIndex private (indices: Chunk[String]) { self =>
  def names(name: IndexName, names: IndexName*): MultiIndex =
    self.copy(indices = indices ++ Chunk.fromIterable(name.toString +: names.map(IndexName.unwrap)))

  def patterns(pattern: IndexPattern, patterns: IndexPattern*): MultiIndex =
    self.copy(indices = indices ++ Chunk.fromIterable(pattern.toString +: patterns.map(IndexPattern.unwrap)))
}

object MultiIndex {
  def names(name: IndexName, names: IndexName*): MultiIndex =
    new MultiIndex(Chunk.fromIterable(name.toString +: names.map(IndexName.unwrap)))

  def patterns(pattern: IndexPattern, patterns: IndexPattern*): MultiIndex =
    new MultiIndex(Chunk.fromIterable(pattern.toString +: patterns.map(IndexPattern.unwrap)))
}
