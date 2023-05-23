package zio.elasticsearch.request.options

import zio.json.ast.Json

private[elasticsearch] trait HasSearchAfter[R <: HasSearchAfter[R]] {

  /**
   * Sets the `search_after` parameter for the [[zio.elasticsearch.ElasticRequest]].
   *
   * @param value
   *   the JSON value to be set as the `search_after` parameter
   * @return
   *   an instance of a [[zio.elasticsearch.ElasticRequest]] enriched with the `search_after` parameter.
   */
  def searchAfter(value: Json): R
}
