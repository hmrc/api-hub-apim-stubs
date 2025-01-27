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

package uk.gov.hmrc.apihubapimstubs.controllers

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.mvc.MultipartFormData
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.apihubapimstubs.models.deployment.Deployment
import uk.gov.hmrc.apihubapimstubs.models.exception.{DeploymentFailedException, DeploymentNotFoundException}
import uk.gov.hmrc.apihubapimstubs.models.simpleapideployment.*

import java.time.{Clock, Instant}
import scala.concurrent.Future

class SimpleApiDeploymentControllerV2Spec extends ControllerSpecBase {

  import SimpleApiDeploymentControllerV2Spec.*

  "validate" - {
    "must return 200 Ok when the OAS document is valid" in {
      val fixture = buildFixture()

      when(fixture.simpleApiDeploymentService.validateOas(eqTo(environment), eqTo(oas)))
        .thenReturn(Right(()))

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentControllerV2.validateOas(environment))
          .withHeaders("Content-Type" -> "application/yaml", authorizationHeader)
          .withBody(oas)
        val result = route(fixture.application, request).value

        status(result) mustBe OK
      }
    }

    "must return 400 BadRequest and a ValidationFailuresResponse when the OAS document is invalid" in {
      val fixture = buildFixture()

      when(fixture.simpleApiDeploymentService.validateOas(eqTo(environment), eqTo(invalidOas)))
        .thenReturn(Left(DeploymentFailedException.forFailuresResponse(FailuresResponse.invalidOas)))

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentControllerV2.validateOas(environment))
          .withHeaders("Content-Type" -> "application/yaml", authorizationHeader)
          .withBody(invalidOas)
        val result = route(fixture.application, request).value

        status(result) mustBe BAD_REQUEST
        contentAsJson(result) mustBe Json.toJson(FailuresResponse.invalidOas)
      }
    }
  }

  "deployNewApi" - {
    "must return 200 Ok and a DeploymentsResponse when the request is valid" in {
      val fixture = buildFixture()

      when(fixture.simpleApiDeploymentService.deployNewApi(eqTo(environment), eqTo(createMetadata), eqTo(oas))(any))
        .thenReturn(Future.successful(Right(DeploymentsResponse(serviceId))))

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentControllerV2.deployNewApi(environment))
          .withHeaders(authorizationHeader)
          .withMultipartFormDataBody(
            MultipartFormData(
              dataParts = Map(
                "metadata" -> Seq(Json.toJson(createMetadata).toString()),
                "openapi" -> Seq(oas)
              ),
              files = Seq.empty,
              badParts = Seq.empty
            )
          )

        val result = route(fixture.application, request).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(DeploymentsResponse(createMetadata.name))
      }
    }

    "must return 400 BadRequest and a ValidationFailuresResponse when the OAS document is invalid" in {
      val fixture = buildFixture()

      when(fixture.simpleApiDeploymentService.deployNewApi(eqTo(environment), eqTo(createMetadata), eqTo(invalidOas))(any))
        .thenReturn(Future.successful(Left(DeploymentFailedException.forFailuresResponse(FailuresResponse.invalidOas))))

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentControllerV2.deployNewApi(environment))
          .withHeaders(authorizationHeader)
          .withMultipartFormDataBody(
            MultipartFormData(
              dataParts = Map(
                "metadata" -> Seq(Json.toJson(createMetadata).toString()),
                "openapi" -> Seq(invalidOas)
              ),
              files = Seq.empty,
              badParts = Seq.empty
            )
          )

        val result = route(fixture.application, request).value

        status(result) mustBe BAD_REQUEST
        contentAsJson(result) mustBe Json.toJson(FailuresResponse.invalidOas)
      }
    }

    "must return 400 Bad Request when the metadata is invalid" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentControllerV2.deployNewApi(environment))
          .withHeaders(authorizationHeader)
          .withMultipartFormDataBody(
            MultipartFormData(
              dataParts = Map(
                "metadata" -> Seq(Json.obj().toString()),
                "openapi" -> Seq(oas)
              ),
              files = Seq.empty,
              badParts = Seq.empty
            )
          )

        val result = route(fixture.application, request).value

        status(result) mustBe BAD_REQUEST
      }
    }
  }

  "getDeploymentDetails" - {
    "must return 200 Ok and a DetailsResponse when the deployment exists" in {
      val fixture = buildFixture()
      val deployment = buildDeployment(clock)

      when(fixture.simpleApiDeploymentService.getDeploymentDetails(eqTo(environment), eqTo(serviceId)))
        .thenReturn(Future.successful(Right(deployment.toDetailsResponse)))

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentControllerV2.getDeploymentDetails(environment, serviceId))
          .withHeaders(authorizationHeader)
        val result = route(fixture.application, request).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(deployment.toDetailsResponse)
      }
    }

    "must return 404 Not Found when the deployment does not exist" in {
      val fixture = buildFixture()

      when(fixture.simpleApiDeploymentService.getDeploymentDetails(eqTo(environment), eqTo(serviceId)))
        .thenReturn(Future.successful(Left(DeploymentNotFoundException.forService(environment, serviceId))))

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentControllerV2.getDeploymentDetails(environment, serviceId))
          .withHeaders(authorizationHeader)
        val result = route(fixture.application, request).value

        status(result) mustBe NOT_FOUND
      }
    }
  }

  "deployExistingApiWithNewConfiguration" - {
    "must return 200 Ok and a DeploymentsResponse when the request is valid" in {
      val fixture = buildFixture()

      when(
        fixture.simpleApiDeploymentService.deployExistingApiWithNewConfiguration(
          eqTo(environment),
          eqTo(serviceId),
          eqTo(updateMetadata),
          eqTo(oas)
        )(any)
      ).thenReturn(Future.successful(Right(DeploymentsResponse(serviceId))))

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentControllerV2.deployExistingApiWithNewConfiguration(environment, serviceId))
          .withHeaders(authorizationHeader)
          .withMultipartFormDataBody(
            MultipartFormData(
              dataParts = Map(
                "metadata" -> Seq(Json.toJson(updateMetadata).toString()),
                "openapi" -> Seq(oas)
              ),
              files = Seq.empty,
              badParts = Seq.empty
            )
          )

        val result = route(fixture.application, request).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(DeploymentsResponse(serviceId))
      }
    }

    "must return 404 Not Found when the deployment does not exist" in {
      val fixture = buildFixture()

      when(
        fixture.simpleApiDeploymentService.deployExistingApiWithNewConfiguration(
          eqTo(environment),
          eqTo(serviceId),
          eqTo(updateMetadata),
          eqTo(oas)
        )(any)
      ).thenReturn(Future.successful(Left(DeploymentNotFoundException.forService(environment, serviceId))))

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentControllerV2.deployExistingApiWithNewConfiguration(environment, serviceId))
          .withHeaders(authorizationHeader)
          .withMultipartFormDataBody(
            MultipartFormData(
              dataParts = Map(
                "metadata" -> Seq(Json.toJson(updateMetadata).toString()),
                "openapi" -> Seq(oas)
              ),
              files = Seq.empty,
              badParts = Seq.empty
            )
          )

        val result = route(fixture.application, request).value

        status(result) mustBe NOT_FOUND
      }
    }

    "must return 400 BadRequest and a ValidationFailuresResponse when the OAS document is invalid" in {
      val fixture = buildFixture()

      when(
        fixture.simpleApiDeploymentService.deployExistingApiWithNewConfiguration(
          eqTo(environment),
          eqTo(serviceId),
          eqTo(updateMetadata),
          eqTo(invalidOas)
        )(any)
      ).thenReturn(Future.successful(Left(DeploymentFailedException.forFailuresResponse(FailuresResponse.invalidOas))))

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentControllerV2.deployExistingApiWithNewConfiguration(environment, serviceId))
          .withHeaders(authorizationHeader)
          .withMultipartFormDataBody(
            MultipartFormData(
              dataParts = Map(
                "metadata" -> Seq(Json.toJson(updateMetadata).toString()),
                "openapi" -> Seq(invalidOas)
              ),
              files = Seq.empty,
              badParts = Seq.empty
            )
          )

        val result = route(fixture.application, request).value

        status(result) mustBe BAD_REQUEST
        contentAsJson(result) mustBe Json.toJson(FailuresResponse.invalidOas)
      }
    }

    "must return 400 Bad Request when the metadata is invalid" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentControllerV2.deployExistingApiWithNewConfiguration(environment, serviceId))
          .withHeaders(authorizationHeader)
          .withMultipartFormDataBody(
            MultipartFormData(
              dataParts = Map(
                "metadata" -> Seq(Json.obj().toString()),
                "openapi" -> Seq(oas)
              ),
              files = Seq.empty,
              badParts = Seq.empty
            )
          )

        val result = route(fixture.application, request).value

        status(result) mustBe BAD_REQUEST
      }
    }
  }

}

