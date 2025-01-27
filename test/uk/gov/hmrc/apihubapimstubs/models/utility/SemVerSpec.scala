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

package uk.gov.hmrc.apihubapimstubs.models.utility

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.apihubapimstubs.models.utility.SemVer.{Major, Minor, Revision}

class SemVerSpec extends AnyFreeSpec with Matchers {

  "SemVer" - {
    "must return the correct major version" in {
      SemVer("1.2.3").major mustBe 1
      SemVer("10.20.30").major mustBe 10
    }

    "must return the correct minor version" in {
      SemVer("1.2.3").minor mustBe 2
      SemVer("10.20.30").minor mustBe 20
    }

    "must return the correct minor revision" in {
      SemVer("1.2.3").revision mustBe 3
      SemVer("10.20.30").revision mustBe 30
    }

    "must increment major correctly" in {
      SemVer("1.2.3").incrementMajor().version mustBe "2.2.3"
      SemVer("1.2.3").increment(Major).version mustBe "2.2.3"
    }

    "must increment minor correctly" in {
      SemVer("1.2.3").incrementMinor().version mustBe "1.3.3"
      SemVer("1.2.3").increment(Minor).version mustBe "1.3.3"
    }

    "must increment revision correctly" in {
      SemVer("1.2.3").incrementRevision().version mustBe "1.2.4"
      SemVer("1.2.3").increment(Revision).version mustBe "1.2.4"
    }
  }

}
