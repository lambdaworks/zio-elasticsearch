---
id: elastic_request_multi_search
title: "Multi Search Request"
---

The `MultiSearch` request executes several search and aggregation requests in a single request.

To create a `MultiSearch` request, pass any combination of `Search`, `SearchAndAggregate` and `Aggregate` requests. Each of them keeps its own index, query, sort, aggregations and other parameters:
```scala
import zio.elasticsearch.ElasticAggregation.maxAggregation
import zio.elasticsearch.ElasticQuery.matchAll
import zio.elasticsearch.ElasticRequest.{MultiSearchRequest, aggregate, multiSearch, search}
import zio.elasticsearch.ElasticSort.sortBy
// this import is required for using `IndexName`
import zio.elasticsearch._

val request: MultiSearchRequest =
  multiSearch(
    search(selectors = IndexName("index1"), query = matchAll).sort(sortBy(User.age)).size(5),
    search(selectors = IndexName("index2"), query = matchAll, aggregation = maxAggregation(name = "maxAge", field = User.age)),
    aggregate(selectors = IndexName("index3"), aggregation = maxAggregation(name = "maxAge", field = User.age))
  )
```

If you want to limit the number of searches executed concurrently, you can use the `maxConcurrentSearches` method:
```scala
val requestWithMaxConcurrentSearches: MultiSearchRequest =
  multiSearch(search(selectors = IndexName("index"), query = matchAll)).maxConcurrentSearches(2)
```

Executing the `MultiSearch` request returns a `MultiSearchResult`:
```scala
import zio._
import zio.elasticsearch.result.{MultiSearchItem, MultiSearchResult, SearchAndAggregateResult}

val result: RIO[Elasticsearch, MultiSearchResult] = Elasticsearch.execute(request)

val users: RIO[Elasticsearch, Chunk[Chunk[User]]] = result.documentAs[User]
val results: RIO[Elasticsearch, Chunk[SearchAndAggregateResult]] = result.flatMap(_.results)
val items: RIO[Elasticsearch, Chunk[MultiSearchItem]] = result.flatMap(_.items)
val failures: RIO[Elasticsearch, Chunk[MultiSearchItem.Failed]] = result.flatMap(_.failures)
```

`results` returns one `SearchAndAggregateResult` per requested search, in the order the searches were requested, which gives you access to the documents, aggregations and total hits of each search. `documentAs` returns the documents found by each search, also in the order the searches were requested. Both fail with an `ElasticException` if any search failed (for example, because its index does not exist), and `documentAs` fails with a `DecodingException` listing, by search position, every document that cannot be decoded.

A failing search does not fail the whole request. To handle failed searches without failing, use `items`, which returns every requested search, in the order it was requested, as a `MultiSearchItem` that is either:
- `Succeeded`, with its HTTP `status` and `result`,
- `Failed`, with its HTTP `status` and the `error` returned by Elasticsearch, or
- `Undecodable`, with its HTTP `status`, the raw `response` and a `message` explaining why the response could not be parsed.

`failures` returns only the `Failed` items.

You can find more information about `MultiSearch` request [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-multi-search.html).
