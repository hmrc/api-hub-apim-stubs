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
import uk.gov.hmrc.apihubapimstubs.models.deployment.Deployment

case class UpdateMetadata(
  description: String,
  status: String,
  egress: String,
  domain: String,
  subdomain: String,
  backends: Seq[String],
  egressMappings: Seq[EgressMapping],
  prefixesToRemove: Seq[String]
) {

  def toCreateMetadata(deployment: Deployment): CreateMetadata = {
    CreateMetadata(
      lineOfBusiness = deployment.lineOfBusiness,
      name = deployment.name,
      description = description,
      egress = egress,
      passthrough = deployment.passthrough,
      status = status,
      domain = domain,
      subdomain = subdomain,
      backends = backends,
      egressMappings = egressMappings,
      prefixesToRemove = prefixesToRemove
    )
  }

}

object UpdateMetadata {

  implicit val formatUpdateMetadata: Format[UpdateMetadata] = Json.format[UpdateMetadata]

}
