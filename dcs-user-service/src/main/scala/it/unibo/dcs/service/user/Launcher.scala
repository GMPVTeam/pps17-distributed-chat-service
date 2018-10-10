package it.unibo.dcs.service.user

import io.vertx.core.eventbus.{EventBus => JEventBus}
import io.vertx.core.json.JsonObject
import io.vertx.lang.scala.ScalaLogger
import io.vertx.scala.core.{DeploymentOptions, Vertx, VertxOptions}
import io.vertx.scala.ext.jdbc.JDBCClient
import io.vertx.scala.ext.sql.SQLConnection
import io.vertx.servicediscovery.{Record, ServiceDiscovery}
import it.unibo.dcs.commons.VertxHelper
import it.unibo.dcs.commons.VertxHelper.Implicits._
import it.unibo.dcs.commons.service.HttpEndpointPublisherImpl
import it.unibo.dcs.commons.service.codecs.RecordMessageCodec
import it.unibo.dcs.service.user.repository.impl.{UserDataStoreDatabase, UserRepositoryImpl}

object Launcher extends App {

  private val logger = ScalaLogger.getLogger(getClass.getName)

  VertxHelper.toObservable[Vertx](Vertx.clusteredVertx(VertxOptions(), _))
    .flatMap { vertx =>
      val config = VertxHelper.readJsonObject("/db_config.json")
      VertxHelper.toObservable[SQLConnection](JDBCClient.createNonShared(vertx, config).getConnection(_))
        .map((vertx, _))
    }
    .subscribe({
      case (vertx, connection) =>
        val discovery = ServiceDiscovery.create(vertx)
        val eventBus = vertx.eventBus
        eventBus.asJava.asInstanceOf[JEventBus].registerDefaultCodec(classOf[Record], new RecordMessageCodec())
        val publisher = new HttpEndpointPublisherImpl(discovery, eventBus)
        val userDataStore = new UserDataStoreDatabase(connection)
        val userRepository = new UserRepositoryImpl(userDataStore)
        vertx.deployVerticle(new UserVerticle(userRepository, publisher), DeploymentOptions().setConfig(new JsonObject().put("host", "localhost").put("port", 8080)))
    }, cause => logger.error("", cause))

}
