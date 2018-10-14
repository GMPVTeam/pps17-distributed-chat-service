package it.unibo.dcs.authentication_service.interactor

import io.vertx.scala.ext.auth.jwt.JWTAuth
import it.unibo.dcs.authentication_service.repository.AuthenticationRepository
import it.unibo.dcs.commons.interactor.executor.{PostExecutionThread, ThreadExecutor}
import rx.lang.scala.Observable

final class LoginUserUseCase(private[this] val threadExecutor: ThreadExecutor,
                             private[this] val postExecutionThread: PostExecutionThread,
                             private[this] val authRepository: AuthenticationRepository,
                             private[this] val jwtAuth: JWTAuth)
  extends ReturningTokenUseCase(threadExecutor, postExecutionThread, authRepository, jwtAuth) {

  protected def getMainObservable(username: String, password: String): Observable[Unit] = {
    authRepository.checkUserCredentials(username, password)
  }

}

object LoginUserUseCase{
  def apply(threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread,
            authRepository: AuthenticationRepository, jwtAuth: JWTAuth) =
    new LoginUserUseCase(threadExecutor, postExecutionThread, authRepository, jwtAuth)
}