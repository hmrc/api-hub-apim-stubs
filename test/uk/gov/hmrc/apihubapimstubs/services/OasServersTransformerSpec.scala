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

package uk.gov.hmrc.apihubapimstubs.services

import io.swagger.v3.oas.models.servers.Server as OasServer
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import uk.gov.hmrc.apihubapimstubs.config.AppConfig
import uk.gov.hmrc.apihubapimstubs.models.openapi.Server as ConfigServer
import uk.gov.hmrc.apihubapimstubs.util.OpenApiStuff

import scala.jdk.CollectionConverters.*

class OasServersTransformerSpec extends AnyFreeSpec with Matchers with OpenApiStuff with MockitoSugar {

  import OasServersTransformerSpec.*

  "addServersOas" - {
    "must add the correct servers section" in {
      val fixture = buildFixture()
      val oas = fixture.transformer.addServersOas(baseOas)

      val expected =
        s"""
           |servers:
           |- url: ${server1.url}
           |  description: ${server1.description}
           |- url: ${server2.url}
           |  description: ${server2.description}
           |""".stripMargin

      oas must include(expected)
    }
  }

  "addServersOpenApi" - {
    "must add the correct servers section" in {
      val fixture = buildFixture()
      val openApi = fixture.transformer.addServersOpenApi(baseOas)

      val expected = Seq(
        buildOasServer(server1),
        buildOasServer(server2)
      )

      openApi.getServers.asScala mustBe expected
    }
  }

  def buildFixture(): Fixture = {
    val appConfig = mock[AppConfig]

    when(appConfig.servers).thenReturn(Seq(server1, server2))

    Fixture(new OasServersTransformer(appConfig))
  }

}

private object OasServersTransformerSpec {

  val baseOas = "openapi: 3.0.3"

  val server1: ConfigServer = ConfigServer("test-description-1", "test-url-1")
  val server2: ConfigServer = ConfigServer("test-description-2", "test-url-2")

  def buildOasServer(server: ConfigServer): OasServer = {
    new OasServer().description(server.description).url(server.url)
  }

  val config: Configuration = Configuration.from(
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

  case class Fixture(transformer: OasServersTransformer)

}
