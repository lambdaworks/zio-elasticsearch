package example

import sttp.client3.httpclient.zio.HttpClientZioBackend
import zio.elasticsearch.ElasticRequest._
import zio.elasticsearch._
import zio._
import zio.elasticsearch.aggregation.TermsAggregation
import zio.elasticsearch.query.BoolQuery
import zio.elasticsearch.result.{Item, SearchAndAggregateResult}
import zio.schema.{DeriveSchema, Schema}
import zio.stream.ZStream

final case class Address(street: String, number: Int)
object Address {
  implicit val schema: Schema.CaseClass2[String, Int, Address] =
    DeriveSchema.gen[Address]
  val (street, number) = schema.makeAccessors(FieldAccessorBuilder)
}

final case class User(id: Int, address: Address)
object User {
  implicit val schema: Schema.CaseClass2[Int, Address, User] =
    DeriveSchema.gen[User]
  val (id, address) = schema.makeAccessors(FieldAccessorBuilder)
}

object ZIOElasticsearchExample extends ZIOAppDefault {
  val indexName: IndexName = IndexName("index")
  val docId: DocumentId    = DocumentId("documentId")

//  val effect: RIO[Elasticsearch, Unit] = for {
//    _ <- Elasticsearch.execute(createIndex(indexName))
//    _ <- Elasticsearch.execute(
//           ElasticRequest.bulk(
//             ElasticRequest.create[User](indexName, User(1, "John Doe")),
//             ElasticRequest.create[User](indexName, DocumentId("documentId2"), User(2, "Jane Doe")),
//             ElasticRequest.upsert[User](indexName, DocumentId("documentId3"), User(3, "Richard Roe")),
//             ElasticRequest.deleteById(indexName, DocumentId("documentId2"))
//           )
//         )
//
//  } yield ()

//  deleteById(IndexName("index"), DocumentId("documentId")).routing(Routing("routing")).refreshTrue
//  ElasticQuery.must(ElasticQuery.range("version").gte(7).lt(10)).should(ElasticQuery.startsWith("name", "ZIO"))
//  ElasticQuery.must(ElasticQuery.range("version").gte(7).lt(10)).should(ElasticQuery.startsWith("name", "ZIO"))
//  ElasticQuery.range(User.age).gte(18).lt(100)

  val query: BoolQuery[User] =
    ElasticQuery
      .must(ElasticQuery.range(User.id).gte(7).lt(10))
      .should(ElasticQuery.startsWith(User.address / Address.street, "ZIO"))

  val aggregation: TermsAggregation =
    ElasticAggregation
      .termsAggregation("termsAgg", User.address / Address.street)

//  val request: SearchAndAggregateRequest =
//    ElasticRequest
//      .search(IndexName("index"), query)
//      .aggregate(aggregation)

  val request: SearchRequest =
    ElasticRequest.search(IndexName("index"), ElasticQuery.range(User.id).gte(5))

  val defaultStream: ZStream[Elasticsearch, Throwable, Item] = Elasticsearch.stream(request)
  val scrollStream: ZStream[Elasticsearch, Throwable, Item] = Elasticsearch.stream(request, StreamConfig.Scroll)
  val searchAfterStream: ZStream[Elasticsearch, Throwable, User] = Elasticsearch.streamAs[User](request, StreamConfig.SearchAfter)

  val effect = Elasticsearch.stream(request)

//  val effect: RIO[Elasticsearch, SearchAndAggregateResult] =
//    Elasticsearch.execute(request)

  override def run =
    effect
      .provide(
        ElasticExecutor.local,
        Elasticsearch.layer,
        HttpClientZioBackend.layer()
      )
}
