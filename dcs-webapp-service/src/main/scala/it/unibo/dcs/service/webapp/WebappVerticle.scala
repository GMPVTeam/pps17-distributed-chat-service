package it.unibo.dcs.service.webapp

import io.vertx.scala.ext.web.Router
import it.unibo.dcs.commons.service.ServiceVerticle

final class WebappVerticle extends ServiceVerticle {

  override protected def initializeRouter(router: Router): Unit = ???

  override def start(): Unit = {
    startHttpServer("localhost", 8080).subscribe()
  }
}
