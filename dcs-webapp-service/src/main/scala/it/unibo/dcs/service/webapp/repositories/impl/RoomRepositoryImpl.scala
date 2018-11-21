package it.unibo.dcs.service.webapp.repositories.impl

import it.unibo.dcs.service.webapp.interaction.Requests._
import it.unibo.dcs.service.webapp.model.{Message, Participation, Room}
import it.unibo.dcs.service.webapp.repositories.RoomRepository
import it.unibo.dcs.service.webapp.repositories.datastores.RoomDataStore
import rx.lang.scala.Observable

class RoomRepositoryImpl(roomDataStore: RoomDataStore) extends RoomRepository {

  override def deleteRoom(request: DeleteRoomRequest): Observable[String] =
    roomDataStore.deleteRoom(request)

  override def createRoom(request: CreateRoomRequest): Observable[Participation] = roomDataStore.createRoom(request)

  override def registerUser(request: RegisterUserRequest): Observable[Unit] =
    roomDataStore.registerUser(request)

  override def getRooms(request: GetRoomsRequest): Observable[List[Room]] = roomDataStore.getRooms(request)

  override def joinRoom(request: RoomJoinRequest): Observable[Participation] = roomDataStore.joinRoom(request)

  override def sendMessage(request: SendMessageRequest): Observable[Message] = roomDataStore.sendMessage(request)
  
  override def leaveRoom(request: RoomLeaveRequest): Observable[Participation] = roomDataStore.leaveRoom(request)
  
  override def getRoomParticipations(request: GetRoomParticipationsRequest): Observable[Set[Participation]] =
    roomDataStore.getRoomParticipations(request)

  override def getUserParticipations(request: GetUserParticipationsRequest): Observable[List[Room]] = roomDataStore.getUserParticipations(request)

  override def getMessages(request: GetMessagesRequest): Observable[List[Message]] = roomDataStore.getMessages(request)
}
