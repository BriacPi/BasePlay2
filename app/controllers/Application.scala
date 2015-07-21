package controllers

import java.util.Calendar
import javax.inject.Inject

<<<<<<< HEAD

import library.AbnormalityDetection._
import library.{AbnormalityHandling, CodesToNames}
=======
>>>>>>> merge
import library.CodesToNames._
import library.Engine._
import library.{AbnormalityHandling, CodesToNames}
import models.{AbnormalityList, Configuration, ErrorBPCE}
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


  val error1 = ErrorBPCE("2014-12-1","1","2","3","4","RDV05","Saisi","2013-17-2","done","RAS","Nicolas","Unknown")

  //List("2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1")

  def sendRequestToApi() = Action.async {
    // TODO ACTOR

    val mapCodesToNames: Future[Map[String, String]] = getMapCodesToNames(CodesToNames.makeRequest())
    mapCodesToNames.flatMap { mapCtN =>
      val abnormalityListList: Future[List[AbnormalityList]] = getAbnormalitiesForAllConfigurations(caisseList, configurations, listOfMonths, mapCtN)
      abnormalityListList.map(abnormalityListList2 =>{
        abnormalityListList2.map(abnormalityList =>
          abnormalityList.abnormalities.map(abnormality => {
            val currentDate = java.time.LocalDate.now().toString
            val error = ErrorBPCE(abnormality.row.date.toString, abnormality.row.dimensions.head, abnormality.row.dimensions(1),
              abnormality.row.dimensions(2), abnormality.row.dimensions(3), abnormalityList.metric,
              "To be specified", currentDate, "Not treated", "", "To be specified")
            AbnormalityHandling.add(error)
          }
          )
        )
        Ok(views.html.index(AbnormalityHandling.filter("Treated"), AbnormalityHandling.filter("Processing"), AbnormalityHandling.filter("Not treated")))}
      )

    }
  }

  def add(date: String, caisse: String, groupe: String, agence: String, pdv: String, abnormalMetric: String): Action[AnyContent] = Action {
    val currentDate = java.time.LocalDate.now().toString

    val error = ErrorBPCE(date,caisse,groupe,agence,pdv,abnormalMetric,"To be specified",currentDate,"Not treated","","To be specified","Unknown")

    AbnormalityHandling.add(error)
    Ok("test")
  }

  def all(): Action[AnyContent] = Action {
    Ok(views.html.index(AbnormalityHandling.filter("Treated"), AbnormalityHandling.filter("Processing"), AbnormalityHandling.filter("Not treated")))
  }


  def findById(date : String,caisse : String, groupe : String, agence :String,pdv :String, metric :String): Action[AnyContent] = Action{
    val error = ErrorBPCE(date,caisse,groupe,agence,pdv,metric,"To be specified","","Not treated","","To be specified","Unknown")

    val existingError = AbnormalityHandling.findErrorById(error)
    existingError match {
      case Some(e) => Ok(views.html.index(e :: List(), List(), List()))
      case None => Ok("Not found")
    }

  }
  def editStatus(date : String,caisse : String, groupe : String, agence :String,pdv :String,metric :String,status : String): Action[AnyContent] = Action{
    val error = ErrorBPCE(date,caisse,groupe,agence,pdv,metric,"To be specified","","Not treated","","To be specified","Unknown")
    val existingError = AbnormalityHandling.editStatus(error,status)
   Ok(views.html.index(AbnormalityHandling.filter("Treated"),AbnormalityHandling.filter("Processing"),AbnormalityHandling.filter("Not treated")))

  }
  def editType(date : String,caisse : String, groupe : String, agence :String,pdv :String,metric :String,errorType : String): Action[AnyContent] = Action{
    val error = ErrorBPCE(date,caisse,groupe,agence,pdv,metric,"To be specified","","Not treated","","To be specified","Unknown")
    val existingError = AbnormalityHandling.editType(error,errorType)
    Ok(views.html.index(AbnormalityHandling.filter("Treated"),AbnormalityHandling.filter("Processing"),AbnormalityHandling.filter("Not treated")))

  }
  def editComment(date : String,caisse : String, groupe : String, agence :String,pdv :String,metric :String,comment: String): Action[AnyContent] = Action{
    val error = ErrorBPCE(date,caisse,groupe,agence,pdv,metric,"To be specified","","Not treated","","To be specified","Unknown")
    val existingError = AbnormalityHandling.editComment(error,comment)
    Ok(views.html.index(AbnormalityHandling.filter("Treated"),AbnormalityHandling.filter("Processing"),AbnormalityHandling.filter("Not treated")))

  }
  def editAdmin(date : String,caisse : String, groupe : String, agence :String,pdv :String,metric :String,admin: String): Action[AnyContent] = Action{
    val error = ErrorBPCE(date,caisse,groupe,agence,pdv,metric,"To be specified","","Not treated","","To be specified","Unknown")
    val existingError = AbnormalityHandling.editAdmin(error,admin)
    Ok(views.html.index(AbnormalityHandling.filter("Treated"),AbnormalityHandling.filter("Processing"),AbnormalityHandling.filter("Not treated")))

  }


}