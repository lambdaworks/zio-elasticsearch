package zio.elasticsearch

import zio.Chunk
import zio.prelude.AssertionError.failure
import zio.prelude.Newtype

sealed trait IndexSelector

trait IndexNameNewtype  {
  object IndexName extends Newtype[String] with IndexSelector {
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
  object IndexPattern extends Newtype[String] with IndexSelector{
    override def assertion = assertCustom { (pattern: String) => // scalafix:ok
      if (IndexNameValidation.isValid(pattern)) {
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

  type IndexPattern = IndexPattern.Type
}

sealed trait Target[Q <: Target[Q]] {
  def names(indexnames: IndexName): Q
  def patterns(indexPattern: IndexPattern): Q
}

final case class MultiIndex(indices: Chunk[IndexSelector]) extends Target[MultiIndex] { self =>
  def names(indexNames: IndexName*): MultiIndex =
    self.copy(indices = indices +: Chunk.fromIterable(indexNames))

  def patterns(indexPatterns: IndexPattern*): MultiIndex =
    self.copy(indices = indices +: Chunk.fromIterable(indexPatterns))
}

object MultiIndex {
  def names(indexNames: IndexName*): MultiIndex =
    new MultiIndex(Chunk.fromIterable(indexNames))

  def patterns(indexPatterns: IndexPattern*): MultiIndex =
    new MultiIndex(Chunk.fromIterable(indexPatterns))
}
