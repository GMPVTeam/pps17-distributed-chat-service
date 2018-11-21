package it.unibo.dcs.service.webapp.verticles.handler.impl

import java.util.Date

import io.vertx.core.http.HttpHeaders
import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.core.Context
import io.vertx.scala.core.eventbus.{EventBus, Message}
import io.vertx.scala.ext.web.RoutingContext
import it.unibo.dcs.commons.VertxHelper.Implicits.RichEventBus
import it.unibo.dcs.commons.dataaccess.Implicits.dateToString
import it.unibo.dcs.exceptions.InternalException
import it.unibo.dcs.service.webapp.interaction.Labels.JsonLabels._
import it.unibo.dcs.service.webapp.interaction.Labels.{JsonLabels, ParamLabels}
import it.unibo.dcs.service.webapp.interaction.Requests.Implicits._
import it.unibo.dcs.service.webapp.interaction.Requests._
import it.unibo.dcs.service.webapp.repositories.{AuthenticationRepository, RoomRepository, UserRepository}
import it.unibo.dcs.service.webapp.usecases._
import it.unibo.dcs.service.webapp.verticles.Addresses._
import it.unibo.dcs.service.webapp.verticles.handler.ServiceRequestHandler
import it.unibo.dcs.service.webapp.verticles.handler.impl.subscribers._

import scala.language.postfixOps

