package library

import java.util.Calendar
import java.util.concurrent.TimeoutException

import library.AbnormalityDetection._
import library.CaissesToNames._
import models.{Configuration, Row}
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.{WS, WSRequest, WSResponse}
import repositories.{StateRepository, MetricRepository}

import scala.concurrent.Future
import scala.util.{Success, Failure}

object Engine {
  def sendRequestToApi():Unit = {

    // List of BPCE caisses to look for
    val caisseList = List("14445", "13825", "11425", "18025", "13485", "14265", "18315")

    // List of Months
    val year = Calendar.getInstance().get(Calendar.YEAR)
    val listOfYears = (2012 to year).map(_.toString)
    val listOfMonths = (for {
      year <- listOfYears
      month <- 1 to 12
    } yield year + "-" + month + "-1").toList

    val mapMetricsToNames: Future[Map[String, String]] = library.MetricsToNames.getMapMetricsToNames(MetricsToNames.makeMetricRequest())
    // List of metrics to analyse
    val metrics = MetricRepository.listCodes()
    // Dimensions **** TIME MUST BE THE FIRST DIMENSION FOR EACH****
    val dimensionsList = List(List("time:weekly", "groupe", "agence", "pdv"))
    // Configurations
    val configurations: List[Configuration] = for {
      metric <- metrics
      dimensions <- dimensionsList
    } yield Configuration(metric, dimensions)

    val mapCaissesToNames: Future[Map[String, String]] = getMapCaissesToNames(CaissesToNames.makeRequest())

    mapCaissesToNames.flatMap { mapCtN =>
      mapMetricsToNames.map {mapMtN=>
        filterAbnormalitiesForAllConfigurations(caisseList, configurations, listOfMonths, mapCtN, mapMtN)
      }
    }


  }

  def filterAbnormalitiesForAllConfigurations(caisseList: List[String], configurationList: List[Configuration], monthList: List[String]
                                              , mapCaissesToNames: Map[String, String], mapMetricsToNames: Map[String, String]): Unit = {
    val iterator = configurationList.foldLeft(Future.successful(List.empty[String]))((acc: Future[List[String]], config: Configuration) =>
      acc.flatMap((listOfAbnormalities: List[String]) => getDataForAllCaisses(caisseList, config, monthList).map((rows: List[Row]) => {
        filterAllAbnormalities(rows, mapCaissesToNames, config.metric, mapMetricsToNames)
        Nil
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
