---
id: elastic_aggregation_percentile_ranks
title: "Percentiles Aggregation"
---

The `Percentile ranks` aggregation is a multi-value metrics aggregation that calculates percentile of values at or below a threshold grouped by a specified value.

In order to use the `Percentile ranks` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.PercentileRanksAggregation
import zio.elasticsearch.ElasticAggregation.percentileRanksAggregation
```

You can create a `Percentile ranks` aggregation using the `percentileRanksAggregation` method this way:
```scala
val aggregation: PercentileRanksAggregation = percentileRanksAggregation(field = "intField", name = "percentileRanksAggregation", values = 500, 600)
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Percentile ranks` aggregation using the `percentileRanksAggregation` method this way:
```scala
// Document.intField must be number value
val aggregation: PercentileRanksAggregation = percentileRanksAggregation(field = Document.intField, name = "percentileRanksAggregation", values = 500, 600)
```

If you want to change the `missing`, you can use `missing` method:
```scala
val aggregationWithMissing: PercentileRanksAggregation = percentileRanksAggregation(field = Document.intField, name = "percentileRanksAggregation", values = 500, 600).missing(10.0)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = percentileRanksAggregation(field = Document.intField, name = "percentileRanksAggregation1", values = 500, 600).withAgg(percentileRanksAggregation(field = Document.doubleField, name = "percentileRanksAggregation2", values = 500, 600))
```

You can find more information about `Percentile ranks` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-metrics-percentile-rank-aggregation.html).

