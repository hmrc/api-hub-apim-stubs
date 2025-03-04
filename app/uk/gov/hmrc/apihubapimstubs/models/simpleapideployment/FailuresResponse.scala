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

case class FailuresResponse(code: String, reason: String, errors: Option[Seq[Error]] = None)

object FailuresResponse {

  val invalidOas: FailuresResponse = FailuresResponse(
    code = "BAD_REQUEST",
    reason = "Validation Failed."
  )

  def deploymentAlreadyExists(name: String, lineOfBusiness: String): FailuresResponse = {
    FailuresResponse(
      code = "BAD_REQUEST",
      reason = s"The API \"$name\" already exists in the Line of Business \"$lineOfBusiness\"."
    )
  }

  def deploymentAlreadyExists(deployment: Deployment): FailuresResponse = {
    deploymentAlreadyExists(deployment.name, deployment.lineOfBusiness)
  }

  implicit val formatFailure: Format[FailuresResponse] = Json.format[FailuresResponse]

}
