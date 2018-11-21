package it.unibo.dcs.demo

import java.net.InetAddress

import it.unibo.dcs.demo.ServiceDeploymentHelper.buildDetachedProcessCommand

object Launcher extends App {

  private val host = InetAddress.getLocalHost.getHostAddress

  private val webAppServicePort = 8080
  private val authenticationServicePort = 8081
  private val userServicePort = 8082
  private val roomServicePort = 8083

  private val authServiceCommand =
    buildDetachedProcessCommand("java -jar dcs-authentication-service-1.0.jar " + authenticationServicePort)
  private val userServiceCommand =
    buildDetachedProcessCommand("java -jar dcs-user-service-1.0.jar " + userServicePort)
  private val roomServiceCommand =
    buildDetachedProcessCommand("java -jar dcs-room-service-1.0.jar " + roomServicePort)
  private val webAppServiceCommand =
    buildDetachedProcessCommand("java -jar dcs-webapp-service-1.0.jar " + webAppServicePort)

  private val InternalServicesWaitPeriod = 40000
  private val webAppServiceWaitPeriod = 15000

  /** Internal server micro-services launch */
  println("Starting internal microservices...")
  Runtime.getRuntime.exec(authServiceCommand)
  Runtime.getRuntime.exec(userServiceCommand)
  Runtime.getRuntime.exec(roomServiceCommand)

  /** Waiting for internal micro-services launch completion. */
  println("Waiting internal microservices start...")
  Thread.sleep(InternalServicesWaitPeriod)

  /** Internal server microservices launch */
  println("Starting WebApp microservice...")
  Runtime.getRuntime.exec(webAppServiceCommand)

  /** Wait for WebApp Service launch completion. */
  println("Waiting WebApp Service launch...")
  Thread.sleep(webAppServiceWaitPeriod)

  /** Launch DCS WebApp in the browser. */
  println("Launching DCS web application in the browser...")
  ServiceDeploymentHelper.openBrowser("http://" + host + ":" + webAppServicePort)

}


