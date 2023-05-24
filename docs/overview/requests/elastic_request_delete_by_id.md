---
id: elastic_request_delete_by_id
title: "Delete By ID Request"
---

This query removes a JSON document from the specified index.

To create a `DeleteById` request do the following:
```scala
import zio.elasticsearch.ElasticRequest.DeleteByIdRequest
import zio.elasticsearch.ElasticRequest.deleteById
// this import is required for using `IndexName` and `DocumentId`
import zio.elasticsearch._

val request: DeleteByIdRequest = deleteById(index = IndexName("index"), id = DocumentId("111"))
```

If you want to change the `refresh`, you can use `refresh`, `refreshFalse` or `refreshTrue` method:
```scala
val requestWithRefresh: DeleteByIdRequest = deleteById(index = IndexName("index"), id = DocumentId("111")).refresh(true)
val requestWithRefreshFalse: DeleteByIdRequest = deleteById(index = IndexName("index"), id = DocumentId("111")).refreshFalse
val requestWithRefreshTrue: DeleteByIdRequest = deleteById(index = IndexName("index"), id = DocumentId("111")).refreshTrue
```

If you want to change the `routing`, you can use the `routing` method:
```scala
// this import is required for `Routing` also
import zio.elasticsearch._

val requestWithRouting: DeleteByIdRequest = deleteById(index = IndexName("index"), id = DocumentId("111")).routing(Routing("routing"))
```

You can find more information about `Count` request [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-count.html).
