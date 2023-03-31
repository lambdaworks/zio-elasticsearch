package zio.elasticsearch.highlighting

import zio.Chunk
import zio.elasticsearch.highlighting.Highlights.HighlightConfig
import zio.json.ast.Json
import zio.json.ast.Json.Obj

case class Highlights(fields: Chunk[HighlightField], globalConfig: HighlightConfig = Map.empty) { self =>
  def toJson: Json = Obj("highlight" -> Obj(configList: _*).merge(fieldsList))

  def withGlobalConfig(configFieldName: String, config: Json): Highlights =
    self.copy(globalConfig = self.globalConfig.updated(configFieldName, config))

  def withHighlight(fieldName: String, fieldConfig: HighlightConfig = Map.empty): Highlights =
    self.copy(fields = HighlightField(fieldName, fieldConfig) +: self.fields)

  private lazy val configList: List[(String, Json)] = globalConfig.toList
  private lazy val fieldsList: Obj                  = Obj("fields" -> Obj(fields.map(_.toStringJsonPair): _*))
}

object Highlights {
  type HighlightConfig = Map[String, Json]
}

case class HighlightField(fieldName: String, fieldConfig: HighlightConfig = Map.empty) {
  def toStringJsonPair: (String, Obj) = fieldName -> Obj(fieldConfig.toList: _*)
}
