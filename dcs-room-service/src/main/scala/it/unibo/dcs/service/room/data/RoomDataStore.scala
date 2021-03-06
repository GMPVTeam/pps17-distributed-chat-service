package it.unibo.dcs.service.room.data

import it.unibo.dcs.service.room.model.{Message, Participation, Room}
import it.unibo.dcs.service.room.request._
import rx.lang.scala.Observable

trait RoomDataStore {

  def getMessages(request: GetMessagesRequest): Observable[List[Message]]

  def sendMessage(request: SendMessageRequest): Observable[Message]
  
  def getRoomParticipations(request: GetRoomParticipationsRequest): Observable[List[Participation]]

  def createUser(request: CreateUserRequest): Observable[Unit]

  def getRoomByName(request: GetRoomRequest): Observable[Room]

  def getRooms(request: GetRoomsRequest): Observable[List[Room]]

  def createRoom(request: CreateRoomRequest): Observable[Room]

  def deleteRoom(request: DeleteRoomRequest): Observable[String]

  def joinRoom(request: JoinRoomRequest): Observable[Participation]

  def leaveRoom(request: LeaveRoomRequest): Observable[Participation]

  def getParticipationByKey(request: JoinRoomRequest): Observable[Participation]

  def getParticipationsByUsername(request: GetUserParticipationsRequest): Observable[List[Room]]

}
