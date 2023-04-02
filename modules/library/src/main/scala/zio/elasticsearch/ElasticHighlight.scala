package zio.elasticsearch

import zio.Chunk
import zio.elasticsearch.highlighting.{Highlights, HighlightField}
import zio.json.ast.Json

object ElasticHighlight {

  def highlightSafe[S](field: Field[S, _], config: Map[String, Json] = Map.empty): Highlights =
    Highlights(Chunk(HighlightField(field.toString, config)))

  def highlight(field: String, config: Map[String, Json] = Map.empty): Highlights =
    Highlights(Chunk(HighlightField(field, config)))
}
