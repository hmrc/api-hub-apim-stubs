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

package uk.gov.hmrc.apihubapimstubs.util

import play.api.Logging
import uk.gov.hmrc.apihubapimstubs.models.deployment.Deployment
import uk.gov.hmrc.apihubapimstubs.models.exception.{ApimStubException, DeploymentExistsException, DeploymentFailedException, DeploymentNotFoundException}
import uk.gov.hmrc.apihubapimstubs.models.simpleapideployment.FailuresResponse

trait ExceptionRaising {
  self: Logging =>

  object raiseDeploymentExistsException {
    def forDeployment(deployment: Deployment): DeploymentExistsException = {
      log(DeploymentExistsException.forDeployment(deployment))
    }
  }

  object raiseDeploymentFailedException {
    def forFailuresResponse(failuresResponse: FailuresResponse): DeploymentFailedException = {
      log(DeploymentFailedException.forFailuresResponse(failuresResponse))
    }
  }

  object raiseDeploymentNotFoundException {
    def forService(environment: String, serviceId: String): DeploymentNotFoundException = {
      log(DeploymentNotFoundException.forService(environment, serviceId))
    }

    def forDeployment(deployment: Deployment): DeploymentNotFoundException = {
      log(DeploymentNotFoundException.forDeployment(deployment))
    }
  }

  private def log[T <: ApimStubException](e: T): T = {
    logger.warn("Raised application exception", e)
    e
  }

}
