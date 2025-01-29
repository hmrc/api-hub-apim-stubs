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

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.apihubapimstubs.TestData
import uk.gov.hmrc.apihubapimstubs.models.exception.DeploymentNotFoundException

import scala.concurrent.Future

class OasDiscoveryApiControllerSpec extends ControllerSpecBase with TestData {

  "getOpenApiDeployments" - {
    "must return 200 Ok and a list of ApiDeployment" in {
      val fixture = buildFixture()
      val deployment1 = buildDeployment(1, clock).toApiDeployment
      val deployment2 = buildDeployment(2, clock).toApiDeployment

      when(fixture.oasDiscoveryService.getOpenApiDeployments(eqTo(environment)))
        .thenReturn(Future.successful(Seq(deployment1, deployment2)))

      running(fixture.application) {
        val request = FakeRequest(routes.OasDiscoveryApiController.getOpenApiDeployments(environment))
          .withHeaders(authorizationHeader)
        val result = route(fixture.application, request).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(Seq(deployment1 ,deployment2))
      }
    }
  }

  "getOpenApiDeployment" - {
    "must return 200 Ok and an ApiDeploymentDetail when the deployment exists" in {
      val fixture = buildFixture()
      val deployment = buildDeployment(1, clock).toApiDeploymentDetail

      when(fixture.oasDiscoveryService.getOpenApiDeployment(eqTo(environment), eqTo(deployment.id)))
        .thenReturn(Future.successful(Right(deployment)))

      running(fixture.application) {
        val request = FakeRequest(routes.OasDiscoveryApiController.getOpenApiDeployment(environment, deployment.id))
          .withHeaders(authorizationHeader)
        val result = route(fixture.application, request).value

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(deployment)
      }
    }

    "must return 404 Not Found when the deployment does not exist" in {
      val fixture = buildFixture()
      val id = "test-id"
      val exception = DeploymentNotFoundException.forService(environment, id)

      when(fixture.oasDiscoveryService.getOpenApiDeployment(eqTo(environment), eqTo(id)))
        .thenReturn(Future.successful(Left(exception)))

      running(fixture.application) {
        val request = FakeRequest(routes.OasDiscoveryApiController.getOpenApiDeployment(environment, id))
          .withHeaders(authorizationHeader)
        val result = route(fixture.application, request).value

        status(result) mustBe NOT_FOUND
      }
    }
  }

  "getOpenApiSpecification" - {
    "must return 200 Ok and the OAS document when the deployment exists" in {
      val fixture = buildFixture()
      val id = "test-id"

      when(fixture.oasDiscoveryService.getOpenApiSpecification(eqTo(environment), eqTo(id)))
        .thenReturn(Future.successful(Right(baseOas)))

      running(fixture.application) {
        val request = FakeRequest(routes.OasDiscoveryApiController.getOpenApiSpecification(environment, id))
          .withHeaders(authorizationHeader)
        val result = route(fixture.application, request).value

        status(result) mustBe OK
        contentAsString(result) mustBe baseOas
      }
    }

    "must return 404 Not Found when the deployment does not exist" in {
      val fixture = buildFixture()
      val id = "test-id"
      val exception = DeploymentNotFoundException.forService(environment, id)

      when(fixture.oasDiscoveryService.getOpenApiSpecification(eqTo(environment), eqTo(id)))
        .thenReturn(Future.successful(Left(exception)))

      running(fixture.application) {
        val request = FakeRequest(routes.OasDiscoveryApiController.getOpenApiSpecification(environment, id))
          .withHeaders(authorizationHeader)
        val result = route(fixture.application, request).value

        status(result) mustBe NOT_FOUND
      }
    }
  }

}
