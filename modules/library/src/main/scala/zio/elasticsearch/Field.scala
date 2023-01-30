package zio.elasticsearch

import zio.schema.{AccessorBuilder, Schema}

import scala.annotation.tailrec

private[elasticsearch] final case class Field[S, A](parent: Option[Field[S, _]], name: String) { self =>

  def /[B](that: Field[A, B]): Field[S, B] =
    Field(that.parent.map(self / _).orElse(Some(self)), that.name)

  override def toString: String = {
    @tailrec
    def loop(field: Field[_, _], acc: List[String]): List[String] = field match {
      case Field(None, name)         => s"$name" +: acc
      case Field(Some(parent), name) => loop(parent, s".$name" +: acc)
    }

    loop(self, Nil).mkString
  }
}

object ElasticQueryAccessorBuilder extends AccessorBuilder {
  override type Lens[_, S, A]   = Field[S, A]
  override type Prism[_, S, A]  = Unit
  override type Traversal[S, A] = Unit

  override def makeLens[F, S, A](product: Schema.Record[S], term: Schema.Field[S, A]): Lens[_, S, A] =
    Field[S, A](None, term.name)

  override def makePrism[F, S, A](sum: Schema.Enum[S], term: Schema.Case[S, A]): Prism[_, S, A] = ()

  override def makeTraversal[S, A](collection: Schema.Collection[S, A], element: Schema[A]): Traversal[S, A] = ()
}
