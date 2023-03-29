---
id: overview_elastic_request
title: "Elastic Request"
---

We can represent an Elasticsearch request as a generic data type that returns a value of type `A`. 
The library offers a DSL for creating these requests, by specifying their required parameters. 
For example, we can create a request for deleting a document with a specified index as follows:

```scala
deleteById(IndexName("index"), DocumentId("documentId"))
```

As you can see above, index names and document IDs are represented with `IndexName` and `DocumentId` respectively,
using new types from ZIO Prelude, in order to increase type-safety with no runtime overhead. 
`IndexName` also validates the passed string according to Elasticsearch's naming criteria at compile-time using the `apply` method,
or with `make` at runtime when dealing with a runtime value as an argument.

All the DSL methods for request creation can be brought into scope with the following import:

```scala
import zio.elasticsearch.ElasticRequest._
```

For methods receiving or returning a document of custom type `A`, you must create a schema for `A`. Here is an example of creating a schema for a custom type `EmployeeDocument`:

```scala
import zio.schema.{DeriveSchema, Schema}

final case class EmployeeDocument(id: String, name: String, degree: String, age: Int)

object EmployeeDocument {
  implicit val schema: Schema[EmployeeDocument] = DeriveSchema.gen[EmployeeDocument]
}
```

As long as we have the implicit schema value in scope, we can call the aforementioned methods, such as `getById`:

```scala
import EmployeeDocument._

getById[EmployeeDocument](IndexName("index"), DocumentId("documentId"))
```
