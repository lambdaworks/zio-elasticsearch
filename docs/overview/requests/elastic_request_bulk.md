---
id: elastic_request_bulk
title: "Bulk Request"
---

The `Bulk` request performs multiple indexing or delete operations in a single API call. This reduces overhead and can greatly increase indexing speed.

In order to use the `Bulk` request import the following:
```scala
import zio.elasticsearch.ElasticRequest.BulkRequest
import zio.elasticsearch.ElasticRequest.bulk
```

You can create a `Bulk` request using the `bulk` method this way:
```scala
// this import is required for using `IndexName` and `DocumentId`
import zio.elasticsearch._

val index = Index("index")

val document1 = new Document(id = DocumentId("111"), intField = 1, stringField = "stringField1")
val document2 = new Document(id = DocumentId("222"), intField = 2, stringField = "stringField2")

val request: BulkRequest = bulk(create(index = index, doc = document1), upsert(index = index, id = DocumentId("111"), doc = document2))
```

If you want to change the `refresh`, you can use `refresh`, `refreshFalse` or `refreshTrue` method:
```scala
val requestWithRefresh: BulkRequest = bulk(create(index = index, doc = document1), upsert(index = index, id = DocumentId("111"), doc = document2)).refresh(true)
val requestWithRefreshFalse: BulkRequest = bulk(create(index = index, doc = document1), upsert(index = index, id = DocumentId("111"), doc = document2)).refreshFalse
val requestWithRefreshTrue: BulkRequest = bulk(create(index = index, doc = document1), upsert(index = index, id = DocumentId("111"), doc = document2)).refreshTrue
```

If you want to change the `routing`, you can use the `routing` method:
```scala
// this import is required for using `Routing` also
import zio.elasticsearch._

val requestWithRouting: BulkRequest = bulk(create(index = index, doc = document1), upsert(index = index, id = DocumentId("111"), doc = document2)).routing(Routing("routing"))
```

You can find more information about `Bulk` request [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/docs-bulk.html).
