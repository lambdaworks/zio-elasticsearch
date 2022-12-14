package zio.elasticsearch

import sttp.client3.ziojson._
import sttp.client3.{Identity, RequestT, Response, ResponseException, SttpBackend, UriContext, basicRequest => request}
import sttp.model.MediaType.ApplicationJson
import sttp.model.StatusCode.Ok
import sttp.model.Uri
import zio.ZIO.logDebug
import zio.elasticsearch.ElasticRequest._
import zio.{Task, ZIO}

private[elasticsearch] final class HttpElasticExecutor private (config: ElasticConfig, client: SttpBackend[Task, Any])
    extends ElasticExecutor {

  import HttpElasticExecutor._

  private val basePath = Uri(config.host, config.port)

  override def execute[A](request: ElasticRequest[A]): Task[A] =
    request match {
      case r: Create         => executeCreate(r)
      case r: CreateIndex    => executeCreateIndex(r)
      case r: CreateOrUpdate => executeCreateOrUpdate(r)
      case r: DeleteById     => executeDeleteById(r)
      case r: DeleteIndex    => executeDeleteIndex(r)
      case r: Exists         => executeExists(r)
      case r: GetById        => executeGetById(r)
      case map @ Map(_, _)   => execute(map.request).map(map.mapper)
    }

  private def executeGetById(r: GetById): Task[Option[Document]] = {
    val uri = uri"$basePath/${r.index}/$Doc/${r.id}".withParam("routing", r.routing.map(Routing.unwrap))

    sendRequestWithCustomResponse[ElasticGetResponse](
      request
        .get(uri)
        .response(asJson[ElasticGetResponse])
    ).map(_.body.toOption).map(_.flatMap(d => if (d.found) Some(Document.from(d.source)) else None))
  }

  private def executeCreate(r: Create): Task[Option[DocumentId]] = {
    val uri = r.id match {
      case Some(documentId) =>
        uri"$basePath/${r.index}/$Create/$documentId".withParam("routing", r.routing.map(Routing.unwrap))
      case None =>
        uri"$basePath/${r.index}/$Doc".withParam("routing", r.routing.map(Routing.unwrap))
    }

    sendRequestWithCustomResponse[ElasticCreateResponse](
      request
        .post(uri)
        .contentType(ApplicationJson)
        .response(asJson[ElasticCreateResponse])
        .body(r.document.json)
    ).map(_.body.toOption).map(_.flatMap(body => DocumentId.make(body.id).toOption))
  }

  private def executeCreateIndex(createIndex: CreateIndex): Task[Unit] =
    sendRequest(
      request
        .put(uri"$basePath/${createIndex.name}")
        .contentType(ApplicationJson)
        .body(createIndex.definition.getOrElse(""))
    ).unit

  private def executeCreateOrUpdate(r: CreateOrUpdate): Task[Unit] = {
    val uri = uri"$basePath/${r.index}/$Doc/${r.id}".withParam("routing", r.routing.map(Routing.unwrap))

    sendRequest(request.put(uri).contentType(ApplicationJson).body(r.document.json)).unit
  }

  private def executeExists(r: Exists): Task[Boolean] = {
    val uri = uri"$basePath/${r.index}/$Doc/${r.id}".withParam("routing", r.routing.map(Routing.unwrap))

    sendRequest(request.head(uri)).map(_.code.equals(Ok))
  }

  private def executeDeleteIndex(r: DeleteIndex): Task[Unit] =
    sendRequest(request.delete(uri"$basePath/${r.name}")).unit

  private def executeDeleteById(r: DeleteById): Task[Option[Unit]] = {
    val uri = uri"$basePath/${r.index}/$Doc/${r.id}".withParam("routing", r.routing.map(Routing.unwrap))

    sendRequestWithCustomResponse(
      request
        .delete(uri)
        .response(asJson[ElasticDeleteResponse])
    ).map(_.body.toOption).map(_.filter(_.result == "deleted").map(_ => ()))
  }

  private def sendRequestWithCustomResponse[A](
    req: RequestT[Identity, Either[ResponseException[String, String], A], Any]
  ): ZIO[Any, Throwable, Response[Either[ResponseException[String, String], A]]] =
    for {
      _    <- logDebug(s"[es-req]: ${req.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set())}")
      resp <- req.send(client)
      _    <- logDebug(s"[es-res]: ${resp.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set())}")
    } yield resp

  private def sendRequest(
    req: RequestT[Identity, Either[String, String], Any]
  ): ZIO[Any, Throwable, Response[Either[String, String]]] =
    for {
      _    <- logDebug(s"[es-req]: ${req.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set())}")
      resp <- req.send(client)
      _    <- logDebug(s"[es-res]: ${resp.show(includeBody = true, includeHeaders = true, sensitiveHeaders = Set())}")
    } yield resp
}

private[elasticsearch] object HttpElasticExecutor {

  private final val Doc    = "_doc"
  private final val Create = "_create"

  def apply(config: ElasticConfig, client: SttpBackend[Task, Any]) =
    new HttpElasticExecutor(config, client)
}
