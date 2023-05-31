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

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = bucketSelectorAggregation(name = "aggregationSelector", script = Script("params.value > 10"), bucketsPath = Map("value" -> "otherAggregation")).withAgg(maxAggregation(name = "maxAggregation", field = Document.doubleField))
```

You can find more information about `BucketSelector` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-pipeline-bucket-selector-aggregation.html).
