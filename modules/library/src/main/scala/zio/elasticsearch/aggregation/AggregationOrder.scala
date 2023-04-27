package zio.elasticsearch.aggregation

import zio.elasticsearch.query.sort.SortOrder
final case class AggregationOrder(
  orderKey: String,
  order: SortOrder
)
