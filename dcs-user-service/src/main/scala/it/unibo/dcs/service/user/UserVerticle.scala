package it.unibo.dcs.service.user

import io.vertx.core.{AbstractVerticle, Context, Vertx}
import io.vertx.scala.ext.web.Router
import io.vertx.scala.ext.web.handler.BodyHandler
import it.unibo.dcs.commons.RxHelper
import it.unibo.dcs.commons.interactor.ThreadExecutorExecutionContext
import it.unibo.dcs.commons.interactor.executor.{PostExecutionThread, ThreadExecutor}
import it.unibo.dcs.commons.service.{HttpEndpointPublisher, ServiceVerticle}
import it.unibo.dcs.service.user.interactor.{CreateUserUseCase, GetUserUseCase}
import it.unibo.dcs.service.user.repository.UserRepository
import it.unibo.dcs.service.user.request.{CreateUserRequest, GetUserRequest}
import it.unibo.dcs.service.user.subscriber.{CreateUserSubscriber, GetUserSubscriber}

final class UserVerticle(private[this] val userRepository: UserRepository, private[this] val publisher: HttpEndpointPublisher) extends ServiceVerticle {

  private var host: String = _
  private var port: Int = _

  private var getUserUseCase: GetUserUseCase = _
  private var createUserUseCase: CreateUserUseCase = _

  override def init(jVertx: Vertx, context: Context, verticle: AbstractVerticle): Unit = {
    super.init(jVertx, context, verticle)
    host = config getString "host"
    port = config getInteger "port"

    val threadExecutor: ThreadExecutor = ThreadExecutorExecutionContext(vertx)
    val postExecutionThread: PostExecutionThread = PostExecutionThread(RxHelper.scheduler(this.ctx))

    getUserUseCase = new GetUserUseCase(threadExecutor, postExecutionThread, userRepository)
    createUserUseCase = new CreateUserUseCase(threadExecutor, postExecutionThread, userRepository)
  }

  override def start(): Unit = {
    startHttpServer(host, port)
      .doOnCompleted(
        publisher.publish(name = "user-service", host = host, port = port)
          .subscribe(_ => println("Record published!"),
            cause => println(s"Could not publish record: ${cause.getMessage}")))
      .subscribe(server => println(s"Server started at http://$host:${server.actualPort}"),
        cause => println(s"Could not start server at http://$host:$port: ${cause.getMessage}"))
  }

  override protected def initializeRouter(router: Router): Unit = {

    router.route().handler(BodyHandler.create())

    router.get("/users/:username")
      .handler(routingContext => {
        val username = routingContext.request().getParam("username").get
        val subscriber: GetUserSubscriber = new GetUserSubscriber(routingContext.response())
        getUserUseCase(GetUserRequest(username), subscriber)
    })

    router.post("/users")
      .consumes("application/json")
      .produces("application/json")
      .handler(routingContext => {
        val user = routingContext.getBodyAsJson().get
        val subscriber: CreateUserSubscriber = new CreateUserSubscriber(routingContext.response())
        createUserUseCase(CreateUserRequest(user.getString("username"),
          user.getString("first_name"), user.getString("last_name")), subscriber)
    })
  }

}

