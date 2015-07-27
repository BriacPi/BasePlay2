package controllers

import java.util.Calendar
import javax.inject.Inject

import components.mvc.AuthController
import library.CaissesToNames._
import library.Engine._
import library.{CaissesToNames, MetricsToNames}
import models.{Configuration, EditionValues, SuspectRow}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.ws.WSClient
import play.api.mvc._
import repositories.{CodeMetricWithoutId, CodeMetric, MetricRepository}

import scala.concurrent.Future


class Application @Inject()(ws: WSClient) extends AuthController {



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


  val mapMetricsToNames: Future[Map[String, String]] = library.MetricsToNames.getMapMetricsToNames(MetricsToNames.makeMetricRequest())


  //List("2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1","2010-1-1")

  def sendRequestToApi() = Action.async {
    // TODO ACTOR

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
      mapMetricsToNames.map { mapMtN =>
        filterAbnormalitiesForAllConfigurations(caisseList, configurations, listOfMonths, mapCtN, mapMtN)
        Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly)))
      }
    }
  }

  def solved(): Action[AnyContent] = AuthenticatedAction() { implicit request =>
    Ok(views.html.solved(SuspectRow.filterByStatus(models.Status.Solved)))
  }

  def beingProcessed(): Action[AnyContent] = AuthenticatedAction() { implicit request =>
    Ok(views.html.beingProcessed(SuspectRow.filterByStatus(models.Status.BeingProcessed)))
  }

  def detectedOnly(): Action[AnyContent] = AuthenticatedAction() { implicit request =>
    Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly)))
  }

  def redirect() = AuthenticatedAction() {
    Redirect(routes.Application.detectedOnly())
  }

  //  def add(date: String, caisse: String, groupe: String, agence: String, pdv: String, metric: String): Action[AnyContent] = Action { implicit request =>
  //    val currentDate = java.time.LocalDate.now()
  //
  //    SuspectRow.create(new SuspectRow(java.time.LocalDate.parse(date), caisse, groupe, agence, pdv, metric,0.0, models.Status.DetectedOnly, models.Nature.NotSpecified, currentDate, "Nobody", " "), NotSpecified)
  //    Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly)))
  //  }

  def find(date: String, caisse: String, groupe: String, agence: String, pdv: String, metric: String): Action[AnyContent] = AuthenticatedAction() { implicit request =>
    val optionOfSuspectRow = SuspectRow.findByKey(date, caisse, groupe, agence, pdv, metric)
    optionOfSuspectRow match {
      case Some(e) => Ok(views.html.suspectRow(e))
      case None => Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly)))
    }
  }

  def findWithId(id: Long): Action[AnyContent] = AuthenticatedAction() { implicit request =>
    val optionOfSuspectRow = SuspectRow.findById(id)
    optionOfSuspectRow match {
      case Some(e) => Ok(views.html.suspectRow(e))
      case None => Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly)))
    }
  }

  def edit(id: Long): Action[AnyContent] = AuthenticatedAction() { implicit request =>
    val optionOfSuspectRow = SuspectRow.findById(id)
    optionOfSuspectRow match {
      case Some(e) =>
        val filledForm = editionForm.fill(EditionValues(e.admin, e.comment, e.nature.toString, e.status.toString))
        Ok(views.html.edit(e, filledForm))
      case None => Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly)))
    }
  }


  def saveEdition(id: Long): Action[AnyContent] = AuthenticatedAction() { implicit request =>
    editionForm.bindFromRequest.fold(
      errors => Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly))),
      l => {
        SuspectRow.edit(id, l.admin, l.comment, models.Nature.withName(l.nature), models.Status.withName(l.status))
        Redirect(routes.Application.findWithId(id))
      }
    )
  }

  def allUsedMetrics(): Action[AnyContent] = AuthenticatedAction().async { implicit request =>
    mapMetricsToNames.map { mapMtN =>
      val allMetrics = mapMtN.toList.map(tuple => CodeMetricWithoutId( tuple._1, tuple._2)).toSet
      val usedMetricsWithID:List[CodeMetric] = MetricRepository.list()
      val usedMetrics =usedMetricsWithID.map(new  CodeMetricWithoutId(_)).toSet
      val unusedMetrics = allMetrics.diff(usedMetrics)
      Ok(views.html.metrics(usedMetrics.toList.sortBy(_.code), unusedMetrics.toList.sortBy(_.code)))

    }
  }

  def addMetric(code:String): Action[AnyContent] = AuthenticatedAction().async {
    mapMetricsToNames.map { mapMtN =>
      MetricRepository.create(CodeMetricWithoutId(code,mapMtN(code)))
      Redirect(routes.Application.allUsedMetrics())
    }
  }

  def removeMetric(code:String): Action[AnyContent] = AuthenticatedAction().async {
    mapMetricsToNames.map { mapMtN =>
      MetricRepository.delete(code)
      Redirect(routes.Application.allUsedMetrics())
    }
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