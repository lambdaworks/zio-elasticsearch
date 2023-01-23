package zio.elasticsearch

import zio.Chunk
import zio.schema.{AccessorBuilder, Schema}

import scala.annotation.tailrec

object Annotation {
  final case class name(value: String) extends scala.annotation.Annotation

  def maybeName(annotations: Chunk[Any]): Option[String] =
    annotations.collect { case name(value) => value }.headOption
}

sealed trait Selection[-From, +To] { self =>

  def /[To2](that: Selection[To, To2]): Selection[From, To2] = that match {
    case Field(parent, key) => Field(parent.map(self / _).orElse(Some(self)), key)
  }

  override def toString: String = {
    @tailrec
    def loop(selection: Selection[_, _], acc: List[String]): List[String] =
      selection match {
        case Field(None, name)         => acc :+ s"$name"
        case Field(Some(parent), name) => loop(parent, acc :+ s".$name")
      }

    loop(self, List.empty).reverse.mkString("")
  }
}

final case class Field[From, To](parent: Option[Selection[From, _]], name: String) extends Selection[From, To]

object ElasticQueryAccessorBuilder extends AccessorBuilder {
  override type Lens[_, From, To]   = Selection[From, To]
  override type Prism[_, From, To]  = Unit
  override type Traversal[From, To] = Unit

  override def makeLens[F, S, A](product: Schema.Record[S], term: Schema.Field[S, A]): Lens[F, S, A] = {
    val label = Annotation.maybeName(term.annotations).getOrElse(term.name)
    Field[S, A](None, label)
  }

  override def makePrism[F, S, A](sum: Schema.Enum[S], term: Schema.Case[S, A]): Prism[F, S, A] = ()

  override def makeTraversal[S, A](collection: Schema.Collection[S, A], element: Schema[A]): Traversal[S, A] = ()
}
