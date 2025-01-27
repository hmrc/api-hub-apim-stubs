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

import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.apihubapimstubs.connectors.AutoPublishConnector
import uk.gov.hmrc.apihubapimstubs.models.deployment.Deployment
import uk.gov.hmrc.apihubapimstubs.models.exception.{ApimStubException, DeploymentFailedException, DeploymentNotFoundException}
import uk.gov.hmrc.apihubapimstubs.models.simpleapideployment.*
import uk.gov.hmrc.apihubapimstubs.models.utility.SemVer
import uk.gov.hmrc.apihubapimstubs.repositories.DeploymentsRepository
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.Future

class SimpleApiDeploymentServiceSpec extends AsyncFreeSpec with Matchers with MockitoSugar {

  import SimpleApiDeploymentServiceSpec.*

  "validateOas" - {
    "must not return failures when the OAS is valid" in {
      val fixture = buildFixture()

      val actual = fixture.simpleApiDeploymentService.validateOas(environment, oas)
      actual mustBe Right(())
    }

    "must return a DeploymentFailedException with failures when the OAS is not valid" in {
      val fixture = buildFixture()

      val actual = fixture.simpleApiDeploymentService.validateOas(environment, invalidOas)
      actual mustBe Left(DeploymentFailedException.forFailuresResponse(FailuresResponse.invalidOas))
    }
  }

  "deployNewApi" - {
    "must store the new deployment and return the DeploymentsResponse on success" in {
      val fixture = buildFixture()
      val deployment = buildDeployment(fixture)
      val deploymentWithId = buildDeploymentWithId(fixture)

      when(fixture.deploymentsRepository.insert(eqTo(deployment)))
        .thenReturn(Future.successful(Right(deploymentWithId)))
      when(fixture.autoPublishConnector.publish(any)(any))
        .thenReturn(Future.successful(Right(())))

      fixture.simpleApiDeploymentService.deployNewApi(environment, createMetadata, oas).map {
        result =>
          verify(fixture.autoPublishConnector).publish(eqTo(deploymentWithId.name))(any)
          result mustBe Right(DeploymentsResponse(createMetadata.name))
      }
    }

    "must return a DeploymentFailedException with failures when the OAS is not valid" in {
      val fixture = buildFixture()

      fixture.simpleApiDeploymentService.deployNewApi(environment, createMetadata, invalidOas).map(
        result =>
          result mustBe Left(DeploymentFailedException.forFailuresResponse(FailuresResponse.invalidOas))
      )
    }
  }

  "getDeploymentDetails" - {
    "must return a DetailsResponse when the deployment exists" in {
      val fixture = buildFixture()
      val deployment = buildDeployment(fixture)

      when(fixture.deploymentsRepository.findInEnvironment(eqTo(environment), eqTo(serviceId)))
        .thenReturn(Future.successful(Right(deployment)))

      fixture.simpleApiDeploymentService.getDeploymentDetails(environment, serviceId).map(
        result =>
          result mustBe Right(deployment.toDetailsResponse)
      )
    }

    "must return DeploymentNotFoundException when the deployment does not exist" in {
      val fixture = buildFixture()
      val expected = DeploymentNotFoundException.forService(environment, serviceId)

      when(fixture.deploymentsRepository.findInEnvironment(eqTo(environment), eqTo(serviceId)))
        .thenReturn(Future.successful(Left(expected)))

      fixture.simpleApiDeploymentService.getDeploymentDetails(environment, serviceId).map(
        result =>
          result mustBe Left(expected)
      )
    }
  }

