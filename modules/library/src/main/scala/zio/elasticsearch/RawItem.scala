package zio.elasticsearch

import zio.json.ast.Json
import zio.schema.Schema
import zio.schema.codec.DecodeError
import zio.schema.codec.JsonCodec.JsonDecoder

final case class RawItem(raw: Json) {
  def documentAs[A](implicit schema: Schema[A]): Either[DecodeError, A] = JsonDecoder.decode(schema, raw.toString)
}
