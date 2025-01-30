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
import play.api.Logging
import play.api.libs.Files
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, MultipartFormData}
import uk.gov.hmrc.apihubapimstubs.controllers.auth.Authenticator
import uk.gov.hmrc.apihubapimstubs.models.exception.{ApimStubException, DeploymentFailedException, DeploymentNotFoundException}
import uk.gov.hmrc.apihubapimstubs.models.simpleapideployment.{CreateMetadata, DeploymentFrom, DeploymentsResponse, EgressGateway, FailuresResponse, UpdateMetadata}
import uk.gov.hmrc.apihubapimstubs.services.SimpleApiDeploymentService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SimpleApiDeploymentController @Inject()(
  cc: ControllerComponents,
  authenticator: Authenticator,
  service: SimpleApiDeploymentService
)(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

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
          Json.parse(metadata).validate[CreateMetadata] match {
            case JsSuccess(createMetadata, _) =>
              service.deployNewApi(environment, createMetadata, oas).map {
                case Right(deploymentsResponse) => Ok(Json.toJson(deploymentsResponse))
                case Left(e: DeploymentFailedException) => BadRequest(Json.toJson(e.failuresResponse))
                case Left(e) => throw e
              }
            case e: JsError =>
              logger.warn(s"Deploy new API error parsing CreateMetadata ${Json.prettyPrint(JsError.toJson(e))}")
              Future.successful(BadRequest)
          }
        case (maybeMetadata, maybeOas) =>
          logger.warn(s"Deploy new API request incomplete, has metadata: ${maybeMetadata.isDefined} has OAS: ${maybeOas.isDefined}")
          Future.successful(BadRequest)
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
          Json.parse(metadata).validate[UpdateMetadata] match {
            case JsSuccess(updateMetadata, _) =>
              service.deployExistingApiWithNewConfiguration(environment, serviceId, updateMetadata, oas).map {
                case Right(deploymentsResponse) => Ok(Json.toJson(deploymentsResponse))
                case Left(e: DeploymentNotFoundException) => NotFound
                case Left(e: DeploymentFailedException) => BadRequest(Json.toJson(e.failuresResponse))
                case Left(e) => throw e
              }
            case e: JsError =>
              logger.warn(s"Deploy existing API with new configuration error parsing UpdateMetadata ${Json.prettyPrint(JsError.toJson(e))}")
              Future.successful(BadRequest)
          }
        case (maybeMetadata, maybeOas) =>
          logger.warn(s"Deploy existing API with new configuration request incomplete, has metadata: ${maybeMetadata.isDefined} has OAS: ${maybeOas.isDefined}")
          Future.successful(BadRequest)
      }
  }

  def getEgressGateways(environment: String): Action[AnyContent] = authenticator {
    Ok(Json.toJson(service.getEgressGateways(environment)))
  }

  def deploymentFrom(environment: String): Action[JsValue] = authenticator(parse.json).async {
    implicit request =>
      request.body.validate[DeploymentFrom] match {
        case JsSuccess(deploymentFrom, _) =>
          service.deploymentFrom(environment, deploymentFrom).map{
            case Right(deploymentsResponse) => Ok(Json.toJson(deploymentsResponse))
            case Left(e: DeploymentNotFoundException) => NotFound
            case Left(e) => throw e
          }
        case e: JsError =>
          logger.warn(s"Deployment from error parsing DeploymentFrom ${Json.prettyPrint(JsError.toJson(e))}")
          Future.successful(BadRequest)
      }
  }

}
