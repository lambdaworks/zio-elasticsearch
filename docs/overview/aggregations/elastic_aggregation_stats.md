---
id: elastic_aggregation_stats
title: "Stats Aggregation"
---

The `Stats` aggregation is a multi-value metrics aggregation that provides statistical information (count, sum, min, max and average of a field) over numeric values extracted from the aggregated documents.

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
// Document.intField must be a number or a date value
val aggregation: StatsAggregation = statsAggregation(name = "statsAggregation", field = Document.intField)
```

If you want to change the `missing` parameter, you can use `missing` method. The value has to be a number or a date (e.g. `LocalDate` or `Instant`).
```scala
val aggregationWithMissing: StatsAggregation = statsAggregation(name = "statsAggregation", field = Document.intField).missing(10.0)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = statsAggregation(name = "statsAggregation1", field = Document.intField).withAgg(statsAggregation(name = "statsAggregation2", field = Document.doubleField))
```

Besides numeric fields, the type-safe `statsAggregation` method also accepts date fields:
```scala
import java.time.LocalDate

val aggregationOnDate: StatsAggregation = statsAggregation(name = "statsAggregation", field = Document.dateField).missing(LocalDate.of(2024, 1, 1))
```

If you want to change the `format` of the `value_as_string` property in the response, you can use `format` method:
```scala
val aggregationWithFormat: StatsAggregation = statsAggregation(name = "statsAggregation", field = Document.intField).format("0.00")
```

If you want to transform the field values before they are aggregated, you can use `script` method (the field value is available as `_value`):
```scala
import zio.elasticsearch.script.Script

val aggregationWithScript: StatsAggregation = statsAggregation(name = "statsAggregation", field = Document.intField).script(Script("_value * 2"))
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: StatsAggregation = statsAggregation(name = "statsAggregation", field = Document.intField).meta("color", "blue")
```

You can find more information about `Stats` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-metrics-stats-aggregation.html).
