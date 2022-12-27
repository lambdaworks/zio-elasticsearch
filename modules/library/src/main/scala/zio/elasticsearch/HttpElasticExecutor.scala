package zio.elasticsearch

import sttp.client3.ziojson._
import sttp.client3.{Identity, RequestT, Response, ResponseException, SttpBackend, UriContext, basicRequest => request}
import sttp.model.MediaType.ApplicationJson
import sttp.model.StatusCode.Ok
import zio.Task
import zio.ZIO.logDebug
import zio.elasticsearch.ElasticRequest._

private[elasticsearch] final class HttpElasticExecutor private (config: ElasticConfig, client: SttpBackend[Task, Any])
    extends ElasticExecutor {

  import HttpElasticExecutor._

  override def execute[A](request: ElasticRequest[A, _]): Task[A] =
    request match {
      case r: CreateRequest         => executeCreate(r)
      case r: CreateIndexRequest    => executeCreateIndex(r)
      case r: CreateOrUpdateRequest => executeCreateOrUpdate(r)
      case r: DeleteByIdRequest     => executeDeleteById(r)
      case r: DeleteIndexRequest    => executeDeleteIndex(r)
      case r: ExistsRequest         => executeExists(r)
      case r: GetByIdRequest        => executeGetById(r)
      case r: GetByQueryRequest     => executeGetByQuery(r)
      case map @ Map(_, _)          => execute(map.request).map(map.mapper)
    }

  private def executeCreate(r: CreateRequest): Task[Option[DocumentId]] = {
    val uri = r.id match {
      case Some(documentId) =>
        uri"${config.uri}/${r.index}/$Create/$documentId"
          .withParam("routing", r.routing.map(Routing.unwrap))
          .withParam("refresh", r.refresh.toString)
      case None =>
        uri"${config.uri}/${r.index}/$Doc"
          .withParam("routing", r.routing.map(Routing.unwrap))
          .withParam("refresh", r.refresh.toString)
    }

    sendRequestWithCustomResponse[ElasticCreateResponse](
      request
        .post(uri)
        .contentType(ApplicationJson)
        .response(asJson[ElasticCreateResponse])
        .body(r.document.json)
    ).map(_.body.toOption).map(_.flatMap(body => DocumentId.make(body.id).toOption))
  }

  private def executeCreateIndex(createIndex: CreateIndexRequest): Task[Unit] =
    sendRequest(
      request
        .put(uri"${config.uri}/${createIndex.name}")
        .contentType(ApplicationJson)
        .body(createIndex.definition.getOrElse(""))
    ).unit

  private def executeCreateOrUpdate(r: CreateOrUpdateRequest): Task[Unit] = {
    val uri = uri"${config.uri}/${r.index}/$Doc/${r.id}"
      .withParam("routing", r.routing.map(Routing.unwrap))
      .withParam("refresh", r.refresh.toString)

    sendRequest(request.put(uri).contentType(ApplicationJson).body(r.document.json)).unit
  }

  private def executeDeleteById(r: DeleteByIdRequest): Task[Boolean] = {
    val uri = uri"${config.uri}/${r.index}/$Doc/${r.id}"
      .withParam("routing", r.routing.map(Routing.unwrap))
      .withParam("refresh", r.refresh.toString)

    sendRequestWithCustomResponse(
      request
        .delete(uri)
        .response(asJson[ElasticDeleteResponse])
    ).map(_.body.toOption).map(_.exists(_.result == "deleted"))
  }

  private def executeDeleteIndex(r: DeleteIndexRequest): Task[Boolean] =
    sendRequest(request.delete(uri"${config.uri}/${r.name}")).map(_.code.equals(Ok))

  private def executeExists(r: ExistsRequest): Task[Boolean] = {
    val uri = uri"${config.uri}/${r.index}/$Doc/${r.id}".withParam("routing", r.routing.map(Routing.unwrap))

    sendRequest(request.head(uri)).map(_.code.equals(Ok))
  }

  private def executeGetById(r: GetByIdRequest): Task[Option[Document]] = {
    val uri = uri"${config.uri}/${r.index}/$Doc/${r.id}".withParam("routing", r.routing.map(Routing.unwrap))

    sendRequestWithCustomResponse[ElasticGetResponse](
      request
        .get(uri)
        .response(asJson[ElasticGetResponse])
    ).map(_.body.toOption).map(_.flatMap(d => if (d.found) Some(Document.from(d.source)) else None))
  }

  private def executeGetByQuery(r: GetByQueryRequest): Task[Option[ElasticQueryResponse]] =
    sendRequestWithCustomResponse(
      request
        .post(uri"${config.uri}/${IndexName.unwrap(r.index)}/_search")
        .response(asJson[ElasticQueryResponse])
        .contentType(ApplicationJson)
        .body(r.query.toJsonBody)
    ).map(_.body.toOption)

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