final class ServiceRequestHandlerImpl(private[this] val eventBus: EventBus,
                                      private[this] val userRepository: UserRepository,
                                      private[this] val authRepository: AuthenticationRepository,
                                      private[this] val roomRepository: RoomRepository) extends ServiceRequestHandler {

  private[this] lazy val roomDeleted = eventBus.address(rooms.deleted)
  private[this] lazy val roomJoined = eventBus.address(rooms.joined)
  private[this] lazy val messageSent = eventBus.address(messages.sent)
  private[this] lazy val roomLeft = eventBus.address(rooms.left)
  private[this] lazy val roomCreated = eventBus.address(rooms.created)
  private[this] lazy val userOnline = eventBus.address(users.online)
  private[this] lazy val userOffline = eventBus.address(users.offline)
  private[this] def userTyped(room: String) = eventBus.address(users.typing + "." + room)

  override def handleRegistration(context: RoutingContext)(implicit ctx: Context): Unit =
    handleRequestBody(context) {
      val useCase = RegisterUserUseCase.create(authRepository, userRepository, roomRepository)
      useCase(_, RegisterUserSubscriber(context.response(), userOnline))
    }

  override def handleLogout(context: RoutingContext)(implicit ctx: Context): Unit =
    handleRequestToken(context) {
      token =>
        handleRequestBody(context) {
          request =>
            val useCase = LogoutUserUseCase.create(authRepository, userRepository)
            useCase(request.put(authenticationLabel, token), LogoutUserSubscriber(context.response(), userOffline))
        }
    }

  override def handleLogin(context: RoutingContext)(implicit ctx: Context): Unit =
    handleRequestBody(context) {
      val useCase = LoginUserUseCase.create(authRepository, userRepository)
      useCase(_, LoginUserSubscriber(context.response, userOnline))
    }

  override def handleUserEditing(context: RoutingContext)(implicit ctx: Context): Unit =
    handleRequestToken(context) {
      token =>
        handleRequestBody(context) {
          request =>
            handleRequestParam(context, ParamLabels.userLabel) {
              userName => {
                val useCase = EditUserUseCase.create(authRepository, userRepository)
                useCase(request.put(authenticationLabel, token).put(usernameLabel, userName),
                  EditUserSubscriber(context.response()))
              }
            }
        }
    }

  override def handleRoomCreation(context: RoutingContext)(implicit ctx: Context): Unit =
    handleRequestToken(context) {
      token =>
        handleRequestBody(context) {
          request =>
            val useCase = CreateRoomUseCase(authRepository, roomRepository)
            useCase(request.put(authenticationLabel, token),
              RoomCreationSubscriber(context.response, roomCreated, roomJoined))
        }
    }

  override def handleRoomDeletion(context: RoutingContext)(implicit ctx: Context): Unit =
    handleRequestToken(context) {
      token =>
        handleRequestParam(context, ParamLabels.roomNameLabel) {
          roomName =>
            handleRequestBody(context) {
              request =>
                val useCase = DeleteRoomUseCase.create(authRepository, roomRepository)
                useCase(request.put(JsonLabels.roomNameLabel, roomName).put(JsonLabels.authenticationLabel, token),
                  RoomDeletionSubscriber(context.response, roomDeleted))
            }
        }
    }

  override def handleJoinRoom(context: RoutingContext)(implicit ctx: Context): Unit =
    handleRequestToken(context) {
      token =>
        handleRequestParam(context, ParamLabels.roomNameLabel) {
          roomName =>
            handleRequestBody(context) {
              request =>
                val useCase = JoinRoomUseCase(authRepository, roomRepository)
                useCase(request.put(JsonLabels.roomNameLabel, roomName).put(JsonLabels.authenticationLabel, token),
                  JoinRoomSubscriber(context.response(), roomJoined))
            }
        }
    }

  override def handleLeaveRoom(context: RoutingContext)(implicit ctx: Context): Unit =
    handleRequestToken(context) {
      token =>
        handleRequestParam(context, ParamLabels.roomNameLabel) {
          roomName =>
            handleRequestParam(context, ParamLabels.userLabel) {
              userName =>
                val useCase = LeaveRoomUseCase(authRepository, roomRepository)
                val request = RoomLeaveRequest(roomName, userName, token)
                useCase(request, LeaveRoomSubscriber(context.response(), roomLeft))
            }
        }
    }

  override def handleGetRooms(context: RoutingContext)(implicit ctx: Context): Unit =
    handleRequestParam(context, ParamLabels.userLabel) {
      username => {
        handleRequestToken(context) {
          token => {
            val useCase = GetRoomsUseCase(authRepository, roomRepository)
            useCase(Json.obj((usernameLabel, username), (tokenLabel, token)), GetRoomsSubscriber(context.response))
          }
        }
      }
    }


  override def handleSendMessage(context: RoutingContext)(implicit ctx: Context): Unit =
    handleRequestToken(context) {
      token =>
        handleRequestParam(context, ParamLabels.roomNameLabel) {
          roomName =>
            handleRequestBody(context) {
              request =>
                val useCase = SendMessageUseCase(authRepository, roomRepository)
                val timestamp: String = new Date
                useCase(request.put(JsonLabels.roomNameLabel, roomName).put(JsonLabels.tokenLabel, token).put(JsonLabels.messageTimestampLabel, timestamp),
                  SendMessageSubscriber(context.response(), messageSent))
            }
        }
    }

  override def handleGetRoomParticipations(context: RoutingContext)(implicit ctx: Context): Unit =
    handleRequestParam(context, ParamLabels.roomNameLabel) {
      roomName => {
        handleRequestToken(context) {
          token => {
            handleRequestParam(context, ParamLabels.userLabel) {
              username =>
                val useCase = GetRoomParticipationsUseCase(authRepository, roomRepository)
                useCase(GetRoomParticipationsRequest(roomName, username, token),
                  GetRoomParticipationsSubscriber(context.response()))
            }
          }
        }
      }
    }

  override def handleGetUserParticipations(context: RoutingContext)(implicit ctx: Context): Unit =
    handleRequestParam(context, ParamLabels.usernameLabel) {
      username => {
        handleRequestToken(context) {
          token => {
            val useCase = GetUserParticipationsUseCase(authRepository, roomRepository)
            useCase(Json.obj((usernameLabel, username), (tokenLabel, token)),
              GetUserParticipationsSubscriber(context.response))
          }
        }
      }
    }

  override def handleGetUser(context: RoutingContext)(implicit ctx: Context): Unit =
    handleRequestParam(context, ParamLabels.usernameLabel) {
      username => {
            val useCase = GetUserUseCase(authRepository, userRepository)
            useCase(GetUserRequest(username), GetUserSubscriber(context.response()))
      }
    }

  override def handleGetMessages(context: RoutingContext)(implicit ctx: Context): Unit =
    handleRequestParam(context, ParamLabels.roomNameLabel) {
      roomName => {
            val useCase = GetMessagesUseCase(authRepository, roomRepository)
            useCase(Json.obj((roomNameLabel, roomName)), GetMessagesSubscriber(context.response()))
      }
    }

  override def handleUserOffline(message: Message[JsonObject])(implicit ctx: Context): Unit = {
    val json = message.body
    val useCase = UserOfflineUseCase(userRepository)
    useCase(json, UserOfflineSubscriber(userOffline))
  }

  override def handleTypingUser(message: Message[JsonObject])(implicit ctx: Context): Unit = {
    val typingUserRequest: NotifyTypingUserRequest = message.body()
    val useCase = NotifyTypingInRoomUseCase(authRepository)
    useCase(typingUserRequest, NotifyTypingInRoomSubscriber(userTyped(typingUserRequest.name)))
  }

  private[this] def handleRequestBody(context: RoutingContext)(handler: JsonObject => Unit): Unit =
    context.getBodyAsJson().fold(throw InternalException("Request body required"))(handler)

  private[this] def handleRequestParam(context: RoutingContext, param: String)(handler: String => Unit): Unit =
    context.request().getParam(param).fold(throw InternalException(s"Request param required: $param"))(handler)

  private[this] def handleRequestHeader(context: RoutingContext, header: String)(handler: String => Unit): Unit =
    context.request().getHeader(header).fold(throw InternalException("Authorization token required"))(handler)

  private[this] def handleRequestToken(context: RoutingContext)(handler: String => Unit): Unit = {
    handleRequestHeader(context, HttpHeaders.AUTHORIZATION.toString)(handler)
  }

}
