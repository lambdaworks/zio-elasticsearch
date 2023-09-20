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

trait IndexSelector[A] {
  def toSelector(a: A): String
}

object IndexSelector {

  implicit object IndexNameSelector extends IndexSelector[IndexName] {
    def toSelector(name: IndexName): String =
      IndexName.unwrap(name)
  }

  implicit object IndexPatternSelector extends IndexSelector[IndexPattern] {
    def toSelector(pattern: IndexPattern): String =
      IndexPattern.unwrap(pattern)
  }

  implicit object MultiIndexSelector extends IndexSelector[MultiIndex] {
    def toSelector(multi: MultiIndex): String =
      multi.indices.mkString(",")
  }

  implicit class IndexNameSyntax[A](a: A)(implicit IS: IndexSelector[A]) {
    def toSelector: String =
      IS.toSelector(a)
  }

}

final case class MultiIndex private[elasticsearch] (indices: Chunk[String]) { self =>

  def names(name: IndexName, names: IndexName*): MultiIndex =
    self.copy(indices = indices ++ Chunk.fromIterable(name.toString +: names.map(IndexName.unwrap)))

  def patterns(pattern: IndexPattern, patterns: IndexPattern*): MultiIndex =
    self.copy(indices = indices ++ Chunk.fromIterable(pattern.toString +: patterns.map(IndexPattern.unwrap)))
}

object MultiIndex {
  def names(name: IndexName, names: IndexName*): MultiIndex =
    MultiIndex(Chunk.fromIterable(name.toString +: names.map(IndexName.unwrap)))

  def patterns(pattern: IndexPattern, patterns: IndexPattern*): MultiIndex =
    MultiIndex(Chunk.fromIterable(pattern.toString +: patterns.map(IndexPattern.unwrap)))
}
