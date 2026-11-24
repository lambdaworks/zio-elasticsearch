---
id: elastic_aggregation_terms
title: "Terms Aggregation"
---

This aggregation is a multi-bucket value source based aggregation where buckets are dynamically built - one per unique value.

In order to use the `Terms` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.TermsAggregation
import zio.elasticsearch.ElasticAggregation.termsAggregation
```

You can create a `Terms` aggregation using the `termsAggregation` method this way:
```scala
val aggregation: TermsAggregation = termsAggregation(name = "termsAggregation", field = "stringField.keyword")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Terms` aggregation using the `termsAggregation` method this way:
```scala
// Document.stringField can be of any type that has an ElasticPrimitive instance (e.g. String, Int, Boolean or LocalDate)
val aggregation: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField.keyword)
```

If you want to change the `order`, you can use `orderBy`, `orderByCountAsc`, `orderByCountDesc`, `orderByKeyAsc` or `orderByKeyDesc` method:
```scala
import zio.elasticsearch.aggregation.AggregationOrder
import zio.elasticsearch.query.sort.SortOrder.Asc

val aggregationWithOrder1: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField).orderBy(AggregationOrder("otherAggregation", Asc))
val aggregationWithOrder2: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField).orderByCountAsc
val aggregationWithOrder3: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField).orderByCountDesc
val aggregationWithOrder4: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField).orderByKeyAsc
val aggregationWithOrder5: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField).orderByKeyDesc
```

If you want to change the `size`, you can use `size` method:
```scala
val aggregationWithSize: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField).size(5)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = termsAggregation(name = "termsAggregation", field = Document.stringField).withAgg(maxAggregation(name = "maxAggregation", field = Document.intField))
```

If you want to add another sub-aggregation, you can use `withSubAgg` method:
```scala
val aggregationWithSubAgg: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField).withSubAgg(maxAggregation(name = "maxAggregation", field = Document.intField))
```

If you want to return only buckets with a minimum number of documents, you can use `minDocCount` method:
```scala
val aggregationWithMinDocCount: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField).minDocCount(2)
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: TermsAggregation = termsAggregation(name = "termsAggregation", field = Document.stringField).meta("color", "blue")
```

You can find more information about `Terms` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-bucket-terms-aggregation.html#search-aggregations-bucket-terms-aggregation).
