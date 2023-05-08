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
  def shards: Option[ShardsResponse]
  def status: Option[Int]
  def error: Option[Error]
}

@jsonHint("create")
final case class Create private[elasticsearch](
  @jsonField("_index")
  index: String,
  @jsonField("_id")
  id: String,
  @jsonField("_version")
  version: Option[Int],
  result: Option[String],
  @jsonField("_shards")
  shards: Option[ShardsResponse],
  status: Option[Int],
  error: Option[Error]
) extends Item

private[elasticsearch] object Create {
  implicit val decoder: JsonDecoder[Create] = DeriveJsonDecoder.gen[Create]
}

@jsonHint("delete")
final case class Delete private[elasticsearch](
  @jsonField("_index")
  index: String,
  @jsonField("_id")
  id: String,
  @jsonField("_version")
  version: Option[Int],
  result: Option[String],
  @jsonField("_shards")
  shards: Option[ShardsResponse],
  status: Option[Int],
  error: Option[Error]
) extends Item

private[elasticsearch] object Delete {
  implicit val decoder: JsonDecoder[Delete] = DeriveJsonDecoder.gen[Delete]
}

@jsonHint("index")
final case class Index private[elasticsearch](
  @jsonField("_index")
  index: String,
  @jsonField("_id")
  id: String,
  @jsonField("_version")
  version: Option[Int],
  result: Option[String],
  @jsonField("_shards")
  shards: Option[ShardsResponse],
  status: Option[Int],
  error: Option[Error]
) extends Item

private[elasticsearch] object Index {
  implicit val decoder: JsonDecoder[Index] = DeriveJsonDecoder.gen[Index]
}

@jsonHint("update")
final case class Update private[elasticsearch] (
  @jsonField("_index")
  index: String,
  @jsonField("_id")
  id: String,
  @jsonField("_version")
  version: Option[Int],
  result: Option[String],
  @jsonField("_shards")
  shards: Option[ShardsResponse],
  status: Option[Int],
  error: Option[Error]
) extends Item

private[elasticsearch] object Update {
  implicit val decoder: JsonDecoder[Update] = DeriveJsonDecoder.gen[Update]
}

private[elasticsearch] object Item {
  implicit val decoder: JsonDecoder[Item] = DeriveJsonDecoder.gen[Item]
}
