---
id: elastic_query_has_child
title: "Has Child Query"
---

The `HasChild` query returns parent documents whose child documents match a provided query.

To create a `HasChild` query do the following:
```scala
import zio.elasticsearch.query.HasChildQuery
import zio.elasticsearch.ElasticQuery._

val query: HasChildQuery = hasChild(childType = "child", query = matches(Document.stringField, "test"))
```

If you want to change `ignore_unmapped`, you can use `ignoreUnmapped` method:
```scala
val queryWithIgnoreUnmapped: HasChildQuery = hasChild(childType = "child", query = matches(Document.stringField, "test")).ignoreUnmapped(true)
```

If you want to change `inner_hits`, you can use `innerHits` method:
```scala
import zio.elasticsearch.query.InnerHits

val queryWithInnerHits: HasChildQuery = hasChild(childType = "child", query = matches(Document.stringField, "test")).innerHits(innerHits = InnerHits.from(5))
```

If you want to change `max_children`, you can use `maxChildren` method:
```scala
val queryWithMaxChildren: HasChildQuery = hasChild(childType = "child", query = matches(Document.stringField, "test")).maxChildren(5)
```

If you want to change `min_children`, you can use `minChildren` method:
```scala
val queryWithMinChildren: HasChildQuery = hasChild(childType = "child", query = matches(Document.stringField, "test")).minChildren(2)
```

If you want to change `score_mode`, you can use `scoreMode` method:
```scala
import zio.elasticsearch.query.ScoreMode

val queryWithScoreMode: HasChildQuery = hasChild(childType = "child", query = matches(Document.stringField, "test")).scoreMode(ScoreMode.Max)
```

You can find more information about `HasChild` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-has-child-query.html).
