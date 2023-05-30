---
id: elastic_request_aggregate
title: "Aggregation Request"
---

This request is used to create aggregations which summarizes your data as metrics, statistics, or other analytics.

To create a `Aggregate` request do the following:
```scala
import zio.elasticsearch.ElasticRequest.AggregateRequest
import zio.elasticsearch.ElasticRequest.aggregate
// this import is required for using `IndexName`
import zio.elasticsearch._
import zio.elasticsearch.ElasticAggregation._

val request: AggregateRequest = aggregate(name = IndexName("index"), aggregation = maxAggregation(name = "aggregation", field = "intField"))
```

You can find more information about `Aggregate` request [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations.html).
