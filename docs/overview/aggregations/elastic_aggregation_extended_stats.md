---
id: elastic_aggregation_extended_stats
title: "Extended stats Aggregation"
---

The `Extended stats` aggregation is a multi-value metrics aggregation that provides statistical information (count, sum, min, max, average, sum od squares, variance and std deviation of a field) over numeric values extracted from the aggregated documents.
The `Extended stats` aggregation is an extended version of the [`Stats`](https://lambdaworks.github.io/zio-elasticsearch/overview/aggregations/elastic_aggregation_stats) aggregation.

In order to use the `Extended stats` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.ExtendedStatsAggregation
import zio.elasticsearch.ElasticAggregation.extendedStatsAggregation
```

You can create a `Extended stats` aggregation using the `extendedStatsAggregation` method this way:
```scala
val aggregation: ExtendedStatsAggregation = extendedStatsAggregation(name = "extendedStatsAggregation", field = "intField")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Extended stats` aggregation using the `extendedStatsAggregation` method this way:
```scala
// Document.intField must be number value, because of Stats aggregation
val aggregation: ExtendedStatsAggregation = extendedStatsAggregation(name = "extendedStatsAggregation", field = Document.intField)
```

If you want to change the `missing` parameter, you can use `missing` method:
```scala
val aggregationWithMissing: ExtendedStatsAggregation = extendedStatsAggregation(name = "extendedStatsAggregation", field = Document.intField).missing(10.0)
```

If you want to change the `sigma` parameter, you can use `sigma` method:
```scala
val aggregationWithSigma: ExtendedStatsAggregation = extendedStatsAggregation(name = "extendedStatsAggregation", field = Document.intField).sigma(3.0)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = extendedStatsAggregation(name = "extendedStatsAggregation", field = Document.intField).withAgg(extendedStatsAggregation(name = "extendedStatsAggregation2", field = Document.doubleField))
```

You can find more information about `Extended stats` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-metrics-extendedstats-aggregation.html#search-aggregations-metrics-extendedstats-aggregation).
