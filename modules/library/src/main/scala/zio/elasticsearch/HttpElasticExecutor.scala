package zio.elasticsearch

import sttp.client3.ziojson._
import sttp.client3.{SttpBackend, UriContext, basicRequest => request}
import sttp.model.MediaType.ApplicationJson
import sttp.model.StatusCode.Ok
import sttp.model.Uri
import zio.Task
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
    request
      .get(uri)
      .response(asJson[ElasticGetResponse])
      .send(client)
      .map(_.body.toOption)
      .map(_.flatMap(d => if (d.found) Some(Document.from(d.source)) else None))
  }

  private def executeCreate(r: Create): Task[Option[DocumentId]] = {
    val uri = r.id match {
      case Some(documentId) =>
        uri"$basePath/${r.index}/$Create/$documentId".withParam("routing", r.routing.map(Routing.unwrap))
      case None =>
        uri"$basePath/${r.index}/$Doc".withParam("routing", r.routing.map(Routing.unwrap))
    }

    request
      .post(uri)
      .contentType(ApplicationJson)
      .response(asJson[ElasticCreateResponse])
      .body(r.document.json)
      .send(client)
      .map(_.body.toOption)
      .map(_.flatMap(body => DocumentId.make(body.id).toOption))
  }

  private def executeCreateIndex(createIndex: CreateIndex): Task[Unit] =
    request
      .put(uri"$basePath/${createIndex.name}")
      .contentType(ApplicationJson)
      .body(createIndex.definition.getOrElse(""))
      .send(client)
      .unit

  private def executeCreateOrUpdate(r: CreateOrUpdate): Task[Unit] = {
    val uri = uri"$basePath/${r.index}/$Doc/${r.id}".withParam("routing", r.routing.map(Routing.unwrap))
    request.put(uri).contentType(ApplicationJson).body(r.document.json).send(client).unit
  }

  private def executeExists(r: Exists): Task[Boolean] = {
    val uri = uri"$basePath/${r.index}/$Doc/${r.id}".withParam("routing", r.routing.map(Routing.unwrap))
    request.head(uri).send(client).map(_.code.equals(Ok))
  }

  private def executeDeleteIndex(r: DeleteIndex): Task[Unit] =
    request.delete(uri"$basePath/${r.name}").send(client).unit

  private def executeDeleteById(deleteById: DeleteById): Task[Option[Unit]] = {
    val uri =
      uri"$basePath/${deleteById.index}/$Doc/${deleteById.id}".withParam("routing", deleteById.routing.map(_.value))
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
