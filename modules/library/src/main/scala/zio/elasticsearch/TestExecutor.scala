package zio.elasticsearch

import zio.Random.nextUUID
import zio.elasticsearch.CreationOutcome.{AlreadyExists, Created}
import zio.elasticsearch.DeletionOutcome.{Deleted, NotFound}
import zio.{Task, ZIO}
import zio.elasticsearch.ElasticRequest._
import zio.json.ast.Json
import zio.stm.{STM, TMap, ZSTM}

private[elasticsearch] final case class TestExecutor private (esMap: TMap[IndexName, TMap[DocumentId, Document]])
    extends ElasticExecutor {
  self =>

  override def execute[A](request: ElasticRequest[A, _]): Task[A] =
    request match {
      case CreateRequest(index, document, _, _) =>
        fakeCreate(index, document)
      case CreateWithIdRequest(index, id, document, _, _) =>
        fakeCreateWithId(index, id, document)
      case CreateIndexRequest(name, _) =>
        fakeCreateIndex(name)
      case CreateOrUpdateRequest(index, id, document, _, _) =>
        fakeCreateOrUpdate(index, id, document)
      case DeleteByIdRequest(index, id, _, _) =>
        fakeDeleteById(index, id)
      case DeleteIndexRequest(name) =>
        fakeDeleteIndex(name)
      case ExistsRequest(index, id, _) =>
        fakeExists(index, id)
      case GetByIdRequest(index, id, _) =>
        fakeGetById(index, id)
      case GetByQueryRequest(index, _, _) =>
        fakeGetByQuery(index)
      case map @ Map(_, _) => execute(map.request).flatMap(a => ZIO.fromEither(map.mapper(a)))
    }

  private def fakeCreate(index: IndexName, document: Document): Task[DocumentId] =
    (for {
      documents <- getDocumentsFromIndex(index)
      documentId = DocumentId(nextUUID.toString)
      _         <- documents.put(documentId, document)
    } yield documentId).commit

  private def fakeCreateWithId(index: IndexName, documentId: DocumentId, document: Document): Task[CreationOutcome] =
    (for {
      documents     <- getDocumentsFromIndex(index)
      alreadyExists <- documents.contains(documentId)
      _             <- documents.putIfAbsent(documentId, document)
    } yield if (alreadyExists) AlreadyExists else Created).commit

  private def fakeCreateIndex(index: IndexName): Task[CreationOutcome] =
    (for {
      alreadyExists  <- self.esMap.contains(index)
      emptyDocuments <- TMap.empty[DocumentId, Document]
      _              <- self.esMap.putIfAbsent(index, emptyDocuments)
    } yield if (alreadyExists) AlreadyExists else Created).commit

  private def fakeCreateOrUpdate(index: IndexName, documentId: DocumentId, document: Document): Task[Unit] =
    (for {
      documents <- getDocumentsFromIndex(index)
      _         <- documents.put(documentId, document)
    } yield ()).commit

  private def fakeDeleteById(index: IndexName, documentId: DocumentId): Task[DeletionOutcome] =
    (for {
      documents <- getDocumentsFromIndex(index)
      notExists <- documents.contains(documentId)
      _         <- documents.delete(documentId)
    } yield if (notExists) NotFound else Deleted).commit

  private def fakeDeleteIndex(index: IndexName): Task[DeletionOutcome] =
    (for {
      notExists <- self.esMap.contains(index)
      _         <- self.esMap.delete(index)
    } yield if (notExists) NotFound else Deleted).commit

  private def fakeExists(index: IndexName, documentId: DocumentId): Task[Boolean] =
    (for {
      documents <- getDocumentsFromIndex(index)
      exists    <- documents.contains(documentId)
    } yield exists).commit

  private def fakeGetById(index: IndexName, documentId: DocumentId): Task[Option[Document]] =
    (for {
      documents     <- getDocumentsFromIndex(index)
      maybeDocument <- documents.get(documentId)
    } yield maybeDocument).commit

  private def fakeGetByQuery(index: IndexName): Task[ElasticQueryResponse] =
    getDocumentsFromIndex(index).flatMap(documents => createElasticQueryResponse(index, documents)).commit

  private def getDocumentsFromIndex(index: IndexName): ZSTM[Any, ElasticException, TMap[DocumentId, Document]] =
    for {
      maybeDocuments <- self.esMap.get(index)
      documents <- maybeDocuments.fold[STM[ElasticException, TMap[DocumentId, Document]]](
                     STM.fail[ElasticException](new ElasticException(s"Index $index does not exists!"))
                   )(STM.succeed(_))
    } yield documents

  private def createElasticQueryResponse(
    index: IndexName,
    documents: TMap[DocumentId, Document]
  ): ZSTM[Any, Nothing, ElasticQueryResponse] = {
    val shards = Shards(total = 1, successful = 1, skipped = 0, failed = 0)

    for {
      items <-
        documents.toList.map(
          _.map(pair =>
            Item(
              index = index.toString,
              `type` = "type",
              id = pair._1.toString,
              score = 1,
              source = Json.Str(pair._2.json)
            )
          )
        )
      hitsSize <- documents.size
      hits      = Hits(total = Total(value = hitsSize, relation = ""), maxScore = 1, hits = items)
    } yield ElasticQueryResponse(took = 1, timedOut = false, shards = shards, hits = hits)
  }
}
