---
id: elastic_aggregation_min
title: "Min Aggregation"
---

The `Min` aggregation is a single-value metrics aggregation that keeps track and returns the minimum value among the numeric values extracted from the aggregated documents.

In order to use the `Min` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.MinAggregation
import zio.elasticsearch.ElasticAggregation.minAggregation
```

You can create a `Min` aggregation using the `minAggregation` method this way:
```scala
val aggregation: MinAggregation = minAggregation(name = "minAggregation", field = "intField")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Min` aggregation using the `minAggregation` method this way:
```scala
// Document.intField must be number value, because of Min aggregation
val aggregation: MinAggregation = minAggregation(name = "minAggregation", field = Document.intField)
```

If you want to change the `missing` parameter, you can use `missing` method:
```scala
val aggregationWithMissing: MinAggregation = minAggregation(name = "minAggregation", field = Document.intField).missing(10.0)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = minAggregation(name = "minAggregation1", field = Document.intField).withAgg(minAggregation(name = "minAggregation2", field = Document.doubleField))
```

You can find more information about `Min` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-metrics-min-aggregation.html).
