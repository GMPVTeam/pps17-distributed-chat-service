package it.unibo.dcs.service.room.repository.impl

import it.unibo.dcs.service.room.data.RoomDataStore
import it.unibo.dcs.service.room.model.{Room, _}
import it.unibo.dcs.service.room.repository.RoomRepository
import it.unibo.dcs.service.room.request._
import rx.lang.scala.Observable

final class RoomRepositoryImpl(private[this] val roomDataStore: RoomDataStore) extends RoomRepository {

  override def createUser(request: CreateUserRequest): Observable[Unit] = roomDataStore.createUser(request)

  override def deleteRoom(request: DeleteRoomRequest): Observable[String] = roomDataStore.deleteRoom(request)

  override def createRoom(request: CreateRoomRequest): Observable[Room] = roomDataStore.createRoom(request)

  override def getRoomByName(request: GetRoomRequest): Observable[Room] = roomDataStore.getRoomByName(request)

  override def getRooms(request: GetRoomsRequest): Observable[List[Room]] = roomDataStore.getRooms(request)
  
  override def joinRoom(request: JoinRoomRequest): Observable[Participation] = roomDataStore.joinRoom(request)

  override def leaveRoom(request: LeaveRoomRequest): Observable[Participation] = roomDataStore.leaveRoom(request)

  override def sendMessage(request: SendMessageRequest): Observable[Message] = roomDataStore.sendMessage(request)
  
  override def getParticipationByKey(request: JoinRoomRequest): Observable[Participation] =
    roomDataStore.getParticipationByKey(request)

  override def getRoomParticipations(request: GetRoomParticipationsRequest): Observable[List[Participation]] =
    roomDataStore.getRoomParticipations(request)

  override def getParticipationsByUsername(request: GetUserParticipationsRequest): Observable[List[Room]] = roomDataStore.getParticipationsByUsername(request)

  override def getMessages(request: GetMessagesRequest): Observable[List[Message]] = roomDataStore.getMessages(request)
}
