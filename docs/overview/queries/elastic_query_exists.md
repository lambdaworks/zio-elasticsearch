---
id: elastic_query_exists
title: "Exists Query"
---

The Exists query is used for returning documents that contain an indexed value for a field.

To create a `Exists` query do the following:

```scala
import zio.elasticsearch.query.ExistsQuery
import zio.elasticsearch.ElasticQuery._
```

type-safe:
```scala
val query: ExistsQuery = exists(field = Document.name)
```

raw:
```scala
val query: ExistsQuery = exists(field = "name")
```

You can find more information about Match query [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-exists-query.html#query-dsl-exists-query).
