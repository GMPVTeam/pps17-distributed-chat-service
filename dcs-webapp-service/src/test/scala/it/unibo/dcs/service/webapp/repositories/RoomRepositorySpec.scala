package it.unibo.dcs.service.webapp.repositories

import it.unibo.dcs.service.webapp.interaction.Requests._
import it.unibo.dcs.service.webapp.model.{Message, Participation, Room}
import it.unibo.dcs.service.webapp.repositories.commons.RepositorySpec
import it.unibo.dcs.service.webapp.repositories.datastores.RoomDataStore
import it.unibo.dcs.service.webapp.repositories.impl.RoomRepositoryImpl
import rx.lang.scala.{Observable, Subscriber}

import scala.language.postfixOps

class RoomRepositorySpec extends RepositorySpec {

  private val roomDataStore: RoomDataStore = mock[RoomDataStore]
  private val repository: RoomRepository = new RoomRepositoryImpl(roomDataStore)

  private val roomCreationRequest = CreateRoomRequest("Room 1", user.username, token)
  private val deleteRoomRequest = DeleteRoomRequest(room.name, user.username, token)
  private val getRoomsRequest = GetRoomsRequest("martynha", token)
  private val joinRoomRequest = RoomJoinRequest(room.name, user.username, token)
  private val sendMessageRequest = SendMessageRequest(room.name, user.username, messageContent, messageTimestamp, token)
  private val leaveRoomRequest = RoomLeaveRequest(room.name, user.username, token)
  private val getRoomParticipationsRequest = GetRoomParticipationsRequest(room.name, user.username, token)
  private val getMessagesRequest = GetMessagesRequest(user.username, room.name, token)

  private val joinRoomSubscriber = stub[Subscriber[Participation]]
  private val leaveRoomSubscriber = stub[Subscriber[Participation]]
  private val createRoomSubscriber: Subscriber[Participation] = stub[Subscriber[Participation]]
  private val deleteRoomSubscriber: Subscriber[String] = stub[Subscriber[String]]
  private val registerUserSubscriber: Subscriber[Unit] = stub[Subscriber[Unit]]
  private val getRoomsSubscriber: Subscriber[List[Room]] = stub[Subscriber[List[Room]]]
  private val sendMessageSubscriber = stub[Subscriber[Message]]
  private val getRoomParticipationsSubscriber: Subscriber[Set[Participation]] = stub[Subscriber[Set[Participation]]]
  private val getMessagesSubscriber: Subscriber[List[Message]] = stub[Subscriber[List[Message]]]

  it should "get all the participations for a given room" in {
    //Given
    (roomDataStore getRoomParticipations _) expects getRoomParticipationsRequest returns
      Observable.just(participations)

    //When
    repository getRoomParticipations getRoomParticipationsRequest subscribe getRoomParticipationsSubscriber

    //Then
    //Verify that 'subscriber.onNext' has been called once
    (getRoomParticipationsSubscriber onNext _) verify participations once()
    // Verify that `subscriber.onCompleted` has been called once
    (() => getRoomParticipationsSubscriber onCompleted) verify() once()
  }

  it should "create a new room" in {
    // Given
    (roomDataStore createRoom _) expects roomCreationRequest returns Observable.just(participation) noMoreThanOnce()

    // When
    repository createRoom roomCreationRequest subscribe createRoomSubscriber

    // Then
    // Verify that `subscriber.onNext` has been called once with `token` as argument
    (createRoomSubscriber onNext _) verify participation once()
    // Verify that `subscriber.onCompleted` has been called once
    (() => createRoomSubscriber onCompleted) verify() once()
  }

  it should "create a new participation when a user join a room" in {
    // Given
    (roomDataStore joinRoom _) expects joinRoomRequest returns Observable.just(participation) once()

    // When
    repository joinRoom joinRoomRequest subscribe joinRoomSubscriber

    // Then
    // Verify that `subscriber.onNext` has been called once with `token` as argument
    (joinRoomSubscriber onNext _) verify participation once()
    // Verify that `subscriber.onCompleted` has been called once
    (() => joinRoomSubscriber onCompleted) verify() once()
  }

  it should "return the old participation when a user leaves a room" in {
    // Given
    (roomDataStore leaveRoom _) expects leaveRoomRequest returns Observable.just(participation) once()

    // When
    repository leaveRoom leaveRoomRequest subscribe leaveRoomSubscriber

    // Then
    // Verify that `subscriber.onNext` has been called once with `token` as argument
    (leaveRoomSubscriber onNext _) verify participation once()
    // Verify that `subscriber.onCompleted` has been called once
    (() => leaveRoomSubscriber onCompleted) verify() once()
  }

  it should "delete an existing room" in {
    // Given
    (roomDataStore createRoom _) expects roomCreationRequest returns Observable.just(participation) noMoreThanOnce()
    (roomDataStore deleteRoom _) expects deleteRoomRequest returns Observable.just(room.name) noMoreThanOnce()

    // When
    repository.createRoom(roomCreationRequest)
      .flatMap(_ => repository.deleteRoom(deleteRoomRequest))
      .subscribe(deleteRoomSubscriber)

    // Then
    (deleteRoomSubscriber onNext _) verify room.name once()
    (() => deleteRoomSubscriber onCompleted) verify() once()
  }

  it should "save a new user" in {
    // Given
    (roomDataStore registerUser _) expects registerRequest returns Observable.empty noMoreThanOnce()

    // When
    repository.registerUser(registerRequest).subscribe(registerUserSubscriber)

    // Then
    (() => registerUserSubscriber onCompleted) verify() once()
  }

  it should "gets a list of rooms" in {
    //Given
    (roomDataStore getRooms _) expects getRoomsRequest returns Observable.just(rooms)

    //When
    repository getRooms getRoomsRequest subscribe getRoomsSubscriber

    //Then
    //Verify that 'subscriber.onNext' has been called once
    (getRoomsSubscriber onNext _) verify rooms once()
    // Verify that `subscriber.onCompleted` has been called once
    (() => getRoomsSubscriber onCompleted) verify() once()
  }

  it should "save a new message in a given room" in {
    //Given
    (roomDataStore sendMessage _) expects sendMessageRequest returns Observable.just(message)

    //When
    repository sendMessage sendMessageRequest subscribe sendMessageSubscriber

    //Then
    //Verify that 'subscriber.onNext' has been called once
    (sendMessageSubscriber onNext _) verify message once()
    // Verify that `subscriber.onCompleted` has been called once
    (() => sendMessageSubscriber onCompleted) verify() once()
  }

  it should "retrieve all the participations for the given user" in {
    val request = GetUserParticipationsRequest(user.username, token)

    val subscriber = stub[Subscriber[List[Room]]]

    //Given
    (roomDataStore getUserParticipations _) expects request returns Observable.just(rooms)

    //When
    repository getUserParticipations request subscribe subscriber

    //Then
    //Verify that 'subscriber.onNext' has been called once
    (subscriber onNext _) verify rooms once()
    // Verify that `subscriber.onCompleted` has been called once
    (() => subscriber onCompleted) verify() once()
  }

  it should "retrieve all the messages for the given room" in {
    //Given
    (roomDataStore getMessages _) expects getMessagesRequest returns Observable.just(messages)

    //When
    repository getMessages getMessagesRequest subscribe getMessagesSubscriber

    //Then
    //Verify that 'suscriber.onNext' has been callen once
    (getMessagesSubscriber onNext _) verify messages once()
    // Verify that `subscriber.onCompleted` has been called once
    (() => getMessagesSubscriber onCompleted) verify() once()
  }

}