private object SimpleApiDeploymentControllerV2Spec {

  val environment: String = "test-environment"
  val serviceId: String = "test-service-id"
  val invalidOas: String = "xxx"
  val oasVersion: String = "1.2.3"
  val defaultVersion = "0.1.0"

  val oas: String =
    s"""
       |openapi: 3.0.3
       |info:
       |  version: $oasVersion
       |""".stripMargin

  val createMetadata: CreateMetadata = CreateMetadata(
    lineOfBusiness = "test-lob",
    name = serviceId,
    description = "test-description",
    egress = "test-egress",
    passthrough = false,
    status = "test-status",
    domain = "test-domain",
    subdomain = "test-sub-domain",
    backends = Seq("test-backend"),
    egressMappings = Seq(EgressMapping("test-prefix", "test-egress-prefix")),
    prefixesToRemove = Seq("test-prefix-to-remove")
  )

  val updateMetadata: UpdateMetadata = UpdateMetadata(
    description = "test-description-updated",
    status = "test-status-updated",
    egress = "test-egress-updated",
    domain = "test-domain-updated",
    subdomain = "test-sub-domain-updated",
    backends = Seq("test-backend-updated"),
    egressMappings = Seq(EgressMapping("test-prefix-updated", "test-egress-prefix-updated")),
    prefixesToRemove = Seq("test-prefix-to-remove-updated")
  )

  def buildDeployment(clock: Clock): Deployment = Deployment(
    id = None,
    environment = environment,
    lineOfBusiness = createMetadata.lineOfBusiness,
    name = createMetadata.name,
    description = createMetadata.description,
    egress = createMetadata.egress,
    passthrough = createMetadata.passthrough,
    status = createMetadata.status,
    apiType = createMetadata.apiType,
    domain = createMetadata.domain,
    subdomain = createMetadata.subdomain,
    backends = createMetadata.backends,
    egressMappings = createMetadata.egressMappings,
    prefixesToRemove = createMetadata.prefixesToRemove,
    deploymentTimestamp = Instant.now(clock),
    deploymentVersion = defaultVersion,
    oasVersion = oasVersion,
    buildVersion = defaultVersion,
    oas = oas
  )

}
