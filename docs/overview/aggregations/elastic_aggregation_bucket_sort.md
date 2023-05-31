---
id: elastic_aggregation_bucket_sort
title: "Bucket Sort Aggregation"
---

The `BucketSort` aggregation is a parent pipeline aggregation which sorts the buckets of its parent multi-bucket aggregation. Zero or more sort fields may be specified together with the corresponding sort order.

To create a `BucketSort` aggregation do the following:
```scala
import zio.elasticsearch.aggregation.BucketSortAggregation
import zio.elasticsearch.ElasticAggregation.bucketSortAggregation

val aggregation: BucketSortAggregation = bucketSortAggregation(name = "aggregationSort")
```

If you want to change the `from`, you can use `from` method:
```scala
val aggregationWithFrom: BucketSortAggregation = bucketSortAggregation(name = "aggregationSort").from(5)
```

If you want to change the `size`, you can use `size` method:
```scala
val aggregationWithSize: BucketSortAggregation = bucketSortAggregation(name = "aggregationSort").size(5)
```

If you want to change the `sort`, you can use `sort` method:
```scala
import zio.elasticsearch.query.sort.SortByField.{byCount, byKey}

val aggregationWithSort: BucketSortAggregation = bucketSortAggregation(name = "aggregationSort").sort(byCount, byKey)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = bucketSortAggregation(name = "aggregationSort").withAgg(maxAggregation(name = "maxAggregation", field = Document.doubleField))
```

You can find more information about `BucketSort` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-pipeline-bucket-sort-aggregation.html).
