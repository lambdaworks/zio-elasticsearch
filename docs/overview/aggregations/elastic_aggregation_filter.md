---
id: elastic_aggregation_filter
title: "Filter Aggregation"
---

The `Filter` aggregation is a single bucket aggregation that narrows down the entire set of documents to a specific set that matches a [query](https://lambdaworks.github.io/zio-elasticsearch/overview/elastic_query).

In order to use the `Filter` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.FilterAggregation
import zio.elasticsearch.ElasticAggregation.filterAggregation
```

You can create a `Filter` aggregation using the `filterAggregation` method in the following manner:
```scala
import zio.elasticsearch.ElasticQuery.term

val aggregation: FilterAggregation = filterAggregation(name = "filterAggregation", query = term(field = Document.stringField, value = "test"))
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
import zio.elasticsearch.ElasticQuery.term

val multipleAggregations: MultipleAggregations = filterAggregation(name = "filterAggregation", query = term(field = Document.stringField, value = "test")).withAgg(maxAggregation(name = "maxAggregation", field = Document.doubleField))
```

If you want to add another sub-aggregation, you can use `withSubAgg` method:
```scala
import zio.elasticsearch.ElasticQuery.term
import zio.elasticsearch.ElasticAggregation.maxAggregation

val aggregationWithSubAgg: FilterAggregation = filterAggregation(name = "filterAggregation", query = term(field = Document.stringField, value = "test")).withSubAgg(maxAggregation(name = "maxAggregation", field = Document.intField))
```

You can find more information about `Filter` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-filter-aggregation.html).
