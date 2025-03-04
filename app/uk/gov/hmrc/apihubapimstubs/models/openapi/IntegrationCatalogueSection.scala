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

package uk.gov.hmrc.apihubapimstubs.models.openapi

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.hmrc.apihubapimstubs.models.simpleapideployment.CreateMetadata

import java.time.{Clock, ZonedDateTime}

case class IntegrationCatalogueSection(
  status: String,
  @JsonProperty("api-type") apiType: String,
  @JsonProperty("reviewed-date") reviewedDate: ZonedDateTime,
  @JsonProperty("short-description") description: String,
  platform: String,
  backends: Seq[String],
  domain: String,
  @JsonProperty("sub-domain") subdomain: String,
  @JsonProperty("API-Generation") apiGeneration: String
)

object IntegrationCatalogueSection {

  val hipPlatform = "HIP"
  val v2 = "V2"

  def apply(metadata: CreateMetadata, clock: Clock): IntegrationCatalogueSection = {
    IntegrationCatalogueSection(
      status = metadata.status,
      apiType = metadata.apiType,
      reviewedDate = ZonedDateTime.now(clock),
      description = metadata.description,
      platform = hipPlatform,
      backends = metadata.backends,
      domain = metadata.domain,
      subdomain = metadata.subdomain,
      apiGeneration = v2
    )
  }

}
