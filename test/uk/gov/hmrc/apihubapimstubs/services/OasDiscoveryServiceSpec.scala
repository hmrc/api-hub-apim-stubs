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

import org.mockito.ArgumentMatchers.eq as eqTo
import org.mockito.Mockito.when
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.apihubapimstubs.TestData
import uk.gov.hmrc.apihubapimstubs.models.deployment.Deployment
import uk.gov.hmrc.apihubapimstubs.models.exception.DeploymentNotFoundException
import uk.gov.hmrc.apihubapimstubs.repositories.DeploymentsRepository

import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.Future

class OasDiscoveryServiceSpec extends AsyncFreeSpec with Matchers with MockitoSugar with TestData {

  import OasDiscoveryServiceSpec.*

  "getOpenApiDeployments" - {
    "must return the correct deployments for the environment" in {
      val fixture = buildFixture()
      val deployment1 = buildDeployment(1, clock)
      val deployment2 = buildDeployment(2, clock)

      when(fixture.deploymentsRepository.findAllInEnvironment(eqTo(environment)))
        .thenReturn(Future.successful(Seq(deployment1, deployment2)))

      fixture.oasDiscoveryService.getOpenApiDeployments(environment).map(
        result =>
          result mustBe Seq(deployment1.toApiDeployment, deployment2.toApiDeployment)
      )
    }
  }

  "getOpenApiDeployment" - {
    "must return the deployment when it exists" in {
      val fixture = buildFixture()
      val deployment = buildDeployment(1, clock)

      when(fixture.deploymentsRepository.findInEnvironment(eqTo(environment), eqTo(deployment.name)))
        .thenReturn(Future.successful(Right(deployment)))

      fixture.oasDiscoveryService.getOpenApiDeployment(environment, deployment.name).map(
        result =>
          result mustBe Right(deployment.toApiDeploymentDetail)
      )
    }

    "must return DeploymentNotFoundException when the deployment does not exist" in {
      val fixture = buildFixture()
      val deployment = buildDeployment(1, clock)
      val expected = DeploymentNotFoundException.forDeployment(deployment)

      when(fixture.deploymentsRepository.findInEnvironment(eqTo(environment), eqTo(deployment.name)))
        .thenReturn(Future.successful(Left(expected)))

      fixture.oasDiscoveryService.getOpenApiDeployment(environment, deployment.name).map(
        result =>
          result mustBe Left(expected)
      )
    }
  }

  "getOpenApiSpecification" - {
    "must return the OAS when the deployment exists" in {
      val fixture = buildFixture()
      val deployment = buildDeployment(1, clock)
      val expected = "oas with servers section"

      when(fixture.deploymentsRepository.findInEnvironment(eqTo(environment), eqTo(deployment.name)))
        .thenReturn(Future.successful(Right(deployment)))

      when(fixture.oasServersTransformer.addServersOas(eqTo(deployment.oas)))
        .thenReturn(expected)

      fixture.oasDiscoveryService.getOpenApiSpecification(environment, deployment.name).map(
        result =>
          result mustBe Right(expected)
      )
    }

    "must return DeploymentNotFoundException when the deployment does not exist" in {
      val fixture = buildFixture()
      val deployment = buildDeployment(1, clock)
      val expected = DeploymentNotFoundException.forDeployment(deployment)

      when(fixture.deploymentsRepository.findInEnvironment(eqTo(environment), eqTo(deployment.name)))
        .thenReturn(Future.successful(Left(expected)))

      fixture.oasDiscoveryService.getOpenApiSpecification(environment, deployment.name).map(
        result =>
          result mustBe Left(expected)
      )
    }
  }

  def buildFixture(): Fixture = {
    val deploymentsRepository = mock[DeploymentsRepository]
    val oasServersTransformer = mock[OasServersTransformer]
    val oasDiscoveryService = new OasDiscoveryService(deploymentsRepository, oasServersTransformer)

    Fixture(deploymentsRepository, oasServersTransformer, oasDiscoveryService)
  }

}

private object OasDiscoveryServiceSpec {

  val clock: Clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

  case class Fixture(
    deploymentsRepository: DeploymentsRepository,
    oasServersTransformer: OasServersTransformer,
    oasDiscoveryService: OasDiscoveryService
  )

}
