package zio.elasticsearch.query.options

private[elasticsearch] trait HasUseField[Q <: HasUseField[Q]] {

  /**
   * Sets the `use_field` parameter for this [[zio.elasticsearch.query.ElasticIntervalQuery]] query.
   *
   * @param value
   *   the name of the field to use
   * @return
   *   a new instance of the query with the `use_field` value set
   */
  def useField(value: String): Q
}
