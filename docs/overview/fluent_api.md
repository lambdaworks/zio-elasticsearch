---
id: overview_fluent_api
title: "Fluent API"
---

Both Elastic requests and queries offer a fluent API, so that you can provide optional parameters in chained method calls for each request or query. For example, if we wanted to add routing and refresh parameters to a `deleteById` request:

```scala
deleteById(IndexName("index"), DocumentId("documentId")).routing(Routing("routing")).refreshTrue

must(range("version").gte(7).lt(10)).should(startsWith("name", "ZIO"))
```

And if we wanted to specify lower and upper bounds for a `range` query:

```scala
range(EmployeeDocument.age).gte(18).lt(100)
```
