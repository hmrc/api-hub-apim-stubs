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

import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.PathItem.HttpMethod.*
import io.swagger.v3.oas.models.security.*
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.{OpenAPI, Operation, PathItem, Paths}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.apihubapimstubs.models.openapi.IntegrationCatalogueSection
import uk.gov.hmrc.apihubapimstubs.models.simpleapideployment.CreateMetadata
import uk.gov.hmrc.apihubapimstubs.services.OasTransformer.TOKEN_URL_NOT_REQUIRED
import uk.gov.hmrc.apihubapimstubs.util.OpenApiStuff

import java.time.{Clock, Instant, ZoneId}
import scala.jdk.CollectionConverters.*

class OasTransformerSpec extends AnyFreeSpec with Matchers with OpenApiStuff {

  import OasTransformerSpec.*

  "transformToConsumerOas" - {
    "must return the correct OAS document" in {
      val fixture = buildFixture()
      val openApi = fixture.oasTransformer.transformToConsumerOpenApi(baseOas, createMetadata)
      val expected = serialiseOpenApi(openApi)
      val actual = fixture.oasTransformer.transformToConsumerOas(baseOas, createMetadata)

      actual mustBe expected
    }
  }

  "transformToConsumerOpenApi" - {
    "must add missing sections to an otherwise empty OAS document" in {
      val fixture = buildFixture()
      val openApi = fixture.oasTransformer.transformToConsumerOpenApi(baseOas, createMetadata)

      openApi.getInfo must not be null
      openApi.getComponents must not be null
      openApi.getPaths must not be null
      openApi.getServers must not be null
    }

    "must add the x-integration-catalogue section" in {
      val fixture = buildFixture()
      val openApi = fixture.oasTransformer.transformToConsumerOpenApi(baseOas, createMetadata)
      val expected = IntegrationCatalogueSection(createMetadata, clock)

      val actual = openApi.getInfo.getExtensions.get(OasTransformer.X_INTEGRATION_CATALOGUE)

      actual mustBe expected
    }

    "must clear the servers section" in {
      val fixture = buildFixture()

      val openApiWithServers = parseOpenApi(baseOas)
      openApiWithServers.setServers(
        (1 to 2)
          .map(i => new Server().url(s"test-url-$i"))
          .asJava
      )

      val openApi = fixture.oasTransformer.transformToConsumerOpenApi(
        serialiseOpenApi(openApiWithServers),
        createMetadata
      )

      openApi.getServers mustBe empty
    }

    "must null the security section" in {
      val fixture = buildFixture()

      val openApiWithSecurity = parseOpenApi(baseOas)
      openApiWithSecurity.setSecurity(Seq(new SecurityRequirement()).asJava)

      val openApi = fixture.oasTransformer.transformToConsumerOpenApi(
        serialiseOpenApi(openApiWithSecurity),
        createMetadata
      )

      openApi.getSecurity must be (null)
    }

    "must add security requirements to all endpoints" in {
      val fixture = buildFixture()

      val openApiWithPaths = buildOpenApiWithPaths()

      val openApi = fixture.oasTransformer.transformToConsumerOpenApi(
        serialiseOpenApi(openApiWithPaths),
        createMetadata
      )

      val operation1 = buildOperationWithSecurityRequirement(1, operation1HttpMethod)
      val operation2 = buildOperationWithSecurityRequirement(2, operation2HttpMethod)
      val operation3 = buildOperationWithSecurityRequirement(3, operation3HttpMethod)

      val pathItem1 = buildPathItem((operation1HttpMethod, operation1), (operation2HttpMethod, operation2))
      val pathItem2 = buildPathItem((operation3HttpMethod, operation3))

      val expected = new Paths()
      expected.addPathItem(buildPathItemName(1), pathItem1)
      expected.addPathItem(buildPathItemName(2), pathItem2)

      openApi.getPaths mustBe expected
    }

    "must add the OAuth2 security scheme" in {
      val fixture = buildFixture()

      val openApiWithPaths = buildOpenApiWithPaths()

      val openApi = fixture.oasTransformer.transformToConsumerOpenApi(
        serialiseOpenApi(openApiWithPaths),
        createMetadata
      )

      val operation1 = buildOperationWithSecurityRequirement(1, operation1HttpMethod)
      val operation2 = buildOperationWithSecurityRequirement(2, operation2HttpMethod)
      val operation3 = buildOperationWithSecurityRequirement(3, operation3HttpMethod)

      val scopes = new Scopes()

      scopes.put(
        buildScopeName(operation1HttpMethod, operation1),
        buildScopeDescription(operation1)
      )

      scopes.put(
        buildScopeName(operation2HttpMethod, operation2),
        buildScopeDescription(operation2)
      )

      scopes.put(
        buildScopeName(operation3HttpMethod, operation3),
        buildScopeDescription(operation3)
      )

      val securityScheme = new SecurityScheme()
      securityScheme.setType(SecurityScheme.Type.OAUTH2)
      securityScheme.setDescription(OasTransformer.CREDENTIALS_FLOW)
      securityScheme.setFlows(
        new OAuthFlows()
          .clientCredentials(
            new OAuthFlow()
              .tokenUrl(TOKEN_URL_NOT_REQUIRED)
              .scopes(scopes)
          )
      )

      val expected = Map(
        OasTransformer.OAUTH2 -> securityScheme
      ).asJava

      openApi.getComponents.getSecuritySchemes mustBe expected
    }
  }

}

