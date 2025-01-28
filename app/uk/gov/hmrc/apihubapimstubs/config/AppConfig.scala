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

package uk.gov.hmrc.apihubapimstubs.config

import com.typesafe.config.Config

import javax.inject.{Inject, Singleton}
import play.api.{ConfigLoader, Configuration}
import uk.gov.hmrc.apihubapimstubs.models.openapi.Server

import scala.jdk.CollectionConverters.*

@Singleton
class AppConfig @Inject()(config: Configuration) {

  import AppConfig.*

  val appName: String = config.get[String]("appName")

  val inboundClientId: String = config.get[String]("credentials.inbound.clientId")
  val inboundSecret: String = config.get[String]("credentials.inbound.secret")

  val servers: Seq[Server] = config.get[Seq[Server]]("oas.servers")

}

object AppConfig {

  implicit val serversConfigLoader: ConfigLoader[Seq[Server]] =
    (rootConfig: Config, path: String) => {
      rootConfig
        .getConfigList(path)
        .asScala
        .toSeq
        .map(
          config =>
            Server(
              config.getString("description"),
              config.getString("url")
            )
        )
    }

}
