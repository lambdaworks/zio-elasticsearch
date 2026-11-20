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
// Document.intField must be a number or a date value
val aggregation: AvgAggregation = avgAggregation(name = "avgAggregation", field = Document.intField)
```

If you want to change the `missing` parameter, you can use `missing` method. The value has to be a number or a date (e.g. `LocalDate` or `Instant`).
```scala
val aggregationWithMissing: AvgAggregation = avgAggregation(name = "avgAggregation", field = Document.intField).missing(10.0)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = avgAggregation(name = "avgAggregation1", field = Document.intField).withAgg(avgAggregation(name = "avgAggregation2", field = Document.doubleField))
```

Besides numeric fields, the type-safe `avgAggregation` method also accepts date fields:
```scala
import java.time.LocalDate

val aggregationOnDate: AvgAggregation = avgAggregation(name = "avgAggregation", field = Document.dateField).missing(LocalDate.of(2024, 1, 1))
```

If you want to change the `format` of the `value_as_string` property in the response, you can use `format` method:
```scala
val aggregationWithFormat: AvgAggregation = avgAggregation(name = "avgAggregation", field = Document.intField).format("0.00")
```

If you want to transform the field values before they are aggregated, you can use `script` method (the field value is available as `_value`):
```scala
import zio.elasticsearch.script.Script

val aggregationWithScript: AvgAggregation = avgAggregation(name = "avgAggregation", field = Document.intField).script(Script("_value * 2"))
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: AvgAggregation = avgAggregation(name = "avgAggregation", field = Document.intField).meta("color", "blue")
```

You can find more information about `Avg` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-metrics-avg-aggregation.html).
