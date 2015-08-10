package models

import play.api.libs.json.{Json, Writes}

case class DashBoard(title:String,numberOfRows:Int,leaderBoard: List[LeaderBoardLign], statusChart: Chart, natureChart: Chart)

case class Chart(labels:List[String],data:List[Int])

case class LeaderBoardLign(name:String,numberOfUnsolvedAnomalies:Long)

object LeaderBoardLign{
  implicit val leaderBoardLignWrites = new Writes[LeaderBoardLign] {
    def writes(leaderBoardLign: LeaderBoardLign) = Json.obj(
      "name"-> leaderBoardLign.name,
      "numberOfUnsolvedAnomalies"-> leaderBoardLign.numberOfUnsolvedAnomalies
    )
  }
}


object Chart {
  implicit val chartWrites = new Writes[Chart] {
    def writes(chart: Chart) = Json.obj(
      "labels"-> chart.labels,
      "data"-> chart.data
    )
  }
}
object DashBoard {
  implicit val dashBoardWrites = new Writes[DashBoard] {
    def writes(dashBoard: DashBoard) = Json.obj(
      "title"-> dashBoard.title,
      "numberOfRows"-> dashBoard.numberOfRows,
      "leaderboard" -> Json.toJson(dashBoard.leaderBoard),
      "statusChart" -> Json.toJson(dashBoard.statusChart),
      "natureChart" -> Json.toJson(dashBoard.natureChart)
    )
  }
}


