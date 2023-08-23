---
id: elastic_aggregation_percentiles
title: "Percentiles Aggregation"
---

The `Percentiles` aggregation is a multi-value metrics aggregation that calculates one or more percentiles over numeric values extracted from the aggregated documents.

In order to use the `Percentiles` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.PercentilesAggregation
import zio.elasticsearch.ElasticAggregation.percentilesAggregation
```

You can create a `Percentiles` aggregation using the `percentilesAggregation` method this way:
```scala
val aggregation: PercentilesAggregation = percentilesAggregation(name = "percentilesAggregation", field = "intField")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Percentiles` aggregation using the `percentilesAggregation` method this way:
```scala
// Document.intField must be number value
val aggregation: PercentilesAggregation = percentilesAggregation(name = "percentilesAggregation", field = Document.intField)
```

If you want to specify the percentiles you want to calculate, you can use `percents` method: 
```scala
val aggregationWithPercents: PercentilesAggregation = percentilesAggregation(name = "percentilesAggregation", field = Document.intField).percents(15, 50, 70)
```

If you want to change the `missing`, you can use `missing` method:
```scala
val aggregationWithMissing: PercentilesAggregation = percentilesAggregation(name = "percentilesAggregation", field = Document.intField).missing(10.0)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = percentilesAggregation(name = "percentilesAggregation1", field = Document.intField).withAgg(percentilesAggregation(name = "percentilesAggregation2", field = Document.doubleField))
```

You can find more information about `Percentiles` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-metrics-percentile-aggregation.html).

