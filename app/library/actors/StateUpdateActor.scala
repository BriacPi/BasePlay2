package library.actors

import akka.actor.{Actor, Props}
import repositories.{State, StateRepository}


object StateUpdateActor {
  def props = Props[StateUpdateActor]

}


class StateUpdateActor extends Actor {

  def receive = {
    case "success" => StateRepository.changeState(State("state.majdone", java.time.LocalDateTime.now()))
    case "maj" => StateRepository.changeState(State("state.majinprogress", java.time.LocalDateTime.now()))
    case "failure" => StateRepository.changeState(State("state.majfailed", java.time.LocalDateTime.now()))
  }
}
