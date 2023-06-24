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

sealed trait GeoShape {
  private[elasticsearch] def toJson: Json
}

final case class GeoLineString(coordinates: Chunk[GeoPoint]) extends GeoShape {
  private[elasticsearch] def toJson: Json =
    Obj(
      "type"        -> Str("linestring"),
      "coordinates" -> Arr(coordinates.map(_.coordinatesToJson))
    )
}

final case class GeoPoint(latitude: Double, longitude: Double) extends GeoShape {
  private[elasticsearch] def toJson: Json =
    Obj(
      "type"        -> Str("point"),
      "coordinates" -> coordinatesToJson
    )

  private[elasticsearch] def coordinatesToJson: Json = Arr(Chunk(Num(longitude), Num(latitude)))
}

final case class GeoPolygon(coordinates: Chunk[GeoPoint]) extends GeoShape {
  private[elasticsearch] def toJson: Json =
    Obj(
      "type"        -> Str("polygon"),
      "coordinates" -> Arr(Arr(coordinates.map(_.coordinatesToJson)))
    )
}
