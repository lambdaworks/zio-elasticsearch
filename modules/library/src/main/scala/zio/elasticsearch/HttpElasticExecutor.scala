package zio.elasticsearch

import sttp.client3.ziojson._
import sttp.client3.{Identity, RequestT, Response, ResponseException, SttpBackend, UriContext, basicRequest => request}
import sttp.model.MediaType.ApplicationJson
import sttp.model.StatusCode.{BadRequest, Conflict, Created => CreatedCode, NotFound => NotFoundCode, Ok}
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
      case r: DeleteIndexRequest    => executeDeleteIndex(r)
      case r: ExistsRequest         => executeExists(r)
      case r: GetByIdRequest        => executeGetById(r)
      case r: GetByQueryRequest     => executeGetByQuery(r)
      case map @ Map(_, _)          => execute(map.request).map(map.mapper)
    }

  private def executeCreate(r: CreateRequest): Task[DocumentId] = {
    val uri = uri"${config.uri}/${r.index}/$Doc"
      .withParam("routing", r.routing.map(Routing.unwrap))
      .withParam("refresh", r.refresh.toString)

    sendRequestWithCustomResponse[ElasticCreateResponse](
      request
        .post(uri)
        .contentType(ApplicationJson)
        .body(r.document.json)
        .response(asJson[ElasticCreateResponse])
    ).flatMap { response =>
      response.code match {
        case CreatedCode =>
          response.body.map(a => DocumentId.apply(a.id)) match {
            case Left(e)      => ZIO.fail(ElasticException(s"Exception occurred: ${e.getMessage}"))
            case Right(value) => ZIO.succeed(value)
          }
        case _ => ZIO.fail(ElasticException(s"Unexpected response from Elasticsearch: $response"))
      }
    }

  }

  private def executeCreateWithId(r: CreateWithIdRequest): Task[CreationOutcome] = {
    val uri = uri"${config.uri}/${r.index}/$Create/${r.id}"
      .withParam("routing", r.routing.map(Routing.unwrap))
      .withParam("refresh", r.refresh.toString)

    sendRequest(
      request
        .post(uri)
        .contentType(ApplicationJson)
        .body(r.document.json)
    ).flatMap { response =>
      response.code match {
        case CreatedCode => ZIO.succeed(Created)
        case Conflict    => ZIO.succeed(AlreadyExists)
        case _           => ZIO.fail(ElasticException(s"Unexpected response from Elasticsearch: $response"))
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
        case Ok         => ZIO.succeed(Created)
        case BadRequest => ZIO.succeed(AlreadyExists)
        case _          => ZIO.fail(ElasticException(s"Unexpected response from Elasticsearch: $response"))
      }
    }

  private def executeCreateOrUpdate(r: CreateOrUpdateRequest): Task[Unit] = {
    val uri = uri"${config.uri}/${r.index}/$Doc/${r.id}"
      .withParam("routing", r.routing.map(Routing.unwrap))
      .withParam("refresh", r.refresh.toString)

    sendRequest(request.put(uri).contentType(ApplicationJson).body(r.document.json)).flatMap { response =>
      response.code match {
        case Ok | CreatedCode => ZIO.unit
        case _                => ZIO.fail(ElasticException(s"Unexpected response from Elasticsearch: $response"))
      }
    }
  }

  private def executeDeleteById(r: DeleteByIdRequest): Task[DeletionOutcome] = {
    val uri = uri"${config.uri}/${r.index}/$Doc/${r.id}"
      .withParam("routing", r.routing.map(Routing.unwrap))
      .withParam("refresh", r.refresh.toString)

    sendRequest(request.delete(uri)).flatMap { response =>
      response.code match {
        case Ok           => ZIO.succeed(Deleted)
        case NotFoundCode => ZIO.succeed(NotFound)
        case _            => ZIO.fail(ElasticException(s"Unexpected response from Elasticsearch: $response"))
      }
    }
  }

  private def executeDeleteIndex(r: DeleteIndexRequest): Task[DeletionOutcome] =
    sendRequest(request.delete(uri"${config.uri}/${r.name}")).flatMap { response =>
      response.code match {
        case Ok           => ZIO.succeed(Deleted)
        case NotFoundCode => ZIO.succeed(NotFound)
        case _            => ZIO.fail(ElasticException(s"Unexpected response from Elasticsearch: $response"))
      }
    }

  private def executeExists(r: ExistsRequest): Task[Boolean] = {
    val uri = uri"${config.uri}/${r.index}/$Doc/${r.id}".withParam("routing", r.routing.map(Routing.unwrap))

    sendRequest(request.head(uri)).flatMap { response =>
      response.code match {
        case Ok           => ZIO.succeed(true)
        case NotFoundCode => ZIO.succeed(false)
        case _            => ZIO.fail(ElasticException(s"Unexpected response from Elasticsearch: $response"))
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
        case Ok           => ZIO.attempt(response.body.toOption.map(d => Document.from(d.source)))
        case NotFoundCode => ZIO.succeed(None)
        case _            => ZIO.fail(ElasticException(s"Unexpected response from Elasticsearch: $response"))
      }
    }
  }

  private def executeGetByQuery(r: GetByQueryRequest): Task[ElasticQueryResponse] =
    sendRequestWithCustomResponse(
      request
        .post(uri"${config.uri}/${IndexName.unwrap(r.index)}/_search")
        .response(asJson[ElasticQueryResponse])
        .contentType(ApplicationJson)
        .body(r.query.toJsonBody)
    ).flatMap { response =>
      response.code match {
        case Ok =>
          response.body match {
            case Left(e)      => ZIO.fail(ElasticException(s"Exception occurred: ${e.getMessage}"))
            case Right(value) => ZIO.succeed(value)
          }
        case _ => ZIO.fail(ElasticException(s"Unexpected response from Elasticsearch: $response"))
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
}

private[elasticsearch] object HttpElasticExecutor {

  private final val Doc    = "_doc"
  private final val Create = "_create"

  def apply(config: ElasticConfig, client: SttpBackend[Task, Any]) =
    new HttpElasticExecutor(config, client)
}
