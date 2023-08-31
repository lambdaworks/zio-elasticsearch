---
id: elastic_request_search
title: "Search Request"
---

The `Search` request allows you to execute a search query (and aggregation) and get back search hits that match the query.

There are two ways of executing a search query: 
1. By using `Search` request
2. By using `SearchAndAggregate` request

To create a `Search` request do the following:
```scala
import zio.elasticsearch.ElasticRequest.SearchRequest
import zio.elasticsearch.ElasticRequest.search
// this import is required for using `IndexName`
import zio.elasticsearch._
import zio.elasticsearch.ElasticQuery._

val request: SearchRequest = search(index = IndexName("index"), query = matchAll)
```

To create a `SearchAndAggregate` request do the following:
```scala
import zio.elasticsearch.ElasticRequest.SearchAndAggregateRequest
import zio.elasticsearch.ElasticRequest.search
import zio.elasticsearch._
import zio.elasticsearch.ElasticQuery._
import zio.elasticsearch.ElasticAggregation._

val request: SearchAndAggregateRequest = search(index = IndexName("index"), query = matchAll, aggregation = maxAggregation(name = "aggregation", field = "intField"))
```

If you want to add aggregation to `SearchRequest`, you can use the `aggregate` method on it:
```scala
import zio.elasticsearch.ElasticAggregation._

val requestWithAggregation: SearchAndAggregateRequest = search(index = IndexName("index"), query = matchAll).aggregate(aggregation = maxAggregation(name = "aggregation", field = "intField"))
```

If you want to change the `excludes`, you can use the `excludes` method on both requests:
```scala
val request1WithExcludes: SearchRequest = search(index = IndexName("index"), query = matchAll).excludes("longField")
val request2WithExcludes: SearchAndAggregateRequest = search(index = IndexName("index"), query = matchAll, aggregation = maxAggregation(name = "aggregation", field = "intField")).excludes("longField", "intField")
// type-safe fields:
val request1TsWithExcludes: SearchRequest = search(index = IndexName("index"), query = matchAll).excludes(Document.longField)
val request2TsWithExcludes: SearchAndAggregateRequest = search(index = IndexName("index"), query = matchAll, aggregation = maxAggregation(name = "aggregation", field = "intField")).excludes(Document.longField, Document.intField)
```

If you want to change the `from`, you can use the `from` method on both requests:
```scala
val request1WithFrom: SearchRequest = search(index = IndexName("index"), query = matchAll).from(2)
val request2WithFrom: SearchAndAggregateRequest = search(index = IndexName("index"), query = matchAll, aggregation = maxAggregation(name = "aggregation", field = "intField")).from(2)
```

If you want to change the `highlight`, you can use the `highlights` method on both requests:
```scala
import zio.elasticsearch.ElasticHighlight.highlight

val request1WithHighlights: SearchRequest = search(index = IndexName("index"), query = matchAll).highlights("intField")
val request2WithHighlights: SearchAndAggregateRequest = search(index = IndexName("index"), query = matchAll, aggregation = maxAggregation(name = "aggregation", field = "intField")).highlights(Document.intField)
```

If you want to change the `includes`, you can use the `includes` method on both requests:
```scala
val request1WithIncludes: SearchRequest = search(index = IndexName("index"), query = matchAll).includes("longField")
val request2WithIncludes: SearchAndAggregateRequest = search(index = IndexName("index"), query = matchAll, aggregation = maxAggregation(name = "aggregation", field = "intField")).includes("longField", "intField")
// type-safe fields:
val request1TsWithIncludes: SearchRequest = search(index = IndexName("index"), query = matchAll).includes(Document.longField)
val request2TsWithIncludes: SearchAndAggregateRequest = search(index = IndexName("index"), query = matchAll, aggregation = maxAggregation(name = "aggregation", field = "intField")).includes(Document.longField, Document.intField)
// with schema
val request1WithIncludesSchema: SearchRequest = search(index = IndexName("index"), query = matchAll).includes[Document]
val request2WithIncludesSchema: SearchAndAggregateRequest = search(index = IndexName("index"), query = matchAll, aggregation = maxAggregation(name = "aggregation", field = "intField")).includes[Document]
```

If you want to change the `routing`, you can use the `routing` method on both requests:
```scala
// this import is required for using `Routing` also
import zio.elasticsearch._

val request1WithRouting: SearchRequest = search(index = IndexName("index"), query = matchAll).routing(Routing("routing"))
val request2WithRouting: SearchAndAggregateRequest = search(index = IndexName("index"), query = matchAll, aggregation = maxAggregation(name = "aggregation", field = "intField")).routing(Routing("routing"))
```

If you want to change the `search_after`, you can use the `searchAfter` method on both requests:
```scala
import zio.json.ast.Json.{Arr, Str}

val request1WithSearchAfter: SearchRequest = search(index = IndexName("index"), query = matchAll).searchAfter(Arr(Str("12345")))
val request2WithSearchAfter: SearchAndAggregateRequest = search(index = IndexName("index"), query = matchAll, aggregation = maxAggregation(name = "aggregation", field = "intField")).searchAfter(Arr(Str("12345")))
```

If you want to change the `size`, you can use the `size` method on both requests:
```scala
val request1WithSize: SearchRequest = search(index = IndexName("index"), query = matchAll).size(5)
val request2WithSize: SearchAndAggregateRequest = search(index = IndexName("index"), query = matchAll, aggregation = maxAggregation(name = "aggregation", field = "intField")).size(5)
```

If you want to change the `sort`, you can use the `sort` method on both requests:
```scala
import zio.elasticsearch.ElasticSort.sortBy
import zio.elasticsearch.query.sort.SortOrder.Asc
import zio.elasticsearch.query.sort.Missing.First

val request1WithSort: SearchRequest = search(index = IndexName("index"), query = matchAll).sort(sortBy(Document.intField).order(Asc))
val request2WithSort: SearchAndAggregateRequest = search(index = IndexName("index"), query = matchAll, aggregation = maxAggregation(name = "aggregation", field = "intField")).sort(sortBy("intField").missing(First))
```

If you want to create `Search` request with `IndexPattern`, do the following:
```scala
val requestWithIndexPattern: SearchRequest = search(index = IndexPattern("index*"), query = matchAll)
```

You can find more information about `Search` and `SearchAndAggregate` requests [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-search.html).
