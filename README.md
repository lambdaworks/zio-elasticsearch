![scala-version][scala-version-badge]
[![CI](https://github.com/lambdaworks/zio-elasticsearch/actions/workflows/ci.yml/badge.svg)](https://github.com/lambdaworks/zio-elasticsearch/actions/workflows/ci.yml)
[![Sonatype Snapshots](https://img.shields.io/nexus/s/https/s01.oss.sonatype.org/io.lambdaworks/zio-elasticsearch_2.13.svg?label=Sonatype%20Snapshot)](https://s01.oss.sonatype.org/content/repositories/snapshots/io/lambdaworks/zio-elasticsearch_2.13/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# ZIO Elasticsearch

## Overview

ZIO Elasticsearch is a type-safe, testable and streaming-friendly ZIO native Elasticsearch client.

The library depends on sttp as an HTTP client for executing requests, and other ZIO libraries such as ZIO Schema and ZIO Prelude.

The following versions are supported:
- Scala: 2.12, 2.13 and 3
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

In order to execute an Elasticsearch request we can rely on the `Elasticsearch` layer which offers an `execute` method accepting an `ElasticRequest`. In order to build the `Elasticsearch` layer we need to provide the following layers:

- `ElasticExecutor`: if you provide `ElasticExecutor.local`, it will run on `localhost:9200`. Otherwise, if you want to use `ElasticExecutor.live`, you will need to provide `ElasticConfig` as well.
- `HttpClientZioBackend`

```scala
import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.elasticsearch.ElasticRequest._
import zio.elasticsearch._
import zio._

object ZIOElasticsearchExample extends ZIOAppDefault {
  val indexName = IndexName("index")
  val effect: RIO[Elasticsearch, Unit] = for {
    _ <- Elasticsearch.execute(createIndex(indexName))
  } yield ()

  override def run =
    effect.provide(
      ElasticExecutor.local,
      Elasticsearch.layer,
      HttpClientZioBackend.layer()
    )
}
```


### Type-safety with ZIO Prelude's new type

The library uses ZIO Prelude's new type for `IndexName`, `DocumentId` and `Routing` in order to preserve type-safety.

```scala
val indexName: IndexName = IndexName("index")
val docId: DocumentId    = DocumentId("documentId")
```

### Fluent API

Both Elastic requests and queries offer a fluent API, allowing us to provide optional parameters in chained method calls for each request or query.
For example, if we wanted to add routing and refresh parameters to a `deleteById` request:

```scala
deleteById(IndexName("index"), DocumentId("documentId")).routing(Routing("routing")).refreshTrue
```

Creating complex queries can be created in the following manner:

```scala
must(range("version").gte(7).lt(10)).should(startsWith("name", "ZIO"))
```

If we want to specify lower and upper bounds for a `range` query, we can do the following:

```scala
range(User.age).gte(18).lt(100)
```

### Bulkable

Elastic requests like `Create`, `CreateOrUpdate`, `CreateWithId`, and `DeleteById` are bulkable requests.
For bulkable request, you can use `bulk` API that accepts request types that inherit the `Bulkable` trait.

```scala
for {
  _ <- Elasticsearch.execute(bulk(requests: _*)) 
} yield ()
```

### Usage of ZIO Schema and its accessors for type-safety

To provide type-safety in your Elasticsearch requests, ZIO Elasticsearch uses ZIO Schema. Here is an example of creating a schema for the custom type `User` and using an implicit schema to create accessors that result in type-safe requests.

```scala
final case class Address(street: String, number: Int)

object Address {
  implicit val schema: Schema.CaseClass2[String, Int, Address] =
    DeriveSchema.gen[Address]

  val (street, number) = schema.makeAccessors(FieldAccessorBuilder)
}

final case class User(id: Int, address: Address)

object User {
  implicit val schema: Schema.CaseClass2[String, Address, User] =
    DeriveSchema.gen[User]

  val (id, address) = schema.makeAccessors(FieldAccessorBuilder)
}

for {
  _ <- Elasticsearch.execute(
    search(
      IndexName("index"),
      must(range(User.id).gte(7).lt(10)).should(startsWith(User.address / Address.street, "ZIO"))
    ).aggregate(aggregation)
  )
} yield ()
```

### Streaming

ZIO Elasticsearch is a streaming-friendly library, and it provides specific APIs for creating ZIO streams. When using the stream API, the result will be an `Item`, which is a case class that contains only one field, `raw`, that represents your response as raw JSON. Additionally, it is important to note that you can use `StreamConfig` to customize your settings when creating a stream. If you don't use `StreamConfig`, the default settings (`StreamConfig.Default`) will be used.

```scala
for {
  request           <- ElasticRequest.search(IndexName("index"), range(User.id).gte(5))
  defaultStream     <- Elasticsearch.stream(request)
  scrollStream      <- Elasticsearch.stream(request, StreamConfig.Scroll)
  searchAfterStream <- Elasticsearch.streamAs[User](request, StreamConfig.SearchAfter)
} yield ()
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
