---
id: elastic_query_constant_score
title: "Constant Score Query"
---

The `ConstantScore` query wraps a filter query and returns every matching document with a relevance score equal to the boost parameter value.

In order to use the `ConstantScore` query import the following:
```scala
import zio.elasticsearch.query.ConstantScoreQuery
import zio.elasticsearch.ElasticQuery._
```

You can create a `ConstantScore` query with arbitrary query(`MatchPhrase` in this example) using the `constantScore` method in the following manner:
```scala
val query: ConstantScoreQuery = constantScore(matchPhrase(field = "name", value = "test"))
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `ConstantScore` query with arbitrary [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) query(`MatchPhrase` in this example) using the `constantScore` method in the following manner:
```scala
val query: ConstantScoreQuery = constantScore(matchPhrase(field = Document.name, value = "test"))
```

If you want to change the `boost`, you can use `boost` method:
```scala
val queryWithBoost: ConstantScoreQuery = constantScore(matchPhrase(field = Document.name, value = "test")).boost(2.2)
```

You can find more information about `ConstantScore` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/query-dsl-constant-score-query.html).

