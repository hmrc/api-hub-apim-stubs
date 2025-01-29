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

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.apihubapimstubs.models.idms.*

import scala.concurrent.Future

class IdmsControllerSpec extends ControllerSpecBase {

  import IdmsControllerSpec.*

  "retrieve Client secret" - {
    "must return 200 and secret json for a valid request" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val clientId = "CLIENTID123"
        val expected = ClientSecretResponse("client-secret-123456-123456")

        val request = FakeRequest(GET, routes.IdmsController.getClientSecret(environment, clientId).url)
          .withHeaders(authorizationHeader)

        when(fixture.idmsService.getClientSecret(clientId)).thenReturn(Future.successful(Some(expected)))
        val result = route(fixture.application, request).value
        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(expected)
      }
    }

    "must return Not Found when non existing clientId is specified" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val clientId = "CLIENTID123"
        val request = FakeRequest(GET, routes.IdmsController.getClientSecret(environment, clientId).url)
          .withHeaders(authorizationHeader)

        when(fixture.idmsService.getClientSecret(clientId)).thenReturn(Future.successful(None))
        val result = route(fixture.application, request).value
        status(result) mustBe Status.NOT_FOUND
      }
    }

    "must return 401 Unauthorized if no valid credentials are presented" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val clientId = "CLIENTID123"
        val request = FakeRequest(GET, routes.IdmsController.getClientSecret(environment, clientId).url)

        val result = route(fixture.application, request).value
        status(result) mustBe Status.UNAUTHORIZED
      }
    }
  }

  "createClient" - {
    "must return Created and a ClientResponse for a valid request" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val client = Client(
          applicationName = "test-application-name",
          description = "test-description"
        )
        val json = Json.toJson(client)
        val request: Request[JsValue] = FakeRequest(POST, routes.IdmsController.createClient(environment).url)
          .withHeaders(
            CONTENT_TYPE -> "application/json",
            authorizationHeader
          ).withBody(json)

        val expected = ClientResponse("CLIENTID123", "SECRET123")

        when(fixture.idmsService.createClient(any[Identity]))
          .thenReturn(Future.successful(Some(expected)))

        val result = route(fixture.application, request).value
        status(result) mustBe Status.CREATED
        contentAsJson(result) mustBe Json.toJson(expected)
      }

    }

    "must return Bad Request for an invalid request for creating a new client identity" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val request = FakeRequest(
          POST,
          routes.IdmsController.createClient(environment).url
        )
          .withHeaders(
            CONTENT_TYPE -> "application/json",
            authorizationHeader
          )
          .withBody(Json.parse("{}"))

        val result = route(fixture.application, request).value
        status(result) mustBe Status.BAD_REQUEST
      }
    }

    "must return 401 Unauthorized if no valid credentials are presented" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val request = FakeRequest(POST, routes.IdmsController.createClient(environment).url)
          .withHeaders(
            CONTENT_TYPE -> "application/json"
          ).withBody(Json.parse("{}"))

        val result = route(fixture.application, request).value
        status(result) mustBe Status.UNAUTHORIZED
      }
    }
  }

  "deleteClient" - {
    "must return Ok" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val clientId = "test-client-id"
        when(fixture.idmsService.deleteClient(ArgumentMatchers.eq(clientId)))
          .thenReturn(Future.successful(Some(())))

        val request = FakeRequest(
          DELETE,
          routes.IdmsController.deleteClient(environment, clientId).url
        )
          .withHeaders(authorizationHeader)

        val result = route(fixture.application, request).value
        status(result) mustBe Status.OK
      }
    }

    "must return 401 Unauthorized if no valid credentials are presented" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val request = FakeRequest(
          DELETE,
          routes.IdmsController.deleteClient(environment, "test-client-id").url
        )

        val result = route(fixture.application, request).value
        status(result) mustBe Status.UNAUTHORIZED
      }
    }

    "must return 404 Not Found if the client does not exist" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val clientId = "test-client-id"
        when(fixture.idmsService.deleteClient(ArgumentMatchers.eq(clientId)))
          .thenReturn(Future.successful(None))

        val request = FakeRequest(
          DELETE,
          routes.IdmsController.deleteClient(environment, clientId).url
        )
          .withHeaders(authorizationHeader)

        val result = route(fixture.application, request).value
        status(result) mustBe Status.NOT_FOUND
      }
    }
  }

  "addClientScope" - {
    "must return Ok" in {
      val id = "test-client-id"
      val clientScopeId = "test-client-scope-id"

      val fixture = buildFixture()

      running(fixture.application) {
        when(fixture.idmsService.addClientScope(ArgumentMatchers.eq(id), ArgumentMatchers.eq(clientScopeId)))
          .thenReturn(Future.successful(Some(())))

        val request = FakeRequest(
          PUT,
          routes.IdmsController.addClientScope(environment, id, clientScopeId).url
        )
          .withHeaders(authorizationHeader)

        val result = route(fixture.application, request).value
        status(result) mustBe Status.OK
      }
    }

    "must return 401 Unauthorized if no valid credentials are presented" in {
      val fixture = buildFixture()

      running(fixture.application) {

        val request = FakeRequest(
          PUT,
          routes.IdmsController.addClientScope(environment, "test-id", "test-client-scope-id").url
        )

        val result = route(fixture.application, request).value
        status(result) mustBe Status.UNAUTHORIZED
      }
    }
  }

  "deleteClientScope" - {
    "must delete the scope and return Ok on success" in {
      val id = "test-client-id"
      val clientScopeId = "test-client-scope-id"

      val fixture = buildFixture()

      running(fixture.application) {
        when(fixture.idmsService.deleteClientScope(ArgumentMatchers.eq(id), ArgumentMatchers.eq(clientScopeId)))
          .thenReturn(Future.successful(Some(())))

        val request = FakeRequest(routes.IdmsController.deleteClientScope(environment, id, clientScopeId))
          .withHeaders(authorizationHeader)

        val result = route(fixture.application, request).value
        status(result) mustBe Status.OK
      }
    }

    "must return 404 Not Found when the client does not exist" in {
      val id = "test-client-id"
      val clientScopeId = "test-client-scope-id"

      val fixture = buildFixture()

      running(fixture.application) {
        when(fixture.idmsService.deleteClientScope(ArgumentMatchers.eq(id), ArgumentMatchers.eq(clientScopeId)))
          .thenReturn(Future.successful(None))

        val request = FakeRequest(routes.IdmsController.deleteClientScope(environment, id, clientScopeId))
          .withHeaders(authorizationHeader)

        val result = route(fixture.application, request).value
        status(result) mustBe Status.NOT_FOUND
      }
    }

    "must return 401 Unauthorized if no valid credentials are presented" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val request = FakeRequest(
          DELETE,
          routes.IdmsController.addClientScope(environment, "test-client-id", "test-client-scope-id").url
        )

        val result = route(fixture.application, request).value
        status(result) mustBe Status.UNAUTHORIZED
      }
    }
  }

  "create new Client secret" - {
    "must return 200 and secret json for a valid request" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val clientId = "CLIENTID123"
        val expected = ClientSecretResponse("client-secret-123456-123456")

        val request = FakeRequest(POST, routes.IdmsController.generateNewClientSecret(environment, clientId).url)
          .withHeaders(authorizationHeader)

        when(fixture.idmsService.generateNewClientSecret(clientId)).thenReturn(Future.successful(Some(expected)))
        val result = route(fixture.application, request).value
        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(expected)
      }
    }

    "must return 401 Unauthorized if no valid credentials are presented" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val clientId = "CLIENTID123"

        val request = FakeRequest(POST, routes.IdmsController.generateNewClientSecret(environment, clientId).url)

        val result = route(fixture.application, request).value
        status(result) mustBe Status.UNAUTHORIZED
      }
    }
  }

  "fetchClientScopes" - {
    "must return 200 and scopes for a valid request" in {
      val id = "test-id"
      val identity = Identity("test-application-name", "test-description", Some(id), "test-secret", Set("scope-1", "scope-2"))

      val fixture = buildFixture()

      running(fixture.application) {
        when(fixture.idmsService.getClient(ArgumentMatchers.eq(id)))
          .thenReturn(Future.successful(Some(identity)))

        val request = FakeRequest(GET, routes.IdmsController.getClientScopes(environment, id).url)
          .withHeaders(authorizationHeader)

        val result = route(fixture.application, request).value

        val expected = identity.scopes.map(ClientScope(_))

        status(result) mustBe Status.OK
        contentAsJson(result) mustBe Json.toJson(expected)
      }
    }

    "must return 404 Not Found when the client Id is not known" in {
      val id = "test-id"

      val fixture = buildFixture()

      running(fixture.application) {
        when(fixture.idmsService.getClient(ArgumentMatchers.eq(id)))
          .thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.IdmsController.getClientScopes(environment, id).url)
          .withHeaders(authorizationHeader)

        val result = route(fixture.application, request).value

        status(result) mustBe Status.NOT_FOUND
      }
    }

    "must return 401 Unauthorized if no valid credentials are presented" in {
      val fixture = buildFixture()

      running(fixture.application) {
        val request = FakeRequest(GET, routes.IdmsController.getClientScopes(environment, "test-id").url)

        val result = route(fixture.application, request).value

        status(result) mustBe Status.UNAUTHORIZED
      }
    }
  }

}

private object IdmsControllerSpec {

  val environment: String = "test-environment"

}
