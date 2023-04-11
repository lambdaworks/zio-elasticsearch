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

package object result {
  class ElasticException(message: String) extends RuntimeException(message)

  final case class DecodingException(message: String) extends ElasticException(message)

  case object UnauthorizedException extends ElasticException("Wrong credentials provided.")

  final case class VersionConflictException(succeeded: Int, failed: Int)
      extends ElasticException(s"There are $failed conflicts in versions. Only $succeeded documents are updated.")
}
