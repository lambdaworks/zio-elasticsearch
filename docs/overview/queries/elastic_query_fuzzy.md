---
id: elastic_query_fuzzy
title: "Fuzzy Query"
---

The `Fuzzy` query returns documents that contain terms similar to the search term, as measured by a [Levenshtein edit distance](https://en.wikipedia.org/wiki/Levenshtein_distance).

In order to use the `Fuzzy` query import the following:
```scala
import zio.elasticsearch.query.{Fuzziness, FuzzyQuery}
import zio.elasticsearch.ElasticQuery._
```

You can create a `Fuzzy` query using the `fuzzy` method this way:
```scala
val query: FuzzyQuery = fuzzy(field = "name", value = "test")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Fuzzy` query using the `fuzzy` method this way:
```scala
val query: FuzzyQuery = fuzzy(field = Document.name, value = "test")
```

If you want to change the `fuzziness`, you can use `fuzziness` method, which accepts a value of type `Fuzziness` (`Fuzziness.Auto`, `Fuzziness.AutoLength`, or `Fuzziness.EditDistance`):
```scala
val queryWithFuzzinessAuto: FuzzyQuery = fuzzy(field = Document.name, value = "test").fuzziness(Fuzziness.Auto)
val queryWithFuzzinessAutoLength: FuzzyQuery =
  fuzzy(field = Document.name, value = "test").fuzziness(Fuzziness.AutoLength(lowLength = 3, highLength = 6))
val queryWithFuzzinessEditDistance: FuzzyQuery =
  fuzzy(field = Document.name, value = "test").fuzziness(Fuzziness.EditDistance.Two)
```

If you want to change the `maxExpansions`, you can use `maxExpansions` method:
```scala
val queryWithMaxExpansions: FuzzyQuery = fuzzy(field = Document.name, value = "test").maxExpansions(50)
```

If you want to change the `prefixLength`, you can use `prefixLength` method:
```scala
val queryWithPrefixLength: FuzzyQuery = fuzzy(field = Document.name, value = "test").prefixLength(3)
```

You can find more information about `Fuzzy` query [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-fuzzy-query.html).

