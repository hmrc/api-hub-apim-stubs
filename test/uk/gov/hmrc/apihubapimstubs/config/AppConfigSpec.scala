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

package uk.gov.hmrc.apihubapimstubs.config

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import uk.gov.hmrc.apihubapimstubs.config.AppConfig.serversConfigLoader
import uk.gov.hmrc.apihubapimstubs.models.openapi.Server

class AppConfigSpec extends AnyFreeSpec with Matchers with MockitoSugar {

  import AppConfigSpec.*

  "serversConfigLoader" - {
    "must correctly loads servers from config" in {
      val config = Configuration.from(
        Map(
          "oas.servers" -> Seq(
            Map(
              "description" -> server1.description,
              "url" -> server1.url
            ),
            Map(
              "description" -> server2.description,
              "url" -> server2.url
            )
          )
        )
      )

      val actual = config.get[Seq[Server]]("oas.servers")

      actual mustBe Seq(server1, server2)
    }
  }

}

private object AppConfigSpec {

  val server1: Server = Server("test-description-1", "test-url-1")
  val server2: Server = Server("test-description-2", "test-url-2")

}