  "deployExistingApiWithNewConfiguration" - {
    "must store the updated deployment and return the DeploymentsResponse on success" in {
      val fixture = buildFixture()
      val deployment = buildDeploymentWithId(fixture)
        .copy(deploymentTimestamp = Instant.now(fixture.clock).minusSeconds(1))

      val createMetadata = updateMetadata.toCreateMetadata(deployment)

      val updatedDeployment = deployment.copy(
        description = updateMetadata.description,
        egress = updateMetadata.egress,
        status = updateMetadata.status,
        domain = updateMetadata.domain,
        subdomain = updateMetadata.subdomain,
        backends = updateMetadata.backends,
        egressMappings = updateMetadata.egressMappings,
        prefixesToRemove = updateMetadata.prefixesToRemove,
        deploymentTimestamp = Instant.now(fixture.clock),
        deploymentVersion = SemVer(deployment.deploymentVersion).incrementMinor().version,
        oas = fixture.oasTransformer.transformToConsumerOas(oas, createMetadata)
      )

      when(fixture.deploymentsRepository.findInEnvironment(eqTo(environment), eqTo(serviceId)))
        .thenReturn(Future.successful(Right(deployment)))

      when(fixture.deploymentsRepository.update(any))
        .thenReturn(Future.successful(Right(())))

      when(fixture.autoPublishConnector.publish(any)(any))
        .thenReturn(Future.successful(Right(())))

      fixture.simpleApiDeploymentService.deployExistingApiWithNewConfiguration(environment, serviceId, updateMetadata, oas).map {
        result =>
          verify(fixture.deploymentsRepository).update(eqTo(updatedDeployment))
          verify(fixture.autoPublishConnector).publish(eqTo(updatedDeployment.name))(any)
          result mustBe Right(DeploymentsResponse(serviceId))
      }
    }

    "must return DeploymentNotFoundException if the deployment does not exist" in {
      val fixture = buildFixture()
      val expected = DeploymentNotFoundException.forService(environment, serviceId)

      when(fixture.deploymentsRepository.findInEnvironment(eqTo(environment), eqTo(serviceId)))
        .thenReturn(Future.successful(Left(expected)))

      fixture.simpleApiDeploymentService.deployExistingApiWithNewConfiguration(environment, serviceId, updateMetadata, oas).map(
        result =>
          result mustBe Left(expected)
      )
    }

    "must return DeploymentFailedException and failures if the OAS is not valid" in {
      val fixture = buildFixture()
      val deployment = buildDeploymentWithId(fixture)
      val expected = DeploymentFailedException.forFailuresResponse(FailuresResponse.invalidOas)

      when(fixture.deploymentsRepository.findInEnvironment(eqTo(environment), eqTo(serviceId)))
        .thenReturn(Future.successful(Right(deployment)))

      fixture.simpleApiDeploymentService.deployExistingApiWithNewConfiguration(environment, serviceId, updateMetadata, invalidOas).map(
        result =>
          result mustBe Left(expected)
      )
    }
  }

  "getEgressGateways" - {
    "must return the canned response" in {
      val fixture = buildFixture()
      val actual = fixture.simpleApiDeploymentService.getEgressGateways(environment)
      actual mustBe EgressGateway.cannedResponse
    }
  }

  private def buildFixture(): Fixture = {
    val oasTransformer = new OasTransformer(clock)
    val deploymentsRepository = mock[DeploymentsRepository]
    val autoPublishConnector = mock[AutoPublishConnector]
    val simpleApiDeploymentService = new SimpleApiDeploymentService(oasTransformer, deploymentsRepository, autoPublishConnector, clock)

    Fixture(oasTransformer, deploymentsRepository, autoPublishConnector, clock, simpleApiDeploymentService)
  }

}

private object SimpleApiDeploymentServiceSpec extends MockitoSugar {

  val environment: String = "test-environment"
  val serviceId: String = "test-service-id"
  val invalidOas: String = "xxx"
  val clock: Clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
  val oasVersion: String = "1.2.3"
  val defaultVersion = "0.1.0"

  val oas: String =
    s"""
      |openapi: 3.0.3
      |info:
      |  version: $oasVersion
      |""".stripMargin

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

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

  val deploymentId = "test-deployment-id"

  def buildDeployment(fixture: Fixture): Deployment = Deployment(
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
    deploymentTimestamp = Instant.now(fixture.clock),
    deploymentVersion = defaultVersion,
    oasVersion = oasVersion,
    buildVersion = defaultVersion,
    oas = fixture.oasTransformer.transformToConsumerOas(oas, createMetadata)
  )

  def buildDeploymentWithId(fixture: Fixture): Deployment = {
    buildDeployment(fixture).copy(id = Some(deploymentId))
  }

  case class Fixture(
    oasTransformer: OasTransformer,
    deploymentsRepository: DeploymentsRepository,
    autoPublishConnector: AutoPublishConnector,
    clock: Clock,
    simpleApiDeploymentService: SimpleApiDeploymentService
  )

}
