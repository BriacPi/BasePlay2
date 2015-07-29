package repositories


import java.time.format.DateTimeFormatter


object StateRepository {
  var message = "etat.majdone"
  var date = java.time.LocalDateTime.now()


  def changeState(newMessage: String, newDate: java.time.LocalDateTime): Unit = {
    message = newMessage
    date = newDate
  }

  def getMessage = message

  def getDate = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))


}