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

package uk.gov.hmrc.apihubapimstubs.models.oasdiscoveryapi

import play.api.libs.json.{Format, Json}

import java.time.Instant

case class ApiDeployment(id: String, deploymentTimestamp: Instant)

object ApiDeployment {

  implicit val formatApiDeployment: Format[ApiDeployment] = Json.format[ApiDeployment]

  val cannedResponse: Seq[ApiDeployment] = Seq(
    ApiDeployment("ems-address-weighting-service", Instant.now()),
    ApiDeployment("fake-service-id-1", Instant.now().minusSeconds(1)),
    ApiDeployment("fake-service-id-2", Instant.now().minusSeconds(2))
  )

}
