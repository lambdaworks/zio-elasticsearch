package zio

import zio.schema.Schema
import zio.schema.codec.DecodeError
import zio.schema.codec.JsonCodec.JsonDecoder

package object elasticsearch {
  private[elasticsearch] final case class Document(json: String) {
    def decode[A](implicit schema: Schema[A]): Either[DecodeError, A] = JsonDecoder.decode(schema, json)
  }
}
