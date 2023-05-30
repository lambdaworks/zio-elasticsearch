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

Also, you can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Exists` query using the `exists` method this way:
```scala
val query: ExistsQuery = exists(field = Document.name)
```

If you want to change the `boost`, you can use `boost` method:
```scala
val queryWithBoost: ExistsQuery = exists(field = "name").boost(2.0)
```

You can find more information about `Exists` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-exists-query.html#query-dsl-exists-query).
