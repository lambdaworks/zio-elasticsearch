---
id: elastic_aggregation_max
title: "Max Aggregation"
---

The `Max` aggregation is a single-value metrics aggregation that keeps track and returns the maximum value among the numeric values extracted from the aggregated documents.

In order to use the `Max` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.MaxAggregation
import zio.elasticsearch.ElasticAggregation.maxAggregation
```

You can create a `Max` aggregation using the `maxAggregation` method this way:
```scala
val aggregation: MaxAggregation = maxAggregation(name = "maxAggregation", field = "intField")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Max` aggregation using the `maxAggregation` method this way:
```scala
// Document.intField must be number value, because of Max aggregation
val aggregation: MaxAggregation = maxAggregation(name = "maxAggregation", field = Document.intField)
```

If you want to change the `missing`, you can use `missing` method:
```scala
val aggregationWithMissing: MaxAggregation = maxAggregation(name = "maxAggregation", field = Document.intField).missing(10.0)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = maxAggregation(name = "maxAggregation1", field = Document.intField).withAgg(maxAggregation(name = "maxAggregation2", field = Document.doubleField))
```

You can find more information about `Max` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-metrics-max-aggregation.html).
