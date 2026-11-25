---
id: elastic_aggregation_histogram
title: "Histogram Aggregation"
---

The `Histogram` aggregation is a multi-bucket aggregation that groups documents into buckets by a numeric value, where each bucket covers one interval (e.g. `[0, 10)`, `[10, 20)`, ...).

In order to use the `Histogram` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.HistogramAggregation
import zio.elasticsearch.ElasticAggregation.histogramAggregation
```

You can create a `Histogram` aggregation using the `histogramAggregation` method this way:
```scala
val aggregation: HistogramAggregation = histogramAggregation(name = "histogramAggregation", field = "intField", interval = 10)
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Histogram` aggregation using the `histogramAggregation` method this way:
```scala
// Document.intField must be number value
val aggregation: HistogramAggregation = histogramAggregation(name = "histogramAggregation", field = Document.intField, interval = 10)
```

If you want to shift the bucket boundaries, you can use `offset` method:
```scala
// buckets are [5, 15), [15, 25), ...
val aggregationWithOffset: HistogramAggregation = histogramAggregation(name = "histogramAggregation", field = Document.intField, interval = 10).offset(5)
```

By default, empty buckets between the first and the last bucket are returned. If you want to return only buckets with a minimum number of documents, you can use `minDocCount` method:
```scala
val aggregationWithMinDocCount: HistogramAggregation = histogramAggregation(name = "histogramAggregation", field = Document.intField, interval = 10).minDocCount(1)
```

If you want to return buckets for a whole range, even if they are empty, you can use `extendedBounds` method, and if you want to limit the buckets to a range, you can use `hardBounds` method. Either of the bounds can be omitted:
```scala
import zio.elasticsearch.aggregation.HistogramBounds

val aggregationWithExtendedBounds: HistogramAggregation = histogramAggregation(name = "histogramAggregation", field = Document.intField, interval = 10).extendedBounds(HistogramBounds().min(0).max(100))
val aggregationWithHardBounds: HistogramAggregation = histogramAggregation(name = "histogramAggregation", field = Document.intField, interval = 10).hardBounds(HistogramBounds().max(50))
```

If you want to change the `order`, you can use `orderBy`, `orderByCountAsc`, `orderByCountDesc`, `orderByKeyAsc` or `orderByKeyDesc` method:
```scala
val aggregationWithOrder: HistogramAggregation = histogramAggregation(name = "histogramAggregation", field = Document.intField, interval = 10).orderByCountDesc
```

If you want to associate each bucket with a unique string key, you can use `keyed` method:
```scala
val aggregationWithKeyed: HistogramAggregation = histogramAggregation(name = "histogramAggregation", field = Document.intField, interval = 10).keyed
```

If you want to put documents without a value in a bucket, you can use `missing` method:
```scala
val aggregationWithMissing: HistogramAggregation = histogramAggregation(name = "histogramAggregation", field = Document.intField, interval = 10).missing(0)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = histogramAggregation(name = "histogramAggregation", field = Document.intField, interval = 10).withAgg(maxAggregation(name = "maxAggregation", field = Document.intField))
```

If you want to add another sub-aggregation, you can use `withSubAgg` method. Parent pipeline aggregations, such as `cumulativeSum` and `derivative`, can be used as sub-aggregations:
```scala
val aggregationWithSubAgg: HistogramAggregation = histogramAggregation(name = "histogramAggregation", field = Document.intField, interval = 10).withSubAgg(cumulativeSumAggregation(name = "cumulativeSumAggregation", bucketsPath = "_count"))
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: HistogramAggregation = histogramAggregation(name = "histogramAggregation", field = Document.intField, interval = 10).meta("color", "blue")
```

The result is retrieved with the `asHistogramAggregation` accessor. Each of its buckets has a numeric `key` (the start of the bucket) and a `docCount`.

You can find more information about `Histogram` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-bucket-histogram-aggregation.html).
