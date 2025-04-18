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

package uk.gov.hmrc.apihubapimstubs.services

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.apihubapimstubs.models.idms.{ClientResponse, ClientSecretResponse, Identity}
import uk.gov.hmrc.apihubapimstubs.repositories.IdentityRepository

import scala.concurrent.Future

class IdmsServiceSpec extends AsyncFreeSpec with Matchers with MockitoSugar with ScalaFutures {

  "createClient" - {
    "must build the correct Identity and submit it to the repository" in {

      val repository = mock[IdentityRepository]
      val service = new IdmsService(repository)
      val identity = Identity("test-app-name", "This is a test application", Some("63bebf8bbbeccc26c12294e5"), "client-secret-bla-bla", Set.empty)
      val expected = ClientResponse("63bebf8bbbeccc26c12294e5", "client-secret-bla-bla")
      when(repository.insert(any[Identity])).thenReturn(Future.successful(identity))
      service.createClient(identity.copy(clientId = None)).map {
        actual =>
          actual mustBe Some(expected)
      }
    }
  }

  "getClientSecret" - {
    "must build the correct Secret object from Identity object returned by repository" in {

      val repository = mock[IdentityRepository]
      val service = new IdmsService(repository)
      val clientId = "63bebf8bbbeccc26c12294e5"
      val identity = Identity("test-app-name", "This is a test application", Some(clientId), "client-secret-123456-123456", Set.empty)
      val expected = ClientSecretResponse("client-secret-123456-123456")
      when(repository.find(clientId)).thenReturn(Future.successful(Some(identity)))
      service.getClientSecret(clientId).map {
        actual =>
          actual mustBe Some(expected)
      }
    }
  }

  "newClientSecret" - {
    "must update the client secret on the Identity object returned by repository" in {

      val repository = mock[IdentityRepository]
      val service = new IdmsService(repository)
      val clientId = "63bebf8bbbeccc26c12294e5"
      val identity = Identity("test-app-name", "This is a test application", Some(clientId), "client-secret-123456-123456", Set.empty)
      val oldSecret = ClientSecretResponse("client-secret-123456-123456")
      when(repository.find(clientId)).thenReturn(Future.successful(Some(identity)))
      when(repository.update(any[Identity])).thenAnswer((invocation: InvocationOnMock) => Future.successful(Some(invocation.getArgument(0))))

      service.generateNewClientSecret(clientId).map {
        actual =>
          actual mustNot be(Some(oldSecret))
      }
    }

  }

  "addClientScope" - {
    "must add the correct scope via the repository" in {
      val repository = mock[IdentityRepository]
      val service = new IdmsService(repository)
      val clientId = "63bebf8bbbeccc26c12294e5"
      val clientScopeId = "test-client-scope-id"

      when(repository.addScope(ArgumentMatchers.eq(clientId), ArgumentMatchers.eq(clientScopeId)))
        .thenReturn(Future.successful(Some(())))

      service.addClientScope(clientId, clientScopeId) map {
        result =>
          result mustBe Some(())
      }
    }

    "must return None when the application does not exist" in {
      val repository = mock[IdentityRepository]
      val service = new IdmsService(repository)
      val clientId = "63bebf8bbbeccc26c12294e5"
      val clientScopeId = "test-client-scope-id"

      when(repository.addScope(ArgumentMatchers.eq(clientId), ArgumentMatchers.eq(clientScopeId)))
        .thenReturn(Future.successful(None))

      service.addClientScope(clientId, clientScopeId) map {
        result =>
          result mustBe None
      }
    }
  }

  "deleteClientScope" - {
    "must remove the correct scope via the repository" in {
      val repository = mock[IdentityRepository]
      val service = new IdmsService(repository)
      val clientId = "63bebf8bbbeccc26c12294e5"
      val clientScopeId = "test-client-scope-id"

      when(repository.removeScope(ArgumentMatchers.eq(clientId), ArgumentMatchers.eq(clientScopeId)))
        .thenReturn(Future.successful(Some(())))

      service.deleteClientScope(clientId, clientScopeId) map {
        result =>
          result mustBe Some(())
      }
    }

    "must return None when the application does not exist" in {
      val repository = mock[IdentityRepository]
      val service = new IdmsService(repository)
      val clientId = "63bebf8bbbeccc26c12294e5"
      val clientScopeId = "test-client-scope-id"

      when(repository.removeScope(ArgumentMatchers.eq(clientId), ArgumentMatchers.eq(clientScopeId)))
        .thenReturn(Future.successful(None))

      service.deleteClientScope(clientId, clientScopeId) map {
        result =>
          result mustBe None
      }
    }
  }

}
