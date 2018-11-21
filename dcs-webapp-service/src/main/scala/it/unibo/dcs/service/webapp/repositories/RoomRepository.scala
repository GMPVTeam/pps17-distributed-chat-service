package it.unibo.dcs.service.webapp.repositories

import it.unibo.dcs.service.webapp.interaction.Requests.{CreateRoomRequest, DeleteRoomRequest, RegisterUserRequest, RoomJoinRequest, _}
import it.unibo.dcs.service.webapp.model.{Message, Participation, Room}
import it.unibo.dcs.service.webapp.repositories.datastores.RoomDataStore
import it.unibo.dcs.service.webapp.repositories.impl.RoomRepositoryImpl
import rx.lang.scala.Observable

/** Structure that handles Rooms data access and storage. */
trait RoomRepository {

  /** It retrieves all the messages for a given room
    *
    * @param request needed data to retrieve all the participations for a given room
    * @return an observable stream of all the participations
    */
  def getMessages(request: GetMessagesRequest): Observable[List[Message]]

  /** It allows you to send a message in a room
    *
    * @param request needed data to retrieve all the participations for a given room
    * @return an observable stream of all the participations
    */
  def sendMessage(request: SendMessageRequest) : Observable[Message]
  
  /** It retrieves all the participations for a given room
    *
    * @param request needed data to retrieve all the participations for a given room
    * @return an observable stream of all the participations
    */
  def getRoomParticipations(request: GetRoomParticipationsRequest): Observable[Set[Participation]]

  /** Store a new user given its information
    *
    * @param request needed data to register a new user
    * @return an empty observable
    */
  def registerUser(request: RegisterUserRequest): Observable[Unit]

  /** Create and store a new room given the creation request
    *
    * @param request Needed data to create a new room
    * @return an observable stream composed by the created participation. */
  def createRoom(request: CreateRoomRequest): Observable[Participation]

  /** Delete a room given the deletion request
    *
    * @param request the room deletion request
    * @return an observable stream of the deleted room's name
    */
  def deleteRoom(request: DeleteRoomRequest): Observable[String]

  /** It get the list of all rooms where the user has not yet joined
    *
    * @param request get rooms request
    * @return an observable stream of the list of rooms
    */
  def getRooms(request: GetRoomsRequest): Observable[List[Room]]

  /** It creates a new user participation in a given room
    *
    * @param request needed data to join a room for a user
    * @return an observable stream of the new participation
    */
  def joinRoom(request: RoomJoinRequest): Observable[Participation]

  def leaveRoom(request: RoomLeaveRequest): Observable[Participation]
  
  def getUserParticipations(request: GetUserParticipationsRequest): Observable[List[Room]]

}

/** Companion object */
object RoomRepository {

  /** Factory method to create the room repository
    *
    * @param roomDataStore room data store reference
    * @return the RoomRepository instance */
  def apply(roomDataStore: RoomDataStore): RoomRepository = new RoomRepositoryImpl(roomDataStore)
}

