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

package uk.gov.hmrc.apihubapimstubs

import uk.gov.hmrc.apihubapimstubs.models.deployment.Deployment
import uk.gov.hmrc.apihubapimstubs.models.simpleapideployment.EgressMapping

import java.time.{Clock, Instant}

trait TestData {

  val environment = "test-environment"
  val baseOas = "openapi: 3.0.3"

  def buildDeployment(index: Int, clock: Clock): Deployment = {
    Deployment(
      id = Some(s"test-id-$index"),
      environment = environment,
      lineOfBusiness = s"test-lob",
      name = s"test-name-$index",
      description = s"test-description-$index",
      egress = "test-egress",
      passthrough = false,
      status = "test-status",
      apiType = "test-api-type",
      domain = "test-domain",
      subdomain = "test-sub-domain",
      backends = Seq("test-backend-updated"),
      egressMappings = Seq(EgressMapping("test-prefix-updated", "test-egress-prefix-updated")),
      prefixesToRemove = Seq("test-prefix-to-remove-updated"),
      deploymentTimestamp = Instant.now(clock),
      deploymentVersion = "0.1.0",
      oasVersion = "0.1.0",
      buildVersion = "0.1.0",
      oas = baseOas
    )
  }

}
