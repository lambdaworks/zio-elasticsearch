package zio.elasticsearch.query.options

private[elasticsearch] trait HasAnalyzer[Q <: HasAnalyzer[Q]] {

  /**
   * Sets the `analyzer` parameter for this [[zio.elasticsearch.query.ElasticIntervalQuery]] query.
   *
   * @param value
   *   the name of the analyzer to use
   * @return
   *   a new instance of the query with the `analyzer` value set
   */
  def analyzer(value: String): Q
}
