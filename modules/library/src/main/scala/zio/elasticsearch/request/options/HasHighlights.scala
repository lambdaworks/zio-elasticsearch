package zio.elasticsearch.request.options

import zio.elasticsearch.highlights.Highlights

private[elasticsearch] trait HasHighlights[R <: HasHighlights[R]] {

  /**
   * Sets the [[zio.elasticsearch.highlights.Highlights]] for the [[zio.elasticsearch.ElasticRequest]].
   *
   * @param value
   *   the [[zio.elasticsearch.highlights.Highlights]] to be set
   * @return
   *   an instance of the [[zio.elasticsearch.ElasticRequest]] enriched with the highlights.
   */
  def highlights(value: Highlights): R
}
