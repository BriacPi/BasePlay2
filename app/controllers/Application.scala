package controllers

import java.util.Calendar
import javax.inject.Inject

import library.CodesToNames
import library.CodesToNames._
import library.Engine._
import models.ReasonForDetection.NotSpecified
import models.{Configuration, SuspectRow}
import play.api.data.Form
import play.api.data.format.Formats._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.mvc._
import models.EditionValues
import scala.concurrent.Future


class Application @Inject()(ws: WSClient) extends Controller {

  // List of metrics to analyse
  val metrics = List("COL01")

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

  //Userform
  val userForm: Form[String] = Form("new value" -> of[String])


  val editionForm: Form[EditionValues] = Form(
    mapping(
      "admin" -> text,
      "comment" -> text,
      "nature" -> nonEmptyText,
      "status" -> nonEmptyText
    )(EditionValues.apply)(EditionValues.unapply)
  )


  //List("2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1")

  def sendRequestToApi() = Action.async {
    // TODO ACTOR

    val mapCodesToNames: Future[Map[String, String]] = getMapCodesToNames(CodesToNames.makeRequest())
    mapCodesToNames.map { mapCtN =>
      filterAbnormalitiesForAllConfigurations(caisseList, configurations, listOfMonths, mapCtN)
      Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly)))
    }
  }

  def solved(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.solved(SuspectRow.filterByStatus(models.Status.Solved)))
  }

  def beingProcessed(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.beingProcessed(SuspectRow.filterByStatus(models.Status.BeingProcessed)))
  }

  def detectedOnly(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly)))
  }

  def redirect() = Action {
    Redirect(routes.Application.detectedOnly())
  }

  def add(date: String, caisse: String, groupe: String, agence: String, pdv: String, metric: String): Action[AnyContent] = Action { implicit request =>
    val currentDate = java.time.LocalDate.now()

    SuspectRow.create(new SuspectRow(java.time.LocalDate.parse(date), caisse, groupe, agence, pdv, metric, models.Status.DetectedOnly, models.Nature.NotSpecified, currentDate, "Nobody", " "), NotSpecified)
    Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly)))
  }

  def find(date: String, caisse: String, groupe: String, agence: String, pdv: String, metric: String): Action[AnyContent] = Action { implicit request =>
    val optionOfSuspectRow = SuspectRow.findByKey(date, caisse, groupe, agence, pdv, metric)
    optionOfSuspectRow match {
      case Some(e) => Ok(views.html.suspectRow(e))
      case None => Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly)))
    }
  }

  def findWithId(id: Long): Action[AnyContent] = Action { implicit request =>
    val optionOfSuspectRow = SuspectRow.findById(id)
    optionOfSuspectRow match {
      case Some(e) => Ok(views.html.suspectRow(e))
      case None => Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly)))
    }
  }

  def edit(id:Long): Action[AnyContent] = Action { implicit request =>
    val optionOfSuspectRow = SuspectRow.findById(id)
    optionOfSuspectRow match {
      case Some(e) =>
        val filledForm = editionForm.fill(EditionValues(e.admin,e.comment,e.nature.toString,e.status.toString))
        Ok(views.html.edit(e, filledForm))
      case None => Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly)))
    }
  }


  def saveEdition(id: Long): Action[AnyContent] =  Action { implicit request =>
    editionForm.bindFromRequest.fold(
      errors => Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly))),
      l => {
        SuspectRow.edit(id,l.admin ,l.comment,models.Nature.withName(l.nature),models.Status.withName(l.status))
        Redirect(routes.Application.findWithId(id))
      }
    )
  }





  //  def findById(date : String,caisse : String, groupe : String, agence :String,pdv :String, metric :String): Action[AnyContent] = Action{
  //    val error = ErrorBPCE(date,caisse,groupe,agence,pdv,metric,"To be specified","","Not treated","","To be specified","Unknown")
  //
  //    val existingError = AbnormalityHandling.findErrorById(error)
  //    existingError match {
  //      case Some(e) => Ok(views.html.index(e :: List(), List(), List()))
  //      case None => Ok("Not found")
  //    }
  //
  //  }
  //  def editStatus(date : String,caisse : String, groupe : String, agence :String,pdv :String,metric :String,status : String): Action[AnyContent] = Action{
  //    val error = ErrorBPCE(date,caisse,groupe,agence,pdv,metric,"To be specified","","Not treated","","To be specified","Unknown")
  //    val existingError = AbnormalityHandling.editStatus(error,status)
  //   Ok(views.html.index(AbnormalityHandling.filter("Treated"),AbnormalityHandling.filter("Processing"),AbnormalityHandling.filter("Not treated")))
  //
  //  }
  //  def editType(date : String,caisse : String, groupe : String, agence :String,pdv :String,metric :String,errorType : String): Action[AnyContent] = Action{
  //    val error = ErrorBPCE(date,caisse,groupe,agence,pdv,metric,"To be specified","","Not treated","","To be specified","Unknown")
  //    val existingError = AbnormalityHandling.editType(error,errorType)
  //    Ok(views.html.index(AbnormalityHandling.filter("Treated"),AbnormalityHandling.filter("Processing"),AbnormalityHandling.filter("Not treated")))
  //
  //  }
  //  def editComment(date : String,caisse : String, groupe : String, agence :String,pdv :String,metric :String,comment: String): Action[AnyContent] = Action{
  //    val error = ErrorBPCE(date,caisse,groupe,agence,pdv,metric,"To be specified","","Not treated","","To be specified","Unknown")
  //    val existingError = AbnormalityHandling.editComment(error,comment)
  //    Ok(views.html.index(AbnormalityHandling.filter("Treated"),AbnormalityHandling.filter("Processing"),AbnormalityHandling.filter("Not treated")))
  //
  //  }
  //  def editAdmin(date : String,caisse : String, groupe : String, agence :String,pdv :String,metric :String,admin: String): Action[AnyContent] = Action{
  //    val error = ErrorBPCE(date,caisse,groupe,agence,pdv,metric,"To be specified","","Not treated","","To be specified","Unknown")
  //    val existingError = AbnormalityHandling.editAdmin(error,admin)
  //    Ok(views.html.index(AbnormalityHandling.filter("Treated"),AbnormalityHandling.filter("Processing"),AbnormalityHandling.filter("Not treated")))
  //
  //  }


}