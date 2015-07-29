package library.actors

import akka.actor.{Actor, Props}
import repositories.StateRepository


object StateUpdateActor {
  def props = Props[StateUpdateActor]

}


class StateUpdateActor extends Actor {

  import StateUpdateActor._

  def receive = {
    case "success" => StateRepository.changeState("state.majdone",java.time.LocalDateTime.now())
    case "maj" => StateRepository.changeState("state.majinprogress",java.time.LocalDateTime.now())
  }
}
