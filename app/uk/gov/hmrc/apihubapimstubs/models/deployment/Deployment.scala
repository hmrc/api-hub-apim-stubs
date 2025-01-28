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

package uk.gov.hmrc.apihubapimstubs.models.deployment

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.apihubapimstubs.models.oasdiscoveryapi.{ApiDeployment, ApiDeploymentDetail}
import uk.gov.hmrc.apihubapimstubs.models.simpleapideployment.{CreateMetadata, DetailsResponse, EgressMapping, UpdateMetadata}
import uk.gov.hmrc.apihubapimstubs.models.utility.{MongoIdentifier, SemVer}
import uk.gov.hmrc.apihubapimstubs.util.OpenApiStuff

import java.time.{Clock, Instant}

case class Deployment(
  id: Option[String],
  environment: String,
  lineOfBusiness: String,
  name: String,
  description: String,
  egress: String,
  passthrough: Boolean,
  status: String,
  apiType: String,
  domain: String,
  subdomain: String,
  backends: Seq[String],
  egressMappings: Seq[EgressMapping],
  prefixesToRemove: Seq[String],
  deploymentTimestamp: Instant,
  deploymentVersion: String,
  oasVersion: String,
  buildVersion: String,
  oas: String
) extends MongoIdentifier with OpenApiStuff {

  def update(metadata: UpdateMetadata, oas: String, clock: Clock): Deployment = {
    Deployment(
      id = id,
      environment = environment,
      lineOfBusiness = lineOfBusiness,
      name = name,
      description = metadata.description,
      egress = metadata.egress,
      passthrough = passthrough,
      status = metadata.status,
      apiType = apiType,
      domain = metadata.domain,
      subdomain = metadata.subdomain,
      backends = metadata.backends,
      egressMappings = metadata.egressMappings,
      prefixesToRemove = metadata.prefixesToRemove,
      deploymentTimestamp = Instant.now(clock),
      deploymentVersion = SemVer(deploymentVersion).incrementMinor().version,
      oasVersion = oasVersion(oas).getOrElse("1.0.0"),
      buildVersion = buildVersion,
      oas = oas
    )
  }

  def toDetailsResponse: DetailsResponse = {
    DetailsResponse(
      description = description,
      status = status,
      apiType = apiType,
      domain = domain,
      subdomain = subdomain,
      backends = backends,
      egressMappings = egressMappings,
      prefixesToRemove = prefixesToRemove,
      deploymentVersion = deploymentVersion,
      egress = egress
    )
  }

  def toApiDeployment: ApiDeployment = {
    ApiDeployment(
      id = name,
      deploymentTimestamp = deploymentTimestamp
    )
  }

  def toApiDeploymentDetail: ApiDeploymentDetail = {
    ApiDeploymentDetail(
      id = name,
      deploymentTimestamp = deploymentTimestamp,
      deploymentVersion = Some(deploymentVersion),
      oasVersion = Some(oasVersion),
      buildVersion = Some(buildVersion)
    )
  }

}

object Deployment extends OpenApiStuff {

  def create(environment: String, metadata: CreateMetadata, oas: String, clock: Clock): Deployment = {
    Deployment(
      id = None,
      environment = environment,
      lineOfBusiness = metadata.lineOfBusiness,
      name = metadata.name,
      description = metadata.description,
      egress = metadata.egress,
      passthrough = metadata.passthrough,
      status = metadata.status,
      apiType = metadata.apiType,
      domain = metadata.domain,
      subdomain = metadata.subdomain,
      backends = metadata.backends,
      egressMappings = metadata.egressMappings,
      prefixesToRemove = metadata.prefixesToRemove,
      deploymentTimestamp = Instant.now(clock),
      deploymentVersion = "0.1.0",
      oasVersion = oasVersion(oas).getOrElse("1.0.0"),
      buildVersion = "0.1.0",
      oas = oas
    )
  }

  implicit val formatDeployment: Format[Deployment] = Json.format[Deployment]

}
