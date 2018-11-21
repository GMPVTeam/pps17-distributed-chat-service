package it.unibo.dcs.service.webapp.repositories.datastores

import it.unibo.dcs.service.webapp.interaction.Requests._
import it.unibo.dcs.service.webapp.model.{Message, Participation, Room}
import it.unibo.dcs.service.webapp.repositories.datastores.api.RoomApi
import it.unibo.dcs.service.webapp.repositories.datastores.impl.RoomDataStoreNetwork
import rx.lang.scala.Observable

/** Structure that allows access to rooms data by different means (e.g. network, file, database, ecc) */
trait RoomDataStore {

  /** It retrieves all the messages for a given room
    *
    * @param request needed data to retrieve all the participations for a given room
    * @return an observable stream of all the participations
    */
  def getMessages(request: GetMessagesRequest): Observable[List[Message]]

  /** It sends the user's message to the room
    *
    * @param request needed data to send message
    * @return an Observable stream of the new message
    */
  def sendMessage(request: SendMessageRequest): Observable[Message]
  /** It retrieves all the participations for a given room
    *
    * @param request needed data to retrieve all the participations for a given room
    * @return an observable stream of all the participations
    */
  def getRoomParticipations(request: GetRoomParticipationsRequest): Observable[Set[Participation]]

  /** It adds the user to the list of participants in the room
    *
    * @param request needed data to join a room
    * @return an observable stream of the new participation
    */
  def joinRoom(request: RoomJoinRequest): Observable[Participation]

  /** It removes the user from the list of participants in the room
    *
    * @param request needed data to leave the room
    * @return an observable stream of the old participation
    */
  def leaveRoom(request: RoomLeaveRequest): Observable[Participation]

  /** Register a new user given its info
    *
    * @param request needed info to register a new user
    * @return an empty observable
    */
  def registerUser(request: RegisterUserRequest): Observable[Unit]

  /** Delete a room given its info
    *
    * @param request needed data to delete a room
    * @return an observable stream of the delete room's identifier
    */
  def deleteRoom(request: DeleteRoomRequest): Observable[String]

  /** Store a new room given its information (e.g. room name)
    *
    * @param request needed data to store a room
    * @return an observable stream of just the created participation */
  def createRoom(request: CreateRoomRequest): Observable[Participation]

  /** Get a list of rooms
    *
    * @param request needed data to get rooms
    * @return an observable stream of the list of rooms
    */
  def getRooms(request: GetRoomsRequest): Observable[List[Room]]

  /** Get the list of user participations
    *
    * @param request needed data to get the participations
    * @return an observable stream of the list of participations
    */
  def getUserParticipations(request: GetUserParticipationsRequest): Observable[List[Room]]
}

/** Companion object */
object RoomDataStore {

  /** Factory method to create a room data store that access data via network
    *
    * @param roomApi APIs to concat the room service
    * @return the RoomDataStore instance */
  def roomDataStoreNetwork(roomApi: RoomApi): RoomDataStore = new RoomDataStoreNetwork(roomApi)
}