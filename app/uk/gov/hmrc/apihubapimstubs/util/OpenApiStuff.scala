/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.apihubapimstubs.util

import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions

trait OpenApiStuff {

  def serialiseOpenApi(openApi: OpenAPI): String = {
    val mapper = Yaml.mapper()
    mapper.registerModule(DefaultScalaModule)
    mapper.writerWithDefaultPrettyPrinter.writeValueAsString(openApi)
  }

  def parseOpenApi(oas: String): OpenAPI = {
    val options: ParseOptions = new ParseOptions()
    options.setResolve(false)
    new OpenAPIV3Parser().readContents(oas, null, options).getOpenAPI
  }

  def isValidOas(oas: String): Boolean = {
    Option(parseOpenApi(oas)).isDefined
  }

}
