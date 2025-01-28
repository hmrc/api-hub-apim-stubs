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

package uk.gov.hmrc.apihubapimstubs.services

import com.google.inject.{Inject, Singleton}
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.servers.Server
import uk.gov.hmrc.apihubapimstubs.config.AppConfig
import uk.gov.hmrc.apihubapimstubs.util.OpenApiStuff

import scala.jdk.CollectionConverters.*

@Singleton
class OasServersTransformer @Inject()(appConfig: AppConfig) extends OpenApiStuff {

  def addServersOpenApi(oas: String): OpenAPI = {
    val openApi = parseOpenApi(oas)

    openApi.setServers(
      appConfig.servers.map(
        server =>
          new Server().description(server.description).url(server.url)
      ).asJava
    )

    openApi
  }

  def addServersOas(oas: String): String = {
    serialiseOpenApi(addServersOpenApi(oas))
  }

}
