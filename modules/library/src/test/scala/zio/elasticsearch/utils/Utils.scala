package zio.elasticsearch.utils

import zio.json.DecoderOps
import zio.json.ast.Json

object Utils {

  implicit class RichString(val text: String) extends AnyVal {
    def toJson: Json = text.fromJson[Json].toOption.get
  }
}
