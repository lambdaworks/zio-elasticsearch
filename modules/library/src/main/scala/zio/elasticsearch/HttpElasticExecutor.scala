package zio.elasticsearch

import sttp.client3.ziojson._
import sttp.client3.{Identity, RequestT, Response, ResponseException, SttpBackend, UriContext, basicRequest => request}
import sttp.model.MediaType.ApplicationJson
import sttp.model.StatusCode.{
  BadRequest => HttpBadRequest,
  Conflict => HttpConflict,
  Created => HttpCreated,
  NotFound => HttpNotFound,
  Ok => HttpOk
}
import zio.ZIO.logDebug
import zio.elasticsearch.CreationOutcome.{AlreadyExists, Created}
import zio.elasticsearch.DeletionOutcome.{Deleted, NotFound}
import zio.elasticsearch.ElasticRequest._
import zio.{Task, ZIO}

private[elasticsearch] final class HttpElasticExecutor private (config: ElasticConfig, client: SttpBackend[Task, Any])
    extends ElasticExecutor {

  import HttpElasticExecutor._

  override def execute[A](request: ElasticRequest[A, _]): Task[A] =
    request match {
      case r: CreateRequest         => executeCreate(r)
      case r: CreateWithIdRequest   => executeCreateWithId(r)
      case r: CreateIndexRequest    => executeCreateIndex(r)
      case r: CreateOrUpdateRequest => executeCreateOrUpdate(r)
      case r: DeleteByIdRequest     => executeDeleteById(r)
      case r: DeleteByQueryRequest  => executeDeleteByQuery(r)
      case r: DeleteIndexRequest    => executeDeleteIndex(r)
      case r: ExistsRequest         => executeExists(r)
      case r: GetByIdRequest        => executeGetById(r)
      case r: GetByQueryRequest     => executeGetByQuery(r)
      case map @ Map(_, _)          => execute(map.request).flatMap(a => ZIO.fromEither(map.mapper(a)))
    }

  private def executeCreate(r: CreateRequest): Task[DocumentId] = {
    val uri = uri"${config.uri}/${r.index}/$Doc?refresh=${r.refresh}"
      .withParam("routing", r.routing.map(Routing.unwrap))

    sendRequestWithCustomResponse[ElasticCreateResponse](
      request
        .post(uri)
        .contentType(ApplicationJson)
        .body(r.document.json)
        .response(asJson[ElasticCreateResponse])
    ).flatMap { response =>
      response.code match {
        case HttpCreated =>
          response.body
            .map(res => DocumentId(res.id))
            .fold(
              e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
              value => ZIO.succeed(value)
            )
        case _ =>
          ZIO.fail(createElasticExceptionFromCustomResponse(response))
      }
    }

  }

  private def executeCreateWithId(r: CreateWithIdRequest): Task[CreationOutcome] = {
    val uri = uri"${config.uri}/${r.index}/$Create/${r.id}?refresh=${r.refresh}"
      .withParam("routing", r.routing.map(Routing.unwrap))

    sendRequest(
      request
        .post(uri)
        .contentType(ApplicationJson)
        .body(r.document.json)
    ).flatMap { response =>
      response.code match {
        case HttpCreated  => ZIO.succeed(Created)
        case HttpConflict => ZIO.succeed(AlreadyExists)
        case _            => ZIO.fail(createElasticException(response))
      }
    }
  }

  private def executeCreateIndex(createIndex: CreateIndexRequest): Task[CreationOutcome] =
    sendRequest(
      request
        .put(uri"${config.uri}/${createIndex.name}")
        .contentType(ApplicationJson)
        .body(createIndex.definition.getOrElse(""))
    ).flatMap { response =>
      response.code match {
        case HttpOk         => ZIO.succeed(Created)
        case HttpBadRequest => ZIO.succeed(AlreadyExists)
        case _              => ZIO.fail(createElasticException(response))
      }
    }

  private def executeCreateOrUpdate(r: CreateOrUpdateRequest): Task[Unit] = {
    val uri = uri"${config.uri}/${r.index}/$Doc/${r.id}?refresh=${r.refresh}"
      .withParam("routing", r.routing.map(Routing.unwrap))

    sendRequest(request.put(uri).contentType(ApplicationJson).body(r.document.json)).flatMap { response =>
      response.code match {
        case HttpOk | HttpCreated => ZIO.unit
        case _                    => ZIO.fail(createElasticException(response))
      }
    }
  }

  private def executeDeleteById(r: DeleteByIdRequest): Task[DeletionOutcome] = {
    val uri = uri"${config.uri}/${r.index}/$Doc/${r.id}?refresh=${r.refresh}"
      .withParam("routing", r.routing.map(Routing.unwrap))

    sendRequest(request.delete(uri)).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.succeed(Deleted)
        case HttpNotFound => ZIO.succeed(NotFound)
        case _            => ZIO.fail(createElasticException(response))
      }
    }
  }

  def executeDeleteByQuery(r: DeleteByQueryRequest): Task[DeletionOutcome] = {
    val uri = uri"${config.uri}/${r.index}/$DeleteByQuery".withParam("refresh", r.refresh.toString)

    sendRequest(
      request
        .post(uri)
        .contentType(ApplicationJson)
        .body(r.query.toJsonBody)
    ).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.succeed(Deleted)
        case HttpNotFound => ZIO.succeed(NotFound)
        case _            => ZIO.fail(createElasticException(response))
      }
    }
  }

  private def executeDeleteIndex(r: DeleteIndexRequest): Task[DeletionOutcome] =
    sendRequest(request.delete(uri"${config.uri}/${r.name}")).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.succeed(Deleted)
        case HttpNotFound => ZIO.succeed(NotFound)
        case _            => ZIO.fail(createElasticException(response))
      }
    }

  private def executeExists(r: ExistsRequest): Task[Boolean] = {
    val uri = uri"${config.uri}/${r.index}/$Doc/${r.id}".withParam("routing", r.routing.map(Routing.unwrap))

    sendRequest(request.head(uri)).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.succeed(true)
        case HttpNotFound => ZIO.succeed(false)
        case _            => ZIO.fail(createElasticException(response))
      }
    }
  }

  private def executeGetById(r: GetByIdRequest): Task[Option[Document]] = {
    val uri = uri"${config.uri}/${r.index}/$Doc/${r.id}".withParam("routing", r.routing.map(Routing.unwrap))

    sendRequestWithCustomResponse[ElasticGetResponse](
      request
        .get(uri)
        .response(asJson[ElasticGetResponse])
    ).flatMap { response =>
      response.code match {
        case HttpOk       => ZIO.attempt(response.body.toOption.map(d => Document.from(d.source)))
        case HttpNotFound => ZIO.succeed(None)
        case _            => ZIO.fail(createElasticExceptionFromCustomResponse(response))
      }
    }
  }

  private def executeGetByQuery(r: GetByQueryRequest): Task[ElasticQueryResponse] =
    sendRequestWithCustomResponse(
      request
        .post(uri"${config.uri}/${r.index}/$Search")
        .response(asJson[ElasticQueryResponse])
        .contentType(ApplicationJson)
        .body(r.query.toJsonBody)
    ).flatMap { response =>
      response.code match {
        case HttpOk =>
          response.body.fold(
            e => ZIO.fail(new ElasticException(s"Exception occurred: ${e.getMessage}")),
            value => ZIO.succeed(value)
          )
        case _ =>
          ZIO.fail(createElasticExceptionFromCustomResponse(response))
      }
    }

  private def sendRequest(
    req: RequestT[Identity, Either[String, String], Any]
  ): Task[Response[Either[String, String]]] =
    for {
      _    <- logDebug(s"[es-req]: ${req.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set.empty)}")
      resp <- req.send(client)
      _    <- logDebug(s"[es-res]: ${resp.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set.empty)}")
    } yield resp

  private def sendRequestWithCustomResponse[A](
    req: RequestT[Identity, Either[ResponseException[String, String], A], Any]
  ): Task[Response[Either[ResponseException[String, String], A]]] =
    for {
      _    <- logDebug(s"[es-req]: ${req.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set.empty)}")
      resp <- req.send(client)
      _    <- logDebug(s"[es-res]: ${resp.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set.empty)}")
    } yield resp

  private def createElasticException(response: Response[Either[String, String]]): ElasticException =
    new ElasticException(
      s"Unexpected response from Elasticsearch. Response body: ${response.body.fold(body => body, _ => "")}"
    )

  private def createElasticExceptionFromCustomResponse[A](
    response: Response[Either[ResponseException[String, String], A]]
  ): ElasticException =
    new ElasticException(
      s"Unexpected response from Elasticsearch. Response body: ${response.body.fold(body => body, _ => "")}"
    )

}

private[elasticsearch] object HttpElasticExecutor {

  private final val Doc           = "_doc"
  private final val Create        = "_create"
  private final val DeleteByQuery = "_delete_by_query"
  private final val Search        = "_search"

  def apply(config: ElasticConfig, client: SttpBackend[Task, Any]) =
    new HttpElasticExecutor(config, client)
}
