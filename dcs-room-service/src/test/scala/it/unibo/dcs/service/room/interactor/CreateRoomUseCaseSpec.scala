package it.unibo.dcs.service.room.interactor

import java.util.Date

import it.unibo.dcs.commons.test.JUnitSpec
import it.unibo.dcs.service.room.Mocks._
import it.unibo.dcs.service.room.interactor.usecases.CreateRoomUseCase
import it.unibo.dcs.service.room.interactor.validations.CreateRoomValidation
import it.unibo.dcs.service.room.model.{Participation, Room}
import it.unibo.dcs.service.room.request.{CreateRoomRequest, JoinRoomRequest}
import it.unibo.dcs.service.room.validator.CreateRoomValidator
import org.scalamock.scalatest.MockFactory
import org.scalatest.OneInstancePerTest
import rx.lang.scala.{Observable, Subscriber}

import scala.language.postfixOps

final class CreateRoomUseCaseSpec extends JUnitSpec with MockFactory with OneInstancePerTest {

  private val createRoomUseCase = {
    val validation = CreateRoomValidation(threadExecutor, postExecutionThread, CreateRoomValidator())
    CreateRoomUseCase(threadExecutor, postExecutionThread, roomRepository, validation)
  }

  private val request = CreateRoomRequest("Test room", "martynha")
  private val joinRequest = JoinRoomRequest(request.name, request.username)
  private val expectedRoom = Room(request.name)
  private val expectedParticipation = Participation(expectedRoom, request.username, new Date())

  private val subscriber = stub[Subscriber[Participation]]

  it should "Create a room when the CreateRoomUseCase is execute" in {
    //Given
    (roomRepository createRoom _) expects request returns (Observable just expectedRoom)
    (roomRepository joinRoom _) expects joinRequest returns (Observable just expectedParticipation)

    //When
    createRoomUseCase(request).subscribe(subscriber)

    //Then
    (subscriber onNext _) verify expectedParticipation once()
    (() => subscriber onCompleted) verify() once()
  }

}
