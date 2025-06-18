package zio.elasticsearch.query.options

import zio.elasticsearch.Field

private[elasticsearch] trait HasUseField[Q <: HasUseField[Q]] {

  /**
   * Sets the `use_field` parameter for this [[zio.elasticsearch.query.ElasticIntervalQuery]] query.
   *
   * @param field
   *   the type-safe field to use from the document definition
   * @return
   *   a new instance of the query with the `use_field` value set.
   */
  def useField(field: Field[_, _]): Q

  /**
   * Sets the `use_field` parameter using a plain string.
   *
   * @param field
   *   the name of the field as a string
   * @return
   *   a new instance of the query with the `use_field` value set.
   */
  def useField(field: String): Q
}
