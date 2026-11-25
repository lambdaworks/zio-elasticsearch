---
id: elastic_aggregation_max_bucket
title: "Max Bucket Aggregation"
---

The `MaxBucket` aggregation is a sibling pipeline aggregation which finds the buckets of a sibling multi-bucket aggregation with the maximum value of a metric.

In order to use the `MaxBucket` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.MaxBucketAggregation
import zio.elasticsearch.ElasticAggregation.maxBucketAggregation
```

You can create a `MaxBucket` aggregation using the `maxBucketAggregation` method, with the path to the metric in the buckets of the sibling aggregation (`>` separates the names of the aggregations in the path):
```scala
val aggregation: MaxBucketAggregation = maxBucketAggregation(name = "maxBucketAggregation", bucketsPath = "salesPerMonth>sales")
```

Since it is a sibling pipeline aggregation, it is used on the same level as the aggregation it refers to, using `withAgg`:
```scala
import zio.elasticsearch.aggregation.CalendarInterval

val multipleAggregations: MultipleAggregations =
  dateHistogramAggregation(name = "salesPerMonth", field = Document.dateField, interval = CalendarInterval.Month)
    .withSubAgg(sumAggregation(name = "sales", field = Document.intField))
    .withAgg(maxBucketAggregation(name = "maxBucketAggregation", bucketsPath = "salesPerMonth>sales"))
```

If you want to change how buckets with missing values are handled, you can use `gapPolicy` method:
```scala
import zio.elasticsearch.aggregation.GapPolicy

val aggregationWithGapPolicy: MaxBucketAggregation = maxBucketAggregation(name = "maxBucketAggregation", bucketsPath = "salesPerMonth>sales").gapPolicy(GapPolicy.InsertZeros)
```

If you want to format the result, you can use `format` method:
```scala
val aggregationWithFormat: MaxBucketAggregation = maxBucketAggregation(name = "maxBucketAggregation", bucketsPath = "salesPerMonth>sales").format("0.00")
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: MaxBucketAggregation = maxBucketAggregation(name = "maxBucketAggregation", bucketsPath = "salesPerMonth>sales").meta("color", "blue")
```

The result is retrieved with the `asMaxBucketAggregation` accessor as a `BucketMetricValueAggregationResult`, which contains the maximum `value`, its `valueAsString`, if a format was set, and the `keys` of the buckets with that value.

You can find more information about `MaxBucket` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-pipeline-max-bucket-aggregation.html).
