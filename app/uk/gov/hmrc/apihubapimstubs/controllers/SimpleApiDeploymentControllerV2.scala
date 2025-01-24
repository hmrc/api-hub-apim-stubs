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
import play.api.libs.Files
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, MultipartFormData}
import uk.gov.hmrc.apihubapimstubs.controllers.auth.Authenticator
import uk.gov.hmrc.apihubapimstubs.models.exception.{ApimStubException, DeploymentFailedException, DeploymentNotFoundException}
import uk.gov.hmrc.apihubapimstubs.models.simpleapideployment.{CreateMetadata, DeploymentsResponse, EgressGateway, FailuresResponse, UpdateMetadata}
import uk.gov.hmrc.apihubapimstubs.services.SimpleApiDeploymentService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SimpleApiDeploymentControllerV2 @Inject()(
  cc: ControllerComponents,
  authenticator: Authenticator,
  service: SimpleApiDeploymentService
)(implicit ec: ExecutionContext) extends BackendController(cc) {

  def validateOas(environment: String): Action[String] = authenticator(parse.tolerantText) {
    implicit request =>
      service.validateOas(environment, request.body) match {
        case Right(_) => Ok
        case Left(e: DeploymentFailedException) => BadRequest(Json.toJson(e.failuresResponse))
        case Left(e) => throw e
      }
  }

  def deployNewApi(environment: String): Action[MultipartFormData[Files.TemporaryFile]] = authenticator(parse.multipartFormData).async {
    implicit request =>
      (request.body.dataParts.get("metadata"), request.body.dataParts.get("openapi")) match {
        case (Some(Seq(metadata)), Some(Seq(oas))) =>
          Json.parse(metadata).validate[CreateMetadata].fold(
            _ => Future.successful(BadRequest),
            validMetadata => {
              service.deployNewApi(environment, validMetadata, oas).map {
                case Right(deploymentsResponse) => Ok(Json.toJson(deploymentsResponse))
                case Left(e: DeploymentFailedException) => BadRequest(Json.toJson(e.failuresResponse))
                case Left(e) => throw e
              }
            }
          )
        case _ => Future.successful(BadRequest)
      }
  }

  def getDeploymentDetails(environment: String, serviceId: String): Action[AnyContent] = authenticator.async {
    implicit request =>
      service.getDeploymentDetails(environment, serviceId).map {
        case Right(detailsResponse) => Ok(Json.toJson(detailsResponse))
        case Left(e: DeploymentNotFoundException) => NotFound
        case Left(e) => throw e
      }
  }

  def deployExistingApiWithNewConfiguration(environment: String, serviceId: String): Action[MultipartFormData[Files.TemporaryFile]] = authenticator(parse.multipartFormData).async {
    implicit request =>
      (request.body.dataParts.get("metadata"), request.body.dataParts.get("openapi")) match {
        case (Some(Seq(metadata)), Some(Seq(oas))) =>
          Json.parse(metadata).validate[UpdateMetadata].fold(
            _ => Future.successful(BadRequest),
            validMetadata => {
              service.deployExistingApiWithNewConfiguration(environment, serviceId, validMetadata, oas).map {
                case Right(deploymentsResponse) => Ok(Json.toJson(deploymentsResponse))
                case Left(e: DeploymentNotFoundException) => NotFound
                case Left(e: DeploymentFailedException) => BadRequest(Json.toJson(e.failuresResponse))
                case Left(e) => throw e
              }
            }
          )
        case _ => Future.successful(BadRequest)
      }
  }

  def getEgressGateways(environment: String): Action[AnyContent] = authenticator {
    Ok(Json.toJson(service.getEgressGateways(environment)))
  }

}
