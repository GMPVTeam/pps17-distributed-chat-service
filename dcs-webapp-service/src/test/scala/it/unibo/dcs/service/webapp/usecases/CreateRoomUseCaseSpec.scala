package it.unibo.dcs.service.webapp.usecases

import it.unibo.dcs.service.webapp.interaction.Requests.CreateRoomRequest
import it.unibo.dcs.service.webapp.interaction.Results.RoomCreationResult
import it.unibo.dcs.service.webapp.usecases.commons.Mocks._
import it.unibo.dcs.service.webapp.usecases.commons.UseCaseSpec
import rx.lang.scala.{Observable, Subscriber}

import scala.language.postfixOps

class CreateRoomUseCaseSpec extends UseCaseSpec {

  private val createRoomRequest = CreateRoomRequest("Room 1", user.username, token)

  private val roomCreationResult = RoomCreationResult(participation)

  private val roomCreationSubscriber: Subscriber[RoomCreationResult] = stub[Subscriber[RoomCreationResult]]

  private val createRoomUseCase = new CreateRoomUseCase(threadExecutor, postExecutionThread, authRepository, roomRepository)


  it should "create a new room when the use case is executed" in {
    // Given
    (roomRepository createRoom _) expects createRoomRequest returns (Observable just participation)
    // userRepository is called with `registerRequest` as parameter returns an observable that contains only `user`
    (authRepository checkToken _) expects checkTokenRequest returns (Observable just token)

    // When
    // createUserUseCase is executed with argument `request`
    createRoomUseCase(createRoomRequest) subscribe roomCreationSubscriber

    // Then
    (roomCreationSubscriber onNext _) verify roomCreationResult once()
    (() => roomCreationSubscriber onCompleted) verify() once()
  }
}
