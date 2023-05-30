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

If you want to change the `missing`, you can use `missing` method:
```scala
val aggregationWithMissing: CardinalityAggregation = cardinalityAggregation(name = "cardinalityAggregation", field = Document.intField).missing(10.0)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = cardinalityAggregation(name = "cardinalityAggregation", field = Document.intField).withAgg(maxAggregation(name = "maxAggregation", field = Document.doubleField))
```

You can find more information about `Cardinality` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-metrics-cardinality-aggregation.html).
