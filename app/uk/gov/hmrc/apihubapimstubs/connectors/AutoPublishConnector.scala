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

package uk.gov.hmrc.apihubapimstubs.connectors

import com.google.inject.{Inject, Singleton}
import uk.gov.hmrc.apihubapimstubs.models.exception.ApimStubException
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AutoPublishConnector @Inject()(
  servicesConfig: ServicesConfig,
  httpClient: HttpClientV2
)(implicit ec: ExecutionContext) {

  def publish(serviceId: String)(implicit hc: HeaderCarrier): Future[Either[ApimStubException, Unit]] = {
    val url = url"$baseUrl/integration-catalogue-autopublish/apis/$serviceId/publish"

    httpClient.put(url)
      .execute[Unit]
      .map(_ => Right(()))
  }

  private def baseUrl: String = {
    val baseUrl = servicesConfig.baseUrl("integration-catalogue-autopublish")
    val path = servicesConfig.getConfString("integration-catalogue-autopublish.path", "")

    if (path.isEmpty) {
      baseUrl
    }
    else {
      s"$baseUrl/$path"
    }
  }

}
