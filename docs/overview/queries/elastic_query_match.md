---
id: elastic_query_match
title: "Match Query"
---

The `Match` query is a type of query that searches for a provided text, number, date or boolean value.
This is the standard query for performing a full-text search, including options for fuzzy matching.

In order to use the `Match` query import following:

```scala
import zio.elasticsearch.query.MatchQuery
import zio.elasticsearch.ElasticQuery._
```

To create a type-safe instance of `MatchQuery`:
```scala
val query: MatchQuery = matches(field = Document.message, value = "this is a test")
```

To create an instance of `MatchQuery`:
```scala
val query: MatchQuery = matches(field = "message", value = "this is a test")
```

You can find more information about Match query [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-match-query.html#query-dsl-match-query).
