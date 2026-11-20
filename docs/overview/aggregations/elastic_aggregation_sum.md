---
id: elastic_aggregation_sum
title: "Sum Aggregation"
---

The `Sum` aggregation is a single-value metrics aggregation that keeps track and returns the sum value among the numeric values extracted from the aggregated documents.

In order to use the `Sum` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.SumAggregation
import zio.elasticsearch.ElasticAggregation.sumAggregation
```

You can create a `Sum` aggregation using the `sumAggregation` method this way:
```scala
val aggregation: SumAggregation = sumAggregation(name = "sumAggregation", field = "intField")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Sum` aggregation using the `sumAggregation` method this way:
```scala
// Document.intField must be a number or a date value
val aggregation: SumAggregation = sumAggregation(name = "sumAggregation", field = Document.intField)
```

If you want to change the `missing` parameter, you can use `missing` method. The value has to be a number or a date (e.g. `LocalDate` or `Instant`).
```scala
val aggregationWithMissing: SumAggregation = sumAggregation(name = "sumAggregation", field = Document.intField).missing(10.0)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = sumAggregation(name = "sumAggregation1", field = Document.intField).withAgg(sumAggregation(name = "sumAggregation2", field = Document.doubleField))
```

Besides numeric fields, the type-safe `sumAggregation` method also accepts date fields:
```scala
import java.time.LocalDate

val aggregationOnDate: SumAggregation = sumAggregation(name = "sumAggregation", field = Document.dateField).missing(LocalDate.of(2024, 1, 1))
```

If you want to change the `format` of the `value_as_string` property in the response, you can use `format` method:
```scala
val aggregationWithFormat: SumAggregation = sumAggregation(name = "sumAggregation", field = Document.intField).format("0.00")
```

If you want to transform the field values before they are aggregated, you can use `script` method (the field value is available as `_value`):
```scala
import zio.elasticsearch.script.Script

val aggregationWithScript: SumAggregation = sumAggregation(name = "sumAggregation", field = Document.intField).script(Script("_value * 2"))
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: SumAggregation = sumAggregation(name = "sumAggregation", field = Document.intField).meta("color", "blue")
```

You can find more information about `Sum` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-metrics-sum-aggregation.html).
