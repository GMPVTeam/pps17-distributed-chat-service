package it.unibo.dcs.service.webapp

import java.net.InetAddress

import io.vertx.lang.scala.json.Json
import io.vertx.scala.core.{DeploymentOptions, Vertx, VertxOptions}
import io.vertx.servicediscovery.Record
import it.unibo.dcs.commons.VertxHelper
import it.unibo.dcs.commons.VertxHelper.Implicits._
import it.unibo.dcs.commons.logging.Logging
import it.unibo.dcs.commons.service.codecs.RecordMessageCodec
import it.unibo.dcs.service.webapp.verticles.{HeartbeatVerticle, WebAppVerticle}

import scala.language.implicitConversions

/** Entry point of the application.
  * It launches the verticle associated with WebAppService in clustered mode. */
object Launcher extends App with Logging {

  private val address = InetAddress.getLocalHost.getHostAddress
  private val port = args(0).toInt
  private val config = Json.obj(("host", address), ("port", port))

  private val startErrorMessage = "Error in WebAppVerticle clustered start"

  VertxHelper.toObservable[Vertx](Vertx.clusteredVertx(VertxOptions(), _))
    .subscribe(vertx => {
      vertx.eventBus.registerDefaultCodec[Record](new RecordMessageCodec())
      vertx deployVerticle(new WebAppVerticle, DeploymentOptions()
        .setConfig(config))
      vertx.deployVerticle(new HeartbeatVerticle(), DeploymentOptions().setWorker(true))
    }, cause => log.error(startErrorMessage, cause))

}
