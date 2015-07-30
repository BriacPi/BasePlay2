package library.actors

import akka.actor.{Actor, Props}
import library.Engine
import library.utils.dateTimeNow
import repositories.StateRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object RefreshActor {
  def props = Props[RefreshActor]

  case class Refresh()

}


class RefreshActor extends Actor {

  import RefreshActor._

  val stateUpdateActor = context.actorOf(StateUpdateActor.props, "stateupdate-actor")

  def receive = {
    case Refresh() =>
      if (StateRepository.state.message == "state.majinprogress" && StateRepository.state.date.plusHours(8).isAfter(dateTimeNow.now()) ) {}
      else {
        stateUpdateActor ! "maj"
        Engine.sendRequestToApi().onComplete {
          case Success(e) => stateUpdateActor ! "success"
          case Failure(e) => stateUpdateActor ! "failure"
        }
      }
  }
}
