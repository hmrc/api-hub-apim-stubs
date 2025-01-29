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

package uk.gov.hmrc.apihubapimstubs.controllers

import com.google.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.apihubapimstubs.controllers.auth.Authenticator
import uk.gov.hmrc.apihubapimstubs.models.exception.DeploymentNotFoundException
import uk.gov.hmrc.apihubapimstubs.services.OasDiscoveryService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class OasDiscoveryApiControllerV2 @Inject()(
  cc: ControllerComponents,
  authenticator: Authenticator,
  oasDiscoveryService: OasDiscoveryService
)(implicit ec: ExecutionContext) extends BackendController(cc) {

  def getOpenApiDeployments(environment: String): Action[AnyContent] = authenticator.async {
    implicit request =>
      oasDiscoveryService.getOpenApiDeployments(environment).map(
        deployments =>
          Ok(Json.toJson(deployments))
      )
  }

  def getOpenApiDeployment(environment: String, id: String): Action[AnyContent] = authenticator.async {
    implicit request =>
      oasDiscoveryService.getOpenApiDeployment(environment, id).map {
        case Right(deployment) => Ok(Json.toJson(deployment))
        case Left(e: DeploymentNotFoundException) => NotFound
        case Left(e) => throw e
      }
  }

  def getOpenApiSpecification(environment: String, id: String): Action[AnyContent] = authenticator.async {
    implicit request =>
      oasDiscoveryService.getOpenApiSpecification(environment, id).map {
        case Right(oas) => Ok(oas).as("application/yaml")
        case Left(e: DeploymentNotFoundException) => NotFound
        case Left(e) => throw e
      }
  }

}
