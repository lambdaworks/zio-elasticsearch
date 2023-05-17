---
id: elastic_query_match_phrase
title: "Match Phrase Query"
---

The Match phrase query analyzes the text and creates a `phrase` query out of the analyzed text.

In order to use the `MatchPhrase` query import following:
```scala
import zio.elasticsearch.query.MatchPhraseQuery
import zio.elasticsearch.ElasticQuery._
```

To create a type-safe instance of `MatchPhraseQuery`:
```scala
val query: MatchPhraseQuery = matchPhrase(field = Document.stringField, value = "test")
```

To create an instance of `MatchPhraseQuery`:
```scala
val query: MatchPhraseQuery = matchPhrase(field = "stringField", value = "test")
```

You can find more information about Match phrase query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-match-query-phrase.html).
