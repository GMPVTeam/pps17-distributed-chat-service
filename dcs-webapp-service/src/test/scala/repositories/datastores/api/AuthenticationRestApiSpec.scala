package repositories.datastores.api

import java.util.Date

import io.vertx.scala.ext.web.client.WebClient
import it.unibo.dcs.commons.service.HttpEndpointDiscovery
import it.unibo.dcs.service.webapp.model.User
import it.unibo.dcs.service.webapp.repositories.Requests.RegisterUserRequest
import it.unibo.dcs.service.webapp.repositories.datastores.api.AuthenticationRestApi
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, OneInstancePerTest}
import rx.lang.scala.Observable

class AuthenticationRestApiSpec extends FlatSpec with MockFactory with OneInstancePerTest {
  val user = User("niklegend", "nicola", "piscaglia", "bla", visible = true, new Date())
  val registerRequest = RegisterUserRequest(user.username, "password", user.firstName,
    user.lastName, user.bio, visible = true)
  //val registeredSubscriber: Subscriber[Boolean] = (result: Boolean) => assert(result)
  val discovery: HttpEndpointDiscovery = mock[HttpEndpointDiscovery]
  val authClient: WebClient = mock[WebClient]
  val authRestApi = new AuthenticationRestApi(discovery)

  it should "register a new user" in {
    // Given
    discovery.getWebClient _ expects "AuthenticationService" returns Observable.just(authClient)

    // When
    authRestApi.registerUser(registerRequest).subscribe((result: Boolean) => assert(result))

    // Then
    // Verify that `subscriber.onNext` has been called once with `user` as argument
    //(registeredSubscriber onNext _) verify true once()
    // Verify that `subscriber.onCompleted` has been called once
    //(registeredSubscriber onCompleted: () => Unit) verify() once()
  }
}