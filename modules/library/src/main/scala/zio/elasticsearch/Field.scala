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

import zio.elasticsearch.ElasticPrimitive.ElasticPrimitive
import zio.schema.{AccessorBuilder, Schema}

import scala.annotation.tailrec

private[elasticsearch] final case class Field[-S, +A](parent: Option[Field[S, _]], name: String) { self =>

  def /[B](that: Field[A, B]): Field[S, B] =
    Field(that.parent.map(self / _).orElse(Some(self)), that.name)

  def keyword[A1 >: A: ElasticPrimitive]: Field[S, A1] = suffix[A1]("keyword")

  def raw[A1 >: A: ElasticPrimitive]: Field[S, A1] = suffix[A1]("raw")

  override def toString: String = {
    @tailrec
    def loop(field: Field[_, _], acc: List[String]): List[String] = field match {
      case Field(None, name)         => s"$name" +: acc
      case Field(Some(parent), name) => loop(parent, s".$name" +: acc)
    }

    loop(self, Nil).mkString
  }

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
