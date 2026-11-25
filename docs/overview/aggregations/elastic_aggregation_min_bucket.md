---
id: elastic_aggregation_min_bucket
title: "Min Bucket Aggregation"
---

The `MinBucket` aggregation is a sibling pipeline aggregation which finds the buckets of a sibling multi-bucket aggregation with the minimum value of a metric.

In order to use the `MinBucket` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.MinBucketAggregation
import zio.elasticsearch.ElasticAggregation.minBucketAggregation
```

You can create a `MinBucket` aggregation using the `minBucketAggregation` method, with the path to the metric in the buckets of the sibling aggregation (`>` separates the names of the aggregations in the path):
```scala
val aggregation: MinBucketAggregation = minBucketAggregation(name = "minBucketAggregation", bucketsPath = "salesPerMonth>sales")
```

Since it is a sibling pipeline aggregation, it is used on the same level as the aggregation it refers to, using `withAgg`:
```scala
import zio.elasticsearch.aggregation.CalendarInterval

val multipleAggregations: MultipleAggregations =
  dateHistogramAggregation(name = "salesPerMonth", field = Document.dateField, interval = CalendarInterval.Month)
    .withSubAgg(sumAggregation(name = "sales", field = Document.intField))
    .withAgg(minBucketAggregation(name = "minBucketAggregation", bucketsPath = "salesPerMonth>sales"))
```

If you want to change how buckets with missing values are handled, you can use `gapPolicy` method:
```scala
import zio.elasticsearch.aggregation.GapPolicy

val aggregationWithGapPolicy: MinBucketAggregation = minBucketAggregation(name = "minBucketAggregation", bucketsPath = "salesPerMonth>sales").gapPolicy(GapPolicy.InsertZeros)
```

If you want to format the result, you can use `format` method:
```scala
val aggregationWithFormat: MinBucketAggregation = minBucketAggregation(name = "minBucketAggregation", bucketsPath = "salesPerMonth>sales").format("0.00")
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: MinBucketAggregation = minBucketAggregation(name = "minBucketAggregation", bucketsPath = "salesPerMonth>sales").meta("color", "blue")
```

The result is retrieved with the `asMinBucketAggregation` accessor as a `BucketMetricValueAggregationResult`, which contains the minimum `value`, its `valueAsString`, if a format was set, and the `keys` of the buckets with that value.

You can find more information about `MinBucket` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-pipeline-min-bucket-aggregation.html).
