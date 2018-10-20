package it.unibo.dcs.service.user

import java.net.InetAddress

import io.vertx.lang.scala.ScalaLogger
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.{DeploymentOptions, Vertx, VertxOptions}
import io.vertx.scala.ext.jdbc.JDBCClient
import io.vertx.scala.ext.sql.SQLConnection
import io.vertx.servicediscovery.{Record, ServiceDiscovery}
import it.unibo.dcs.commons.{IoHelper, VertxHelper}
import it.unibo.dcs.commons.VertxHelper.Implicits._
import it.unibo.dcs.commons.service.HttpEndpointPublisherImpl
import it.unibo.dcs.commons.service.codecs.RecordMessageCodec
import it.unibo.dcs.service.user.data.UserDataStore
import it.unibo.dcs.service.user.data.impl.UserDataStoreDatabase
import it.unibo.dcs.service.user.repository.impl.UserRepositoryImpl

import scala.language.implicitConversions

object Launcher extends App {

  private val logger = ScalaLogger.getLogger(getClass.getName)

  VertxHelper.toObservable[Vertx](Vertx.clusteredVertx(VertxOptions(), _))
    .flatMap { vertx =>
      val config = IoHelper.readJsonObject("/db_config.json")
      VertxHelper.toObservable[SQLConnection](JDBCClient.createNonShared(vertx, config).getConnection(_))
        .map((vertx, _))
    }
    .subscribe({
      case (vertx, connection) =>
        val discovery = ServiceDiscovery.create(vertx)
        val eventBus = vertx.eventBus
        vertx.eventBus.registerDefaultCodec[Record](new RecordMessageCodec())
        val publisher = new HttpEndpointPublisherImpl(discovery, eventBus)
        val userDataStore: UserDataStore = new UserDataStoreDatabase(connection)
        val userRepository = new UserRepositoryImpl(userDataStore)
        val config = new JsonObject()
          .put("host", InetAddress.getLocalHost.getHostAddress)
          .put("port", args(0).toInt)
        vertx.deployVerticle(new UserVerticle(userRepository, publisher), DeploymentOptions().setConfig(config))
    }, cause => logger.error("", cause))

}
