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
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.apihubapimstubs.controllers.auth.Authenticator
import uk.gov.hmrc.apihubapimstubs.models.idms.{Client, ClientScope, Identity}
import uk.gov.hmrc.apihubapimstubs.services.IdmsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IdmsController @Inject()(
  cc: ControllerComponents,
  identityService: IdmsService,
  authenticator: Authenticator
)(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def createClient(environment: String): Action[JsValue] = authenticator.async(parse.json) {
    implicit request =>
      request.body.validate[Client] match {
        case JsSuccess(client, _) =>
          logger.info(s"Creating client: $client")
          identityService.createClient(Identity(client)).map {
            case Some(clientResponse) => Created(Json.toJson(clientResponse))
            case None =>
              logger.error(s"Error creating new Identity object for application: ${client.applicationName}")
              InternalServerError
          }
        case e: JsError =>
          logger.warn(s"Error parsing request body: ${JsError.toJson(e)}")
          Future.successful(BadRequest)
      }
  }

  def deleteClient(environment: String, id: String): Action[AnyContent] = authenticator.async {
    logger.info(s"Deleting client $id")
    identityService.deleteClient(id).map {
      case Some(()) => Ok
      case None => NotFound
    }
  }

  def getClientSecret(environment: String, id: String): Action[AnyContent] = authenticator.async {
    logger.info(s"Getting secret for client id = $id")
    identityService.getClientSecret(id).map {
      case Some(secret) => Ok(Json.toJson(secret))
      case None => NotFound
    }
  }

  def generateNewClientSecret(environment: String, id: String): Action[AnyContent] = authenticator.async {
    logger.info(s"Creating new client secret for client id = $id")
    identityService.generateNewClientSecret(id).map {
      case Some(secret) => Ok(Json.toJson(secret))
      case None => NotFound
    }
  }

  def addClientScope(environment: String, id: String, clientScopeId: String): Action[AnyContent] = authenticator.async {
    logger.info(s"Adding client scope $id $clientScopeId")
    identityService.addClientScope(id, clientScopeId).map {
      case Some(_) => Ok
      case _ => NotFound
    }
  }

  def deleteClientScope(environment: String, id: String, clientScopeId: String): Action[AnyContent] = authenticator.async {
    logger.info(s"Deleting client scope $id $clientScopeId")
    identityService.deleteClientScope(id, clientScopeId).map {
      case Some(_) => Ok
      case _ => NotFound
    }
  }

  def getClientScopes(environment: String, id: String): Action[AnyContent] = authenticator.async {
    logger.info(s"Getting scopes for client id $id")
    identityService.getClient(id).map {
      case Some(identity) => Ok(Json.toJson(identity.scopes.map(ClientScope(_))))
      case _ => NotFound
    }
  }

}
