---
id: elastic_aggregation_stats_bucket
title: "Stats Bucket Aggregation"
---

The `StatsBucket` aggregation is a sibling pipeline aggregation which calculates stats (`count`, `min`, `max`, `avg` and `sum`) of a metric across the buckets of a sibling multi-bucket aggregation.

In order to use the `StatsBucket` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.StatsBucketAggregation
import zio.elasticsearch.ElasticAggregation.statsBucketAggregation
```

You can create a `StatsBucket` aggregation using the `statsBucketAggregation` method, with the path to the metric in the buckets of the sibling aggregation (`>` separates the names of the aggregations in the path):
```scala
val aggregation: StatsBucketAggregation = statsBucketAggregation(name = "statsBucketAggregation", bucketsPath = "salesPerMonth>sales")
```

Since it is a sibling pipeline aggregation, it is used on the same level as the aggregation it refers to, using `withAgg`:
```scala
import zio.elasticsearch.aggregation.CalendarInterval

val multipleAggregations: MultipleAggregations =
  dateHistogramAggregation(name = "salesPerMonth", field = Document.dateField, interval = CalendarInterval.Month)
    .withSubAgg(sumAggregation(name = "sales", field = Document.intField))
    .withAgg(statsBucketAggregation(name = "statsBucketAggregation", bucketsPath = "salesPerMonth>sales"))
```

If you want to change how buckets with missing values are handled, you can use `gapPolicy` method:
```scala
import zio.elasticsearch.aggregation.GapPolicy

val aggregationWithGapPolicy: StatsBucketAggregation = statsBucketAggregation(name = "statsBucketAggregation", bucketsPath = "salesPerMonth>sales").gapPolicy(GapPolicy.InsertZeros)
```

If you want to format the result, you can use `format` method:
```scala
val aggregationWithFormat: StatsBucketAggregation = statsBucketAggregation(name = "statsBucketAggregation", bucketsPath = "salesPerMonth>sales").format("0.00")
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: StatsBucketAggregation = statsBucketAggregation(name = "statsBucketAggregation", bucketsPath = "salesPerMonth>sales").meta("color", "blue")
```

The result is retrieved with the `asStatsBucketAggregation` accessor as a `StatsAggregationResult`, which contains the same fields as the result of a `stats` aggregation.

You can find more information about `StatsBucket` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-pipeline-stats-bucket-aggregation.html).
