---
id: overview_bulkable
title: "Bulkable"
---

Elastic Requests like `Create`, `CreateOrUpdate`, `CreateWithId`, `DeleteById` are bulkable requests. For bulkable request you can use `bulk` API that accepts request types that inherit `Bulkable` trait.

```scala
for {
  _ <- Elasticsearch.execute(bulk(requests: _*)) 
} yield ()
```
