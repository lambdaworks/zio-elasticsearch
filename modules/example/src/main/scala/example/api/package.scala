package example

import zio.Chunk
import zio.json._

package object api {

  final case class ErrorResponseData(body: Chunk[String])

  object ErrorResponseData {
    implicit val encoder: JsonEncoder[ErrorResponseData] = DeriveJsonEncoder.gen[ErrorResponseData]
  }

  final case class ErrorResponse(errors: ErrorResponseData) extends AnyVal

  object ErrorResponse {
    implicit val encoder: JsonEncoder[ErrorResponse] = DeriveJsonEncoder.gen[ErrorResponse]

    def fromReasons(reasons: String*): ErrorResponse =
      new ErrorResponse(ErrorResponseData(Chunk.fromIterable(reasons)))
  }

}
