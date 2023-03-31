---
id: elastic_query_match_all
title: "Match All Query"
---

The most simple query, which matches all documents, giving them all a `_score` of `1.0`.

To create a `MatchAll` query do the following:

```scala
import zio.elasticsearch.query.MatchAllQuery
import zio.elasticsearch.ElasticQuery._

val query: MatchAllQuery = matchAll
```

If you want to change the `_score`, you can use the `boost` method:

```scala
val queryWithBoost: MatchAllQuery = matchAll.boost(1.2)
```
