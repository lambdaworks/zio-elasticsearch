---
id: elastic_aggregation_bucket_script
title: "Bucket Script Aggregation"
---

The `BucketScript` aggregation is a parent pipeline aggregation which executes a script for each bucket of its parent multi-bucket aggregation, using metrics of the bucket as script variables, and returns the result as a new metric of the bucket.

In order to use the `BucketScript` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.BucketScriptAggregation
import zio.elasticsearch.ElasticAggregation.bucketScriptAggregation
import zio.elasticsearch.script.Script
```

You can create a `BucketScript` aggregation using the `bucketScriptAggregation` method, with a map of script variables and the paths to the metrics they refer to:
```scala
val aggregation: BucketScriptAggregation =
  bucketScriptAggregation(
    name = "averagePrice",
    script = Script("params.sales / params.count"),
    bucketsPath = Map("sales" -> "salesAggregation", "count" -> "_count")
  )
```

Since it is a parent pipeline aggregation, it can only be used as a sub-aggregation of a multi-bucket aggregation:
```scala
val aggregation: TermsAggregation =
  termsAggregation(name = "termsAggregation", field = Document.stringField.keyword)
    .withSubAgg(sumAggregation(name = "salesAggregation", field = Document.intField))
    .withSubAgg(bucketScriptAggregation(name = "averagePrice", script = Script("params.sales / params.count"), bucketsPath = Map("sales" -> "salesAggregation", "count" -> "_count")))
```

If you want to change how buckets with missing values are handled, you can use `gapPolicy` method:
```scala
import zio.elasticsearch.aggregation.GapPolicy

val aggregationWithGapPolicy: BucketScriptAggregation = bucketScriptAggregation(name = "averagePrice", script = Script("params.sales / params.count"), bucketsPath = Map("sales" -> "salesAggregation", "count" -> "_count")).gapPolicy(GapPolicy.InsertZeros)
```

If you want to format the result, you can use `format` method:
```scala
val aggregationWithFormat: BucketScriptAggregation = bucketScriptAggregation(name = "averagePrice", script = Script("params.sales / params.count"), bucketsPath = Map("sales" -> "salesAggregation", "count" -> "_count")).format("0.00")
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: BucketScriptAggregation = bucketScriptAggregation(name = "averagePrice", script = Script("params.sales / params.count"), bucketsPath = Map("sales" -> "salesAggregation", "count" -> "_count")).meta("color", "blue")
```

The result in each bucket is a `SimpleValueAggregationResult`, which contains the `value` and its `valueAsString`, if a format was set:
```scala
import zio.elasticsearch.result.SimpleValueAggregationResult

bucket.subAggregationAs[SimpleValueAggregationResult]("averagePrice")
```

You can find more information about `BucketScript` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-pipeline-bucket-script-aggregation.html).
