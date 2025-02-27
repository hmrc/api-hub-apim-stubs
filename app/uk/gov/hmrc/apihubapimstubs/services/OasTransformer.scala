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
import io.swagger.v3.oas.models.*
import io.swagger.v3.oas.models.info.Info
import uk.gov.hmrc.apihubapimstubs.models.openapi.IntegrationCatalogueSection
import uk.gov.hmrc.apihubapimstubs.models.simpleapideployment.CreateMetadata
import uk.gov.hmrc.apihubapimstubs.util.OpenApiStuff

import java.time.Clock
import scala.jdk.CollectionConverters.*

@Singleton
class OasTransformer @Inject()(clock: Clock) extends OpenApiStuff {

  import OasTransformer.*

  def transformToConsumerOas(targetOas: String, createMetadata: CreateMetadata): String = {
    serialiseOpenApi(transformToConsumerOpenApi(targetOas, createMetadata))
  }

  def transformToConsumerOpenApi(targetOas: String, createMetadata: CreateMetadata): OpenAPI = {
    val openApi = parseOpenApi(targetOas)

    setInfo(openApi, createMetadata)
    setComponents(openApi)
    setPaths(openApi)
    clearServers(openApi)

    openApi
  }

  private def setInfo(openApi: OpenAPI, createMetadata: CreateMetadata): Unit = {
    val info = Option(openApi.getInfo).getOrElse(new Info())
    info.addExtension(X_INTEGRATION_CATALOGUE, IntegrationCatalogueSection(createMetadata, clock))
    openApi.setInfo(info)
  }

  private def setComponents(openApi: OpenAPI): Unit = {
    openApi.setComponents(Option(openApi.getComponents).getOrElse(new Components()))
  }

  private def setPaths(openApi: OpenAPI): Unit = {
    openApi.setPaths(Option(openApi.getPaths).getOrElse(new Paths()))
  }

  private def clearServers(openApi: OpenAPI): Unit = {
    openApi.setServers(Seq.empty.asJava)
  }

}

object OasTransformer {

  val OAUTH2 = "oAuth2"
  val CREDENTIALS_FLOW = "OAuth2 Client Credentials Flow"
  val TOKEN_URL_NOT_REQUIRED = "/tokenUrl/not-required"
  val X_INTEGRATION_CATALOGUE = "x-integration-catalogue"

}
