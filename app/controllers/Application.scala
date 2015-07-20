package controllers

import java.util.Calendar
import javax.inject.Inject

import library.Engine._
import models.{Configuration, Row}
import org.apache.spark.SparkConf
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.mvc._
import library.AbnormalityDetection._
import scala.concurrent.Future


class Application @Inject()(ws: WSClient) extends Controller {

  // List of BPCE caisses to look for
  val caisseList = List("14445", "13825", "11425", "18025", "13485", "14265", "18315")

  // Configuration **** TIME MUST BE THE FIRST DIMENSION****
  val configuration = Configuration("COL01", List("time:weekly", "groupe", "agence", "pdv"))
  val conf = new SparkConf().setAppName("doctor-strange").setMaster("local")

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
    //    val optionOfRows: Option[List[Row]] = Cache.getAs[List[Row]]("rows")
    //    optionOfRows match {
    //      case Some(rowss: List[Row]) => {
    //        println("test if ")
    //        val rows: Future[List[Row]] = Future.successful(rowss)
    //        rows.map { (listOfRows: List[Row]) =>
    //          Ok(runRandomForestRegression(scaleFeatures(rowsToLabeledPoint(listOfRows, sc,configuration)), sc))
    //        }
    //
    //      }
    //      case None => {
    //        println("test else")
    //        val rows: Future[List[Row]] = getDataForAllMetrics(caisseList, configuration)
    //        rows.map((lisOftRows: List[Row]) => Cache.set("rows", lisOftRows))
    //        rows.map {
    //          (listOfRows: List[Row]) =>
    //            Ok(runRandomForestRegression(scaleFeatures(rowsToLabeledPoint(listOfRows, sc,configuration)), sc))
    //        }
    //
    //
    //      }
    //
    //    }
    val rows: Future[List[Row]] = getDataForAllCaisses(caisseList, configuration, listOfMonths)
    rows.map {
      (listOfRows: List[Row]) => {
        Ok(getAllAbnormalities(listOfRows).toString())
      }
    }
  }
}