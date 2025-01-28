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
import uk.gov.hmrc.apihubapimstubs.models.exception.ApimStubException
import uk.gov.hmrc.apihubapimstubs.models.oasdiscoveryapi.{ApiDeployment, ApiDeploymentDetail}
import uk.gov.hmrc.apihubapimstubs.repositories.DeploymentsRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OasDiscoveryService @Inject()(
  deploymentsRepository: DeploymentsRepository,
  oasServersTransformer: OasServersTransformer
)(implicit ec: ExecutionContext) {

  def getOpenApiDeployments(environment: String): Future[Seq[ApiDeployment]] = {
    deploymentsRepository.findAllInEnvironment(environment)
      .map(_.map(_.toApiDeployment))
  }

  def getOpenApiDeployment(environment: String, id: String): Future[Either[ApimStubException, ApiDeploymentDetail]] = {
    deploymentsRepository.findInEnvironment(environment, id)
      .map(_.map(_.toApiDeploymentDetail))
  }

  def getOpenApiSpecification(environment: String, id: String): Future[Either[ApimStubException, String]] = {
    deploymentsRepository.findInEnvironment(environment, id)
      .map(_.map(deployment => oasServersTransformer.addServersOas(deployment.oas)))
  }

}
