package zio.elasticsearch

import zio.{Task, ZIO}
import zio.schema.Schema
import zio.prelude.{ForEachOps, ZValidation}

sealed trait ElasticResult[F[_]] {
  def result[A: Schema]: Task[F[A]]
}

final class GetResult(private val doc: Option[Document]) extends ElasticResult[Option] {
  override def result[A: Schema]: Task[Option[A]] =
    ZIO.fromEither(doc.forEach(_.decode)).mapError(e => DecodingException(s"Could not parse the document: ${e.message}"))
}
final class SearchResult(private val hits: List[Document]) extends ElasticResult[List] {
  override def result[A: Schema]: Task[List[A]] =
    ZIO.fromEither {
      ZValidation.validateAll(hits.map(d => ZValidation.fromEither(d.decode))).toEitherWith { errors =>
        DecodingException(s"Could not parse all documents successfully: ${errors.map(_.message).mkString(",")})")
      }
    }
}
