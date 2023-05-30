---
id: elastic_request_delete_by_query
title: "Delete By Query Request"
---

The `DeleteByQuery` request deletes documents that match the specified query.

To create a `DeleteById` request do the following:
```scala
import zio.elasticsearch.ElasticRequest.DeleteByQueryRequest
import zio.elasticsearch.ElasticRequest.deleteByQuery
// this import is required for using `IndexName`
import zio.elasticsearch._
import zio.elasticsearch.ElasticQuery._

val request: DeleteByQueryRequest = deleteByQuery(index = IndexName("index"), query = contains(field = Document.name, value = "test"))
```

If you want to change the `refresh`, you can use `refresh`, `refreshFalse` or `refreshTrue` method:
```scala
val requestWithRefresh: DeleteByQueryRequest = deleteByQuery(index = IndexName("index"), query = contains(field = Document.name, value = "test")).refresh(true)
val requestWithRefreshFalse: DeleteByQueryRequest = deleteByQuery(index = IndexName("index"), query = contains(field = Document.name, value = "test")).refreshFalse
val requestWithRefreshTrue: DeleteByQueryRequest = deleteByQuery(index = IndexName("index"), query = contains(field = Document.name, value = "test")).refreshTrue
```

If you want to change the `routing`, you can use the `routing` method:
```scala
// this import is required for `Routing` also
import zio.elasticsearch._

val requestWithRouting: DeleteByQueryRequest = deleteByQuery(index = IndexName("index"), query = contains(field = Document.name, value = "test")).routing(Routing("routing"))
```

You can find more information about `DeleteByQuery` request [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/docs-delete-by-query.html).
