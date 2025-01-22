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
import io.swagger.v3.oas.models.security.*
import uk.gov.hmrc.apihubapimstubs.models.deployment.DeploymentMetadata
import uk.gov.hmrc.apihubapimstubs.models.openapi.IntegrationCatalogueSection
import uk.gov.hmrc.apihubapimstubs.util.OpenApiStuff

import java.time.Clock
import scala.jdk.CollectionConverters.*

@Singleton
class OasTransformer @Inject()(clock: Clock) extends OpenApiStuff {

  import OasTransformer.*

  def transformToConsumerOas(targetOas: String, metadata: DeploymentMetadata): String = {
    serialiseOpenApi(transformToConsumerOpenApi(targetOas, metadata))
  }

  def transformToConsumerOpenApi(targetOas: String, metadata: DeploymentMetadata): OpenAPI = {
    val openApi = parseOpenApi(targetOas)

    setInfo(openApi, metadata)
    setComponents(openApi)
    setPaths(openApi)
    clearServers(openApi)
    setSecurity(openApi)
    generatePathSecurityAndSecuritySchemes(openApi, metadata)

    openApi
  }

  private def setInfo(openApi: OpenAPI, metadata: DeploymentMetadata): Unit = {
    val info = Option(openApi.getInfo).getOrElse(new Info())
    info.addExtension(X_INTEGRATION_CATALOGUE, IntegrationCatalogueSection(metadata, clock))
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

  private def setSecurity(openApi: OpenAPI): Unit = {
    openApi.setSecurity(null)
  }

  private def generatePathSecurityAndSecuritySchemes(
    openApi: OpenAPI,
    metadata: DeploymentMetadata
  ): Unit = {
    val scopeDefinitions = openApi.getPaths.asScala.toSeq
      .flatMap {
        case (name, item) =>
          item.readOperationsMap().asScala
            .map {
              case (method, operation) =>
                val scopeName = buildScopeName(method, operation, metadata)
                operation.security(Seq(buildSecurityRequirement(Seq(scopeName))).asJava)
                ScopeDefinition(scopeName, buildScopeDescription(operation, metadata))
            }
      }

    openApi.getComponents
      .addSecuritySchemes(
        OAUTH2,
        buildOAuth2SecurityScheme(scopeDefinitions)
      )
  }

  private def buildScopeName(
    method: PathItem.HttpMethod,
    operation: Operation,
    metadata: DeploymentMetadata
  ): String = {
    String.format(
      "%s:%s-%s-%s",
      method.name.toLowerCase,
      metadata.lineOfBusiness.toLowerCase,
      metadata.name,
      operation.getOperationId
    )
  }

  private def buildScopeDescription(
    operation: Operation,
    metadata: DeploymentMetadata
  ): String = {
    String.format(
      "Scope for %s %s %s",
      metadata.lineOfBusiness,
      metadata.name,
      operation.getOperationId
    )
  }

  private def buildSecurityRequirement(scopes: Seq[String]): SecurityRequirement = {
    new SecurityRequirement().addList(OAUTH2, scopes.asJava)
  }

  private def buildOAuth2SecurityScheme(scopeDefinitions: Seq[ScopeDefinition]): SecurityScheme = {
    val scopes = new Scopes()

    scopeDefinitions.foreach(
      scopeDefinition =>
        scopes.put(scopeDefinition.scope, scopeDefinition.description)
    )

    new SecurityScheme()
      .`type`(SecurityScheme.Type.OAUTH2)
      .description(CREDENTIALS_FLOW)
      .flows(
        new OAuthFlows()
          .clientCredentials(
            new OAuthFlow()
              .tokenUrl(TOKEN_URL_NOT_REQUIRED)
              .scopes(scopes)
          )
      )
  }

}

object OasTransformer {

  val OAUTH2 = "oAuth2"
  val CREDENTIALS_FLOW = "OAuth2 Client Credentials Flow"
  val TOKEN_URL_NOT_REQUIRED = "/tokenUrl/not-required"
  val X_INTEGRATION_CATALOGUE = "x-integration-catalogue"

  private case class ScopeDefinition(scope: String, description: String)

}
