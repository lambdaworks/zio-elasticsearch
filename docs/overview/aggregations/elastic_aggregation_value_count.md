---
id: elastic_aggregation_value_count
title: "Value count Aggregation"
---

The `Value count` aggregation is a single-value metrics aggregation that calculates the number of values that an aggregation is based on.

In order to use the `Value count` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.ValueCountAggregation
import zio.elasticsearch.ElasticAggregation.valueCountAggregation
```

You can create a `Value count` aggregation using the `valueCountAggregation` method this way:
```scala
val aggregation: ValueCountAggregation = valueCountAggregation(name = "valueCountAggregation", field = "stringField")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Value count` aggregation using the `valueCountAggregation` method this way:
```scala
val aggregation: ValueCountAggregation = valueCountAggregation(name = "valueCountAggregation", field = Document.stringField)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = valueCountAggregation(name = "valueCountAggregation1", field = Document.stringField).withAgg(valueCountAggregation(name = "valueCountAggregation2", field = Document.intField))
```

If you want to transform the field values before they are aggregated, you can use `script` method (the field value is available as `_value`):
```scala
import zio.elasticsearch.script.Script

val aggregationWithScript: ValueCountAggregation = valueCountAggregation(name = "valueCountAggregation", field = Document.stringField).script(Script("_value * 2"))
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: ValueCountAggregation = valueCountAggregation(name = "valueCountAggregation", field = Document.stringField).meta("color", "blue")
```

You can find more information about `Value count` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-metrics-valuecount-aggregation.html#search-aggregations-metrics-valuecount-aggregation).
