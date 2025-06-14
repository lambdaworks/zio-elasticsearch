---
id: elastic_aggregation_range
title: "Range Aggregation"
---

The `Range` aggregation is a multi-value aggregation enables the user to define a set of ranges. During the aggregation process, the values extraced from each document will be checked against each bucket range.

In order to use the `Range` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.RangeAggregation
import zio.elasticsearch.ElasticAggregation.RangeAggregation
```

You can create a `Range` aggregation using the `rangeAggregation` method this way:
```scala
val aggregation: RangeAggregation = rangeAggregation(name = "rangeAggregation", field =  "testField", range = SingleRange.to(23.9))
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Range` aggregation using the `rangeAggregation` method this way:
```scala
// Document.intField must be number value, because of Min aggregation
val aggregation: RangeAggregation = rangeAggregation(name = "rangeAggregation", field =  Document.intField, range = SingleRange.to(23.9))
```

// TODO: check this
If you want to change the `missing` parameter, you can use `missing` method:
```scala
val aggregationWithMissing: MinAggregation = minAggregation(name = "minAggregation", field = Document.intField).missing(10.0)
```
// TODO: check this
If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = minAggregation(name = "minAggregation1", field = Document.intField).withAgg(minAggregation(name = "minAggregation2", field = Document.doubleField))
```

You can find more information about `Range` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-bucket-range-aggregation.html).
