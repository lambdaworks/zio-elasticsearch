---
id: elastic_aggregation_cumulative_sum
title: "Cumulative Sum Aggregation"
---

The `CumulativeSum` aggregation is a parent pipeline aggregation which calculates the cumulative sum of a metric in each bucket of its parent `histogram` or `dateHistogram` aggregation.

In order to use the `CumulativeSum` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.CumulativeSumAggregation
import zio.elasticsearch.ElasticAggregation.cumulativeSumAggregation
```

You can create a `CumulativeSum` aggregation using the `cumulativeSumAggregation` method, with the path to the metric to sum (`_count` sums the document counts of the buckets):
```scala
val aggregation: CumulativeSumAggregation = cumulativeSumAggregation(name = "cumulativeSumAggregation", bucketsPath = "sumAggregation")
```

Since it is a parent pipeline aggregation, it can only be used as a sub-aggregation of a multi-bucket aggregation:
```scala
import zio.elasticsearch.aggregation.CalendarInterval

val aggregation: DateHistogramAggregation =
  dateHistogramAggregation(name = "salesPerMonth", field = Document.dateField, interval = CalendarInterval.Month)
    .withSubAgg(sumAggregation(name = "sumAggregation", field = Document.intField))
    .withSubAgg(cumulativeSumAggregation(name = "cumulativeSumAggregation", bucketsPath = "sumAggregation"))
```

If you want to format the result, you can use `format` method:
```scala
val aggregationWithFormat: CumulativeSumAggregation = cumulativeSumAggregation(name = "cumulativeSumAggregation", bucketsPath = "sumAggregation").format("0.00")
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: CumulativeSumAggregation = cumulativeSumAggregation(name = "cumulativeSumAggregation", bucketsPath = "sumAggregation").meta("color", "blue")
```

The result in each bucket is a `SimpleValueAggregationResult`, which contains the `value` and its `valueAsString`, if a format was set:
```scala
import zio.elasticsearch.result.SimpleValueAggregationResult

bucket.subAggregationAs[SimpleValueAggregationResult]("cumulativeSumAggregation")
```

You can find more information about `CumulativeSum` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-pipeline-cumulative-sum-aggregation.html).
