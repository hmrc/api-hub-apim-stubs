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

package uk.gov.hmrc.apihubapimstubs.repositories

import com.google.inject.{Inject, Singleton}
import com.mongodb.client.model.IndexOptions
import org.mongodb.scala.{MongoWriteException, ObservableFuture, SingleObservableFuture}
import org.mongodb.scala.model.{Filters, IndexModel, Indexes, ReplaceOptions}
import play.api.Logging
import uk.gov.hmrc.apihubapimstubs.models.deployment.Deployment
import uk.gov.hmrc.apihubapimstubs.models.exception.ApimStubException
import uk.gov.hmrc.apihubapimstubs.models.utility.MongoIdentifier.*
import uk.gov.hmrc.apihubapimstubs.util.ExceptionRaising
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.play.http.logging.Mdc

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeploymentsRepository @Inject()(mongoComponent: MongoComponent)(implicit ec: ExecutionContext) extends PlayMongoRepository[Deployment](
  collectionName = "deployments",
  domainFormat = formatDataWithMongoIdentifier[Deployment],
  mongoComponent = mongoComponent,
  indexes = Seq(
    IndexModel(
      Indexes.ascending("name", "environment"),
      new IndexOptions().name("unique-name-environment-index").unique(true)
    )
  )
) with Logging with RepositoryStuff with ExceptionRaising {

  def insert(deployment: Deployment): Future[Either[ApimStubException, Deployment]] = {
    Mdc.preservingMdc {
      collection
        .insertOne(
          document = deployment
        )
        .toFuture()
    } map (
      result =>
        Right(
          deployment.copy(
            id = Some(result.getInsertedId.asObjectId().getValue.toString)
          )
        )
    ) recoverWith {
      case e: MongoWriteException if isDuplicateKey(e) =>
        Future.successful(Left(raiseDeploymentExistsException.forDeployment(deployment)))
    }
  }

  def findAllInEnvironment(environment: String): Future[Seq[Deployment]] = {
    Mdc.preservingMdc {
      collection
        .find(Filters.equal("environment", environment))
        .toFuture()
    }
  }

  def findInEnvironment(environment: String, name: String): Future[Either[ApimStubException, Deployment]] = {
    Mdc.preservingMdc {
      collection
        .find(Filters.and(Filters.equal("name", name), Filters.equal("environment", environment)))
        .toFuture()
        .map(_.headOption)
    } map {
      case Some(deployment) => Right(deployment)
      case None => Left(raiseDeploymentNotFoundException.forService(environment, name))
    }
  }

  def update(deployment: Deployment): Future[Either[ApimStubException, Unit]] = {
    stringToObjectId(deployment.id) match {
      case Some(id) =>
        Mdc.preservingMdc {
          collection
            .replaceOne(
              filter = Filters.equal("_id", id),
              replacement = deployment,
              options     = ReplaceOptions().upsert(false)
            )
            .toFuture()
        } map (
          result =>
            if (result.getMatchedCount > 0) {
              if (result.getModifiedCount == 0) {
                logger.warn(s"Updating Deployment: Deployment with id $id was found, but was not updated.")
              }
              Right(())
            }
            else {
              Left(raiseDeploymentNotFoundException.forDeployment(deployment))
            }
        )
      case _ => Future.successful(Left(raiseDeploymentNotFoundException.forDeployment(deployment)))
    }
  }

}
