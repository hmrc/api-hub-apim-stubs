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

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers
import play.api.test.Helpers.AUTHORIZATION
import uk.gov.hmrc.apihubapimstubs.services.{IdmsService, OasDiscoveryService, SimpleApiDeploymentService}

import java.time.{Clock, Instant, ZoneId}

trait ControllerSpecBase extends AnyFreeSpec with Matchers with MockitoSugar with OptionValues {

  val authorizationHeader: (String, String) = (AUTHORIZATION, "Basic YXBpbS1zdHViLWNsaWVudC1pZDphcGltLXN0dWItc2VjcmV0")
  val clock: Clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())

  case class Fixture(
    application: Application,
    idmsService: IdmsService,
    simpleApiDeploymentService: SimpleApiDeploymentService,
    oasDiscoveryService: OasDiscoveryService
  )

  def buildFixture(): Fixture = {
    val idmsService = MockitoSugar.mock[IdmsService]
    val simpleApiDeploymentService= MockitoSugar.mock[SimpleApiDeploymentService]
    val oasDiscoveryService = MockitoSugar.mock[OasDiscoveryService]

    val application = new GuiceApplicationBuilder()
      .overrides(
        bind[Clock].toInstance(clock),
        bind[IdmsService].toInstance(idmsService),
        bind[SimpleApiDeploymentService].toInstance(simpleApiDeploymentService),
        bind[OasDiscoveryService].toInstance(oasDiscoveryService)
      )
      .build()

    Fixture(application, idmsService, simpleApiDeploymentService, oasDiscoveryService)
  }

}
