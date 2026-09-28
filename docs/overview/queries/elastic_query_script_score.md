---
id: elastic_query_script_score
title: "Script Score Query"
---

The `ScriptScore` query uses a script to compute a custom relevance score for the documents matched by a wrapped query.

In order to use the `ScriptScore` query import the following:
```scala
import zio.elasticsearch.query.ScriptScoreQuery
import zio.elasticsearch.ElasticQuery._
import zio.elasticsearch.script.Script
```

You can create a `ScriptScore` query with arbitrary query (`MatchAll` in this example) and a `Script` using the `scriptScore` method in the following manner:
```scala
val query: ScriptScoreQuery[Any] = scriptScore(matchAll, Script("doc['intField'].value * 2"))
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `ScriptScore` query with arbitrary [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) query using the `scriptScore` method in the following manner:
```scala
val query: ScriptScoreQuery[Document] =
  scriptScore(matches(Document.stringField, "test"), Script("doc['intField'].value * 2"))
```

If you want to change the `boost`, you can use `boost` method:
```scala
val queryWithBoost: ScriptScoreQuery[Document] =
  scriptScore(matches(Document.stringField, "test"), Script("doc['intField'].value * 2")).boost(2.0)
```

If you want to change the `minScore`, you can use `minScore` method:
```scala
val queryWithMinScore: ScriptScoreQuery[Document] =
  scriptScore(matches(Document.stringField, "test"), Script("doc['intField'].value * 2")).minScore(5.0)
```

You can find more information about `ScriptScore` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-script-score-query.html).
