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

package uk.gov.hmrc.apihubapimstubs.models.simpleapideployment

import play.api.libs.json.{Format, Json}

case class DetailsResponse(
  description: String,
  status: String,
  apiType: String,
  domain: String,
  subdomain: String,
  backends: Seq[String],
  egressMappings: Seq[EgressMapping],
  prefixesToRemove: Seq[String],
  deploymentVersion: String,
  egress: String,
)

object DetailsResponse {

  val cannedResponse: DetailsResponse = DetailsResponse(
    description = "A short description of the API",
    status = "ALPHA",
    apiType = "SIMPLE",
    domain = "8",
    subdomain = "8.1",
    backends = Seq("NPS"),
    egressMappings = Seq(EgressMapping("/mapping-from", "/mapping-to")),
    prefixesToRemove = Seq("/v1"),
    deploymentVersion = "0.1.0",
    egress = "egress",
  )

  implicit val formatDetailsResponse: Format[DetailsResponse] = Json.format[DetailsResponse]

}
