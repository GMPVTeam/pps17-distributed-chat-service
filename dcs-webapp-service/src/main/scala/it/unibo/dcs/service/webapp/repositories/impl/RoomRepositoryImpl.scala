package it.unibo.dcs.service.webapp.repositories.impl

import it.unibo.dcs.service.webapp.interaction.Requests._
import it.unibo.dcs.service.webapp.model.Room
import it.unibo.dcs.service.webapp.repositories.RoomRepository
import it.unibo.dcs.service.webapp.repositories.datastores.RoomDataStore
import rx.lang.scala.Observable

class RoomRepositoryImpl(roomDataStore: RoomDataStore) extends RoomRepository {

  override def deleteRoom(request: DeleteRoomRequest): Observable[Unit] =
    roomDataStore.deleteRoom(request)

  override def createRoom(request: CreateRoomRequest): Observable[Room] = roomDataStore.createRoom(request)

  override def registerUser(request: RegisterUserRequest, token: String): Observable[Unit] =
    roomDataStore.registerUser(request, token)
}
