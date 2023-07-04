---
id: elastic_query_ids
title: "IDs Query"
---

The `IDs` returns documents based on their IDs. This query uses document IDs stored in the _id field. 

In order to use the `IDs` query import the following:
```scala
import zio.elasticsearch.query.IdsQuery
import zio.elasticsearch.ElasticQuery._
```

The `IDs` query can be created with `ids` method.

To create a `IDs` query use the following method:
```scala
val query: IdsQuery = ids("id1")
val query: IdsQuery = ids("id1", "id2", "id3")
```

You can find more information about `IDs` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-ids-query.html).
