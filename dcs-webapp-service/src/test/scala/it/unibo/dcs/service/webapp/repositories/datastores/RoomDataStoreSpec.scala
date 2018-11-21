package it.unibo.dcs.service.webapp.repositories.datastores

import it.unibo.dcs.service.webapp.interaction.Requests._
import it.unibo.dcs.service.webapp.model.{Message, Participation, Room}
import it.unibo.dcs.service.webapp.repositories.datastores.api.RoomApi
import it.unibo.dcs.service.webapp.repositories.datastores.commons.DataStoreSpec
import it.unibo.dcs.service.webapp.repositories.datastores.impl.RoomDataStoreNetwork
import rx.lang.scala.{Observable, Subscriber}

import scala.language.postfixOps

class RoomDataStoreSpec extends DataStoreSpec {

  private val roomApi: RoomApi = mock[RoomApi]
  private val dataStore: RoomDataStore = new RoomDataStoreNetwork(roomApi)

  private val roomCreationRequest = CreateRoomRequest("Room 1", user.username, token)
  private val roomDeletionRequest = DeleteRoomRequest(room.name, user.username, token)
  private val getRoomsRequest = GetRoomsRequest("martynha", token)
  private val joinRoomRequest = RoomJoinRequest(room.name, user.username, token)
  private val sendMessageRequest = SendMessageRequest(room.name, user.username, messageContent, messageTimestamp, token)
  private val leaveRoomRequest = RoomLeaveRequest(room.name, user.username, token)
  private val getRoomParticipationsRequest = GetRoomParticipationsRequest(room.name, user.username, token)
  private val getMessagesRequest = GetMessagesRequest(user.username, room.name, token)

  private val deleteRoomSubscriber = stub[Subscriber[String]]
  private val createRoomSubscriber = stub[Subscriber[Participation]]
  private val registrationSubscriber = stub[Subscriber[Unit]]
  private val getRoomsSubscriber: Subscriber[List[Room]] = stub[Subscriber[List[Room]]]
  private val joinRoomSubscriber = stub[Subscriber[Participation]]
  private val sendMessageSubscriber = stub[Subscriber[Message]]
  private val leaveRoomSubscriber = stub[Subscriber[Participation]]
  private val getRoomParticipationsSubscriber: Subscriber[Set[Participation]] = stub[Subscriber[Set[Participation]]]
  private val getMessagesSubscriber: Subscriber[List[Message]] = stub[Subscriber[List[Message]]]

  it should "gets a set of participations for a given room" in {
    //Given
    (roomApi getRoomParticipations _) expects getRoomParticipationsRequest returns Observable.just(participations)

    //When
    dataStore getRoomParticipations getRoomParticipationsRequest subscribe getRoomParticipationsSubscriber

    //Then
    //Verify that 'suscriber.onNext' has been callen once
    (getRoomParticipationsSubscriber onNext _) verify participations once()
    // Verify that `subscriber.onCompleted` has been called once
    (() => getRoomParticipationsSubscriber onCompleted) verify() once()
  }

  it should "create a new room" in {
    // Given
    (roomApi createRoom _) expects roomCreationRequest returns Observable.just(participation) noMoreThanOnce()

    // When
    dataStore.createRoom(roomCreationRequest).subscribe(createRoomSubscriber)

    // Then
    // Verify that `subscriber.onNext` has been called once with `token` as argument
    (createRoomSubscriber onNext _) verify participation once()
    // Verify that `subscriber.onCompleted` has been called once
    (() => createRoomSubscriber onCompleted) verify() once()
  }

  it should "create a new participation when a user joins a room" in {
    // Given
    (roomApi joinRoom _) expects joinRoomRequest returns Observable.just(participation) once()

    // When
    dataStore.joinRoom(joinRoomRequest).subscribe(joinRoomSubscriber)

    // Then
    // Verify that `subscriber.onNext` has been called once with `token` as argument
    (joinRoomSubscriber onNext _) verify participation once()
    // Verify that `subscriber.onCompleted` has been called once
    (() => joinRoomSubscriber onCompleted) verify() once()
  }

  it should "return the old participation when a user leaves a room" in {
    // Given
    (roomApi leaveRoom _) expects leaveRoomRequest returns Observable.just(participation) once()

    // When
    dataStore.leaveRoom(leaveRoomRequest).subscribe(leaveRoomSubscriber)

    // Then
    // Verify that `subscriber.onNext` has been called once with `token` as argument
    (leaveRoomSubscriber onNext _) verify participation once()
    // Verify that `subscriber.onCompleted` has been called once
    (() => leaveRoomSubscriber onCompleted) verify() once()
  }

  it should "save a new user" in {
    // Given
    (roomApi registerUser _) expects registerRequest returns Observable.empty

    // When
    dataStore.registerUser(registerRequest).subscribe(registrationSubscriber)

    // Then
    (() => registrationSubscriber onCompleted) verify() once()
  }

  it should "delete an existing room" in {
    // Given
    (roomApi createRoom _) expects roomCreationRequest returns Observable.just(participation) noMoreThanOnce()
    (roomApi deleteRoom _) expects roomDeletionRequest returns Observable.just(room.name) noMoreThanOnce()

    // When
    dataStore.createRoom(roomCreationRequest)
      .flatMap(_ => dataStore.deleteRoom(roomDeletionRequest))
      .subscribe(deleteRoomSubscriber)

    // Then
    // Verify that `subscriber.onNext` has been called once with `token` as argument
    (deleteRoomSubscriber onNext _) verify room.name once()
    // Verify that `subscriber.onCompleted` has been called once
    (() => deleteRoomSubscriber onCompleted) verify() once()
  }

  it should "gets a list of rooms" in {
    //Given
    (roomApi getRooms _) expects getRoomsRequest returns Observable.just(rooms)

    //When
    dataStore getRooms getRoomsRequest subscribe getRoomsSubscriber

    //Then
    //Verify that 'suscriber.onNext' has been callen once
    (getRoomsSubscriber onNext _) verify rooms once()
    // Verify that `subscriber.onCompleted` has been called once
    (() => getRoomsSubscriber onCompleted) verify() once()
    //
  }

  it should "save a new message in a given room" in {
    //Given
    (roomApi sendMessage _) expects sendMessageRequest returns Observable.just(message)

    //When
    dataStore sendMessage sendMessageRequest subscribe sendMessageSubscriber

    //Then
    //Verify that 'suscriber.onNext' has been callen once
    (sendMessageSubscriber onNext _) verify message once()
    // Verify that `subscriber.onCompleted` has been called once
    (() => sendMessageSubscriber onCompleted) verify() once()
    //
  }

  it should "retrieve all the messages for the given room" in {
    //Given
    (roomApi getMessages _) expects getMessagesRequest returns Observable.just(messages)

    //When
    dataStore getMessages getMessagesRequest subscribe getMessagesSubscriber

    //Then
    //Verify that 'suscriber.onNext' has been callen once
    (getMessagesSubscriber onNext _) verify messages once()
    // Verify that `subscriber.onCompleted` has been called once
    (() => getMessagesSubscriber onCompleted) verify() once()
  }
}
