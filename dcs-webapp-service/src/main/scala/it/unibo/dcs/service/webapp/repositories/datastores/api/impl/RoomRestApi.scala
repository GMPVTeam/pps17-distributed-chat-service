package it.unibo.dcs.service.webapp.repositories.datastores.api.impl

import java.util.Date

import io.vertx.lang.scala.json.{Json, JsonArray, JsonObject}
import it.unibo.dcs.commons.JsonHelper.Implicits.RichGson
import it.unibo.dcs.commons.RxHelper.Implicits.RichObservable
import it.unibo.dcs.commons.dataaccess.Implicits._
import it.unibo.dcs.commons.service.{AbstractApi, HttpEndpointDiscovery}
import it.unibo.dcs.exceptions.{InternalException, RoomServiceErrorException, bodyAsJsonArray, bodyAsJsonObject}
import it.unibo.dcs.service.webapp.gson
import it.unibo.dcs.service.webapp.interaction.Labels.JsonLabels
import it.unibo.dcs.service.webapp.interaction.Requests.Implicits._
import it.unibo.dcs.service.webapp.interaction.Requests._
import it.unibo.dcs.service.webapp.model.{Message, Participation, Room}
import it.unibo.dcs.service.webapp.repositories.datastores.api.RoomApi
import it.unibo.dcs.service.webapp.repositories.datastores.api.impl.RoomRestApi.Implicits._
import it.unibo.dcs.service.webapp.repositories.datastores.api.impl.RoomRestApi._
import rx.lang.scala.Observable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

class RoomRestApi(private[this] val discovery: HttpEndpointDiscovery)
  extends AbstractApi(discovery, "room-service") with RoomApi {

  override def createRoom(createRoomRequest: CreateRoomRequest): Observable[Participation] = {
    makeRequest(client =>
      Observable.from(client.post(roomsURI).sendJsonObjectFuture(createRoomRequest)))
      .map(bodyAsJsonObject(throw InternalException(emptyBodyErrorMessage)))
      .mapImplicitly
  }

  override def deleteRoom(deletionRequest: DeleteRoomRequest): Observable[String] = {
    makeRequest(client =>
      Observable.from(client.delete(deleteRoomURI(deletionRequest.name))
        .sendJsonObjectFuture(toDeleteRoomRequest(deletionRequest))))
      .map(bodyAsJsonObject(throw InternalException(emptyBodyErrorMessage)))
      .map(_.getString(JsonLabels.roomNameLabel))
  }

  override def registerUser(userRegistrationRequest: RegisterUserRequest): Observable[Unit] = {
    makeRequest(client =>
      Observable.from(client.post(usersURI).sendJsonObjectFuture(toCreateUserRequest(userRegistrationRequest))))
      .map(bodyAsJsonObject())
      .onErrorResumeNext(cause => Observable.error(RoomServiceErrorException(cause)))
      .toCompletable
  }

  override def joinRoom(request: RoomJoinRequest): Observable[Participation] = {
    makeRequest(client =>
      Observable.from(client.post(joinRoomURI(request.name))
        .sendJsonObjectFuture(toJoinRoomRequest(request))))
      .map(bodyAsJsonObject(throw InternalException(emptyBodyErrorMessage)))
      .mapImplicitly
  }

  override def leaveRoom(request: RoomLeaveRequest): Observable[Participation] = {
    makeRequest(client =>
      Observable.from(client.delete(leaveRoomURI(request.name, request.username))
        .sendFuture()))
      .map(bodyAsJsonObject(throw InternalException(emptyBodyErrorMessage)))
      .mapImplicitly
  }

  override def getRooms(request: GetRoomsRequest): Observable[List[Room]] = {
    makeRequest(client =>
      Observable.from(client.get(getRoomsURI(request.username)).sendFuture()))
      .map(bodyAsJsonArray(throw InternalException(emptyBodyErrorMessage)))
      .mapImplicitly
  }

  override def sendMessage(request: SendMessageRequest): Observable[Message] = {
    makeRequest(client =>
      Observable.from(client.post(sendMessageURI(request.name))
      .sendJsonObjectFuture(toSendMessageRequest(request))))
      .map(bodyAsJsonObject(throw InternalException(emptyBodyErrorMessage)))
      .mapImplicitly
  }
  
  override def getRoomParticipations(request: GetRoomParticipationsRequest): Observable[Set[Participation]] = {
    makeRequest(client =>
      Observable.from(client.get(RoomRestApi.roomParticipationsURI(request.name)).sendFuture()))
      .map(bodyAsJsonArray(throw InternalException(emptyBodyErrorMessage)))
      .mapImplicitly
  }
  
  override def getUserParticipations(request: GetUserParticipationsRequest): Observable[List[Room]] =
    makeRequest(client =>
      Observable.from(client.get(userParticipationsURI(request.username)).sendFuture()))
      .map(bodyAsJsonArray(throw InternalException(emptyBodyErrorMessage)))
      .mapImplicitly

  override def getMessages(request: GetMessagesRequest): Observable[List[Message]] =
    makeRequest(client =>
      Observable.from(client.get(getMessagesURI(request.name)).sendFuture()))
      .map(bodyAsJsonArray(throw InternalException(emptyBodyErrorMessage)))
      .mapImplicitly
}

