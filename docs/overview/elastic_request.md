---
id: elastic_request
title: "Overview"
---

We can represent an Elasticsearch request as a generic data type `ElasticRequest[A]`, where `A` represents the result of the executed request. 
The library offers a DSL for creating these requests, by specifying their required parameters.

For example, we can create a request for deleting a document with a specified index as follows:

```scala
ElasticRequest.deleteById(IndexName("index"), DocumentId("documentId"))
```

As you can see above, index names and document IDs are represented with `IndexName` and `DocumentId` respectively,
using Newtypes from ZIO Prelude, to increase type-safety with no runtime overhead.

Elastic requests for creating and deleting return `CreationOutcome` and `DeletionOutcome` respectively, notifying us of the outcome of the request.

```scala
import zio._
import zio.elasticsearch._
import zio.elasticsearch.ElasticRequest._

val createIndexWithoutMappingResult: RIO[Elasticsearch, CreationOutcome] =
  Elasticsearch.execute(createIndex(IndexName("index")))
        
val createIndexWithMappingResult: RIO[Elasticsearch, CreationOutcome] =
  Elasticsearch.execute(createIndex(IndexName("index"), "..."))
      
val deleteIndexResult: RIO[Elasticsearch, DeletionOutcome] =
  Elasticsearch.execute(deleteIndex(IndexName("index")))
```

All the DSL methods for request creation can be brought into scope with the following import:

```scala
import zio.elasticsearch.ElasticRequest._
```

Executing the `getById` request returns the following effect, where `GetResult` represents a successful value:

```scala
import zio._
import zio.elasticsearch._
import zio.elasticsearch.ElasticRequest._
import zio.elasticsearch.result.{DecodingException, GetResult}

val getByIdResult: RIO[Elasticsearch, GetResult] =
  Elasticsearch.execute(getById(IndexName("index"), DocumentId("documentId")))
```

To return a document of custom type `A`, you must create a schema for `A`. Here is an example of creating a schema for a custom type `User`:

```scala
import zio.schema.{DeriveSchema, Schema}

final case class User(id: String, name: String, email: String, age: Int)

object User {
  implicit val schema: Schema[User] = DeriveSchema.gen[User]
}
```

As long as we have the implicit schema value in scope, we can transform `GetResult` to a desired type `A`:

```scala
val getUserByIdResult: RIO[Elasticsearch, IO[DecodingException, Option[User]]] =
  Elasticsearch.execute(getById(IndexName("index"), DocumentId("documentId")))
    .map(_.documentAs[User])
```
