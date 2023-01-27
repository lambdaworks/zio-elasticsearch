package zio.elasticsearch

import zio.Chunk
import zio.schema.{AccessorBuilder, Schema}

import scala.annotation.tailrec

object Annotation {
  final case class name(value: String) extends scala.annotation.Annotation

  def maybeName(annotations: Chunk[Any]): Option[String] =
    annotations.collect { case name(value) => value }.headOption
}

private[elasticsearch] final case class Field[From, To](parent: Option[Field[From, _]], name: String) { self =>

  def /[To2](that: Field[To, To2]): Field[From, To2] =
    Field(that.parent.map(self / _).orElse(Some(self)), that.name)

  override def toString: String = {
    @tailrec
    def loop(field: Field[_, _], acc: List[String]): List[String] = field match {
      case Field(None, name)         => s"$name" +: acc
      case Field(Some(parent), name) => loop(parent, s".$name" +: acc)
    }

    loop(self, List.empty).mkString("")
  }
}

object ElasticQueryAccessorBuilder extends AccessorBuilder {
  override type Lens[_, From, To]   = Field[From, To]
  override type Prism[_, From, To]  = Unit
  override type Traversal[From, To] = Unit

  override def makeLens[F, S, A](product: Schema.Record[S], term: Schema.Field[S, A]): Lens[F, S, A] = {
    val label = Annotation.maybeName(term.annotations).getOrElse(term.name)
    Field[S, A](None, label)
  }

  override def makePrism[F, S, A](sum: Schema.Enum[S], term: Schema.Case[S, A]): Prism[F, S, A] = ()

  override def makeTraversal[S, A](collection: Schema.Collection[S, A], element: Schema[A]): Traversal[S, A] = ()
}
