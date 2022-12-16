package zio

import zio.json.DecoderOps
import zio.json.ast.Json

package object elasticsearch {

  implicit class RichString(val text: String) extends AnyVal {
    def toJson: Json = text.fromJson[Json].toOption.get
  }
}
