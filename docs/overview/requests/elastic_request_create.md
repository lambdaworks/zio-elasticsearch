---
id: elastic_request_create
title: "Create Request, CreateWithId Request and CreateOrUpdate Request"
---

The `Create`, the `CreateWithId` and the `CreateOrUpdate` requests add a JSON document to the specified data stream and make it searchable.

There are three ways of adding documents to the Elasticsearch index:
1. By using `Create` request - creates a JSON document without specifying ID (Elasticsearch creates one)
2. By using `CreateWithId` request - creates a JSON document with specified ID
3. By using `CreateOrUpdate` request - creates JSON document with specified ID, or updates the document (if it already exists)

In order to use the `Create` request import the following:
```scala
import zio.elasticsearch.ElasticRequest.CreateRequest
import zio.elasticsearch.ElasticRequest.create
```

In order to use the `CreateWithId` request import the following:
```scala
import zio.elasticsearch.ElasticRequest.CreateWithIdRequest
import zio.elasticsearch.ElasticRequest.create
```

In order to use the `CreateOrUpdate` request import the following:
```scala
import zio.elasticsearch.ElasticRequest.CreateOrUpdateRequest
import zio.elasticsearch.ElasticRequest.upsert
```

Except imports, you must specify a document you want to create, with its implicit schema.
```scala
import zio.schema.Schema
// example of document
final case class User(id: String, username: String)

val user: User = User(id = "1", username = "johndoe")

implicit val schema: Schema.CaseClass2[String, String, User] = DeriveSchema.gen[GitHubRepo]
```

You can create a `Create` request using the `create` method this way:
```scala
// this import is required for using `IndexName`
import zio.elasticsearch._

val request: CreateRequest = create(index = IndexName("index"), doc = user)
```

You can create a `CreateWithId` request using the `create` method this way:
```scala
// this import is required for using `DocumentId` also
import zio.elasticsearch._ 

val request: CreateWithIdRequest = create(index = IndexName("index"), id = DocumentId("documentId"), doc = user)
```

You can create a `CreateOrUpdate` request using the `upsert` method this way:
```scala
import zio.elasticsearch._ 

val request: CreateOrUpdateRequest = upsert(index = IndexName("index"), id = DocumentId("documentId"), doc = user)
```

If you want to change the `refresh`, you can use `refresh`, `refreshFalse` or `refreshTrue` method on any of previously mentioned requests:
```scala
val requestWithRefresh: CreateRequest = create(index = IndexName("index"), doc = user).refresh(true)
val requestWithRefreshFalse: CreateWithIdRequest = create(index = IndexName("index"), id = DocumentId("documentId"), doc = user).refreshFalse
val requestWithRefreshTrue: CreateOrUpdateRequest = upsert(index = IndexName("index"), id = DocumentId("documentId"), doc = user).refreshTrue
```

If you want to change the `routing`, you can use the `routing` method on any of previously mentioned requests:
```scala
// this import is required for using `Routing` also
import zio.elasticsearch._

val request1WithRouting: CreateRequest = create(index = IndexName("index"), doc = user).routing(Routing("routing"))
val request2WithRouting: CreateWithIdRequest = create(index = IndexName("index"), id = DocumentId("documentId"), doc = user).routing(Routing("routing"))
val request3WithRouting: CreateOrUpdateRequest = upsert(index = IndexName("index"), id = DocumentId("documentId"), doc = user).routing(Routing("routing"))
```

You can find more information about `Create`, `CreateWithId`, `CreateOrUpdate` requests [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/docs-index_.html).
