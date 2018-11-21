package it.unibo.dcs.service.user.data.impl

import java.util.Date

import io.vertx.lang.scala.json.{Json, JsonArray, JsonObject}
import io.vertx.scala.ext.sql.SQLConnection
import it.unibo.dcs.commons.JsonHelper.Implicits.RichGson
import it.unibo.dcs.commons.RxHelper.Implicits.RichObservable
import it.unibo.dcs.commons.dataaccess.DataStoreDatabase
import it.unibo.dcs.commons.dataaccess.Implicits.dateToString
import it.unibo.dcs.commons.dataaccess.ResultSetHelper.Implicits.RichResultSet
import it.unibo.dcs.commons.dataaccess.ResultSetHelper.foldResult
import it.unibo.dcs.exceptions.{UserAlreadyExistsException, UserNotFoundException}
import it.unibo.dcs.service.user.data.UserDataStore
import it.unibo.dcs.service.user.data.impl.Implicits.userDtoToUser
import it.unibo.dcs.service.user.data.impl.UserDataStoreDatabase.Implicits._
import it.unibo.dcs.service.user.data.impl.UserDataStoreDatabase._
import it.unibo.dcs.service.user.gson
import it.unibo.dcs.service.user.model.User
import it.unibo.dcs.service.user.request.{CreateUserRequest, EditUserRequest, GetUserRequest}
import rx.lang.scala.Observable

import scala.language.implicitConversions

final class UserDataStoreDatabase(connection: SQLConnection) extends DataStoreDatabase(connection) with UserDataStore {

  override def checkIfUserExists(request: GetUserRequest): Observable[Unit] =
    query(selectUserByUsername, request)
    .map(foldResult()(_ => throw UserAlreadyExistsException(request.username)))

  override def getUserByUsername(request: GetUserRequest): Observable[User] =
    query(selectUserByUsername, request)
      .map(foldResult(throw UserNotFoundException(request.username))(_.getRows.head))

  override def createUser(request: CreateUserRequest): Observable[User] =
    update(insertUser, request)
      .flatMap(_ => getUserByUsername(GetUserRequest(request.username)))

  override def editUser(request: EditUserRequest): Observable[User] =
    update(updateUser, request)
      .flatMap(_ => getUserByUsername(GetUserRequest(request.username)))

  override def updateAccess(username: String): Observable[Unit] = {
    update(updateAccessSql, Json.arr(dateToString(new Date()), username))
      .toCompletable
  }

}

private[impl] object UserDataStoreDatabase {

  val selectUserByUsername = "SELECT * FROM `users` WHERE `username` = ?"

  val insertUser = "INSERT INTO `users` (`username`, `first_name`, `last_name`) VALUES (?, ?, ?)"

  val updateUser: String = "UPDATE `users` SET `first_name` = ?, `last_name` = ?, `bio` = ?, `visible` = ?" +
    " WHERE (`username` = ?)"

  val updateAccessSql: String = "UPDATE `users` SET `last_seen` = ? WHERE (`username` = ?)"

  object Implicits {

    implicit def requestToParams(request: CreateUserRequest): JsonArray =
      new JsonArray().add(request.username).add(request.firstName).add(request.lastName)

    implicit def requestToParams(request: EditUserRequest): JsonArray =
      Json.arr(request.firstName, request.lastName, request.bio, request.visible, request.username)

    implicit def requestToParams(request: GetUserRequest): JsonArray =
      new JsonArray().add(request.username)

    implicit def jsonObjectToUser(json: JsonObject): User = gson.fromJsonObject[UserDto](json)

  }

}
