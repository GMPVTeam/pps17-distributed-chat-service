package it.unibo.dcs.service.webapp.repositories.datastores


import it.unibo.dcs.service.webapp.model.User
import it.unibo.dcs.service.webapp.repositories.Requests.RegisterUserRequest
import it.unibo.dcs.service.webapp.repositories.datastores.api.UserApi
import it.unibo.dcs.service.webapp.repositories.datastores.impl.UserDataStoreNetwork
import rx.lang.scala.Observable

trait UserDataStore {

  def getUserByUsername(username: String): Observable[User]

  def createUser(request: RegisterUserRequest): Observable[User]

}

object UserDataStore {
  def userDataStoreNetwork(userApi: UserApi): UserDataStore = new UserDataStoreNetwork(userApi)
}
