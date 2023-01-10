package zio.elasticsearch

import zio.elasticsearch.Refresh.WithRefresh
import zio.elasticsearch.Routing.{Routing, WithRouting}
import zio.schema.Schema
import zio.schema.codec.JsonCodec.JsonDecoder
import zio.{RIO, ZIO}

sealed trait ElasticRequest[+A, ERT <: ElasticRequestType] { self =>

  final def execute: RIO[ElasticExecutor, A] =
    ZIO.serviceWithZIO[ElasticExecutor](_.execute(self))

  final def map[B](f: A => Either[DecodingException, B]): ElasticRequest[B, ERT] = ElasticRequest.Map(self, f)

  final def refresh(value: Boolean)(implicit wr: WithRefresh[ERT]): ElasticRequest[A, ERT] =
    wr.withRefresh(request = self, value = value)

  final def refreshFalse(implicit wr: WithRefresh[ERT]): ElasticRequest[A, ERT] =
    wr.withRefresh(request = self, value = false)

  final def refreshTrue(implicit wr: WithRefresh[ERT]): ElasticRequest[A, ERT] =
    wr.withRefresh(request = self, value = true)

  final def routing(value: Routing)(implicit wr: WithRouting[ERT]): ElasticRequest[A, ERT] =
    wr.withRouting(request = self, routing = value)
}

object ElasticRequest {

  import ElasticRequestType._

  def create[A: Schema](index: IndexName, doc: A): ElasticRequest[DocumentId, Create] =
    CreateRequest(index, Document.from(doc))

  def create[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[CreationOutcome, CreateWithId] =
    CreateWithIdRequest(index, id, Document.from(doc))

  def createIndex(name: IndexName, definition: Option[String]): ElasticRequest[CreationOutcome, CreateIndex] =
    CreateIndexRequest(name, definition)

  def deleteById(index: IndexName, id: DocumentId): ElasticRequest[DeletionOutcome, DeleteById] =
    DeleteByIdRequest(index, id)

  def deleteByQuery(index: IndexName, query: ElasticQuery[_]): ElasticRequest[Unit, DeleteByQuery] =
    DeleteByQueryRequest(index, query)

  def deleteIndex(name: IndexName): ElasticRequest[DeletionOutcome, DeleteIndex] =
    DeleteIndexRequest(name)

  def exists(index: IndexName, id: DocumentId): ElasticRequest[Boolean, Exists] =
    ExistsRequest(index, id)

  def getById[A: Schema](index: IndexName, id: DocumentId): ElasticRequest[Option[A], GetById] =
    GetByIdRequest(index, id).map {
      case Some(document) =>
        document.decode match {
          case Left(e)    => Left(DecodingException(s"Could not parse the document: ${e.message}"))
          case Right(doc) => Right(Some(doc))
        }
      case None => Right(None)
    }

  def search[A](index: IndexName, query: ElasticQuery[_])(implicit
    schema: Schema[A]
  ): ElasticRequest[List[A], GetByQuery] =
    GetByQueryRequest(index, query).map { response =>
      val (failed, successful) = response.results.partitionMap(json => JsonDecoder.decode(schema, json.toString))
      if (failed.nonEmpty) {
        Left(
          DecodingException(s"Could not parse all documents successfully: ${failed.map(_.message).mkString(",")})")
        )
      } else {
        Right(successful)
      }
    }

  def upsert[A: Schema](index: IndexName, id: DocumentId, doc: A): ElasticRequest[Unit, Upsert] =
    CreateOrUpdateRequest(index, id, Document.from(doc))

  private[elasticsearch] final case class CreateRequest(
    index: IndexName,
    document: Document,
    refresh: Boolean = false,
    routing: Option[Routing] = None
  ) extends ElasticRequest[DocumentId, Create]

  private[elasticsearch] final case class CreateWithIdRequest(
    index: IndexName,
    id: DocumentId,
    document: Document,
    refresh: Boolean = false,
    routing: Option[Routing] = None
  ) extends ElasticRequest[CreationOutcome, CreateWithId]

  private[elasticsearch] final case class CreateIndexRequest(
    name: IndexName,
    definition: Option[String]
  ) extends ElasticRequest[CreationOutcome, CreateIndex]

  private[elasticsearch] final case class CreateOrUpdateRequest(
    index: IndexName,
    id: DocumentId,
    document: Document,
    refresh: Boolean = false,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Unit, Upsert]

  private[elasticsearch] final case class DeleteByIdRequest(
    index: IndexName,
    id: DocumentId,
    refresh: Boolean = false,
    routing: Option[Routing] = None
  ) extends ElasticRequest[DeletionOutcome, DeleteById]

  private[elasticsearch] final case class DeleteByQueryRequest(
    index: IndexName,
    query: ElasticQuery[_],
    routing: Option[Routing] = None
  ) extends ElasticRequest[Unit, DeleteByQuery]

  private[elasticsearch] final case class DeleteIndexRequest(name: IndexName)
      extends ElasticRequest[DeletionOutcome, DeleteIndex]

  private[elasticsearch] final case class ExistsRequest(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Boolean, Exists]

  private[elasticsearch] final case class GetByIdRequest(
    index: IndexName,
    id: DocumentId,
    routing: Option[Routing] = None
  ) extends ElasticRequest[Option[Document], GetById]

  private[elasticsearch] final case class GetByQueryRequest(
    index: IndexName,
    query: ElasticQuery[_],
    routing: Option[Routing] = None
  ) extends ElasticRequest[ElasticQueryResponse, GetByQuery]

  private[elasticsearch] final case class Map[A, B, ERT <: ElasticRequestType](
    request: ElasticRequest[A, ERT],
    mapper: A => Either[DecodingException, B]
  ) extends ElasticRequest[B, ERT]
}

sealed trait ElasticRequestType

object ElasticRequestType {
  trait CreateIndex   extends ElasticRequestType
  trait Create        extends ElasticRequestType
  trait CreateWithId  extends ElasticRequestType
  trait DeleteById    extends ElasticRequestType
  trait DeleteByQuery extends ElasticRequestType
  trait DeleteIndex   extends ElasticRequestType
  trait Exists        extends ElasticRequestType
  trait GetById       extends ElasticRequestType
  trait GetByQuery    extends ElasticRequestType
  trait Upsert        extends ElasticRequestType
}

sealed abstract class CreationOutcome

object CreationOutcome {
  case object Created       extends CreationOutcome
  case object AlreadyExists extends CreationOutcome
}

sealed abstract class DeletionOutcome

object DeletionOutcome {
  case object Deleted  extends DeletionOutcome
  case object NotFound extends DeletionOutcome
}
