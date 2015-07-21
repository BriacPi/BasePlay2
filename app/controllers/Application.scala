package controllers

import java.util.Calendar
import javax.inject.Inject

import library.AbnormalityDetection._
import library.CodesToNames
import library.CodesToNames._
import library.Engine._
import models.{AbnormalityList, Configuration}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.Future


class Application @Inject()(ws: WSClient) extends Controller {

  // List of metrics to analyse
  val metrics = List("COL01", "COL02")

  // Dimensions **** TIME MUST BE THE FIRST DIMENSION FOR EACH****
  val dimensionsList = List(List("time:weekly", "groupe", "agence", "pdv"))

  // Configurations
  val configurations: List[Configuration] = for {
    metric <- metrics
    dimensions <- dimensionsList
  } yield Configuration(metric, dimensions)

  // List of BPCE caisses to look for
  val caisseList = List("14445", "13825", "11425", "18025", "13485", "14265", "18315")

  // List of Months
  val year = Calendar.getInstance().get(Calendar.YEAR)
  val listOfYears = (2012 to year).map(_.toString)
  val listOfMonths = (for {
    year <- listOfYears
    month <- 1 to 12
  } yield year + "-" + month + "-1").toList


  //List("2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1")

  def sendRequestToApi() = Action.async {
    // TODO ACTOR

    val mapCodesToNames: Future[Map[String, String]] = getMapCodesToNames(CodesToNames.makeRequest())
    mapCodesToNames.flatMap { mapCtN =>
      val abnormalityListList: Future[List[AbnormalityList]] = getAbnormalitiesForAllConfigurations(caisseList, configurations, listOfMonths, mapCtN)
      abnormalityListList.map(abnormalityList => Ok(abnormalityList.toString()))
    }
  }
}