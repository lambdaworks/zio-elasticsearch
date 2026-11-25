---
id: elastic_aggregation_derivative
title: "Derivative Aggregation"
---

The `Derivative` aggregation is a parent pipeline aggregation which calculates the derivative of a metric in each bucket of its parent `histogram` or `dateHistogram` aggregation, i.e. the difference from the value in the previous bucket.

In order to use the `Derivative` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.DerivativeAggregation
import zio.elasticsearch.ElasticAggregation.derivativeAggregation
```

You can create a `Derivative` aggregation using the `derivativeAggregation` method, with the path to the metric:
```scala
val aggregation: DerivativeAggregation = derivativeAggregation(name = "derivativeAggregation", bucketsPath = "sumAggregation")
```

Since it is a parent pipeline aggregation, it can only be used as a sub-aggregation of a multi-bucket aggregation:
```scala
import zio.elasticsearch.aggregation.CalendarInterval

val aggregation: DateHistogramAggregation =
  dateHistogramAggregation(name = "salesPerMonth", field = Document.dateField, interval = CalendarInterval.Month)
    .withSubAgg(sumAggregation(name = "sumAggregation", field = Document.intField))
    .withSubAgg(derivativeAggregation(name = "derivativeAggregation", bucketsPath = "sumAggregation"))
```

If you want the derivative per unit of time (e.g. per day for monthly buckets) in `normalizedValue`, you can use `unit` method:
```scala
val aggregationWithUnit: DerivativeAggregation = derivativeAggregation(name = "derivativeAggregation", bucketsPath = "sumAggregation").unit(CalendarInterval.Day)
```

If you want to change how buckets with missing values are handled, you can use `gapPolicy` method:
```scala
import zio.elasticsearch.aggregation.GapPolicy

val aggregationWithGapPolicy: DerivativeAggregation = derivativeAggregation(name = "derivativeAggregation", bucketsPath = "sumAggregation").gapPolicy(GapPolicy.InsertZeros)
```

If you want to format the result, you can use `format` method:
```scala
val aggregationWithFormat: DerivativeAggregation = derivativeAggregation(name = "derivativeAggregation", bucketsPath = "sumAggregation").format("0.00")
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: DerivativeAggregation = derivativeAggregation(name = "derivativeAggregation", bucketsPath = "sumAggregation").meta("color", "blue")
```

The result in each bucket (except the first one) is a `DerivativeAggregationResult`, which contains the `value`, its `valueAsString`, if a format was set, and the `normalizedValue`, if a unit was set:
```scala
import zio.elasticsearch.result.DerivativeAggregationResult

bucket.subAggregationAs[DerivativeAggregationResult]("derivativeAggregation")
```

You can find more information about `Derivative` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-pipeline-derivative-aggregation.html).
