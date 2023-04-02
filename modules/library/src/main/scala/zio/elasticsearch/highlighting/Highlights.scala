package zio.elasticsearch.highlighting

import zio.Chunk
import zio.elasticsearch.highlighting.Highlights.HighlightConfig
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Obj}

final case class Highlights(
  fields: Chunk[HighlightField],
  globalConfig: HighlightConfig = Map.empty,
  explicitFieldOrder: Boolean = false
) { self =>
  def toJson: Json = Obj("highlight" -> Obj(configList: _*).merge(fieldsList))

  def withGlobalConfig(field: String, value: Json): Highlights =
    self.copy(globalConfig = self.globalConfig.updated(field, value))

  def withHighlight(field: String, config: HighlightConfig = Map.empty): Highlights =
    self.copy(fields = HighlightField(field, config) +: self.fields)

  def withExplicitFieldOrder: Highlights = self.copy(explicitFieldOrder = true)

  private def configList: List[(String, Json)] = globalConfig.toList
  private def fieldsList: Obj =
    if (explicitFieldOrder) {
      Obj("fields" -> Arr(fields.reverse.map(_.toJsonObj)))
    } else {
      Obj("fields" -> Obj(fields.reverse.map(_.toStringJsonPair): _*))
    }
}

object Highlights {
  type HighlightConfig = Map[String, Json]
}

final case class HighlightField(field: String, config: HighlightConfig = Map.empty) {
  def toStringJsonPair: (String, Obj) = field -> Obj(config.toList: _*)

  def toJsonObj: Json = Obj(field -> Obj(config.toList: _*))
}
