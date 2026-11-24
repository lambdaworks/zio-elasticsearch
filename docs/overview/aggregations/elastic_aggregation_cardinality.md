---
id: elastic_aggregation_cardinality
title: "Cardinality Aggregation"
---

The `Cardinality` aggregation is a single-value metrics aggregation that calculates an approximate count of distinct values.

In order to use the `Cardinality` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.CardinalityAggregation
import zio.elasticsearch.ElasticAggregation.cardinalityAggregation
```

You can create a `Cardinality` aggregation using the `cardinalityAggregation` method in the following manner:
```scala
val aggregation: CardinalityAggregation = cardinalityAggregation(name = "cardinalityAggregation", field = "intField")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Cardinality` aggregation using the `cardinalityAggregation` method in the following manner:
```scala
val aggregation: CardinalityAggregation = cardinalityAggregation(name = "cardinalityAggregation", field = Document.intField)
```

If you want to change the `missing`, you can use `missing` method. The value should match the type of the field:
```scala
val aggregationWithMissing: CardinalityAggregation = cardinalityAggregation(name = "cardinalityAggregation", field = Document.intField).missing(10)
val aggregationWithStringMissing: CardinalityAggregation = cardinalityAggregation(name = "cardinalityAggregation", field = Document.stringField.keyword).missing("N/A")
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = cardinalityAggregation(name = "cardinalityAggregation", field = Document.intField).withAgg(maxAggregation(name = "maxAggregation", field = Document.doubleField))
```

If you want to transform the field values before they are aggregated, you can use `script` method (the field value is available as `_value`):
```scala
import zio.elasticsearch.script.Script

val aggregationWithScript: CardinalityAggregation = cardinalityAggregation(name = "cardinalityAggregation", field = Document.intField).script(Script("_value * 2"))
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: CardinalityAggregation = cardinalityAggregation(name = "cardinalityAggregation", field = Document.intField).meta("color", "blue")
```

You can find more information about `Cardinality` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-metrics-cardinality-aggregation.html).
