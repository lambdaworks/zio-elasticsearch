---
id: overview_fluent_api
title: "Fluent API"
---

Both Elastic requests and queries offer a fluent API so that we could provide optional parameters in chained method calls for each request or query.
If you are creating a `Bool` query that can possibly contain `must`, `mustNot`, `should`, and `filter` queries, you can just use one of the methods from the `ElasticQuery` object to create any of them and then just fluently chain any other to the original one.

```scala
ElasticQuery.must(ElasticQuery.range("version").gte(7).lt(10)).should(ElasticQuery.startsWith("name", "ZIO"))
```

And if we wanted to specify lower and upper bounds for a `range` query:

```scala
ElasticQuery.range(User.age).gte(18).lt(100)
```

Fluent API is also supported for parameters like `routing` and `refresh`, for example, if we wanted to add routing and refresh parameters to a `deleteById` request:
Methods `refreshTrue` and `refreshFalse` are just shortcuts for using `refresh(true)` or `refresh(false)`.

```scala
ElasticRequest.deleteById(IndexName("index"), DocumentId("documentId")).routing(Routing("routing")).refreshTrue
```

When creating aggregations we can also use `withAgg` method to add another aggregation and return the `MultipleAggregations` type that contains both aggregations.

```scala
ElasticAggregation.termsAggregation(name = "firstAggregation", field = "name")
  .withAgg(ElasticAggregation.termsAggregation(name = "secondAggregation", field = "age"))
```

Creating `sort` also supports fluent API, as it is shown in the code below:

```scala
ElasticSort.sortBy("age").mode(SortMode.Avg)
ElasticSort.sortBy("first_name").missing(Missing.First)
ElasticSort.sortBy("created_at").format("strict_date_optional_time_nanos")
```
