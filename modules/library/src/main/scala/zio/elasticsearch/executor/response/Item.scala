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

package zio.elasticsearch.executor.response

import zio.json.{DeriveJsonDecoder, JsonDecoder, jsonField, jsonHint}

sealed trait Item {
  def index: String
  def id: String
  def version: Option[Int]
  def result: Option[String]
  def shards: Option[Shards]
  def status: Option[Int]
  def error: Option[Error]
}

private[elasticsearch] object Item {
  implicit val decoder: JsonDecoder[Item] = DeriveJsonDecoder.gen[Item]
}

@jsonHint("create")
final case class CreateBulkResponse private[elasticsearch] (
  @jsonField("_index")
  index: String,
  @jsonField("_id")
  id: String,
  @jsonField("_version")
  version: Option[Int],
  result: Option[String],
  @jsonField("_shards")
  shards: Option[Shards],
  status: Option[Int],
  error: Option[Error]
) extends Item

private[elasticsearch] object CreateBulkResponse {
  implicit val decoder: JsonDecoder[CreateBulkResponse] = DeriveJsonDecoder.gen[CreateBulkResponse]
}

@jsonHint("delete")
final case class DeleteBulkResponse private[elasticsearch] (
  @jsonField("_index")
  index: String,
  @jsonField("_id")
  id: String,
  @jsonField("_version")
  version: Option[Int],
  result: Option[String],
  @jsonField("_shards")
  shards: Option[Shards],
  status: Option[Int],
  error: Option[Error]
) extends Item

private[elasticsearch] object DeleteBulkResponse {
  implicit val decoder: JsonDecoder[DeleteBulkResponse] = DeriveJsonDecoder.gen[DeleteBulkResponse]
}

@jsonHint("index")
final case class IndexBulkResponse private[elasticsearch] (
  @jsonField("_index")
  index: String,
  @jsonField("_id")
  id: String,
  @jsonField("_version")
  version: Option[Int],
  result: Option[String],
  @jsonField("_shards")
  shards: Option[Shards],
  status: Option[Int],
  error: Option[Error]
) extends Item

private[elasticsearch] object IndexBulkResponse {
  implicit val decoder: JsonDecoder[IndexBulkResponse] = DeriveJsonDecoder.gen[IndexBulkResponse]
}

@jsonHint("update")
final case class UpdateBulkResponse private[elasticsearch] (
  @jsonField("_index")
  index: String,
  @jsonField("_id")
  id: String,
  @jsonField("_version")
  version: Option[Int],
  result: Option[String],
  @jsonField("_shards")
  shards: Option[Shards],
  status: Option[Int],
  error: Option[Error]
) extends Item

private[elasticsearch] object UpdateBulkResponse {
  implicit val decoder: JsonDecoder[UpdateBulkResponse] = DeriveJsonDecoder.gen[UpdateBulkResponse]
}
