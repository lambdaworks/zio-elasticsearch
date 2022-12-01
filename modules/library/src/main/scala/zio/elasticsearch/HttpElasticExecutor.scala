package zio.elasticsearch

import sttp.client3.ziojson._
import sttp.client3.{SttpBackend, UriContext, basicRequest => request}
import sttp.model.MediaType.ApplicationJson
import sttp.model.StatusCode.Ok
import sttp.model.Uri
import zio.Task
import zio.ZIO.{logError, logInfo}
import zio.elasticsearch.ElasticRequest._

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

    for {
      _ <- logInfo(s"Executing get document by id: ${r.id}...")
      maybeDocument <- request
                         .get(uri)
                         .response(asJson[ElasticGetResponse])
                         .send(client)
                         .map(_.body.toOption)
                         .map(_.flatMap(d => if (d.found) Some(Document.from(d.source)) else None))
    } yield maybeDocument
  }

  private def executeCreate(r: Create): Task[Option[DocumentId]] = {
    val uri = r.id match {
      case Some(documentId) =>
        uri"$basePath/${r.index}/$Create/$documentId".withParam("routing", r.routing.map(Routing.unwrap))
      case None =>
        uri"$basePath/${r.index}/$Doc".withParam("routing", r.routing.map(Routing.unwrap))
    }

    // for now, it is still "happy path", I don't know if there are any other ways for failure
    for {
      _ <- logInfo("Executing create document...")
      maybeDocumentId <- request
                           .post(uri)
                           .contentType(ApplicationJson)
                           .response(asJson[ElasticCreateResponse])
                           .body(r.document.json)
                           .send(client)
                           .map(_.body.toOption)
                           .map(_.flatMap(body => DocumentId.make(body.id).toOption))
      _ <- if (maybeDocumentId.isEmpty)
             logError("Document could not be created - document with given id already exists.")
           else logInfo("Document is successfully created.")
    } yield maybeDocumentId
  }

  private def executeCreateIndex(createIndex: CreateIndex): Task[Unit] =
    for {
      _ <- logInfo("Executing create index...")
      _ <- request
             .put(uri"$basePath/${createIndex.name}")
             .contentType(ApplicationJson)
             .body(createIndex.definition.getOrElse(""))
             .send(client)
    } yield ()

  private def executeCreateOrUpdate(r: CreateOrUpdate): Task[Unit] = {
    val uri = uri"$basePath/${r.index}/$Doc/${r.id}".withParam("routing", r.routing.map(Routing.unwrap))

    for {
      _ <- logInfo("Executing create or update document...")
      _ <- request.put(uri).contentType(ApplicationJson).body(r.document.json).send(client)
    } yield ()
  }

  private def executeExists(r: Exists): Task[Boolean] = {
    val uri = uri"$basePath/${r.index}/$Doc/${r.id}".withParam("routing", r.routing.map(Routing.unwrap))
    request.head(uri).send(client).map(_.code.equals(Ok))
  }

  private def executeDeleteIndex(r: DeleteIndex): Task[Unit] =
    for {
      _ <- logInfo("Executing delete index...")
      _ <- request.delete(uri"$basePath/${r.name}").send(client)
    } yield ()

  private def executeDeleteById(r: DeleteById): Task[Option[Unit]] = {
    val uri = uri"$basePath/${r.index}/$Doc/${r.id}".withParam("routing", r.routing.map(Routing.unwrap))
    request
      .delete(uri)
      .response(asJson[ElasticDeleteResponse])
      .send(client)
      .map(_.body.toOption)
      .map(_.filter(_.result == "deleted").map(_ => ()))
  }
}

private[elasticsearch] object HttpElasticExecutor {

  private final val Doc    = "_doc"
  private final val Create = "_create"

  def apply(config: ElasticConfig, client: SttpBackend[Task, Any]) =
    new HttpElasticExecutor(config, client)
}
