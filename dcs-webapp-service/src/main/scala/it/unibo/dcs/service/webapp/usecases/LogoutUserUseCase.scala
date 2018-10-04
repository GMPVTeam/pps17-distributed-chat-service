package it.unibo.dcs.service.webapp.usecases

import it.unibo.dcs.commons.interactor.UseCase
import it.unibo.dcs.commons.interactor.executor.{PostExecutionThread, ThreadExecutor}
import Results.LogoutResult
import it.unibo.dcs.service.webapp.repositories.AuthenticationRepository
import rx.lang.scala.Observable

final class LogoutUserUseCase(private[this] val threadExecutor: ThreadExecutor,
                              private[this] val postExecutionThread: PostExecutionThread,
                              private[this] val authRepository: AuthenticationRepository)
  extends UseCase[LogoutResult, String](threadExecutor, postExecutionThread) {

  override protected[this] def createObservable(username: String): Observable[LogoutResult] = ???
}
