package zio.elasticsearch

object ElasticError {

  sealed abstract class DocumentGettingError

  object DocumentGettingError {

    case object DocumentNotFound extends DocumentGettingError

    case class JsonDecoderError(errorMsg: String) extends DocumentGettingError

  }
}
