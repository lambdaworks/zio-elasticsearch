package zio.elasticsearch

import zio.json.DecoderOps
import zio.json.ast.Json

package object utils extends UnsafeWrapUtil {

  final implicit class RichString(private val text: String) extends AnyVal {
    def toJson: Json = text.fromJson[Json].toOption.get
  }
}
