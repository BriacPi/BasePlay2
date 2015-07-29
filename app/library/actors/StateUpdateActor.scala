package library.actors

import akka.actor.{Actor, Props}
import library.utils.dateTimeNow
import repositories.{State, StateRepository}


object StateUpdateActor {
  def props = Props[StateUpdateActor]

}


class StateUpdateActor extends Actor {

  def receive = {
    case "success" => StateRepository.changeState(State("state.majdone", dateTimeNow.now()))
    case "maj" => StateRepository.changeState(State("state.majinprogress", dateTimeNow.now()))
    case "failure" => StateRepository.changeState(State("state.majfailed", dateTimeNow.now()))
  }
}
