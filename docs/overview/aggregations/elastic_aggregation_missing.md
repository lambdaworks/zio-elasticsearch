---
id: elastic_aggregation_missing
title: "Missing Aggregation"
---

The `Missing` aggregation is a field data based single bucket aggregation, that creates a bucket of all documents in the current document set context that are missing a field value.

In order to use the `Missing` aggregation import the following:
```scala
import zio.elasticsearch.aggregation.MissingAggregation
import zio.elasticsearch.ElasticAggregation.missingAggregation
```

You can create a `Missing` aggregation using the `missingAggregation` method this way:
```scala
val aggregation: MissingAggregation = missingAggregation(name = "missingAggregation", field = "stringField")
```

You can create a [type-safe](https://lambdaworks.github.io/zio-elasticsearch/overview/overview_zio_prelude_schema) `Missing` aggregation using the `missingAggregation` method this way:
```scala
// Document.stringField must be string value, because of Missing aggregation
val aggregation: MissingAggregation = missingAggregation(name = "missingAggregation", field = Document.stringField)
```

If you want to add aggregation (on the same level), you can use `withAgg` method:
```scala
val multipleAggregations: MultipleAggregations = missingAggregation(name = "missingAggregation1", field = Document.stringField).withAgg(missingAggregation(name = "missingAggregation2", field = Document.stringField))
```

You can find more information about `Missing` aggregation [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-aggregations-bucket-missing-aggregation.html).
