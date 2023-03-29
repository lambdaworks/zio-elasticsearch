---
id: overview_bulkable
title: "Bulkable"
---

If you want to use Elasticsearche's Bulk API you can do so using `bulk` method.
`Bulk` method accepts sequence of bulkable requests which are Elastic Requests that inherit `Bulkable` trait.
Bulk API for Elasticsearch supports only index,create, delete and update actions 
and for that reason you can use only `Create`, `CreateOrUpdate`, `CreateWithId`, `DeleteById` in your bulkable requests. 


```scala
case class User(id: Int, name: String)

object User {
  implicit val schema: Schema[User] = DeriveSchema.gen[User]
}

val requests = List(
  ElasticRequest.create[User](indexName, User(0, "Marc")),
  ElasticRequest.create[User](indexName, DocumentId("document-id-1"), User(1, "Luke")),
  ElasticRequest.upsert[User](indexName, DocumentId("document-id-2"), User(2, "Zack")),
  ElasticRequest.deleteById(indexName, DocumentId("document-id-0"))
)
for {
  _ <- Elasticsearch.execute(bulk(requests: _*)) 
} yield ()
```
