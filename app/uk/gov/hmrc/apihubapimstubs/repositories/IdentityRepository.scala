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

package uk.gov.hmrc.apihubapimstubs.repositories

import com.google.inject.{Inject, Singleton}
import org.bson.types.ObjectId
import org.mongodb.scala.SingleObservableFuture
import org.mongodb.scala.model.{Filters, Updates}
import play.api.Logging
import play.api.libs.json.{Format, JsPath, Reads, Writes}
import uk.gov.hmrc.apihubapimstubs.models.idms.Identity
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IdentityRepository @Inject()
(mongoComponent: MongoComponent)
(implicit ec: ExecutionContext)
  extends PlayMongoRepository[Identity](
    collectionName = "identities",
    mongoComponent = mongoComponent,
    domainFormat = IdentityRepository.mongoIdentityFormat,
    indexes = Seq()
  ) {

  import IdentityRepository.*

  def insert(identity: Identity): Future[Identity] = {
    collection
      .insertOne(
        document = identity
      )
      .toFuture()
      .map(
        result => identity.copy(
          clientId = Some(result.getInsertedId.asObjectId().getValue.toString)
        )
      )
  }

  def find(id: String): Future[Option[Identity]] = {
    stringToObjectId(id) match {
      case Some(objectId) =>
        collection
          .find(Filters.equal("_id", objectId))
          .headOption()
      case None => Future.successful(None)
    }
  }

  def update(identity: Identity): Future[Option[Identity]] = {
    identity.clientId match {
      case Some(clientId) =>
        stringToObjectId(clientId) match {
          case Some(objectId) =>
            collection.replaceOne(Filters.equal("_id", objectId), identity)
              .toFuture()
              .map(_ => Some(identity))
          case None => Future.successful(None)
        }
      case None => Future.successful(None)
    }
  }

  def addScope(clientId: String, clientScopeId: String): Future[Option[Unit]] = {
    stringToObjectId(clientId) match {
      case Some(objectId) =>
        collection
          .updateOne(
            filter = Filters.equal("_id", objectId),
            update = Updates.addToSet("scopes", clientScopeId)
          )
          .toFuture()
          .map {
            case result if result.getMatchedCount > 0 => Some(())
            case _ => None
          }
      case None => Future.successful(None)
    }
  }

  def removeScope(clientId: String, clientScopeId: String): Future[Option[Unit]] = {
    stringToObjectId(clientId) match {
      case Some(objectId) =>
        collection
          .updateOne(
            filter = Filters.equal("_id", objectId),
            update = Updates.pull("scopes", clientScopeId)
          )
          .toFuture()
          .map {
            case result if result.getMatchedCount > 0 => Some(())
            case _ => None
          }
      case None => Future.successful(None)
    }
  }

  def delete(clientId: String): Future[Option[Unit]] = {
    stringToObjectId(clientId) match {
      case Some(objectId) =>
        collection.deleteOne(Filters.equal("_id", objectId))
          .toFuture()
          .map(
            result =>
              if (result.getDeletedCount != 0) {
                Some(())
              }
              else {
                None
              }
          )
      case None => Future.successful(None)
    }
  }

}

object IdentityRepository extends Logging {

  private val mongoIdentityWithIdWrites: Writes[Identity] =
    Identity.formatIdentity.transform(
      json => json.transform(
        JsPath.json.update((JsPath \ "_id" \ "$oid").json.copyFrom((JsPath \ "clientId").json.pick))
          andThen (JsPath \ "clientId").json.prune
      ).get
    )

  private val mongoIdentityWrites: Writes[Identity] = (identity: Identity) => {
    identity.clientId match {
      case Some(_) => mongoIdentityWithIdWrites.writes(identity)
      case _ => Identity.formatIdentity.writes(identity)
    }
  }

  private val mongoIdentityReads: Reads[Identity] =
    JsPath.json.update((JsPath \ "clientId").json
      .copyFrom((JsPath \ "_id" \ "$oid").json.pick))
      .andThen(Identity.formatIdentity)

  val mongoIdentityFormat: Format[Identity] = Format(mongoIdentityReads, mongoIdentityWrites)

  private def stringToObjectId(id: String): Option[ObjectId] = {
    try {
      Some(new ObjectId(id))
    }
    catch {
      case _: IllegalArgumentException =>
        logger.debug(s"Invalid ObjectId specified: $id")
        None
    }
  }

}
