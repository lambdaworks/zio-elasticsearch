---
id: overview_index
title: "Summary"
---

![scala-version][scala-version-badge]
[![CI](https://github.com/lambdaworks/zio-elasticsearch/actions/workflows/ci.yml/badge.svg)](https://github.com/lambdaworks/zio-elasticsearch/actions/workflows/ci.yml)
[![Sonatype Snapshots](https://img.shields.io/nexus/s/https/s01.oss.sonatype.org/io.lambdaworks/zio-elasticsearch_2.13.svg?label=Sonatype%20Snapshot)](https://s01.oss.sonatype.org/content/repositories/snapshots/io/lambdaworks/zio-elasticsearch_2.13/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# ZIO Elasticsearch

## Overview

ZIO Elasticsearch is a type-safe, testable and streaming-friendly ZIO native Elasticsearch client.

The library depends on sttp as an HTTP client for executing requests, and other ZIO libraries such as ZIO Schema and ZIO Prelude.

The following versions are supported:
- Scala: 2.12+
- ZIO: 2
- Elasticsearch: 7
- JVM 11+

## Installation

To use ZIO Elasticsearch in your project, add the following to your `build.sbt` file:

```scala
resolvers += "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "io.lambdaworks" %% "zio-elasticsearch" % "<snapshot version>"
```

Where `<snapshot version>` refers to the version in the Sonatype Snapshot badge above.

## Usage

In order to get the functional effect of executing a specified `Elasticsearch` request, we must provide the `Elasticsearch` layer to that effect.  We can then call the `execute`method defined in `Elasticsearch` that accepts `ElasticRequest` as parameter.
To create this layer we also have to provide following layers:

- `ElasticExecutor` - if you provide `ElasticExecutor.local` it will run on localhost:9200, otherwise if you want to use `ElasticExecutor.live` you will have to provide `ElasticConfig` as well
- `HttpClientZioBackend`

```scala
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.elasticsearch.ElasticRequest._
import zio.elasticsearch._
import zio._

object ZIOElasticsearchExample extends ZIOAppDefault {
  val indexName = IndexName("test-es-index")
  val effect: ZIO[Elasticsearch, Throwable, Unit] = for {
    _ <- Elasticsearch.execute(createIndex(indexName))
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    effect.provide(
      ElasticExecutor.local,
      Elasticsearch.layer,
      HttpClientZioBackend.layer()
    )
}
```


### Typesafety with ZIO-prelude's NewType

We use ZIO-prelude's NewType for `IndexName`, `DocumentId` and `Routing` in order to preserve type safety and have strings validated when these types are being created.

```scala
val indexName: IndexName = IndexName("test-es-index")
val docId: DocumentId = DocumentId("document-id")
```

### Fluent API

Both Elastic requests and queries offer a fluent API, so that you can provide optional parameters in chained method calls for each request or query. For example, if we wanted to add routing and refresh parameters to a `deleteById` request:

```scala
deleteById(IndexName("index"), DocumentId("documentId")).routing(Routing("routing")).refreshTrue

must(range("version").gte(7).lt(10)).should(startsWith("name", "ZIO"))
```

And if we wanted to specify lower and upper bounds for a `range` query:

```scala
range(EmployeeDocument.age).gte(18).lt(100)
```

### Bulkable

Elastic Requests like `Create`, `CreateOrUpdate`, `CreateWithId`, `DeleteById` are bulkable requests. For bulkable request you can use `bulk` API that accepts request types that inherit `Bulkable` trait.

```scala
for {
  _ <- Elasticsearch.execute(bulk(requests: _*)) 
} yield()
```

### Usage of ZIO Schema and its accessors for type safety

To provide type safety in your requests zio-elasticsearch uses ZIO Schema. Here is an example of creating schema for custom type `User` and using implicit schema to create accessors which results in type safe request and response.

```scala
final case class Address(street: String, number: Int)

object Address {

  implicit val schema: Schema.CaseClass2[String, Int, Address] =
    DeriveSchema.gen[Address]

  val (street, number) = schema.makeAccessors(FieldAccessorBuilder)
}

case class User(id: Int, address: Address)

object User {
  implicit val schema: Schema.CaseClass2[String, Address, User] =
    DeriveSchema.gen[User]

  val (id, address) = schema.makeAccessors(FieldAccessorBuilder)
}

for {
  _ <- Elasticsearch.execute(
    search(
      IndexName("index-name"),
      must(range(User.id).gte(7).lt(10)).should(startsWith(User.address.name, "ZIO"))
    ).aggregate(aggregation)
  )
} yield()
```

### Streaming

Zio-elastic search is streaming friendly library and there are few specific API's that are used for creating ZIO streams. When using `stream` the result will be `Item` that is case class that contains only one field and that is `raw` that represents your response as raw JSON. Also it is important to note that you can use `StramConfig` to use your own settings when creating a stream, if you omit using `StreamConfig` then `StreamConfig.Default` will be used.

```scala
for {
  stream <- Elasticsearch.stream(ElasticRequest.search(secondSearchIndex, range("id").gte(5)))
  stream <- Elasticsearch.stream(ElasticRequest.search(secondSearchIndex, range("id").gte(5)), StreamConfig.Scroll)
} yield()
```

```scala
for {
  stream <- Elasticsearch.streamAs[User](ElasticRequest.search(secondSearchIndex, range(User.id).gte(5)))
  stream <- Elasticsearch.streamAs[User](ElasticRequest.search(secondSearchIndex, range(User.id).gte(5)), StreamConfig.SearchAfter)
} yield()
```

## Example

For a full-fledged example using this library, you can check out the [example](modules/example) module, which contains an application with both a description and instructions on how to run it.

## Contributing

For the general guidelines, see ZIO Elasticsearch [contributor's guide](https://lambdaworks.github.io/zio-elasticsearch/about/about_contributing).

## Code of Conduct

See the [Code of Conduct](https://lambdaworks.github.io/zio-elasticsearch/about/about_code_of_conduct).

## License
[License](LICENSE)


[scala-version-badge]: https://img.shields.io/badge/scala-2.13.10-blue?logo=scala&color=red

