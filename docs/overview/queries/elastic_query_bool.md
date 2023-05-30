---
id: elastic_query_bool
title: "Boolean Query"
---

The query that matches documents matching boolean combinations of other queries. It is built using one or more boolean clauses (queries):
- `filter`: The clause (query) must appear in matching documents. However, unlike `must` the score of the query will be ignored.
- `must`: the clause (query) must appear in matching documents and will contribute to the score.
- `must not`: the clause (query) must not appear in the matching documents.
- `should`: the clause (query) should appear in the matching document.

In order to use the `Bool` query import the following:
```scala
import zio.elasticsearch.query.BoolQuery
import zio.elasticsearch.ElasticQuery._
```

The `Bool` query can be created with `filter`, `must`, `mustNot` or `should` method:
```scala
val filterQuery: BoolQuery = filter(contains(field = Document.name, value = "a"), startsWith(field = Document.id, value = "b"))
val mustQuery: BoolQuery = must(contains(field = Document.name, value = "a"), startsWith(field = Document.id, value = "b"))
val mustNotQuery: BoolQuery = mustNot(contains(field = Document.name, value = "a"))
val shouldQuery: BoolQuery = should(startsWith(field = Document.name, value = "a"))
```

Once the `Bool` query is created, you can call `filter`, `must`, `mustNot`, `should`, `boost` and `minimumShouldMatch` methods on it.

If you want to add `Filter` query to `Bool` query, you can use `filter` method (you can also call `filter` method on `Bool` query that is created with `filter` method):
```scala
val filterQuery: BoolQuery = filter(contains(field = Document.name, value = "a")).filter(contains(field = Document.name, value = "c"))
```

If you want to add `Must` query to the `Bool` query, you can use `must` method:
```scala
val boolQuery: BoolQuery = filter(contains(field = Document.name, value = "a")).must(contains(field = Document.name, value = "c"))
```

If you want to add `MustNot` query to the `Bool` query, you can use `mustNot` method:
```scala
val boolQuery: BoolQuery = filter(contains(field = Document.name, value = "a")).mustNot(contains(field = Document.name, value = "c"))
```

If you want to add `Should` query to the `Bool` query, you can use `should` method:
```scala
val boolQuery: BoolQuery = filter(contains(field = Document.name, value = "a")).should(contains(field = Document.name, value = "c"))
```

If you want to change the `_score` parameter, you can use the `boost` method:
```scala
val queryWithBoost: BoolQuery = filter(contains(field = Document.name, value = "a")).boost(1.2)
```

If you want to change the `minimum_should_match` parameter, you can use the `minimumShouldMatch` method:
```scala
val queryWithMinimumShouldMatch: BoolQuery = should(contains(field = Document.name, value = "a")).minimumShouldMatch(2)
```

You can find more information about Boolean query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-bool-query.html).
