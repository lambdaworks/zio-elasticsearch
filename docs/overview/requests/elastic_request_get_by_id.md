---
id: elastic_request_get_by_id
title: "Get By ID Request"
---

The `GetById` request retrieves the specified JSON document from an Elasticsearch index.

To create a `GetById` request do the following:
```scala
import zio.elasticsearch.ElasticRequest.GetByIdRequest
import zio.elasticsearch.ElasticRequest.getById
// this import is required for using `IndexName` and `DocumentId`
import zio.elasticsearch._

val request: ExistsRequest = getById(index = IndexName("index"), id = DocumentId("111"))
```

If you want to change the `refresh`, you can use `refresh`, `refreshFalse` or `refreshTrue` method:
```scala
val requestWithRefresh: GetByIdRequest = getById(index = IndexName("index"), id = DocumentId("111")).refresh(true)
val requestWithRefreshFalse: GetByIdRequest = getById(index = IndexName("index"), id = DocumentId("111")).refreshFalse
val requestWithRefreshTrue: GetByIdRequest = getById(index = IndexName("index"), id = DocumentId("111")).refreshTrue
```

If you want to change the `routing`, you can use the `routing` method:
```scala
// this import is required for `Routing` also
import zio.elasticsearch._

val requestWithRouting: GetByIdRequest = getById(index = IndexName("index"), id = DocumentId("111")).routing(Routing("routing"))
```

You can find more information about `GetById` request [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/docs-get.html).
