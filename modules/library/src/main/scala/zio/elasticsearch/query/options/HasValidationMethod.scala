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

package zio.elasticsearch.query.options

import zio.elasticsearch.query.ValidationMethod

private[elasticsearch] trait HasValidationMethod[Q <: HasValidationMethod[Q]] {

  /**
   * Sets the `validationMethod` parameter for this [[zio.elasticsearch.query.ElasticQuery]]. Defines handling of
   * incorrect coordinates.
   *
   * @param value
   *   defines how to handle invalid latitude and longitude:
   *   - [[zio.elasticsearch.query.ValidationMethod.Strict]]: Default method
   *   - [[zio.elasticsearch.query.ValidationMethod.IgnoreMalformed]]: Accepts geo points with invalid latitude or
   *     longitude
   *   - [[zio.elasticsearch.query.ValidationMethod.Coerce]]: Additionally try and infer correct coordinates
   * @return
   *   a new instance of the [[zio.elasticsearch.query.ElasticQuery]] with the `validationMethod` value set.
   */
  def validationMethod(value: ValidationMethod): Q
}
