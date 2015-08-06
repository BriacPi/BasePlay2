package models

import play.api.libs.json.{Json, Writes}

case class DashBoard(leaderBoard: List[(String, Long)], statusChart: List[(String, Double)], natureChart: List[(String, Double)])

object DashBoard {
  implicit val dashBoardWrites = new Writes[DashBoard] {
    def writes(dashBoard: DashBoard) = Json.obj(
      "leaderboard" -> Json.toJson(dashBoard.leaderBoard),
      "statusChart" -> Json.toJson(dashBoard.statusChart),
      "natureChart" -> Json.toJson(dashBoard.natureChart)
    )
  }
}


