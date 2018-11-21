package it.unibo.dcs.service.webapp.verticles

import io.vertx.lang.scala.json.{Json, JsonObject}
import io.vertx.scala.core.eventbus.Message
import io.vertx.scala.ext.web.Router
import it.unibo.dcs.commons.Publisher
import it.unibo.dcs.commons.VertxHelper.Implicits.RichEventBus
import it.unibo.dcs.commons.service.ServiceVerticle
import it.unibo.dcs.exceptions.UsernameRequiredException
import it.unibo.dcs.service.webapp.verticles.Addresses.{internal, users}
import it.unibo.dcs.service.webapp.verticles.HeartbeatVerticle.{KEY_ONLINE, KEY_USERNAME, TIMEOUT, usernameHandler}

final class HeartbeatVerticle extends ServiceVerticle {

  override protected[this] def initializeRouter(router: Router): Unit = ()

  override def start(): Unit = {
    var activeUsers = Set[String]()

    eventBus.address(users.online).handle[JsonObject](usernameHandler { username =>
      activeUsers = activeUsers + username
    })

    eventBus.address(users.offline).handle[JsonObject](usernameHandler { username =>
      activeUsers = activeUsers filterNot { _ == username }
    })

    val isUserOnlineResponse = eventBus.address(internal.isUserOnline.response)

    eventBus.address(internal.isUserOnline.request).handle[JsonObject](usernameHandler { username =>
      val online = activeUsers(username)
      isUserOnlineResponse.publish(Json.obj((KEY_ONLINE, online)))
    })

    val heartbeatRequest: Publisher = eventBus.address(users.hearthbeat.request)

    heartbeatRequest.publish(Json.emptyObj())

    var responses = Set[String]()

    eventBus.address(users.hearthbeat.response).handle[JsonObject](usernameHandler { username =>
      responses = responses + username
    })

    val userOffline: Publisher = eventBus.address(internal.userOffline)
    vertx.setPeriodic(TIMEOUT, _ => {
      val inactiveUser = activeUsers diff responses
      responses = Set[String]()

      inactiveUser foreach { username =>
        userOffline.publish(Json.obj((KEY_USERNAME, username)))
      }

      activeUsers = activeUsers diff inactiveUser

      heartbeatRequest.publish(Json.emptyObj())
    })
  }

}

object HeartbeatVerticle {

  private val TIMEOUT = 5000L

  private val KEY_ONLINE = "online"

  private val KEY_USERNAME = "username"

  private def usernameHandler(f: String => Unit): Message[JsonObject] => Unit = {
    def getUsername(message: Message[JsonObject]): Option[String] = {
      val json = message.body

      if (json containsKey KEY_USERNAME)
        Some(json getString KEY_USERNAME)
      else
        None
    }

    getUsername(_).fold(throw UsernameRequiredException)(f)
  }

}
