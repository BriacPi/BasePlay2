package controllers

import javax.inject.Inject

import akka.actor._
import components.mvc.AuthController
import library.Engine._
import library.MetricsToNames
import library.actors.{StateUpdateActor, RefreshActor}
import library.actors.RefreshActor.Refresh
import models.authentication.User
import models.{EditionValues, SuspectRow}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.i18n.{MessagesApi, Messages, I18nSupport}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Writes, Json}
import play.api.libs.ws.WSClient
import play.api.mvc._
import repositories._
import scala.concurrent.Future
import scala.util.{Success, Failure}

class Application @Inject()(ws: WSClient)(system: ActorSystem)(val messagesApi: MessagesApi) extends AuthController with I18nSupport {
  //ACTOR
  val refreshActor = system.actorOf(RefreshActor.props, "refresh-actor")

  import scala.concurrent.duration._

  val cancellable = system.scheduler.schedule(
    0.microseconds, 4.hours, refreshActor, Refresh())

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


  def data() = AuthenticatedAction() {
    refreshActor ! Refresh()
    Redirect(routes.Application.detectedOnly())
  }


  def solved(): Action[AnyContent] = AuthenticatedAction() { implicit request =>

    Ok(views.html.solved(SuspectRow.filterByStatus(models.Status.Solved), request.user))

  }

  def beingProcessed(): Action[AnyContent] = AuthenticatedAction() { implicit request =>
    Ok(views.html.beingProcessed(SuspectRow.filterByStatus(models.Status.BeingProcessed), request.user))
  }

  def detectedOnly(): Action[AnyContent] = AuthenticatedAction() { implicit request =>
    Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly), request.user))
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
      case Some(e) => Ok(views.html.suspectRow(e, request.user))
      case None => Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly), request.user))
    }
  }

  def findWithId(id: Long): Action[AnyContent] = AuthenticatedAction() { implicit request =>
    val optionOfSuspectRow = SuspectRow.findById(id)
    optionOfSuspectRow match {
      case Some(e) => Ok(views.html.suspectRow(e, request.user))
      case None => Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly), request.user))
    }
  }

  def edit(id: Long): Action[AnyContent] = AuthenticatedAction() { implicit request =>
    val optionOfSuspectRow = SuspectRow.findById(id)
    optionOfSuspectRow match {
      case Some(e) =>
        val filledForm = editionForm.fill(EditionValues(e.admin, e.comment, e.nature.toString, e.status.toString))
        Ok(views.html.edit(e, filledForm, request.user))
      case None => Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly), request.user))
    }
  }


  def saveEdition(id: Long): Action[AnyContent] = AuthenticatedAction() { implicit request =>
    editionForm.bindFromRequest.fold(
      errors => Ok(views.html.detectedOnly(SuspectRow.filterByStatus(models.Status.DetectedOnly), request.user)),
      l => {
        SuspectRow.edit(id, l.admin, l.comment, models.Nature.withName(l.nature), models.Status.withName(l.status))
        Redirect(routes.Application.findWithId(id))
      }
    )
  }

  def allUsedMetrics(): Action[AnyContent] = AuthenticatedAction().async { implicit request =>
    mapMetricsToNames.map { mapMtN =>
      val allMetrics = mapMtN.toList.map(tuple => CodeMetricWithoutId(tuple._1, tuple._2)).toSet
      val usedMetricsWithID: List[CodeMetric] = MetricRepository.list()
      val usedMetrics = usedMetricsWithID.map(new CodeMetricWithoutId(_)).toSet
      val unusedMetrics = allMetrics.diff(usedMetrics)
      Ok(views.html.metrics(usedMetrics.toList, unusedMetrics.toList, request.user))

    }
  }

  def addMetric(code: String): Action[AnyContent] = AuthenticatedAction().async {
    mapMetricsToNames.map { mapMtN =>
      MetricRepository.create(CodeMetricWithoutId(code, mapMtN(code)))
      Redirect(routes.Application.allUsedMetrics())
    }
  }

  def removeMetric(code: String): Action[AnyContent] = AuthenticatedAction().async {
    mapMetricsToNames.map { mapMtN =>
      MetricRepository.delete(code)
      Redirect(routes.Application.allUsedMetrics())
    }
  }

  def currentUserTasks = AuthenticatedAction() { implicit request =>
    val userTasks = SuspectRow.findByAdmin(request.user.email)
    Ok(views.html.myaccount.mytasks(userTasks, request.user))

  }


  implicit val stateMessageWrites = new Writes[StateMessage] {
    def writes(state: StateMessage) = Json.obj(
      "niceMessage" -> state.niceMessage,
      "color" -> state.color
    )
  }

  def currentState = AuthenticatedAction() { implicit request =>
    val state = StateRepository.state
    val stateMessage = if (state.message=="state.majfailed") {
      StateMessage(Messages(state.message) + " " + state.niceDate + ".","red")
    }
    else {StateMessage(Messages(state.message) + " " + state.niceDate + ".","")}
    Ok(Json.toJson(stateMessage))
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