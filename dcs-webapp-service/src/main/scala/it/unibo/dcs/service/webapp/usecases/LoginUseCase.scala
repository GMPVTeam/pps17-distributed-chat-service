package it.unibo.dcs.service.webapp.usecases

import it.unibo.dcs.commons.interactor.UseCase
import it.unibo.dcs.commons.interactor.executor.{PostExecutionThread, ThreadExecutor}
import it.unibo.dcs.service.webapp.model.User
import it.unibo.dcs.service.webapp.repositories.AuthenticationRepository
import it.unibo.dcs.service.webapp.repositories.Requests.LoginUserRequest
import rx.lang.scala.Observable

final class LoginUseCase(private[this] val threadExecutor: ThreadExecutor,
                         private[this] val postExecutionThread: PostExecutionThread,
                         private[this] val authRepository: AuthenticationRepository)
  extends UseCase[User, LoginUserRequest](threadExecutor, postExecutionThread) {

  override protected[this] def createObservable(loginRequest: LoginUserRequest): Observable[User] = ???
}
