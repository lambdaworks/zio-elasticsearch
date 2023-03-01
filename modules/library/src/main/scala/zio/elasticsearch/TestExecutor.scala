/*
 * Copyright 2022 LambdaWorks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zio.elasticsearch

import zio.Random.nextUUID
import zio.elasticsearch.ElasticRequest._
import zio.json.ast.Json
import zio.stm.{STM, TMap, USTM, ZSTM}
import zio.{Task, ZIO}

private[elasticsearch] final case class TestExecutor private (data: TMap[IndexName, TMap[DocumentId, Document]])
    extends ElasticExecutor {
  self =>

  def execute[A](request: ElasticRequest[A]): Task[A] =
    request match {
      case Bulk(requests, _, _, _) =>
        fakeBulk(requests)
      case Create(index, document, _, _) =>
        fakeCreate(index, document)
      case CreateWithId(index, id, document, _, _) =>
        fakeCreateWithId(index, id, document)
      case CreateIndex(name, _) =>
        fakeCreateIndex(name)
      case CreateOrUpdate(index, id, document, _, _) =>
        fakeCreateOrUpdate(index, id, document)
      case DeleteById(index, id, _, _) =>
        fakeDeleteById(index, id)
      case DeleteByQuery(index, _, _, _) =>
        fakeDeleteByQuery(index)
      case DeleteIndex(name) =>
        fakeDeleteIndex(name)
      case Exists(index, id, _) =>
        fakeExists(index, id)
      case GetById(index, id, _) =>
        fakeGetById(index, id)
      case GetByQuery(index, _, _) =>
        fakeGetByQuery(index)
    }

  private def fakeBulk(requests: List[BulkableRequest[_]]): Task[Unit] =
    ZIO.attempt {
      requests.map { r =>
        execute(r)
      }
    }.unit

  private def fakeCreate(index: IndexName, document: Document): Task[DocumentId] =
    for {
      uuid      <- nextUUID
      documents <- getDocumentsFromIndex(index).commit
      documentId = DocumentId(uuid.toString)
      _         <- documents.put(documentId, document).commit
    } yield documentId

  private def fakeCreateWithId(index: IndexName, documentId: DocumentId, document: Document): Task[CreationOutcome] =
    (for {
      documents     <- getDocumentsFromIndex(index)
      alreadyExists <- documents.contains(documentId)
      _             <- documents.putIfAbsent(documentId, document)
    } yield if (alreadyExists) AlreadyExists else Created).commit

  private def fakeCreateIndex(index: IndexName): Task[CreationOutcome] =
    (for {
      alreadyExists  <- self.data.contains(index)
      emptyDocuments <- TMap.empty[DocumentId, Document]
      _              <- self.data.putIfAbsent(index, emptyDocuments)
    } yield if (alreadyExists) AlreadyExists else Created).commit

  private def fakeCreateOrUpdate(index: IndexName, documentId: DocumentId, document: Document): Task[Unit] =
    (for {
      documents <- getDocumentsFromIndex(index)
      _         <- documents.put(documentId, document)
    } yield ()).commit

  private def fakeDeleteById(index: IndexName, documentId: DocumentId): Task[DeletionOutcome] =
    (for {
      documents <- getDocumentsFromIndex(index)
      exists    <- documents.contains(documentId)
      _         <- documents.delete(documentId)
    } yield if (exists) Deleted else NotFound).commit

  private def fakeDeleteByQuery(index: IndexName): Task[DeletionOutcome] =
    (for {
      exists <- self.data.contains(index)
    } yield if (exists) Deleted else NotFound).commit
  // until we have a way of using query to delete we can either delete all or delete none documents

  private def fakeDeleteIndex(index: IndexName): Task[DeletionOutcome] =
    (for {
      exists <- self.data.contains(index)
      _      <- self.data.delete(index)
    } yield if (exists) Deleted else NotFound).commit

  private def fakeExists(index: IndexName, documentId: DocumentId): Task[Boolean] =
    (for {
      documents <- getDocumentsFromIndex(index)
      exists    <- documents.contains(documentId)
    } yield exists).commit

  private def fakeGetById(index: IndexName, documentId: DocumentId): Task[GetResult] =
    (for {
      documents     <- getDocumentsFromIndex(index)
      maybeDocument <- documents.get(documentId)
    } yield new GetResult(maybeDocument)).commit

  private def fakeGetByQuery(index: IndexName): Task[SearchResult] = {
    def createSearchResult(
      index: IndexName,
      documents: TMap[DocumentId, Document]
    ): USTM[SearchResult] =
      for {
        items <-
          documents.toList.map(
            _.map { case (id, document) =>
              Item(
                index = index.toString,
                `type` = "type",
                id = id.toString,
                score = 1,
                source = Json.Str(document.json)
              )
            }
          )
      } yield new SearchResult(items.map(_.source.toString).map(Document(_)))

    (for {
      documents <- getDocumentsFromIndex(index)
      response  <- createSearchResult(index, documents)
    } yield response).commit
  }

  private def getDocumentsFromIndex(index: IndexName): ZSTM[Any, ElasticException, TMap[DocumentId, Document]] =
    for {
      maybeDocuments <- self.data.get(index)
      documents <- maybeDocuments.fold[STM[ElasticException, TMap[DocumentId, Document]]](
                     STM.fail[ElasticException](new ElasticException(s"Index $index does not exists!"))
                   )(STM.succeed(_))
    } yield documents
}