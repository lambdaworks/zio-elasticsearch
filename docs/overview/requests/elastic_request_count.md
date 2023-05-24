---
id: elastic_request_count
title: "Count Request"
---

The `Count` request is used for getting the number of matches for a search query. If no query is specified, `matchAll` query will be used to count all the documents.

In order to use the `Count` request import the following:
```scala
import zio.elasticsearch.ElasticRequest.CountRequest
import zio.elasticsearch.ElasticRequest.count
```

You can create a `Count` request using the `count` method without specified query this way:
```scala
// this import is required for using `IndexName`
import zio.elasticsearch._

val request: CountRequest = count(index = IndexName("index"))
```

You can create a `Count` request using the `count` method with specified query this way:
```scala
val request: CountRequest = count(index = IndexName("index"), query = contains(field = Document.name, value = "test"))
```

If you want to change the `routing`, you can use the `routing` method:
```scala
// this import is required for using `Routing` also
import zio.elasticsearch._

val requestWithRouting: CountRequest = count(index = Index("index")).routing(Routing("routing"))
```

You can find more information about `Count` request [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/search-count.html).
