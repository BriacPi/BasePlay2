package models

import play.api.libs.functional.syntax._
import play.api.libs.json._


case class RawData(headers: List[Header], rows: List[List[JsValue]]) {
  def toRows(caisse: String): List[Row] = {
    rows.map { row =>
      val metric: List[Double] = row.collect {
        case JsNumber(s) => s.toDouble
      }
      val dimensions: List[String] = row.collect {
        case JsString(s) => s
      }
      new Row(transformDateFromStringToDateObject(dimensions.head), caisse :: dimensions.tail, metric.head)
    }

  }

  def transformDateFromStringToDateObject(dateAsString: String): java.time.LocalDate = {
    val idOfTime = headers.head.id

    if (idOfTime.contains("yearly")) java.time.LocalDate.of(dateAsString.toInt, 1, 1)
    else if (idOfTime.contains("monthly")) {
      val YearAndMonth = dateAsString.split("-")
      java.time.LocalDate.of(YearAndMonth.apply(0).toInt, YearAndMonth.apply(1).toInt, 1)
    }
    else {
      val YearAndMonthAndDay = dateAsString.split("-")
      java.time.LocalDate.of(YearAndMonthAndDay.apply(0).toInt, YearAndMonthAndDay.apply(1).toInt, YearAndMonthAndDay.apply(2).toInt)
    }
  }
}


object RawData {

  implicit val headerReader: Reads[Header] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "type").read[String]
    )(Header.apply _)

  implicit val rawDataReader: Reads[RawData] = (
    (JsPath \ "headers").read[List[Header]] and
      (JsPath \ "rows").read[List[List[JsValue]]]
    )(RawData.apply _)


}


case class Header(id: String, typeOfData: String)

case class Headers(headers: List[Header])