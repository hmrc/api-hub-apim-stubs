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

package uk.gov.hmrc.apihubapimstubs.models.exception

import uk.gov.hmrc.apihubapimstubs.models.deployment.Deployment

case class DeploymentNotFoundException(message: String) extends ApimStubException(message, null)

object DeploymentNotFoundException {

  def forService(environment: String, serviceId: String): DeploymentNotFoundException = {
    DeploymentNotFoundException(s"Cannot find service $serviceId in environment $environment")
  }

  def forDeployment(deployment: Deployment): DeploymentNotFoundException = {
    forService(deployment.environment, deployment.name)
  }

}
