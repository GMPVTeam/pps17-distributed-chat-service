package it.unibo.dcs.service.webapp.usecases

import it.unibo.dcs.service.webapp.interaction.Requests.{CheckTokenRequest, DeleteRoomRequest}
import it.unibo.dcs.service.webapp.usecases.commons.Mocks._
import it.unibo.dcs.service.webapp.usecases.commons.UseCaseSpec
import rx.lang.scala.{Observable, Subscriber}

import scala.language.postfixOps

class DeleteRoomUseCaseSpec extends UseCaseSpec {

  private val deleteRoomRequest = DeleteRoomRequest(room.name, user.username, token)

  private val roomDeletionSubscriber = stub[Subscriber[String]]

  private val deleteRoomUseCase =
    new DeleteRoomUseCase(threadExecutor, postExecutionThread, authRepository, roomRepository)


  it should "delete the chosen room when the use case is executed" in {
    // Given
    (roomRepository deleteRoom _) expects deleteRoomRequest returns (Observable just roomName)
    // userRepository is called with `registerRequest` as parameter returns an observable that contains only `user`
    (authRepository checkToken _) expects CheckTokenRequest(token, user.username) returns (Observable just roomName)

    // When
    // createUserUseCase is executed with argument `request`
    deleteRoomUseCase(deleteRoomRequest) subscribe roomDeletionSubscriber

    // Then
    (roomDeletionSubscriber onNext _) verify roomName once()
  }
}
