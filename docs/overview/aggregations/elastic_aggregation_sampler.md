---
id: elastic_aggregation_sampler
title: "Sampler Aggregation"
---
The Sampler aggregation is a single-bucket aggregation that returns a sample of the documents that fall into the aggregation scope. This aggregation is
particularly useful when you want to run sub-aggregations on a representative sample of documents rather than on the entire dataset.

To use the `Sampler` aggregation, import the following:
```scala
import zio.elasticsearch.aggregation.SamplerAggregation
import zio.elasticsearch.ElasticAggregation.samplerAggregation
```

A Sampler aggregation must always have at least one sub-aggregation.
You can create a Sampler aggregation with an initial sub-aggregation using the `samplerAggregation` method this way:
```scala
import zio.elasticsearch.ElasticAggregation.avgAggregation
val aggregation: SamplerAggregation = samplerAggregation(
  name = "samplerAggregation", 
  subAgg = avgAggregation(name = "avgRating", field = Document.intField)
)
```

If you want to add another sub-aggregation, you can use `withSubAgg` method:
```scala
val aggregationWithMultipleSubAggs: SamplerAggregation = samplerAggregation(
  name = "termsAggregation", 
  field = Document.stringField
).withSubAgg(maxAggregation(name = "maxAggregation", field = Document.intField))
```
By default, the `shard_size` parameter for a Sampler aggregation is set to 100. This means that each shard will return a maximum of 100 documents to be 
sampled.
If you want to change the `shard_size`, you can use the `maxDocumentsPerShard` method:
```scala
val aggregationWithShardSize: SamplerAggregation = samplerAggregation(
  name = "samplerAggregation",
  subAgg = avgAggregation(name = "avgRating", field = Document.intField)
).maxDocumentsPerShard(500)
```
You can find more detailed information about the `Sampler` aggregation in the official Elasticsearch documentation [here](https://www.elastic.co/docs/reference/aggregations/search-aggregations-bucket-sampler-aggregation).