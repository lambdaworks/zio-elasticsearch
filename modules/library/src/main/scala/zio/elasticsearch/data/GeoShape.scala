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

package zio.elasticsearch.data

import zio.Chunk
import zio.json.ast.Json
import zio.json.ast.Json.{Arr, Num, Obj, Str}

private[elasticsearch] sealed trait GeoShape {
  def toJson: Json
}

private[elasticsearch] final case class GeoPoint(latitude: Double, longitude: Double) extends GeoShape {
  override def toJson: Json = Obj(
    "type"        -> Str("point"),
    "coordinates" -> coordinatesToJson
  )

  def coordinatesToJson: Json = Arr(Chunk(Num(longitude), Num(latitude)))
}

private[elasticsearch] final case class GeoLineString(coordinates: Chunk[GeoPoint]) extends GeoShape {
  override def toJson: Json = Obj(
    "type"        -> Str("linestring"),
    "coordinates" -> Arr(coordinates.map(_.coordinatesToJson))
  )
}

private[elasticsearch] final case class GeoPolygon(coordinates: Chunk[GeoPoint]) extends GeoShape {
  override def toJson: Json = Obj(
    "type"        -> Str("polygon"),
    "coordinates" -> Arr(Arr(coordinates.map(_.coordinatesToJson)))
  )
}
