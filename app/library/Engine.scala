package library

import java.util.concurrent.TimeoutException

import library.AbnormalityDetection._
import models.{AbnormalityList, Configuration, Row}
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.{WS, WSRequest, WSResponse}

import scala.concurrent.Future

object Engine {

  def getAbnormalitiesForAllConfigurations(caisseList: List[String], configurationList: List[Configuration], monthList: List[String]
                                           , mapCodesToNames: Map[String, String]): Future[List[AbnormalityList]] = {
    configurationList.foldLeft(Future.successful(List.empty[AbnormalityList]))((acc: Future[List[AbnormalityList]], config: Configuration) =>
      acc.flatMap((listOfAbnormalities: List[AbnormalityList]) => getDataForAllCaisses(caisseList, config, monthList).map((rows: List[Row]) => {
        val abnormalities: AbnormalityList = getAllAbnormalities(rows, mapCodesToNames, config.metric)
        abnormalities :: listOfAbnormalities
      }
      )
      ))
  }

  def getDataForAllCaisses(caisseList: List[String], config: Configuration, monthList: List[String]): Future[List[Row]] = {
    caisseList.foldLeft(Future.successful(List.empty[Row]))((acc: Future[List[Row]], caisse: String) => acc.flatMap(
      listOfRows => getDataForAllMonths(caisse, config, monthList).map(rows => listOfRows ::: rows)))

  }

  def getDataForAllMonths(caisse: String, config: Configuration, monthList: List[String]): Future[List[Row]] = {
    monthList.foldLeft(Future.successful(List.empty[Row]))((acc: Future[List[Row]], month: String) => acc.flatMap(
      listOfRows => sendRequestToApi(caisse, config, month).map(rows => (listOfRows ::: rows).distinct)))
  }


  def sendRequestToApi(caisse: String, config: Configuration, month: String): Future[List[Row]] = DataStorage.sendRequestToApiWithStorage(caisse, config, month, 0)


  def makeRequest(caisse: String, configuration: Configuration, month: String, numberOfTry: Int): Future[WSResponse] = {
    val metric: String = configuration.metric
    val dimensions: String = configuration.dimensions.reduce((x: String, y: String) => x + "," + y)
    val url: String = createUrl(caisse, dimensions, metric, month)
    println(url)

    val request: WSRequest = WS.url(url)

    val complexRequest: WSRequest =
      request.withHeaders("Accept" -> "application/json")
        .withRequestTimeout(30000)
    complexRequest.get().recoverWith {
      case e: TimeoutException =>
        println("Timeout for caisse " + caisse + " on try number " + numberOfTry)
        makeRequest(caisse, configuration, month, numberOfTry + 1)
    }
  }

  def createUrl(caisse: String, dimensions: String, metric: String, month: String): String =
    "https://vpc-2-bpce-apiu.capback.fr/bpce/select?tokens=foo%3Bbar&from=" +
      month + "&to=" +
      nextMonth(month) + "&metrics=" +
      metric + "&filters=caisse==" +
      caisse + "&dimensions=" +
      dimensions

  def nextMonth(month: String): String = {
    val date = month.split("-").map(_.toInt)
    if (date(1) == 12) (date(0) + 1) + "-1-" + date(2)
    else date(0) + "-" + (date(1) + 1) + "-" + date(2)
  }
}
