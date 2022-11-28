package zio.elasticsearch

import sttp.client3._
import sttp.client3.ziojson._
import sttp.model.Uri
import zio.elasticsearch.ElasticRequest._
import zio.{Task, ZIO}

private[elasticsearch] final class HttpElasticExecutor private (config: ElasticConfig, client: SttpBackend[Task, Any])
    extends ElasticExecutor {

  import HttpElasticExecutor._

  private val basePath = Uri(config.host, config.port)

  override def execute[A](request: ElasticRequest[A]): Task[A] =
    request match {
      case _: Create         => ZIO.unit
      case _: CreateOrUpdate => ZIO.unit
      case r: GetById        => executeGetById(r)
      case map @ Map(_, _)   => execute(map.request).map(map.mapper)
    }

  private def executeGetById(getById: GetById): Task[Option[Document]] = {
    val uri = uri"$basePath/${getById.index}/$Doc/${getById.id}".withParam("routing", getById.routing.map(_.value))
    basicRequest
      .get(uri)
      .response(asJson[ElasticResponse])
      .send(client)
      .map(_.body.toOption)
      .map(_.flatMap(d => if (d.found) Option(Document.from(d.source)) else None))
  }

}

private[elasticsearch] object HttpElasticExecutor {

  private final val Doc = "_doc"

  def apply(config: ElasticConfig, client: SttpBackend[Task, Any]) =
    new HttpElasticExecutor(config, client)
}
