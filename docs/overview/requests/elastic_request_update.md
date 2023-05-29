---
id: elastic_request_update
title: "Update Request"
---

This request is used for updating a document either with script or with other document as parameter.

In order to use the `Update` request import the following:
```scala
import zio.elasticsearch.ElasticRequest.UpdateRequest
import zio.elasticsearch.ElasticRequest._
```

You can create a `Update` request using the `update` method with specified document this way:
```scala
// this import is required for using `IndexName` and `DocumentId`
import zio.elasticsearch._
import zio.schema.Schema

// example of document
final case class User(id: String, username: String)

val user: User = User(id = "1", username = "johndoe")

implicit val schema: Schema.CaseClass2[String, String, User] = DeriveSchema.gen[GitHubRepo]

val request: UpdateRequest = update(index = IndexName("index"), id = DocumentId("documentId"), doc = user)
```

You can create a `Update` request using the `updateByScript` method with specified script this way:
```scala
import zio.elasticsearch._
import zio.elasticsearch.script.Script

val request: UpdateRequest = updateByScript(index = IndexName("index"), id = DocumentId("documentId"), script = Script("ctx._source.intField += params['factor']").params("factor" -> 2))
```

If you want to change the `upsert`, you can use the `orCreate` method:
```scala
val newUser: User = User(id = "2", username = "janedoe")

val requestWithUpsert: UpdateRequest = update(index = IndexName("index"), id = DocumentId("documentId"), doc = user).orCreate(newUser)
```

If you want to change the `refresh`, you can use `refresh`, `refreshFalse` or `refreshTrue` method:
```scala
val requestWithRefresh: UpdateRequest = update(index = IndexName("index"), id = DocumentId("documentId"), doc = user).refresh(true)
val requestWithRefreshFalse: UpdateRequest = update(index = IndexName("index"), id = DocumentId("documentId"), doc = user).refreshFalse
val requestWithRefreshTrue: UpdateRequest = update(index = IndexName("index"), id = DocumentId("documentId"), doc = user).refreshTrue
```

If you want to change the `routing`, you can use the `routing` method:
```scala
// this import is required for using `Routing` also
import zio.elasticsearch._

val requestWithRouting: UpdateRequest = update(index = IndexName("index"), id = DocumentId("documentId"), doc = user).routing(Routing("routing"))
```

You can find more information about `Update` request [here](https://www.elastic.co/guide/en/elasticsearch/reference/7.17/docs-update.html).
