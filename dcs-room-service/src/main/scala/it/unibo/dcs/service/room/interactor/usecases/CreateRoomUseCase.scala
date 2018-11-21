package it.unibo.dcs.service.room.interactor.usecases

import it.unibo.dcs.commons.interactor.UseCase
import it.unibo.dcs.commons.interactor.executor.{PostExecutionThread, ThreadExecutor}
import it.unibo.dcs.service.room.interactor.validations.CreateRoomValidation
import it.unibo.dcs.service.room.model.Participation
import it.unibo.dcs.service.room.repository.RoomRepository
import it.unibo.dcs.service.room.request.{CreateRoomRequest, JoinRoomRequest}
import rx.lang.scala.Observable

final class CreateRoomUseCase(threadExecutor: ThreadExecutor,
                              postExecutionThread: PostExecutionThread,
                              private[this] val roomRepository: RoomRepository,
                              private[this] val createRoomValidation: CreateRoomValidation)
  extends UseCase[Participation, CreateRoomRequest](threadExecutor, postExecutionThread) {

  override protected[this] def createObservable(request: CreateRoomRequest): Observable[Participation] =
    for {
      _ <- createRoomValidation(request)
      _ <- roomRepository.createRoom(request)
      participation <- roomRepository.joinRoom(JoinRoomRequest(request.name, request.username))
    } yield participation

}

/** Companion object */
object CreateRoomUseCase {

  /** Factory method to create the use case
    *
    * @param threadExecutor      thread executor that will perform the subscription
    * @param postExecutionThread thread that will be notified of the subscription result
    * @param roomRepository      room repository reference
    * @param validation          validation reference
    * @return an instantiation of the class
    */
  def apply(threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread,
            roomRepository: RoomRepository, validation: CreateRoomValidation): CreateRoomUseCase = {
    new CreateRoomUseCase(threadExecutor, postExecutionThread, roomRepository, validation)
  }

}
