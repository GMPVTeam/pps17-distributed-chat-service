package it.unibo.dcs.service.webapp.usecases

import io.vertx.scala.core.Context
import it.unibo.dcs.commons.RxHelper
import it.unibo.dcs.commons.interactor.executor.{PostExecutionThread, ThreadExecutor}
import it.unibo.dcs.commons.interactor.{ThreadExecutorExecutionContext, UseCase}
import it.unibo.dcs.service.webapp.repositories.Requests.RegisterUserRequest
import it.unibo.dcs.service.webapp.repositories.{AuthenticationRepository, UserRepository}
import it.unibo.dcs.service.webapp.usecases.Results.RegisterResult
import rx.lang.scala.Observable

final class RegisterUserUseCase(private[this] val threadExecutor: ThreadExecutor,
                                private[this] val postExecutionThread: PostExecutionThread,
                                private[this] val authRepository: AuthenticationRepository,
                                private[this] val userRepository: UserRepository)
  extends UseCase[RegisterResult, RegisterUserRequest](threadExecutor, postExecutionThread) {

  override protected[this] def createObservable(registerRequest: RegisterUserRequest): Observable[RegisterResult] = {
    /* Monadic style */
    for {
      token <- authRepository.registerUser(registerRequest)
      user <- userRepository.registerUser(registerRequest)
    } yield RegisterResult(user, token)
  }
}

object RegisterUserUseCase {
  def create(authRepository: AuthenticationRepository,
             userRepository: UserRepository)(implicit ctx: Context): RegisterUserUseCase = {
    val threadExecutor: ThreadExecutor = ThreadExecutorExecutionContext(ctx.owner())
    val postExecutionThread: PostExecutionThread = PostExecutionThread(RxHelper.scheduler(ctx))
    new RegisterUserUseCase(threadExecutor, postExecutionThread, authRepository, userRepository)
  }
}
