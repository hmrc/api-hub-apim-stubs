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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, put, stubFor, urlEqualTo}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Configuration
import play.api.http.Status.NO_CONTENT
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class AutoPublishConnectorSpec
  extends AsyncFreeSpec
    with Matchers
    with WireMockSupport
    with HttpClientV2Support {

  "publish" - {
    "must place the correct request to the auto-publish service" in {
      val serviceId = "test-service-id"

      stubFor(
        put(urlEqualTo(s"/integration-catalogue-autopublish/apis/$serviceId/publish"))
          .willReturn(
            aResponse()
              .withStatus(NO_CONTENT)
          )
      )

      buildConnector().publish(serviceId)(HeaderCarrier()).map {
        result =>
          result mustBe Right(())
      }
    }
  }

  private def buildConnector(): AutoPublishConnector = {
    val configuration = Configuration.from(
      Map(
        "microservice.services.integration-catalogue-autopublish.host" -> wireMockHost,
        "microservice.services.integration-catalogue-autopublish.port" -> wireMockPort
      )
    )

    val servicesConfig = new ServicesConfig(configuration)

    new AutoPublishConnector(servicesConfig, httpClientV2)
  }

}