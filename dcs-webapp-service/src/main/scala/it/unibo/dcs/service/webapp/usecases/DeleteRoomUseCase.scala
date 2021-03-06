package it.unibo.dcs.service.webapp.usecases

import io.vertx.scala.core.Context
import it.unibo.dcs.commons.RxHelper
import it.unibo.dcs.commons.interactor.executor.{PostExecutionThread, ThreadExecutor}
import it.unibo.dcs.commons.interactor.{ThreadExecutorExecutionContext, UseCase}
import it.unibo.dcs.service.webapp.interaction.Requests.{CheckTokenRequest, DeleteRoomRequest}
import it.unibo.dcs.service.webapp.repositories.{AuthenticationRepository, RoomRepository}
import rx.lang.scala.Observable

/** It represents the room deletion functionality.
  * It calls the authentication service to check the token validity and then
  * it contacts the room service to delete the room.
  *
  * @param threadExecutor      thread executor that will perform the subscription
  * @param postExecutionThread thread that will be notified of the subscription result
  * @param authRepository      authentication repository reference
  * @param roomRepository      room repository reference
  * @usecase deletion of the room */
final class DeleteRoomUseCase(private[this] val threadExecutor: ThreadExecutor,
                              private[this] val postExecutionThread: PostExecutionThread,
                              private[this] val authRepository: AuthenticationRepository,
                              private[this] val roomRepository: RoomRepository)
  extends UseCase[String, DeleteRoomRequest](threadExecutor, postExecutionThread) {

  override protected[this] def createObservable(request: DeleteRoomRequest): Observable[String] = {
    for {
      _ <- authRepository.checkToken(CheckTokenRequest(request.token, request.username))
      name <- roomRepository.deleteRoom(request)
    } yield name
  }

}

object DeleteRoomUseCase {

  /** Factory method to create the use case
    *
    * @param authRepository authentication repository reference
    * @param roomRepository room repository reference
    * @param ctx            Vertx context
    * @return the use case object */
  def create(authRepository: AuthenticationRepository, roomRepository: RoomRepository)
            (implicit ctx: Context): DeleteRoomUseCase = {
    val threadExecutor: ThreadExecutor = ThreadExecutorExecutionContext(ctx.owner())
    val postExecutionThread: PostExecutionThread = PostExecutionThread(RxHelper.scheduler(ctx))
    new DeleteRoomUseCase(threadExecutor, postExecutionThread, authRepository, roomRepository)
  }
}