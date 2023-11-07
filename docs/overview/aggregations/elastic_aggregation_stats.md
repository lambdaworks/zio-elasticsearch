---
id: elastic_aggregation_stats
title: "Stats Aggregation"
---

The `Stats` aggregation is a multi-value metrics aggregation that provides statistical information (count, sum, min, max, and average of a field) over numeric values extracted from the aggregated documents.

In order to use the `Stats` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.StatsAggregation
import zio.elasticsearch.ElasticAggregation.statsAggregation
```

You can create a `Stats` aggregation using the `statsAggregation` method this way:
```scala
val aggregation: StatsAggregation = statsAggregation(name = "statsAggregation", field = "intField")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Stats` aggregation using the `statsAggregation` method this way:
```scala
// Document.intField must be number value, because of Stats aggregation
val aggregation: StatsAggregation = statsAggregation(name = "statsAggregation", field = Document.intField)
```

If you want to change the `missing` parameter, you can use `missing` method:
```scala
val aggregationWithMissing: StatsAggregation = statsAggregation(name = "statsAggregation", field = Document.intField).missing(10.0)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = statsAggregation(name = "statsAggregation1", field = Document.intField).withAgg(statsAggregation(name = "statsAggregation2", field = Document.doubleField))
```

You can find more information about `Stats` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-metrics-stats-aggregation.html).
