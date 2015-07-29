package library.actors

import akka.actor.{Actor, Props}
import library.Engine
import repositories.StateRepository


object RefreshActor {
  def props = Props[RefreshActor]

  case class Refresh()

}


class RefreshActor extends Actor {

  import RefreshActor._
  import StateUpdateActor._

  def receive = {
    case Refresh() =>
      StateRepository.changeState("state.majinprogress",java.time.LocalDateTime.now())
      Engine.sendRequestToApi()
      val stateUpdateActor = context.actorOf(StateUpdateActor.props, "stateupdate-actor")
      stateUpdateActor ! "success"
  }
}
