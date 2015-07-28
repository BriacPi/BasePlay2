package library.actors

import akka.actor.{Actor, Props}
import library.Engine

object RefreshActor {
  def props = Props[RefreshActor]

  case class Refresh()

}


class RefreshActor extends Actor {

  import RefreshActor._

  def receive = {
    case Refresh() =>
      Engine.sendRequestToApi()
  }
}
