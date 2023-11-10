---
id: elastic_query_disjunction_max
title: "Disjunction max Query"
---

The `Disjunction max` query returns documents  that match one or more query clauses. For documents that match multiple query clauses, the relevance score is set to the highest relevance score from all matching query clauses. When the relevance scores of the returned documents are identical, tie breaker parameter gives more weight to documents that match multiple query clauses.

In order to use the `Disjunction max` query import the following:
```scala
import zio.elasticsearch.query.DisjunctionMax
import zio.elasticsearch.ElasticQuery.disjunctionMax
```

You can create a `Disjunction max` query using the `disjunctionMax` method this way:
```scala
val query: DisjunctionMaxQuery = disjunctionMax( queries = Chunk( term( field = "stringField", value = "test"), exists( field = "intField")))
```

If you want to change the `tieBreaker`, you can use `tieBreaker` method:
```scala
val queryWithPrefixLength: DisjunctionMaxQuery = disjunctionMax( queries = Chunk( queries = Chunk( exists("existsField"), ids("1", "2", "3"))).tieBreaker(0.5f))
```

You can find more information about `Disjunction max` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-dis-max-query.html).

