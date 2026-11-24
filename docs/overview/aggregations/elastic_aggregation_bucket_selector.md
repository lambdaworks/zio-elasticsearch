---
id: elastic_aggregation_bucket_selector
title: "Bucket Selector Aggregation"
---

This aggregation is a parent pipeline aggregation which executes a script which determines whether the current bucket will be retained in the parent multi-bucket aggregation.

To create a `BucketSelector` aggregation do the following:
```scala
import zio.elasticsearch.aggregation.BucketSelectorAggregation
import zio.elasticsearch.ElasticAggregation.bucketSelectorAggregation
import zio.elasticsearch.script.Script

val aggregation: BucketSelectorAggregation = bucketSelectorAggregation(name = "aggregationSelector", script = Script("params.value > 10"), bucketsPath = Map("value" -> "otherAggregation"))
```

If you want to change how buckets with missing values are handled, you can use `gapPolicy` method:
```scala
import zio.elasticsearch.aggregation.GapPolicy

val aggregationWithGapPolicy: BucketSelectorAggregation = bucketSelectorAggregation(name = "aggregationSelector", script = Script("params.value > 10"), bucketsPath = Map("value" -> "otherAggregation")).gapPolicy(GapPolicy.InsertZeros)
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: BucketSelectorAggregation = bucketSelectorAggregation(name = "aggregationSelector", script = Script("params.value > 10"), bucketsPath = Map("value" -> "otherAggregation")).meta("color", "blue")
```

Since this is a parent pipeline aggregation, it can only be used as a sub-aggregation of a bucket aggregation, and not on the top level of a request:
```scala
val aggregation: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField).withSubAgg(bucketSelectorAggregation(name = "aggregationSelector", script = Script("params.value > 10"), bucketsPath = Map("value" -> "otherAggregation")))
```

You can find more information about `BucketSelector` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-pipeline-bucket-selector-aggregation.html).
