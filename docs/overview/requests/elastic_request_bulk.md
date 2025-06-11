---
id: elastic_request_bulk
title: "Bulk Request"
---

The `Bulk` request performs multiple indexing or delete operations in a single API call. This reduces overhead and can greatly increase indexing speed.

In order to use the `Bulk` request import the following:
```scala
import zio.elasticsearch.ElasticRequest.BulkRequest
import zio.elasticsearch.ElasticRequest.bulk
// this import is required for using `IndexName` and `DocumentId`
import zio.elasticsearch._
```

You can create a `Bulk` request using the `bulk` method, which offers two main ways to handle indices:
1. With a global index:
This approach is ideal when most (or all) of your bulk operations target the same index. You provide a default index for the entire bulk request as 
the first argument. Any individual operation within this `bulk` request that doesn't explicitly specify its own index will automatically use this 
global index.
_**Important: If an individual operation does specify its own index, that individual index will always take precedence over the global one for that 
specific operation.**_
```scala
val index = IndexName("my-global-index")

val document1 = new Document(id = DocumentId("111"), intField = 1, stringField = "stringField1")
val document2 = new Document(id = DocumentId("222"), intField = 2, stringField = "stringField2")

val request: BulkRequest = bulk(
index,
create(doc = document1),
upsert(id = DocumentId("111"), doc = document2)
)
```
2. Without a global index:
Choose this method when your bulk operations frequently target different indices, or when you prefer to explicitly define the index for every single 
operation. When using this variant, each individual `BulkableRequest` must specify its own index.
```scala
val index1 = IndexName("first-index")
val index2 = IndexName("second-index")

val document1 = new Document(id = DocumentId("111"), intField = 1, stringField = "stringField1")
val document2 = new Document(id = DocumentId("222"), intField = 2, stringField = "stringField2")

val request: BulkRequest = bulk(
  create(index = index1, doc = document1),
  upsert(index = index2, id = DocumentId("111"), doc = document2)
)
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
