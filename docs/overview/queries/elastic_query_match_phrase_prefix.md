---
id: elastic_query_match_phrase_prefix
title: "Match Phrase Prefix Query"
---

The `MatchPhrasePrefix` query returns documents that contain the words of a provided text, in the same order as provided. 
The last term of the provided text is treated as a prefix, matching any words that begin with that term.

In order to use the `MatchPhrasePrefix` query import the following:
```scala
import zio.elasticsearch.query.MatchPhrasePrefixQuery
import zio.elasticsearch.ElasticQuery._
```

You can create a `MatchPhrasePrefix` query using the `matchPhrasePrefix` method this way:
```scala
val query: MatchPhrasePrefixQuery = matchPhrasePrefix(field = "stringField", value = "test")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `MatchPhrasePrefix` query using the `matchPhrasePrefix` method this way:
```scala
val query: MatchPhrasePrefixQuery = matchPhrasePrefix(field = Document.stringField, value = "test")
```

You can find more information about `MatchPhrasePrefix` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-match-query-phrase-prefix.html).

