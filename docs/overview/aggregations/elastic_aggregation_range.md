---
id: elastic_aggregation_range
title: "Range Aggregation"
---

The `Range` aggregation is a multi-bucket aggregation that enables the user to define a set of ranges, each representing a bucket. During the aggregation process, the values extracted from each document will be checked against each bucket range. Note that each range includes the `from` value and excludes the `to` value.

In order to use the `Range` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.{RangeAggregation, RangeBound}
import zio.elasticsearch.ElasticAggregation.rangeAggregation
```

You can create a `RangeAggregation` using the `rangeAggregation` method this way:
```scala
val aggregation: RangeAggregation =
  rangeAggregation(
    name = "rangeAggregation",
    field = "intField",
    range = RangeBound().to(100),
    ranges = RangeBound().from(100).to(200)
  )
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `RangeAggregation` using the `rangeAggregation` method this way:
```scala
// Document.intField must be number value
val aggregation: RangeAggregation =
  rangeAggregation(
    name = "rangeAggregation",
    field = Document.intField,
    range = RangeBound().to(100),
    ranges = RangeBound().from(100).to(200)
  )
```

If you want to associate each bucket with a unique string key, you can use the `keyed` method together with the `key` of each range:
```scala
val aggregationWithKeyed: RangeAggregation =
  rangeAggregation(
    name = "rangeAggregation",
    field = Document.intField,
    range = RangeBound().to(100).key("cheap"),
    ranges = RangeBound().from(100).key("expensive")
  ).keyed
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations =
  rangeAggregation(name = "rangeAggregation", field = Document.intField, range = RangeBound().to(100))
    .withAgg(maxAggregation(name = "maxAggregation", field = Document.intField))
```

If you want to add another sub-aggregation, you can use `withSubAgg` method:
```scala
val aggregationWithSubAgg: RangeAggregation =
  rangeAggregation(name = "rangeAggregation", field = Document.intField, range = RangeBound().to(100))
    .withSubAgg(maxAggregation(name = "maxAggregation", field = Document.intField))
```

You can find more information about `Range` aggregation [here](https://www.elastic.co/docs/reference/aggregations/search-aggregations-bucket-range-aggregation).
