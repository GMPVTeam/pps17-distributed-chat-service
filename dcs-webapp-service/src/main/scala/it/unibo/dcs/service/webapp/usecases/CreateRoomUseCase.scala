package it.unibo.dcs.service.webapp.usecases

import io.vertx.scala.core.Context
import it.unibo.dcs.commons.RxHelper
import it.unibo.dcs.commons.interactor.executor.{PostExecutionThread, ThreadExecutor}
import it.unibo.dcs.commons.interactor.{ThreadExecutorExecutionContext, UseCase}
import it.unibo.dcs.service.webapp.interaction.Requests.{CheckTokenRequest, CreateRoomRequest}
import it.unibo.dcs.service.webapp.interaction.Results.RoomCreationResult
import it.unibo.dcs.service.webapp.repositories.{AuthenticationRepository, RoomRepository}
import rx.lang.scala.Observable

/** It represents the room creation functionality.
  * It calls the authentication service to check the token validity and then
  * it contacts the room service to store the new room.
  *
  * @param threadExecutor      thread executor that will perform the subscription
  * @param postExecutionThread thread that will be notified of the subscription result
  * @param authRepository      authentication repository reference
  * @param roomRepository      room repository reference
  * @usecase creation of a new room */
final class CreateRoomUseCase(private[this] val threadExecutor: ThreadExecutor,
                              private[this] val postExecutionThread: PostExecutionThread,
                              private[this] val authRepository: AuthenticationRepository,
                              private[this] val roomRepository: RoomRepository)
  extends UseCase[RoomCreationResult, CreateRoomRequest](threadExecutor, postExecutionThread) {

  override protected[this] def createObservable(request: CreateRoomRequest): Observable[RoomCreationResult] =
    for {
      _ <- authRepository.checkToken(CheckTokenRequest(request.token, request.username))
      participation <- roomRepository.createRoom(request)
    } yield RoomCreationResult(participation)
}

/** Companion object */
object CreateRoomUseCase {

  /** Factory method to create the use case
    *
    * @param authRepository authentication repository reference
    * @param roomRepository room repository reference
    * @param ctx            Vertx context
    * @return the use case object */
  def apply(authRepository: AuthenticationRepository, roomRepository: RoomRepository)
           (implicit ctx: Context): CreateRoomUseCase = {
    val threadExecutor: ThreadExecutor = ThreadExecutorExecutionContext(ctx.owner())
    val postExecutionThread: PostExecutionThread = PostExecutionThread(RxHelper.scheduler(ctx))
    new CreateRoomUseCase(threadExecutor, postExecutionThread, authRepository, roomRepository)
  }
}