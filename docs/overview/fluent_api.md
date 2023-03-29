---
id: overview_fluent_api
title: "Fluent API"
---

Both Elastic requests and queries offer a fluent API, so that we could provide optional parameters in chained method calls for each request or query.

If you are creating `Bool` query that can possibly contain `must`, `mustNot`, `should` and `filter queries`, you can just use method from `ElasticQuery` object to crate any of them and then just fluently chain any other to the original one.

```scala
must(range("version").gte(7).lt(10)).should(startsWith("name", "ZIO"))
```

And if we wanted to specify lower and upper bounds for a `range` query:

```scala
range(EmployeeDocument.age).gte(18).lt(100)
```

Fluent API is also supported for parameters like `routing` and `refresh`, for example, if we wanted to add routing and refresh parameters to a `deleteById` request:
Methods `refreshTrue` and `refresh` false are just shortcut for using `.refresh(true)` or `.refresh(false)`.

```scala
deleteById(IndexName("index"), DocumentId("documentId")).routing(Routing("routing")).refreshTrue
```

When creating aggregations we can also use `withAgg` method to add another aggregation and return `MultipleAggregations` that contains both aggregations.

```scala
termsAggregation(name = "firstAggregation", field = "day_of_week")
            .withAgg(termsAggregation(name = "secondAggregation", field = "customer_age"))
```

Creating `sort` also supports fluent API, as it is shown in code below 

```scala
sortBy("day_of_week").mode(Avg)

sortBy("day_of_week").missing(First)

sortBy("day_of_week").format("strict_date_optional_time_nanos")
```
