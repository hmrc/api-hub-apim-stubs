/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.apihubapimstubs.controllers.controllers

import play.api.libs.json.Json
import play.api.mvc.MultipartFormData
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apihubapimstubs.controllers.routes
import uk.gov.hmrc.apihubapimstubs.models.simpleapideployment.{CreateMetadata, DeploymentFrom, DeploymentsResponse, DetailsResponse, EgressGateway, EgressMapping, FailuresResponse, UpdateMetadata}

class SimpleAPiDeploymentControllerSpec extends ControllerSpecBase {

  import SimpleAPiDeploymentControllerSpec._

  "validate" - {
    "must return 200 Ok when the OAS document is valid" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentController.validateOas())
          .withHeaders("Content-Type" -> "application/yaml")
          .withBody(oas)
        val result = route(fixture.application, request).value

        status(result) mustBe OK
      }
    }

    "must return 400 BadRequest and a ValidationFailuresResponse when the OAS document is invalid" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentController.validateOas())
          .withHeaders("Content-Type" -> "application/yaml")
          .withBody("rhubarb")
        val result = route(fixture.application, request).value

        status(result) mustBe BAD_REQUEST
        contentAsJson(result) mustBe Json.toJson(FailuresResponse.cannedResponse)
      }
    }
  }

  "deployNewApi" - {
    "must return 200 Ok and a DeploymentsResponse when the request is valid" in {
      val fixture = buildFixture()

      val metadata = CreateMetadata(
        lineOfBusiness = "test-lob",
        name = "test-name",
        description = "test-description",
        egress = "test-egress",
        prefixesToRemove = Some(Seq("test-prefix-1")),
        egressMappings = Some(Seq(EgressMapping("prefix", "egress-prefix")))
      )

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentController.deployNewApi())
          .withMultipartFormDataBody(
            MultipartFormData(
              dataParts = Map(
                "metadata" -> Seq(Json.toJson(metadata).toString()),
                "openapi" -> Seq(oas)
              ),
              files = Seq.empty,
              badParts = Seq.empty
            )
          )

        val result = route(fixture.application, request).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(DeploymentsResponse(metadata.name))
      }
    }

    "must return 400 BadRequest and a ValidationFailuresResponse when the OAS document is invalid" in {
      val fixture = buildFixture()

      val metadata = CreateMetadata(
        lineOfBusiness = "test-lob",
        name = "test-name",
        description = "test-description",
        egress = "test-egress",
        prefixesToRemove = Some(Seq("test-prefix-1")),
        egressMappings = None
      )

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentController.deployNewApi())
          .withMultipartFormDataBody(
            MultipartFormData(
              dataParts = Map(
                "metadata" -> Seq(Json.toJson(metadata).toString()),
                "openapi" -> Seq("rhubarb")
              ),
              files = Seq.empty,
              badParts = Seq.empty
            )
          )

        val result = route(fixture.application, request).value

        status(result) mustBe BAD_REQUEST
        contentAsJson(result) mustBe Json.toJson(FailuresResponse.cannedResponse)
      }
    }

    "must return 400 Bad Request when the metadata is invalid" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentController.deployNewApi())
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

  "update" - {
    "must return 200 Ok and a DeploymentsResponse when the request is valid" in {
      val fixture = buildFixture()

      val serviceId = "test-service-id"

      val metadata = UpdateMetadata(
        description = "test-description",
        status = "test-status",
        prefixesToRemove = Some(Seq("test-prefix-1", "test-prefix-2")),
        egressMappings = Some(Seq(EgressMapping("prefix", "egress-prefix")))
      )

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentController.deployExistingApiWithNewConfiguration(serviceId))
          .withMultipartFormDataBody(
            MultipartFormData(
              dataParts = Map(
                "metadata" -> Seq(Json.toJson(metadata).toString()),
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

    "must return 400 BadRequest and a ValidationFailuresResponse when the OAS document is invalid" in {
      val fixture = buildFixture()

      val serviceId = "test-service-id"

      val metadata = UpdateMetadata(
        description = "test-description",
        status = "test-status",
        prefixesToRemove = Some(Seq("test-prefix-1", "test-prefix-2")),
        egressMappings = Some(Seq(EgressMapping("prefix", "egress-prefix")))
      )

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentController.deployExistingApiWithNewConfiguration(serviceId))
          .withMultipartFormDataBody(
            MultipartFormData(
              dataParts = Map(
                "metadata" -> Seq(Json.toJson(metadata).toString()),
                "openapi" -> Seq("rhubarb")
              ),
              files = Seq.empty,
              badParts = Seq.empty
            )
          )

        val result = route(fixture.application, request).value

        status(result) mustBe BAD_REQUEST
        contentAsJson(result) mustBe Json.toJson(FailuresResponse.cannedResponse)
      }
    }

    "must return 400 Bad Request when the metadata is invalid" in {
      val fixture = buildFixture()

      val serviceId = "test-service-id"

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentController.deployExistingApiWithNewConfiguration(serviceId))
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

  "deploymentFrom" - {
    "must return 200 Ok and a DeploymentsResponse on success" in {
      val fixture = buildFixture()

      val deploymentFrom = DeploymentFrom(
        env = "test-env",
        serviceId = "test-service-id"
      )

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentController.deploymentFrom())
          .withJsonBody(Json.toJson(deploymentFrom))
        val result = route(fixture.application, request).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(DeploymentsResponse.apply(deploymentFrom.serviceId))
      }
    }

    "must return 400 Bad Request if the request body is invalid" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentController.deploymentFrom())
          .withJsonBody(Json.obj())
        val result = route(fixture.application, request).value

        status(result) mustBe BAD_REQUEST
      }
    }
  }

  "getDeploymentDetails" - {
    "must return 200 Ok can a DetailsResponse" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentController.getDeploymentDetails("test-service-id"))
        val result = route(fixture.application, request).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(DetailsResponse.cannedResponse)
      }
    }
  }

  "getEgressGateways" - {
    "must return 200 Ok and a canned response" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val request = FakeRequest(routes.SimpleApiDeploymentController.getEgressGateways())
        val result = route(fixture.application, request).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(EgressGateway.cannedResponse)
      }
    }
  }

}

object SimpleAPiDeploymentControllerSpec {

  val oas: String =
    """
      |openapi: 3.0.0
      |info:
      |  version: 1.0.0
      |  title: Single Path
      |  description: This is a slimmed down single path version of the Petstore definition.
      |servers:
      |  - url: https://httpbin.org
      |paths:
      |  '/pet/{id}':
      |    parameters:
      |      - name: id
      |        in: path
      |        required: true
      |        schema:
      |          type: integer
      |    put:
      |      tags:
      |        - pet
      |      summary: Update a pet
      |      description: This operation will update a pet in the database.
      |      responses:
      |        '400':
      |          description: Invalid id value
      |      security:
      |        - apiKey: []
      |    get:
      |      tags:
      |        - pet
      |      summary: Find a pet
      |      description: This operation will find a pet in the database.
      |      responses:
      |        '400':
      |          description: Invalid status value
      |      security: []
      |components:
      |  securitySchemes:
      |    apiKey:
      |      type: http
      |      scheme: basic
      |""".stripMargin

}
