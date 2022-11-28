package zio.elasticsearch

sealed trait ElasticError

object ElasticError {

  sealed abstract class DocumentRetrievingError

  object DocumentRetrievingError {

    final case object DocumentNotFound extends DocumentRetrievingError

    final case class DecoderError(reason: String) extends DocumentRetrievingError

  }
}
