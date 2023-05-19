---
id: elastic_query_has_parent
title: "Has Parent Query"
---

The `HasParent` query returns child documents whose parent document matches a provided query.

To create a `HasParent` query do the following:
```scala
import zio.elasticsearch.query.HasParentQuery
import zio.elasticsearch.ElasticQuery._

val query: HasParentQuery = hasParent(parentType = "parent", query = matches(Document.stringField, "test"))
```

If you want to change `ignore_unmapped`, you can use `ignoreUnmapped` method:
```scala
val queryWithIgnoreUnmapped: HasParentQuery = hasParent(parentType = "parent", query = matches(Document.stringField, "test")).ignoreUnmapped(true)
```

If you want to change `inner_hits`, you can use `innerHits` method:
```scala
import zio.elasticsearch.query.InnerHits

val queryWithInnerHits: HasParentQuery = hasParent(parentType = "parent", query = matches(Document.stringField, "test")).innerHits(innerHits = InnerHits.from(5))
```

If you want to change `score`, you can use `withScore`, `withScoreFalse` or `withScoreTrue` method:
```scala
val queryWithScore: HasParentQuery = hasParent(parentType = "parent", query = matches(Document.intField, "test")).withScore(true)
val queryWithScoreFalse: HasParentQuery = hasParent(parentType = "parent", query = matches(Document.intField, "test")).withScoreFalse
val queryWithScoreTrue: HasParentQuery = hasParent(parentType = "parent", query = matches(Document.intField, "test")).withScoreTrue
```

You can find more information about `HasParent` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-has-parent-query.html).
