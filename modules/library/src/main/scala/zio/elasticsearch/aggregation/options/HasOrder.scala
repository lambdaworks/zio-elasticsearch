package zio.elasticsearch.aggregation.options

import zio.elasticsearch.aggregation.AggregationOrder

private[elasticsearch] trait HasOrder[A <: HasOrder[A]] {

  /**
   * Sets the `order` parameter for the [[zio.elasticsearch.aggregation.ElasticAggregation]].
   *
   * @param orders
   *   a list of [[zio.elasticsearch.aggregation.AggregationOrder]] defining the sort order for the aggregation results
   * @return
   *   an instance of the [[zio.elasticsearch.aggregation.ElasticAggregation]] enriched with the `order` parameter.
   */
  def order(orders: AggregationOrder*): A
}
