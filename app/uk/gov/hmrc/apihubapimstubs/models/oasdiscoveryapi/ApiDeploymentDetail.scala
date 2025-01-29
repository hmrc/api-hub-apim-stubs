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

import java.time.{Clock, Instant}

case class ApiDeploymentDetail(
  id: String,
  deploymentTimestamp: Instant,
  deploymentVersion: Option[String],
  oasVersion: Option[String],
  buildVersion: Option[String]
)

object ApiDeploymentDetail {

  def cannedResponse(id: String, clock: Clock): ApiDeploymentDetail = {
    ApiDeploymentDetail(
      id = id,
      deploymentTimestamp = Instant.now(clock),
      deploymentVersion = Some("0.1.0"),
      oasVersion = Some("0.1.0"),
      buildVersion = Some("0.1.0")
    )
  }

  implicit val formatApiDeploymentDetail: Format[ApiDeploymentDetail] = Json.format[ApiDeploymentDetail]

}
