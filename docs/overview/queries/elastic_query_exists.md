---
id: elastic_query_exists
title: "Exists Query"
---

The `Exists` query is used for returning documents that contain an indexed value for a field.

In order to use the `Exists` query import the following:
```scala
import zio.elasticsearch.query.ExistsQuery
import zio.elasticsearch.ElasticQuery._
```

You can create an `Exists` query using the `exists` method this way:
```scala
val query: ExistsQuery = exists(field = "name")
```

Also, you can create a type-safe `Exists` query using the `exists` method this way:
```scala
val query: ExistsQuery = exists(field = Document.name)
```

You can find more information about Exists query [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-exists-query.html#query-dsl-exists-query).
