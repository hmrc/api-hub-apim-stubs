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

case class EgressGateway(
    id: String,
    friendlyName: String
)

object EgressGateway {

  implicit val formatEgressGateway: Format[EgressGateway] = Json.format[EgressGateway]

  val cannedResponse: Seq[EgressGateway] = Seq(
    EgressGateway("bpr", "Basis Period Reform - Denodo HIP"),
    EgressGateway("cbs", "Child Benefit System (CBS) - Integration Framework (IF)"),
    EgressGateway("cdcs-debt-pursuit", "Customer-centric Debt Collection Service (CDCS) - Debt Pursuit"),
    EgressGateway("cesa", "Computerised Environment for Self Assessment (CESA)"),
    EgressGateway("crdl", "CRDL - Denodo HIP"),
    EgressGateway("etmp", "Enterprise Tax Management Platform"),
    EgressGateway("gforms", "GForms - Denodo HIP"),
    EgressGateway("idms", "Integrated Debt Management System (IDMS) - (IF)"),
    EgressGateway("ipp", "Intelligent Payment Processing - Denodo HIP"),
    EgressGateway("itsd", "Income Tax Sub Domain (ITSD) - Integration Framework (IF)"),
    EgressGateway("mdtp", "Multi-channel Digital Tax Platform (MDTP)"),
    EgressGateway("nps", "National Insurance & PAYE System"),
    EgressGateway("ods-agents", "ODS-Agents"),
    EgressGateway("vdp", "VDP")
  )

}
