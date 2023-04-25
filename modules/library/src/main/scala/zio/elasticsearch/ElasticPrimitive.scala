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

import zio.json.ast.Json
import zio.json.ast.Json.{Num, Str}

import java.util.UUID

object ElasticPrimitive {
  sealed trait ElasticPrimitive[A] {
    def toJson(value: A): Json
  }

  implicit object ElasticBigDecimal extends ElasticPrimitive[BigDecimal] {
    def toJson(value: BigDecimal): Json = Num(value)
  }

  implicit object ElasticBool extends ElasticPrimitive[Boolean] {
    def toJson(value: Boolean): Json = Json.Bool(value)
  }

  implicit object ElasticDouble extends ElasticPrimitive[Double] {
    def toJson(value: Double): Json = Num(value)
  }

  implicit object ElasticInt extends ElasticPrimitive[Int] {
    def toJson(value: Int): Json = Num(value)
  }

  implicit object ElasticLong extends ElasticPrimitive[Long] {
    def toJson(value: Long): Json = Num(value)
  }

  implicit object ElasticString extends ElasticPrimitive[String] {
    def toJson(value: String): Json = Str(value)
  }

  implicit object ElasticUUID extends ElasticPrimitive[UUID] {
    def toJson(value: UUID): Json = Str(value.toString)
  }

  final implicit class ElasticPrimitiveOps[A](private val value: A) extends AnyVal {
    def toJson(implicit EP: ElasticPrimitive[A]): Json = EP.toJson(value)
  }
}
