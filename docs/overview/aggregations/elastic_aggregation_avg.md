---
id: elastic_aggregation_avg
title: "Avg Aggregation"
---

The `Avg` aggregation is a single-value metrics aggregation that keeps track and returns the average value among the numeric values extracted from the aggregated documents.

In order to use the `Avg` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.AvgAggregation
import zio.elasticsearch.ElasticAggregation.avgAggregation
```

You can create a `Avg` aggregation using the `avgAggregation` method this way:
```scala
val aggregation: AvgAggregation = avgAggregation(name = "avgAggregation", field = "intField")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Avg` aggregation using the `avgAggregation` method this way:
```scala
// Document.intField must be number value, because of Avg aggregation
val aggregation: AvgAggregation = avgAggregation(name = "avgAggregation", field = Document.intField)
```

If you want to change the `missing` parameter, you can use `missing` method:
```scala
val aggregationWithMissing: AvgAggregation = avgAggregation(name = "avgAggregation", field = Document.intField).missing(10.0)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = avgAggregation(name = "avgAggregation1", field = Document.intField).withAgg(avgAggregation(name = "avgAggregation2", field = Document.doubleField))
```

You can find more information about `Avg` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-metrics-avg-aggregation.html).
