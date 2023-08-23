---
id: elastic_query_match_boolean_prefix
title: "Match Boolean Prefix Query"
---

The `MatchBooleanPrefix` query analyzes its input and constructs a `bool` query from the terms. Each term except the last is used in a `term` query. 
The last term is used in a `prefix` query.

In order to use the `MatchBooleanPrefix` query import the following:
```scala
import zio.elasticsearch.query.MatchBooleanPrefixQuery
import zio.elasticsearch.ElasticQuery._
```

You can create a `MatchBooleanPrefix` query using the `matchBooleanPrefix` method this way:
```scala
val query: MatchBooleanPrefixQuery = matchBooleanPrefix(field = "stringField", value = "test")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `MatchBooleanPrefix` query using the `matchBooleanPrefix` method this way:
```scala
val query: MatchBooleanPrefixQuery = matchBooleanPrefix(field = Document.stringField, value = "test")
```

If you want to change the `minimum_should_match` parameter, you can use the `minimumShouldMatch` method:
```scala
val queryWithMinimumShouldMatch: MatchBooleanPrefixQuery = matchBooleanPrefix(field = Document.stringField, value = "test").minimumShouldMatch(2)
```

You can find more information about `MatchBooleanPrefix` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-match-bool-prefix-query.html).

