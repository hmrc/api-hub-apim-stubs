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

package uk.gov.hmrc.apihubapimstubs.models.idms

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Identity(
  applicationName: String,
  description: String,
  clientId: Option[String],
  clientSecret: String,
  scopes: Set[String]
){

  def toClientResponse: Option[ClientResponse] = clientId.map(ClientResponse(_, clientSecret))

}

object Identity {

  def apply(client:Client): Identity = {
    Identity(client.applicationName, client.description, None, generateSecret(), Set.empty)
  }

  implicit val readsIdentity: Reads[Identity] = (
    (JsPath \ "applicationName").read[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "clientId").readNullable[String] and
      (JsPath \ "clientSecret").read[String] and
      (JsPath \ "scopes").readNullable[Set[String]]
  )((applicationName, description, clientId, clientSecret, scopes) =>
    Identity(applicationName, description, clientId, clientSecret, scopes.getOrElse(Set.empty))
  )

  implicit val writesIdentity: Writes[Identity] = Json.writes[Identity]
  implicit val formatIdentity: Format[Identity] = Format(readsIdentity, writesIdentity)

  def generateSecret(): String = {
      val random = new scala.util.Random
      f"client-secret-${random.alphanumeric.take(6).mkString}-${random.alphanumeric.take(6).mkString}"
  }

}
