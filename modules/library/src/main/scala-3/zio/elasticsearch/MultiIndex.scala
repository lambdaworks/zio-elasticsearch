package zio.elasticsearch

import zio.Chunk
import zio.prelude.AssertionError.failure
import zio.prelude.Newtype

trait IndexSelector[A] {
  def selectorString(a: A): String
}

object IndexSelector {

  implicit object IndexNameSelector extends IndexSelector[IndexName] {
    def selectorString(a: IndexName): String = IndexName.unwrap(a)
  }

  implicit object IndexPatternSelector extends IndexSelector[IndexPattern] {
    def selectorString(a: IndexPattern): String = IndexPattern.unwrap(a)
  }

  implicit object MultiIndexSelector extends IndexSelector[MultiIndex] {
    def selectorString(a: MultiIndex): String = a.indices.mkString(",")
  }

  implicit class IndexNameSyntax[A](a: A)(implicit IS: IndexSelector[A]) {
    def selectorString: String = IS.selectorString(a)
  }

}

trait IndexPatternNewType {
  object IndexPattern extends NewtypeCustom[String] {
    protected def validate(pattern: String) =
      IndexPatternValidator.validate(pattern)

    protected inline def validateInline(inline pattern: String) =
    ${
      IndexNameValidator.validateInlineImpl('pattern)}
  }

  type IndexPattern = IndexPattern.Type
}

final case class MultiIndex private (indices: Chunk[String]) { self =>
  def names(indexNames: IndexName*): MultiIndex =
    self.copy(indices = indices ++ Chunk.fromIterable(indexNames.map(IndexName.unwrap)))

  def patterns(indexPatterns: IndexPattern*): MultiIndex =
    self.copy(indices = indices ++ Chunk.fromIterable(indexPatterns.map(IndexPattern.unwrap)))
}

object MultiIndex {
  def names(indexNames: IndexName*): MultiIndex =
    new MultiIndex(Chunk.fromIterable(indexNames.map(IndexName.unwrap)))

  def patterns(indexPatterns: IndexPattern*): MultiIndex =
    new MultiIndex(Chunk.fromIterable(indexPatterns.map(IndexPattern.unwrap)))
}
