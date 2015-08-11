package controllers

import javax.inject.Inject

import akka.actor._
import components.mvc.AuthController
import library.Engine._
import library.{DashBoardGenerator, MetricsToNames}
import library.actors.{StateUpdateActor, RefreshActor}
import library.actors.RefreshActor.Refresh
import models.authentication.User


import models._

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


  val mapMetricsToNames: Future[Metrics] = library.MetricsToNames.getMapMetricsToNames(MetricsToNames.makeMetricRequest())


  def data() = AuthenticatedAction() {
    refreshActor ! Refresh()
    Redirect(routes.Application.allData())
  }




  def allData(): Action[AnyContent] = AuthenticatedAction() { implicit request =>

    Ok(views.html.data( request.user))


  }


  def redirect() = AuthenticatedAction() {
    Redirect(routes.Application.allData())
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
      case None => Ok(views.html.data( request.user))
    }
  }

  def findWithId(id: Long): Action[AnyContent] = AuthenticatedAction() { implicit request =>
    val optionOfSuspectRow = SuspectRow.findById(id)
    optionOfSuspectRow match {
      case Some(e) =>
        val filledForm = editionForm.fill(EditionValues(e.admin, e.comment, e.nature.toString, e.status.toString))
        Ok(views.html.edit(e, filledForm, request.user))
      case None => Ok(views.html.data( request.user))
    }
  }

  def edit(id: Long): Action[AnyContent] = AuthenticatedAction() { implicit request =>
    val optionOfSuspectRow = SuspectRow.findById(id)
    optionOfSuspectRow match {
      case Some(e) =>
        val filledForm = editionForm.fill(EditionValues(e.admin, e.comment, e.nature.toString, e.status.toString))
        Ok(views.html.edit(e, filledForm, request.user))
      case None => Ok(views.html.data( request.user))
    }
  }


  def saveEdition(id: Long): Action[AnyContent] = AuthenticatedAction() { implicit request =>
    editionForm.bindFromRequest.fold(
      errors => Ok(views.html.data( request.user)),
      l => {
        SuspectRow.edit(id, l.admin, l.comment, models.Nature.withName(l.nature), models.Status.withName(l.status))
        Redirect(routes.Application.findWithId(id))
      }
    )
  }

  def allUsedMetrics(): Action[AnyContent] = AuthenticatedAction().async { implicit request =>
    mapMetricsToNames.map { mapMtN =>
      val allMetrics = mapMtN.metrics.toList.map(tuple => CodeMetricWithoutId(tuple._1, tuple._2.label,tuple._2.formatString)).toSet
      val usedMetricsWithID: List[CodeMetric] = MetricRepository.list()
      val usedMetrics = usedMetricsWithID.map(new CodeMetricWithoutId(_)).toSet
      val unusedMetrics = allMetrics.diff(usedMetrics)
      Ok(views.html.metrics( request.user))

    }
  }

  def addMetric(code: String): Action[AnyContent] = AuthenticatedAction().async {
    mapMetricsToNames.map { mapMtN =>
      MetricRepository.create(CodeMetricWithoutId(code, mapMtN.metrics(code).label,mapMtN.metrics(code).formatString))
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
    Ok(views.html.myaccount.mytasks( request.user))

  }

  def sendUsedMetrics: Action[AnyContent] = AuthenticatedAction().async { implicit request =>
    mapMetricsToNames.map { mapMtN =>
      val allMetrics = mapMtN.metrics.toList.map(tuple => CodeMetricWithoutId(tuple._1, tuple._2.label,tuple._2.formatString)).toSet
      val usedMetricsWithID: List[CodeMetric] = MetricRepository.list()
      val usedMetrics = usedMetricsWithID.map(new CodeMetricWithoutId(_))
      val usedMetricsForJSON = usedMetrics.map(metric =>
        List(metric.code, metric.metricName)
      )
      Ok(Json.toJson(MetricsForJSON(usedMetricsForJSON)))
    }
  }

  def sendUnusedMetrics: Action[AnyContent] = AuthenticatedAction().async { implicit request =>
    mapMetricsToNames.map { mapMtN =>
      val allMetrics = mapMtN.metrics.toList.map(tuple => CodeMetricWithoutId(tuple._1, tuple._2.label,tuple._2.formatString)).toSet
      val usedMetricsWithID: List[CodeMetric] = MetricRepository.list()
      val usedMetrics = usedMetricsWithID.map(new CodeMetricWithoutId(_)).toSet
      val unusedMetrics = allMetrics.diff(usedMetrics).toList
      val unusedMetricsForJSON = unusedMetrics.map(metric =>
        List(metric.code, metric.metricName)
      )
      Ok(Json.toJson(MetricsForJSON(unusedMetricsForJSON)))
    }
  }


  def sendData(parameter: String): Action[AnyContent] = AuthenticatedAction() { implicit request =>
    val suspectRows = parameter match {
      case "all" => SuspectRow.filterByMetrics()
      case "mytasks" => SuspectRow.findByAdmin(request.user.email)
      case _ => List.empty[SuspectRow]
    }
    Ok(Json.toJson(suspectRows))
  }
  def sendTiles(caisse:String,groupe:String,agence:String,pdv:String): Action[AnyContent] = AuthenticatedAction() { implicit request =>
    Ok(Json.toJson(SuspectRow.filterByPdv(caisse,groupe,agence,pdv)))
  }

  implicit val metricsWrites = new Writes[MetricsForJSON] {
    def writes(metricsForJSON: MetricsForJSON) = Json.obj(
      "data" -> metricsForJSON.data
    )
  }


  implicit def statusWrites[A](implicit request :Request[A]) = new Writes[models.Status] {
    def writes(status: models.Status) = Json.toJson(
      Messages(status.toString)
    )
  }
  implicit val natureWrites = new Writes[models.Nature] {
    def writes(nature: Nature) = Json.toJson(
      nature.toString
    )
  }
  implicit def suspectRowsWrites[A](implicit request :Request[A]) = new Writes[SuspectRow] {
    def writes(suspectRow: SuspectRow) = Json.obj(
      "id"  -> suspectRow.id,
      "date" -> suspectRow.date,
      "caisse" -> suspectRow.caisse,
      "groupe" -> suspectRow.groupe,
      "agence" -> suspectRow.agence,
      "pdv" -> suspectRow.pdv,
      "metricName" -> suspectRow.metricName,
      "value" -> Math.ceil(suspectRow.value),
      "status" -> Json.toJson(suspectRow.status),
      "nature" -> Json.toJson(suspectRow.nature),
      "firstDate" -> suspectRow.firstDate,
      "admin" -> suspectRow.admin,
      "comment" -> suspectRow.comment,
      "reasonsForDetection" -> suspectRow.reasonsForDetection.map(_.toString).mkString,
      "format" -> (MetricRepository.findByCode(suspectRow.metric) match {
        case None => ".d"
        case Some(metric:CodeMetric)=> metric.format
      })

    )
  }


  implicit val stateMessageWrites = new Writes[StateMessage] {
    def writes(state: StateMessage) = Json.obj(
      "niceMessage" -> state.niceMessage,
      "color" -> state.color
    )
  }

  def currentState = AuthenticatedAction() { implicit request =>
    val state = StateRepository.state

    val stateMessage = if (state.message == "state.majfailed") {
      StateMessage(Messages(state.message) + " " + state.niceDate + ".", "red")
    }
    else {
      StateMessage(Messages(state.message) + " " + state.niceDate + ".", "")
    }

    Ok(Json.toJson(stateMessage))
  }

    def translate[A](dashboard:DashBoard)(implicit request :Request[A]):DashBoard= {
      dashboard.copy(statusChart=dashboard.statusChart.copy(labelsForDisplay=dashboard.statusChart.labels.map(Messages(_))),
        natureChart=dashboard.natureChart.copy(labelsForDisplay=dashboard.natureChart.labels.map(Messages(_)))
      )
    }


  def dashBoardPdvs(caisse: String , groupe: String, agence: String )= AuthenticatedAction() { implicit request =>
    val dashboards = DashBoardGenerator.getDashBoardsForAgence(caisse, groupe, agence)
    Ok(Json.toJson(dashboards.map(translate(_)(request))))
  }
  def dashBoardAgences(caisse: String , groupe: String)= AuthenticatedAction() { implicit request =>
    val dashboards =DashBoardGenerator.getDashBoardsForGroupe(caisse, groupe)
    Ok(Json.toJson(dashboards.map(translate(_)(request))))
  }
  def dashBoardGroupes(caisse: String )= AuthenticatedAction() { implicit request =>
    val dashboards =DashBoardGenerator.getDashBoardsForCaisse(caisse)
    Ok(Json.toJson(dashboards.map(translate(_)(request))))
  }
  def dashBoardCaisses()= AuthenticatedAction() { implicit request =>
    val dashboards =DashBoardGenerator.getDashBoardsForAllCaisses()
    Ok(Json.toJson(dashboards.map(translate(_)(request))))
  }
  def dashBoardAll()= AuthenticatedAction() { implicit request =>
    val dashboard =DashBoardGenerator.getDashBoardsForAll()
    Ok(Json.toJson(translate(dashboard)(request)))
  }
  def dashBoard()= AuthenticatedAction() { implicit request =>
    Ok(views.html.dashboard(request.user))
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