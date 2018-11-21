package it.unibo.dcs.service.webapp.interaction

import io.vertx.lang.scala.json.{JsonArray, JsonObject}
import it.unibo.dcs.commons.JsonHelper.Implicits.RichGson
import it.unibo.dcs.service.webapp.gson
import it.unibo.dcs.service.webapp.interaction.Labels.JsonLabels.tokenLabel
import it.unibo.dcs.service.webapp.model.{Message, Participation, Room, User}

import scala.language.{implicitConversions, postfixOps}

/** It wraps all results produced by the use cases executions. */
object Results {

  /** Sum type that represents all the use cases results used in Distributed Chat Service application */
  sealed trait DcsResult

  final case class LoginResult(user: User, token: String) extends DcsResult

  final case class RegisterResult(user: User, token: String) extends DcsResult

  final case class LogoutResult(user: User) extends DcsResult

  final case class UserEditingResult(user: User) extends DcsResult

  final case class RoomCreationResult(participation: Participation) extends DcsResult

  final case class RoomJoinResult(participation: Participation) extends DcsResult

  final case class RoomLeaveResult(participation: Participation) extends DcsResult

  final case class GetRoomsResult(rooms: List[Room]) extends DcsResult

  final case class SendMessageResult(message: Message) extends DcsResult

  final case class GetRoomParticipationsResult(participations: Set[Participation]) extends DcsResult

  final case class GetUserParticipationsResult(rooms: List[Room]) extends DcsResult

  final case class NotifyTypingUserResult(username: String) extends DcsResult

  final case class GetUserResult(user: User) extends DcsResult

  final case class GetMessagesResult(messages: List[Message]) extends DcsResult

  /** It enables implicit conversions in order to clean code that deals with results. */
  object Implicits {

    implicit def registrationResultToJsonString(result: RegisterResult): String =
      resultToJsonString(result.user, _.put(tokenLabel, result.token))

    implicit def loginResultToJsonString(result: LoginResult): String =
      resultToJsonString(result.user, _.put(tokenLabel, result.token))

    implicit def editUserResultToJsonString(result: UserEditingResult): String =
      resultToJsonString(result.user, identity)

    implicit def roomCreationResultToJsonString(result: RoomCreationResult): String =
      resultToJsonString(result.participation)

    implicit def roomJoinResultToJsonString(result: RoomJoinResult): String = resultToJsonString(result.participation)

    implicit def roomLeaveResultToJsonString(result: RoomLeaveResult): String = resultToJsonString(result.participation)

    private def resultToJsonString(result: Product, transformations: JsonObject => JsonObject = identity): String =
      transformations(gson toJsonObject result) encode

    implicit def registrationResultToJsonObject(result: RegisterResult): JsonObject = {
      (gson toJsonObject result.user).put(tokenLabel, result.token)
    }

    implicit def loginResultToJsonObject(result: LoginResult): JsonObject =
      (gson toJsonObject result.user).put(tokenLabel, result.token)

    implicit def logoutResultToJsonObject(result: LogoutResult): JsonObject =
      gson toJsonObject result.user

    implicit def editUserResultToJsonObject(result: UserEditingResult): JsonObject =
      gson toJsonObject result.user

    implicit def roomCreationResultToJsonObject(result: RoomCreationResult): JsonObject =
      gson toJsonObject result.participation

    implicit def roomJoinResultToJsonObject(result: RoomJoinResult): JsonObject =
      gson toJsonObject result.participation

    implicit def sendMessageResultToJsonObject(result: SendMessageResult): JsonObject =
      gson toJsonObject result.message

    implicit def roomLeaveResultToJsonObject(result: RoomLeaveResult): JsonObject =
      gson toJsonObject result.participation

    implicit def roomToJsonObject(room: Room): JsonObject = gson toJsonObject room

    implicit def getRoomsToJsonArray(result: GetRoomsResult): JsonArray =
      gson toJsonArray result.rooms

    implicit def roomParticipationsToJsonArray(result: GetRoomParticipationsResult): JsonArray =
      gson toJsonArray result.participations

    implicit def getUserParticipationsToJsonArray(result: GetUserParticipationsResult): JsonArray =
      gson toJsonArray result.rooms

    implicit def getUserToJsonObject(result: GetUserResult): JsonObject =
      gson toJsonObject result.user

    implicit def getMessagesToJsonArray(result: GetMessagesResult): JsonArray =
      gson toJsonArray result.messages

  }

}