private[impl] object RoomRestApi {

  private val uriSeparator = "/"

  private val roomsURI = "/rooms"

  private val usersURI = "/users"

  private def userParticipationsURI(username: String): String = s"$usersURI/$username/participations"

  private def roomParticipationsURI(roomName: String) = roomsURI + uriSeparator + roomName + "/participations"

  private def joinRoomURI(roomName: String) = s"$roomsURI/$roomName/participations"

  private def leaveRoomURI(roomName: String, username: String) = joinRoomURI(roomName) + "/" + username

  private def deleteRoomURI(roomName: String) = roomsURI + uriSeparator + roomName

  private def sendMessageURI(roomName: String) = s"$roomsURI/$roomName/messages"

  private def getMessagesURI(roomName: String) = s"$roomsURI/$roomName/messages"

  private def getRoomsURI(username: String) = s"${RoomRestApi.roomsURI}?user=$username"

  private val emptyBodyErrorMessage = "Room service returned an empty body"

  private def toDeleteRoomRequest(deleteRoomRequest: DeleteRoomRequest): JsonObject = {
    Json.obj((JsonLabels.usernameLabel, deleteRoomRequest.username))
  }

  private def toCreateUserRequest(registerUserRequest: RegisterUserRequest): JsonObject = {
    Json.obj((JsonLabels.usernameLabel, registerUserRequest.username))
  }

  private def toJoinRoomRequest(joinRoomRequest: RoomJoinRequest): JsonObject = {
    Json.obj((JsonLabels.usernameLabel, joinRoomRequest.username))
  }

  private def toSendMessageRequest(sendMessageRequest: SendMessageRequest): JsonObject = {
      Json.obj((JsonLabels.usernameLabel, sendMessageRequest.username),
        (JsonLabels.messageContentLabel, sendMessageRequest.content))
  }

  object Implicits {

    implicit def jsonObjectToParticipation(json: JsonObject): Participation = {
      println(json)
      gson fromJsonObject[Participation] json
    }

    implicit def jsonObjectToMessage(json: JsonObject): Message = {
      println(json)
      gson fromJsonObject[Message] json
    }

    implicit def jsonArrayToRooms(json: JsonArray): List[Room] = {
      Stream.range(0, json.size)
        .map(json.getJsonObject)
        .map(_.getString("name"))
        .map(Room)
        .toList
    }

    implicit def jsonArrayToMessages(json: JsonArray): List[Message] = {
      println(json)
      Stream.range(0, json.size)
        .map(json.getJsonObject)
        .map(json => {
          val timestamp: Date = json.getString("timestamp")
          Message(Room(json.getString("name")), json.getString("username"),
            json.getString("content"), timestamp)
        })
        .toList
    }
  }

}
