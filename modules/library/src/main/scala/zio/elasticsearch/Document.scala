package zio.elasticsearch

import zio.schema.Schema
import zio.schema.codec.JsonCodec.JsonDecoder
import zio.schema.codec.{DecodeError, JsonCodec}

private[elasticsearch] final case class Document(json: String) {
  def decode[A](implicit schema: Schema[A]): Either[DecodeError, A] = JsonDecoder.decode(schema, json)
}

private[elasticsearch] object Document {
  def from[A](doc: A)(implicit schema: Schema[A]): Document = Document(
    JsonCodec.jsonEncoder(schema).encodeJson(a = doc, indent = None).toString
  )
}
