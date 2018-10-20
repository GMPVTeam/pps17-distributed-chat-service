package it.unibo.dcs.service.webapp.repositories.impl

import it.unibo.dcs.service.webapp.interaction.Requests.RegisterUserRequest
import it.unibo.dcs.service.webapp.model.User
import it.unibo.dcs.service.webapp.repositories.UserRepository
import it.unibo.dcs.service.webapp.repositories.datastores.UserDataStore
import rx.lang.scala.Observable

class UserRepositoryImpl(userDataStore: UserDataStore) extends UserRepository {

  override def getUserByUsername(username: String): Observable[User] = userDataStore.getUserByUsername(username)

  override def registerUser(request: RegisterUserRequest, token: String): Observable[User] = userDataStore.createUser(request, token)

}
