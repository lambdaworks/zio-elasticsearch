package zio.elasticsearch

import sttp.client3.ziojson._
import sttp.client3.{SttpBackend, UriContext, basicRequest => request}
import sttp.model.MediaType.ApplicationJson
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
      case r: CreateOrUpdate => executeCreateOrUpdate(r)
      case r: GetById        => executeGetById(r)
      case map @ Map(_, _)   => execute(map.request).map(map.mapper)
    }

  private def executeGetById(r: GetById): Task[Option[Document]] = {
    val uri = uri"$basePath/${r.index}/$Doc/${r.id}".withParam("routing", r.routing.map(_.value))
    request
      .get(uri)
      .response(asJson[ElasticGetResponse])
      .send(client)
      .map(_.body.toOption)
      .map(_.flatMap(d => if (d.found) Some(Document.from(d.source)) else None))
  }

  private def executeCreate(r: Create): Task[Option[DocumentId]] = {
    def createUri(maybeDocumentId: Option[DocumentId]) =
      maybeDocumentId match {
        case Some(documentId) =>
          uri"$basePath/${r.index}/$Create/$documentId".withParam("routing", r.routing.map(_.value))
        case None =>
          uri"$basePath/${r.index}/$Doc".withParam("routing", r.routing.map(_.value))
      }

    request
      .post(createUri(r.id))
      .contentType(ApplicationJson)
      .response(asJson[ElasticCreateResponse])
      .body(r.document.json)
      .send(client)
      .map(_.body.toOption)
      .map(_.flatMap(body => Some(DocumentId(body.id))))
  }

  private def executeCreateOrUpdate(r: CreateOrUpdate): Task[Unit] = {
    val u = uri"$basePath/${r.index}/$Doc/${r.id}"
      .withParam("routing", r.routing.map(_.value))

    request
      .put(u)
      .contentType(ApplicationJson)
      .body(r.document.json)
      .send(client)
      .unit
  }
}

private[elasticsearch] object HttpElasticExecutor {

  private final val Doc    = "_doc"
  private final val Create = "_create"

  def apply(config: ElasticConfig, client: SttpBackend[Task, Any]) =
    new HttpElasticExecutor(config, client)
}
