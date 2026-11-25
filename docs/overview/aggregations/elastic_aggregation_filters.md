---
id: elastic_aggregation_filters
title: "Filters Aggregation"
---

The `Filters` aggregation is a multi-bucket aggregation that creates a bucket for each of the given [queries](https://lambdaworks.github.io/zio-elasticsearch/overview/elastic_query), with the documents that match it.

In order to use the `Filters` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.FiltersAggregation
import zio.elasticsearch.ElasticAggregation.filtersAggregation
```

Filters can be named, in which case each bucket of the result has the name of its filter as `key`:
```scala
import zio.elasticsearch.ElasticQuery.term

val aggregation: FiltersAggregation =
  filtersAggregation(
    name = "filtersAggregation",
    filter = "errors" -> term(field = Document.stringField.keyword, value = "error"),
    filters = "warnings" -> term(field = Document.stringField.keyword, value = "warning")
  )
```

Filters can also be anonymous, in which case the buckets of the result are in the order of the filters and have no key:
```scala
val aggregation: FiltersAggregation =
  filtersAggregation(
    name = "filtersAggregation",
    query = term(field = Document.stringField.keyword, value = "error"),
    queries = term(field = Document.stringField.keyword, value = "warning")
  )
```

If you want an additional bucket for the documents that do not match any of the filters, you can use `otherBucket` method. For named filters, the key of that bucket is `_other_`, and you can change it with `otherBucketKey` method:
```scala
val aggregationWithOtherBucket: FiltersAggregation = filtersAggregation(name = "filtersAggregation", filter = "errors" -> term(field = Document.stringField.keyword, value = "error")).otherBucket
val aggregationWithOtherBucketKey: FiltersAggregation = filtersAggregation(name = "filtersAggregation", filter = "errors" -> term(field = Document.stringField.keyword, value = "error")).otherBucketKey("other")
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = filtersAggregation(name = "filtersAggregation", filter = "errors" -> term(field = Document.stringField.keyword, value = "error")).withAgg(maxAggregation(name = "maxAggregation", field = Document.intField))
```

If you want to add another sub-aggregation, you can use `withSubAgg` method:
```scala
val aggregationWithSubAgg: FiltersAggregation = filtersAggregation(name = "filtersAggregation", filter = "errors" -> term(field = Document.stringField.keyword, value = "error")).withSubAgg(maxAggregation(name = "maxAggregation", field = Document.intField))
```

If you want to add custom metadata, which Elasticsearch returns together with the aggregation result, you can use `meta` method:
```scala
val aggregationWithMeta: FiltersAggregation = filtersAggregation(name = "filtersAggregation", filter = "errors" -> term(field = Document.stringField.keyword, value = "error")).meta("color", "blue")
```

The result is retrieved with the `asFiltersAggregation` accessor. Its `buckets` are a `Chunk` in which each bucket has a `key` (`Some` name for named filters and `None` for anonymous ones) and a `docCount`. For named filters, `bucketsByKey` gives the buckets as a `Map` by name.

You can find more information about `Filters` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-bucket-filters-aggregation.html).
