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

import scala.util.matching.Regex

case class SemVer(version: String) {

  import SemVer.*

  def major: Int = {
    getPart(Major)
  }

  def minor: Int = {
    getPart(Minor)
  }

  def revision: Int = {
    getPart(Revision)
  }

  def increment(part: Part): SemVer = {
    part match {
      case Major => incrementMajor()
      case Minor => incrementMinor()
      case Revision => incrementRevision()
    }
  }

  def incrementMajor(): SemVer = {
    SemVer(major + 1, minor, revision)
  }

  def incrementMinor(): SemVer = {
    SemVer(major, minor + 1, revision)
  }

  def incrementRevision(): SemVer = {
    SemVer(major, minor, revision + 1)
  }

  private def getPart(part: Part): Int = {
    version match {
      case semVerRegex(major, minor, revision) =>
        part match {
          case Major => major.toInt
          case Minor => minor.toInt
          case Revision => revision.toInt
        }
      case _ => throw new IllegalStateException(s"Not valid SemVer: $version")
    }
  }

}

object SemVer {

  sealed trait Part

  case object Major extends Part
  case object Minor extends Part
  case object Revision extends Part

  private val semVerRegex: Regex = """^(\d*)[.](\d*)[.](\d*)$""".r

  def apply(major: Int, minor: Int, revision: Int): SemVer = {
    SemVer(s"$major.$minor.$revision")
  }

}
