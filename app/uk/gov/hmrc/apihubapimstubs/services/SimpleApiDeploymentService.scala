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

import cats.data.EitherT
import com.google.inject.{Inject, Singleton}
import play.api.Logging
import uk.gov.hmrc.apihubapimstubs.connectors.AutoPublishConnector
import uk.gov.hmrc.apihubapimstubs.models.deployment.Deployment
import uk.gov.hmrc.apihubapimstubs.models.exception.{ApimStubException, DeploymentExistsException, DeploymentFailedException}
import uk.gov.hmrc.apihubapimstubs.models.simpleapideployment.{CreateMetadata, DeploymentsResponse, DetailsResponse, EgressGateway, FailuresResponse, UpdateMetadata}
import uk.gov.hmrc.apihubapimstubs.repositories.DeploymentsRepository
import uk.gov.hmrc.apihubapimstubs.util.{ExceptionRaising, OpenApiStuff}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Clock
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SimpleApiDeploymentService @Inject()(
  oasTransformer: OasTransformer,
  deploymentsRepository: DeploymentsRepository,
  autoPublishConnector: AutoPublishConnector,
  clock: Clock
)(implicit ec: ExecutionContext) extends OpenApiStuff with Logging with ExceptionRaising {

  def validateOas(environment: String, oas: String): Either[ApimStubException, Unit] = {
    if (isValidOas(oas)) {
      Right(())
    }
    else {
      Left(
        raiseDeploymentFailedException.forFailuresResponse(
          FailuresResponse.invalidOas
        )
      )
    }
  }

  def deployNewApi(environment: String, createMetadata: CreateMetadata, oas: String)(implicit hc: HeaderCarrier): Future[Either[ApimStubException, DeploymentsResponse]] = {
    (for {
      _ <- EitherT(Future.successful(validateOas(environment, oas)))
      consumerOas = oasTransformer.transformToConsumerOas(oas, createMetadata)
      deployment = Deployment.create(environment, createMetadata, consumerOas, clock)
      deployment <- EitherT(insertDeployment(deployment))
      _ <- EitherT(autoPublishConnector.publish(deployment.name))
    } yield DeploymentsResponse(deployment.name)).value
  }

  def getDeploymentDetails(environment: String, serviceId: String): Future[Either[ApimStubException, DetailsResponse]] = {
    deploymentsRepository.findInEnvironment(environment, serviceId)
      .map(_.map(_.toDetailsResponse))
  }

  def deployExistingApiWithNewConfiguration(
    environment: String,
    serviceId: String,
    updateMetadata: UpdateMetadata,
    oas: String
  )(implicit hc: HeaderCarrier): Future[Either[ApimStubException, DeploymentsResponse]] = {
    (for {
      deployment <- EitherT(deploymentsRepository.findInEnvironment(environment, serviceId))
      _ <- EitherT(Future.successful(validateOas(environment, oas)))
      createMetadata = updateMetadata.toCreateMetadata(deployment)
      consumerOas = oasTransformer.transformToConsumerOas(oas, createMetadata)
      updated = deployment.update(updateMetadata, consumerOas, clock)
      _ <- EitherT(deploymentsRepository.update(updated))
      _ <- EitherT(autoPublishConnector.publish(updated.name))
    } yield DeploymentsResponse(serviceId)).value
  }

  def getEgressGateways(environment: String): Seq[EgressGateway] = {
    EgressGateway.cannedResponse
  }

  private def insertDeployment(deployment: Deployment): Future[Either[ApimStubException, Deployment]] = {
    deploymentsRepository.insert(deployment)
      .map {
        case Right(deployment) => Right(deployment)
        case Left(_: DeploymentExistsException) =>
          Left(
            DeploymentFailedException.forFailuresResponse(
              FailuresResponse.deploymentAlreadyExists(deployment)
            )
          )
        case Left(e) => Left(e)
      }
  }

}
