package it.unibo.dcs.service.webapp.repositories.datastores.impl

import it.unibo.dcs.service.webapp.interaction.Requests
import it.unibo.dcs.service.webapp.interaction.Requests._
import it.unibo.dcs.service.webapp.model.{Message, Participation, Room}
import it.unibo.dcs.service.webapp.repositories.datastores.RoomDataStore
import it.unibo.dcs.service.webapp.repositories.datastores.api.RoomApi
import rx.lang.scala.Observable

class RoomDataStoreNetwork(private val roomApi: RoomApi) extends RoomDataStore {

  override def deleteRoom(request: DeleteRoomRequest): Observable[String] = roomApi.deleteRoom(request)

  override def createRoom(request: CreateRoomRequest): Observable[Participation] = roomApi.createRoom(request)

  override def registerUser(request: Requests.RegisterUserRequest): Observable[Unit] = roomApi.registerUser(request)

  override def getRooms(request: GetRoomsRequest): Observable[List[Room]] = roomApi.getRooms(request)

  override def joinRoom(request: RoomJoinRequest): Observable[Participation] = roomApi.joinRoom(request)

  override def sendMessage(request: SendMessageRequest): Observable[Message] = roomApi.sendMessage(request)
  
  override def leaveRoom(request: RoomLeaveRequest): Observable[Participation] = roomApi.leaveRoom(request)
  
  override def getRoomParticipations(request: GetRoomParticipationsRequest): Observable[Set[Participation]] =
    roomApi.getRoomParticipations(request)
    
  override def getUserParticipations(request: GetUserParticipationsRequest): Observable[List[Room]] =
    roomApi.getUserParticipations(request)

  override def getMessages(request: GetMessagesRequest): Observable[List[Message]] = roomApi.getMessages(request)
}