private object OasTransformerSpec extends OpenApiStuff {

  val clock: Clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

  case class Fixture(clock: Clock, oasTransformer: OasTransformer)

  def buildFixture(): Fixture = {
    Fixture(clock, new OasTransformer(clock))
  }

  val createMetadata: CreateMetadata = CreateMetadata(
    lineOfBusiness = "test-lob",
    name = "test-name",
    description = "test-description",
    egress = "test-egress",
    passthrough = false,
    status = "test-status",
    domain = "test-domain",
    subdomain = "test-sub-domain",
    backends = Seq("test-backend-1", "test-backend-2"),
    egressMappings = Seq.empty,
    prefixesToRemove = Seq.empty
  )

  val baseOas = "openapi: 3.0.3"

  val operation1HttpMethod: HttpMethod = GET
  val operation2HttpMethod: HttpMethod = POST
  val operation3HttpMethod: HttpMethod = DELETE

  def buildOperation(index: Int): Operation = {
    new Operation().operationId(s"test-operation-id-$index")
  }

  def buildOperationWithSecurityRequirement(index: Int, httpMethod: HttpMethod): Operation = {
    val operation = buildOperation(index)

    operation.setSecurity(
      Seq(
        new SecurityRequirement().addList(
          OasTransformer.OAUTH2,
          Seq(buildScopeName(httpMethod, operation)).asJava
        )
      ).asJava
    )

    operation
  }

  def buildPathItem(operations: (HttpMethod, Operation)*): PathItem = {
    val pathItem = new PathItem()

    operations.foreach {
      case (httpMethod, operation) =>
        pathItem.operation(httpMethod, operation)
    }

    pathItem
  }

  def buildPathItemName(index: Int): String = {
    s"test-name-$index"
  }

  def buildScopeName(httpMethod: HttpMethod, operation: Operation): String = {
    s"${httpMethod.name.toLowerCase}:${createMetadata.lineOfBusiness.toLowerCase}-${createMetadata.name}-${operation.getOperationId}"
  }

  def buildScopeDescription(operation: Operation): String = {
    s"Scope for ${createMetadata.lineOfBusiness} ${createMetadata.name} ${operation.getOperationId}"
  }

  def buildOpenApiWithPaths(): OpenAPI = {
    val openApiWithPaths = parseOpenApi(baseOas)
    val paths = new Paths()

    val operation1 = buildOperation(1)
    val operation2 = buildOperation(2)
    val operation3 = buildOperation(3)

    val pathItem1 = buildPathItem((operation1HttpMethod, operation1), (operation2HttpMethod, operation2))
    val pathItem2 = buildPathItem((operation3HttpMethod, operation3))

    paths.addPathItem(buildPathItemName(1), pathItem1)
    paths.addPathItem(buildPathItemName(2), pathItem2)

    openApiWithPaths.setPaths(paths)

    openApiWithPaths
  }

}
