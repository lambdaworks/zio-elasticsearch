---
id: elastic_aggregation_avg_bucket
title: "Avg Bucket Aggregation"
---

The `AvgBucket` aggregation is a sibling pipeline aggregation which calculates the average value of a metric across the buckets of a sibling multi-bucket aggregation. Buckets without a value are skipped by default.

In order to use the `AvgBucket` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.AvgBucketAggregation
import zio.elasticsearch.ElasticAggregation.avgBucketAggregation
```

You can create a `AvgBucket` aggregation using the `avgBucketAggregation` method, with the path to the metric in the buckets of the sibling aggregation (`>` separates the names of the aggregations in the path):
```scala
val aggregation: AvgBucketAggregation = avgBucketAggregation(name = "avgBucketAggregation", bucketsPath = "salesPerMonth>sales")
```

Since it is a sibling pipeline aggregation, it is used on the same level as the aggregation it refers to, using `withAgg`:
```scala
import zio.elasticsearch.aggregation.CalendarInterval

val multipleAggregations: MultipleAggregations =
  dateHistogramAggregation(name = "salesPerMonth", field = Document.dateField, interval = CalendarInterval.Month)
    .withSubAgg(sumAggregation(name = "sales", field = Document.intField))
    .withAgg(avgBucketAggregation(name = "avgBucketAggregation", bucketsPath = "salesPerMonth>sales"))
```

If you want to change how buckets with missing values are handled, you can use `gapPolicy` method:
```scala
import zio.elasticsearch.aggregation.GapPolicy

val aggregationWithGapPolicy: AvgBucketAggregation = avgBucketAggregation(name = "avgBucketAggregation", bucketsPath = "salesPerMonth>sales").gapPolicy(GapPolicy.InsertZeros)
```

If you want to format the result, you can use `format` method:
```scala
val aggregationWithFormat: AvgBucketAggregation = avgBucketAggregation(name = "avgBucketAggregation", bucketsPath = "salesPerMonth>sales").format("0.00")
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: AvgBucketAggregation = avgBucketAggregation(name = "avgBucketAggregation", bucketsPath = "salesPerMonth>sales").meta("color", "blue")
```

The result is retrieved with the `asAvgBucketAggregation` accessor as a `SimpleValueAggregationResult`, which contains its `value` (`None` if no bucket has a value) and `valueAsString`, if a format was set.

You can find more information about `AvgBucket` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-pipeline-avg-bucket-aggregation.html).
