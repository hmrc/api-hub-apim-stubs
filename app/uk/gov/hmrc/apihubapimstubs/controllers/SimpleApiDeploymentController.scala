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
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions
import play.api.Logging
import play.api.libs.Files
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, MultipartFormData}
import uk.gov.hmrc.apihubapimstubs.controllers.auth.Authenticator
import uk.gov.hmrc.apihubapimstubs.models.simpleapideployment.*
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

@Singleton
class SimpleApiDeploymentController @Inject()(
  cc: ControllerComponents,
  authenticator: Authenticator
) extends BackendController(cc) with Logging {

  import SimpleApiDeploymentController._

  def validateOas(): Action[String] = authenticator(parse.tolerantText) {
    request =>
      if (isValidOas(request.body)) {
        Ok
      }
      else {
        BadRequest(Json.toJson(FailuresResponse.cannedResponse))
      }
  }

  def deployNewApi(): Action[MultipartFormData[Files.TemporaryFile]] = authenticator(parse.multipartFormData) {
    request =>
      (request.body.dataParts.get("metadata"), request.body.dataParts.get("openapi")) match {
        case (Some(Seq(metadata)), Some(Seq(openapi))) =>
          Json.parse(metadata).validate[CreateMetadata].fold(
            _ => BadRequest,
            validMetadata => {
              logger.info(s"JSON CreateMetadata body: ${Json.prettyPrint(Json.toJson(validMetadata))}")
              if (isValidOas(openapi)) {
                Ok(Json.toJson(DeploymentsResponse(validMetadata.name)))
              }
              else {
                BadRequest(Json.toJson(FailuresResponse.cannedResponse))
              }
            }
          )
        case _ => BadRequest
      }
  }

  def deployExistingApiWithNewConfiguration(serviceId: String): Action[MultipartFormData[Files.TemporaryFile]] = authenticator(parse.multipartFormData) {
    request =>
      (request.body.dataParts.get("metadata"), request.body.dataParts.get("openapi")) match {
        case (Some(Seq(metadata)), Some(Seq(openapi))) =>
          Json.parse(metadata).validate[UpdateMetadata].fold(
            _ => BadRequest,
            validMetadata => {
              logger.info(s"JSON UpdateMetadata body: ${Json.prettyPrint(Json.toJson(validMetadata))}")

              if (isValidOas(openapi)) {
                Ok(Json.toJson(DeploymentsResponse(serviceId)))
              }
              else {
                BadRequest(Json.toJson(FailuresResponse.cannedResponse))
              }
            }
          )
        case _ => BadRequest
      }
  }

  def deploymentFrom(): Action[JsValue] = authenticator(parse.json) {
    implicit request =>
      request.body.validate[DeploymentFrom].fold(
        _ => BadRequest,
        deploymentFrom => Ok(Json.toJson(DeploymentsResponse.apply(deploymentFrom.serviceId)))
      )
  }

  def getDeploymentDetails(serviceId: String): Action[AnyContent] = authenticator {
    Ok(Json.toJson(DetailsResponse.cannedResponse))
  }

  def getEgressGateways(): Action[AnyContent] = authenticator {
    Ok(Json.toJson(EgressGateway.cannedResponse))
  }

}

object SimpleApiDeploymentController {

  private def isValidOas(oas: String): Boolean = {
    val options: ParseOptions = new ParseOptions()
    options.setResolve(false)

    Option(new OpenAPIV3Parser().readContents(oas, null, options).getOpenAPI).isDefined
  }

}
