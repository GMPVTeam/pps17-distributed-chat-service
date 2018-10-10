package it.unibo.dcs.service.webapp.repositories.datastores.api

import io.vertx.core.Vertx
import io.vertx.scala.core.eventbus.EventBus
import io.vertx.servicediscovery.ServiceDiscovery
import it.unibo.dcs.commons.service.HttpEndpointDiscoveryImpl
import it.unibo.dcs.service.webapp.model.User
import it.unibo.dcs.service.webapp.repositories.Requests.RegisterUserRequest
import it.unibo.dcs.service.webapp.repositories.datastores.api.impl.UserRestApi
import rx.lang.scala.Observable

trait UserApi {
  def createUser(request: RegisterUserRequest): Observable[User]

  def getUserByUsername(username: String): Observable[User]
}

/* Companion object */
object UserApi {
  /* Factory method */
  def userRestApi(vertx: Vertx, eventBus: EventBus): UserRestApi = new UserRestApi(
    new HttpEndpointDiscoveryImpl(ServiceDiscovery.create(vertx), eventBus))
}
