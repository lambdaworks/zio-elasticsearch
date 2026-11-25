---
id: elastic_aggregation_sum_bucket
title: "Sum Bucket Aggregation"
---

The `SumBucket` aggregation is a sibling pipeline aggregation which calculates the sum of a metric across the buckets of a sibling multi-bucket aggregation.

In order to use the `SumBucket` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.SumBucketAggregation
import zio.elasticsearch.ElasticAggregation.sumBucketAggregation
```

You can create a `SumBucket` aggregation using the `sumBucketAggregation` method, with the path to the metric in the buckets of the sibling aggregation (`>` separates the names of the aggregations in the path):
```scala
val aggregation: SumBucketAggregation = sumBucketAggregation(name = "sumBucketAggregation", bucketsPath = "salesPerMonth>sales")
```

Since it is a sibling pipeline aggregation, it is used on the same level as the aggregation it refers to, using `withAgg`:
```scala
import zio.elasticsearch.aggregation.CalendarInterval

val multipleAggregations: MultipleAggregations =
  dateHistogramAggregation(name = "salesPerMonth", field = Document.dateField, interval = CalendarInterval.Month)
    .withSubAgg(sumAggregation(name = "sales", field = Document.intField))
    .withAgg(sumBucketAggregation(name = "sumBucketAggregation", bucketsPath = "salesPerMonth>sales"))
```

If you want to change how buckets with missing values are handled, you can use `gapPolicy` method:
```scala
import zio.elasticsearch.aggregation.GapPolicy

val aggregationWithGapPolicy: SumBucketAggregation = sumBucketAggregation(name = "sumBucketAggregation", bucketsPath = "salesPerMonth>sales").gapPolicy(GapPolicy.InsertZeros)
```

If you want to format the result, you can use `format` method:
```scala
val aggregationWithFormat: SumBucketAggregation = sumBucketAggregation(name = "sumBucketAggregation", bucketsPath = "salesPerMonth>sales").format("0.00")
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: SumBucketAggregation = sumBucketAggregation(name = "sumBucketAggregation", bucketsPath = "salesPerMonth>sales").meta("color", "blue")
```

The result is retrieved with the `asSumBucketAggregation` accessor as a `SimpleValueAggregationResult`, which contains its `value` and `valueAsString`, if a format was set.

You can find more information about `SumBucket` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-pipeline-sum-bucket-aggregation.html).
