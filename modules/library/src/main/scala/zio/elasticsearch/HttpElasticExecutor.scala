package zio.elasticsearch

import sttp.client3._
import sttp.client3.ziojson._
import sttp.model.Uri
import zio.Task
import zio.elasticsearch.ElasticRequest._

private[elasticsearch] final class HttpElasticExecutor private (config: ElasticConfig, client: SttpBackend[Task, Any])
    extends ElasticExecutor {

  import HttpElasticExecutor._

  private val basePath = Uri(config.host, config.port)

  override def execute[A](request: ElasticRequest[A]): Task[A] =
    request match {
      case c: Create           => executeCreate(c)
      case cou: CreateOrUpdate => executeCreateOrUpdate(cou)
      case r: GetById          => executeGetById(r)
      case map @ Map(_, _)     => execute(map.request).map(map.mapper)
    }

  private def executeGetById(getById: GetById): Task[Option[Document]] = {
    val uri = uri"$basePath/${getById.index}/$Doc/${getById.id}".withParam("routing", getById.routing.map(_.value))
    basicRequest
      .get(uri)
      .response(asJson[ElasticGetResponse])
      .send(client)
      .map(_.body.toOption)
      .map(_.flatMap(d => if (d.found) Option(Document.from(d.source)) else None))
  }

  private def executeCreate(create: Create): Task[Option[DocumentId]] = {
    def createUri(maybeDocumentId: Option[DocumentId]) =
      maybeDocumentId match {
        case Some(documentId) =>
          uri"$basePath/${create.index}/$Create/$documentId".withParam("routing", create.routing.map(_.value))
        case None =>
          uri"$basePath/${create.index}/$Doc/".withParam("routing", create.routing.map(_.value))
      }

    basicRequest
      .post(createUri(create.id))
      .header(ContentType, ContentTypeVal)
      .response(asJson[ElasticCreateResponse])
      .body(create.document.json)
      .send(client)
      .map(_.body.toOption)
      .map(_.flatMap(body => Option(DocumentId(body.id))))
  }

  private def executeCreateOrUpdate(createOrUpdate: CreateOrUpdate): Task[Unit] = {
    val u = uri"$basePath/${createOrUpdate.index}/$Doc/${createOrUpdate.id}"
      .withParam("routing", createOrUpdate.routing.map(_.value))

    basicRequest
      .put(u)
      .header(ContentType, ContentTypeVal)
      .body(createOrUpdate.document.json)
      .send(client)
      .map(_ => ())
  }
}

private[elasticsearch] object HttpElasticExecutor {

  private final val Doc            = "_doc"
  private final val Create         = "_create"
  private final val ContentType    = "Content-Type"
  private final val ContentTypeVal = "application/json"

  def apply(config: ElasticConfig, client: SttpBackend[Task, Any]) =
    new HttpElasticExecutor(config, client)
}
