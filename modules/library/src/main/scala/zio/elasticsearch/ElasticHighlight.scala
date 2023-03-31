package zio.elasticsearch

import zio.Chunk
import zio.elasticsearch.highlighting.{Highlights, HighlightField}
import zio.json.ast.Json

object ElasticHighlight {

  def highlight[S](field: Field[S, _], fieldConfig: Map[String, Json]): Highlights =
    Highlights(Chunk(HighlightField(field.toString, fieldConfig)))

  def highlight(field: String, fieldConfig: Map[String, Json] = Map.empty): Highlights =
    Highlights(Chunk(HighlightField(field, fieldConfig)))
}
