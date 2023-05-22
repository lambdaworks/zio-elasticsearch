---
id: elastic_query_match_phrase
title: "Match Phrase Query"
---

The `MatchPhrase` query analyzes the text and creates a `phrase` query out of the analyzed text.

In order to use the `MatchPhrase` query import the following:
```scala
import zio.elasticsearch.query.MatchPhraseQuery
import zio.elasticsearch.ElasticQuery._
```

You can create a `MatchPhrase` query using the `matchPhrase` method this way:
```scala
val query: MatchPhraseQuery = matchPhrase(field = "stringField", value = "test")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `MatchPhrase` query using the `matchPhrase` method this way:
```scala
val query: MatchPhraseQuery = matchPhrase(field = Document.stringField, value = "test")
```

You can find more information about `MatchPhrase` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-match-query-phrase.html).
