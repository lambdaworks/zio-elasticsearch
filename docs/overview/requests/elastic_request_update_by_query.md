---
id: elastic_request_update_by_query
title: "Update By Query Request"
---

The `UpdateByQuery` request updates documents that match the specified query. If no query is specified, performs an update on every document in the specified Elasticsearch index.

In order to use the `UpdateByQuery` request import the following:
```scala
import zio.elasticsearch.ElasticRequest.UpdateByQueryRequest
import zio.elasticsearch.ElasticRequest._
```

You can create a `UpdateByQuery` request using the `updateAllByQuery` method in the following manner:
```scala
// this import is required for using `IndexName`
import zio.elasticsearch._
import zio.elasticsearch.script.Script

val request: UpdateByQueryRequest = updateAllByQuery(index = IndexName("index"), script = Script("ctx._source.intField += params['factor']").params("factor" -> 2))
```

You can create a `UpdateByQuery` request using the `updateByQuery` method in the following manner:
```scala
import zio.elasticsearch._
import zio.elasticsearch.script.Script
import zio.elasticsearch.ElasticQuery._

val request: UpdateByQueryRequest = updateByQuery(index = IndexName("index"), query = contains(field = Document.name, value = "test"), script = Script("ctx._source.intField += params['factor']").params("factor" -> 2))
```

If you want to change the `conflicts`, you can use the `conflicts` method:
```scala
import zio.elasticsearch.request.UpdateConflicts.Proceed

val requestWithConflicts: UpdateByQueryRequest = updateAllByQuery(index = IndexName("index"), script = Script("ctx._source.intField += params['factor']").params("factor" -> 2)).conflicts(Proceed)
```

If you want to change the `refresh`, you can use `refresh`, `refreshFalse` or `refreshTrue` method:
```scala
val requestWithRefresh: UpdateByQueryRequest = updateAllByQuery(index = IndexName("index"), script = Script("ctx._source.intField += params['factor']").params("factor" -> 2)).refresh(true)
val requestWithRefreshFalse: UpdateByQueryRequest = updateAllByQuery(index = IndexName("index"), script = Script("ctx._source.intField += params['factor']").params("factor" -> 2)).refreshFalse
val requestWithRefreshTrue: UpdateByQueryRequest = updateAllByQuery(index = IndexName("index"), script = Script("ctx._source.intField += params['factor']").params("factor" -> 2)).refreshTrue
```

If you want to change the `routing`, you can use the `routing` method on any of previously mentioned methods:
```scala
// this import is required for using `Routing` also
import zio.elasticsearch._

val requestWithRouting: UpdateByQueryRequest = updateAllByQuery(index = IndexName("index"), script = Script("ctx._source.intField += params['factor']").params("factor" -> 2)).routing(Routing("routing"))
```

You can find more information about `UpdateByQuery` request [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/docs-update-by-query.html).
