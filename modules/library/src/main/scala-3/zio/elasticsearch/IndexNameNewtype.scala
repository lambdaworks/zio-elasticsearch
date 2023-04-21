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

import zio.prelude.NewtypeCustom

trait IndexNameNewtype {
  object IndexName extends NewtypeCustom[String] {
    protected def validate(name: String) =
      IndexNameValidator.validate(name)

    protected inline def validateInline(inline name: String) =
      ${ IndexNameValidator.validateInlineImpl('name) }
  }
}
