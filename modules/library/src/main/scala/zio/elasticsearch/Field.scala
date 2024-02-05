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
import zio.elasticsearch.ElasticPrimitive.ElasticPrimitive
import zio.schema.{AccessorBuilder, Schema}

import scala.annotation.tailrec

private[elasticsearch] final case class Field[-S, +A](parent: Option[Field[S, _]], name: String) { self =>

  /**
   * Creates a new [[Field]] that represents the path from this [[Field]] to the specified [[Field]].
   *
   * @param that
   *   the target [[Field]]
   * @tparam B
   *   the type of the specified [[Field]]
   * @return
   *   a new [[Field]] that represents the path from this [[Field]] to the specified [[Field]]
   */
  def /[B](that: Field[A, B]): Field[S, B] =
    Field(that.parent.map(self / _).orElse(Some(self)), that.name)

  /**
   * Creates a new [[Field]] that represents the current [[Field]] suffixed by `keyword`.
   *
   * @tparam A1
   *   the underlying type of the current [[Field]], constrained by the [[ElasticPrimitive]], which specifies that it
   *   must be a supertype of the field's value
   * @return
   *   a new [[Field]] that represents the current [[Field]] suffixed by `keyword`
   */
  def keyword[A1 >: A: ElasticPrimitive]: Field[S, A1] = suffix[A1]("keyword")

  /**
   * Creates a new [[Field]] that represents the current [[Field]] suffixed by `raw`.
   *
   * @tparam A1
   *   the underlying type of the current [[Field]], constrained by the [[ElasticPrimitive]], which specifies that it
   *   must be a supertype of the field's value
   * @return
   *   a new [[Field]] that represents the current [[Field]] suffixed by `raw`
   */
  def raw[A1 >: A: ElasticPrimitive]: Field[S, A1] = suffix[A1]("raw")

  override def toString: String = {
    @tailrec
    def loop(field: Field[_, _], acc: Chunk[String]): Chunk[String] = field match {
      case Field(None, name)         => s"$name" +: acc
      case Field(Some(parent), name) => loop(parent, s".$name" +: acc)
    }

    loop(self, Chunk.empty).mkString
  }

  /**
   * Appends a suffix to the name of the [[Field]]. The type of the field's value is preserved.
   *
   * @param suffix
   *   a user-defined suffix to append (e.g. 'keyword', 'raw')
   * @tparam A1
   *   the underlying type of the current [[Field]], constrained by the [[ElasticPrimitive]], which specifies that it
   *   must be a supertype of the field's value
   * @return
   *   a new instance of the [[Field]] with the updated name.
   */
  def suffix[A1 >: A: ElasticPrimitive](suffix: String): Field[S, A1] =
    self.copy(name = name + s".$suffix")
}

object FieldAccessorBuilder extends AccessorBuilder {
  type Lens[_, S, A]   = Field[S, A]
  type Prism[_, S, A]  = Unit
  type Traversal[S, A] = Unit

  def makeLens[F, S, A](product: Schema.Record[S], term: Schema.Field[S, A]): Lens[_, S, A] =
    Field[S, A](None, term.name)

  def makePrism[F, S, A](sum: Schema.Enum[S], term: Schema.Case[S, A]): Prism[_, S, A] = ()

  def makeTraversal[S, A](collection: Schema.Collection[S, A], element: Schema[A]): Traversal[S, A] = ()
}
