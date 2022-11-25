package zio.elasticsearch

object ElasticError {

  sealed abstract class DocumentGettingError

  object DocumentGettingError {

    final case object DocumentNotFound extends DocumentGettingError

    final case class JsonDecoderError(message: String) extends DocumentGettingError

  }
}
