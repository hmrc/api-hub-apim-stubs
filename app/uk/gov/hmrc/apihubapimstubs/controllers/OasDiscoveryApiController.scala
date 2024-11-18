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

package uk.gov.hmrc.apihubapimstubs.controllers

import com.google.inject.{Inject, Singleton}
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.apihubapimstubs.controllers.auth.Authenticator
import uk.gov.hmrc.apihubapimstubs.models.oasdiscoveryapi.{ApiDeployment, ApiDeploymentDetail}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.Clock

@Singleton
class OasDiscoveryApiController @Inject()(
  cc: ControllerComponents,
  clock: Clock,
  authenticator: Authenticator
) extends BackendController(cc) with Logging {

  def getOpenApiDeployments(): Action[AnyContent] = authenticator {
    Ok(Json.toJson(ApiDeployment.cannedResponse))
  }

  def getOpenApiDeployment(id: String): Action[AnyContent] = authenticator {
    Ok(Json.toJson(ApiDeploymentDetail(id, clock)))
  }

}
