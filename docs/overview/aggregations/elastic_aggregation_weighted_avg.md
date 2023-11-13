---
id: elastic_aggregation_weighted_avg
title: "Weighted Avg Aggregation"
---

The `Weighted Avg` aggregation is a single-value metrics aggregation that computes the average while taking into account the varying degrees of importance of numeric values. As a formula, a weighted average is the `∑(value * weight) / ∑(weight)`

In order to use the `Weighted Avg` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.WeightedAvgAggregation
import zio.elasticsearch.ElasticAggregation.weightedAvgAggregation
```

You can create a `Weighted Avg` aggregation using the `weightedAvgAggregation` method this way:
```scala
val aggregation: WeightedAvgAggregation = weightedAvgAggregation(name = "weightedAvgAggregation", valueField = "doubleField", weightField = "intField")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Weighted Avg` aggregation using the `weightedAvgAggregation` method this way:
```scala
val aggregation: WeightedAvgAggregation = weightedAvgAggregation(name = "weightedAvgAggregation", valueField = Document.doubleField, weightField = Document.intField)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = weightedAvgAggregation(name = "weightedAvgAggregation1", valueField = Document.intField, weightField = Document.doubleField).withAgg(weightedAvgAggregation(name = "weightedAvgAggregation2", valueField = Document.doubleField, weightField = Document.intField))
```

If you want to change the `valueMissing`, you can use `valueMissing` method:
```scala
val aggregationWithValueMissing: WeightedAvgAggregation = weightedAvgAggregation(name = "weightedAvgAggregation", field = Document.intField).valueMissing(10.0)
```

If you want to change the `weightMissing`, you can use `weightMissing` method:
```scala
val aggregationWithWeightMissing: WeightedAvgAggregation = weightedAvgAggregation(name = "weightedAvgAggregation", field = Document.intField).weightMissing(5.0)
```

If you want to change the `weightMissing` and `valueMissing`, you can use `weightMissing` and `valueMissing` methods:
```scala
val aggregationWithValueAndWeightMissing: WeightedAvgAggregation = weightedAvgAggregation(name = "weightedAvgAggregation", field = Document.intField).valueMissing(5.0).weightMissing(10.0)
```

You can find more information about `Weighted Avg` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-metrics-weight-avg-aggregation.html).
