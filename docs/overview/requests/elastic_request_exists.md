---
id: elastic_request_exists
title: "Exists Request"
---

This request is used for checking whether document exists.

To create a `Exists` request do the following:
```scala
import zio.elasticsearch.ElasticRequest.ExistsRequest
import zio.elasticsearch.ElasticRequest.exists
// this import is required for using `IndexName` and `DocumentId`
import zio.elasticsearch._

val request: ExistsRequest = exists(index = IndexName("index"), id = DocumentId("111"))
```

If you want to change the `routing`, you can use the `routing` method:
```scala
// this import is required for `Routing` also
import zio.elasticsearch._

val requestWithRouting: ExistsRequest = exists(index = IndexName("index"), id = DocumentId("111")).routing(Routing("routing"))
```

You can find more information about `Exists` request [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/indices-exists.html#indices-exists).
