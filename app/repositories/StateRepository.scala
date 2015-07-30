package repositories


import java.time.format.DateTimeFormatter

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current
import library.utils.dateTimeNow

case class StateMessage(niceMessage:String,color:String)

case class State(message: String, date: java.time.LocalDateTime) {
  def niceDate:String = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))
}

object StateRepository extends StateRepository{
  def changeState(newState:State): Unit = {
    val oldState = state
    update(newState)
  }

  def state: State = {
    get() match {
      case None =>
        val newState = State("state.initialisation",dateTimeNow.now())
        create(newState)
        newState
      case Some(somestate)=>
        somestate
    }
  }
}



trait StateRepository {

  private[repositories] val recordMapper = {

      str("message") ~
      str("date") map {
      case  message ~ date => State(message, java.time.LocalDateTime.parse(date))
    }
  }


  def update(state: State): Unit = {
    DB.withConnection { implicit c =>
      SQL("update state set message={message}, date={date} ").on(
        'message -> state.message,
        'date -> state.date.toString
      ).executeUpdate()
    }
  }

  def get(): Option[State] = {
    DB.withConnection { implicit current =>
      SQL(
        """
          SELECT * FROM state
        """
      ).as(recordMapper.singleOpt )
    }
  }

  def create(state: State): Unit = {
    DB.withConnection { implicit c =>
      SQL("insert into state (message,date) values " +
        "({message},{date})").on(
          'message -> state.message,
          'date -> state.date.toString
        ).executeInsert()
    }
  }


}