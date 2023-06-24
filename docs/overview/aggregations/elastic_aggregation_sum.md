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
// Document.intField must be number value, because of Sum aggregation
val aggregation: SumAggregation = sumAggregation(name = "sumAggregation", field = Document.intField)
```

If you want to change the `missing` parameter, you can use `missing` method:
```scala
val aggregationWithMissing: SumAggregation = sumAggregation(name = "sumAggregation", field = Document.intField).missing(10.0)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = sumAggregation(name = "sumAggregation1", field = Document.intField).withAgg(sumAggregation(name = "sumAggregation2", field = Document.doubleField))
```

You can find more information about `Sum` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-metrics-sum-aggregation.html).
