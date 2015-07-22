package models

import anorm.SqlParser._
import anorm._
import models.Nature._
import play.api.Play.current
import play.api.db.DB

import scala.language.postfixOps


case class SuspectRow(id: Long, date: java.time.LocalDate, caisse: String, groupe: String, agence: String, pdv: String,
                      metric: String, status: Status, nature: Nature, firstDate: java.time.LocalDate, admin: String, comment: String) {

  // Constructeurs
  def this(date: java.time.LocalDate, caisse: String, groupe: String, agence: String, pdv: String, metric: String,
           status: Status, nature: Nature, firstDate: java.time.LocalDate, admin: String, comment: String) =
    this(0, date, caisse, groupe, agence, pdv, metric, status, nature, firstDate, admin, comment)

  def this(id: Long, date: java.time.LocalDate, caisse: String, groupe: String, agence: String, metric: String,
           status: Status, nature: Nature, firstDate: java.time.LocalDate, admin: String, comment: String) =
    this(id: Long, date, caisse, groupe, agence, "Aggregated", metric, status, nature, firstDate, admin, comment)

  def this(id: Long, date: java.time.LocalDate, caisse: String, groupe: String, metric: String,
           status: Status, nature: Nature, firstDate: java.time.LocalDate, admin: String, comment: String) =
    this(id, date, caisse, groupe, "Aggregated", "Aggregated", metric, status, nature, firstDate, admin, comment)

  def this(id: Long, date: java.time.LocalDate, caisse: String, metric: String,
           status: Status, nature: Nature, firstDate: java.time.LocalDate, admin: String, comment: String) =
    this(id, date, caisse, "Aggregated", "Aggregated", "Aggregated", metric, status, nature, firstDate, admin, comment)

  def this(row: Row, metric: String) =
    this(0, row.date, row.dimensions.head, row.dimensions(1), row.dimensions(2), row.dimensions(3), metric, Status.DetectedOnly, Nature.NotSpecified, java.time.LocalDate.now, "Nobody", "")

  //Methodes
  def isAbnormal: Boolean = this.nature match {
    case Abnormality => true
    case NotAbnormality => false
    case NotSpecified => false

  }
}

// Data Base
object SuspectRow {
  val suspectRow = {
    get[Long]("id") ~
      get[String]("caisse") ~
      get[String]("groupe") ~
      get[String]("agence") ~
      get[String]("pdv") ~
      get[String]("metric") ~
      get[String]("status") ~
      get[String]("nature") ~
      get[String]("first_date") ~
      get[String]("admin") ~
      get[String]("comment") ~
      get[String]("date") map {
      case id ~ caisse ~ groupe ~ agence ~ pdv ~ metric ~ status ~ nature ~ firstDate ~ admin ~ comment ~ date =>
        SuspectRow(id, java.time.LocalDate.parse(date), caisse, groupe, agence, pdv, metric, Status.withName(status),
          Nature.withName(nature), java.time.LocalDate.parse(firstDate), admin, comment)
    }
  }

  def all(): List[SuspectRow] = DB.withConnection { implicit c =>
    SQL("select * from suspect_rows order by date").as(suspectRow *)
  }


  def create(suspectRow: SuspectRow): Unit = {
    DB.withConnection { implicit c =>
      val id: Option[Long] =
        SQL("insert into suspect_rows (date,caisse,groupe,agence,pdv,metric,status,nature,first_date,admin, comment) values " +
          "({date},{caisse},{groupe},{agence},{pdv},{metric},{status},{nature},{firstDate},{admin},{comment})").on(
            'date -> suspectRow.date.toString,
            'caisse -> suspectRow.caisse,
            'groupe -> suspectRow.groupe,
            'agence -> suspectRow.agence,
            'pdv -> suspectRow.pdv,
            'metric -> suspectRow.metric,
            'status -> suspectRow.status.toString,
            'nature -> suspectRow.nature.toString,
            'firstDate -> suspectRow.firstDate.toString,
            'admin -> suspectRow.admin,
            'comment -> suspectRow.comment
          ).executeInsert()
    }
  }

  def filterOnStatus(status: Status): List[SuspectRow] = {
    DB.withConnection {
      implicit c =>
        SQL("select * from suspect_rows where status = {status}").on('status -> status.toString).as(suspectRow *)
    }
  }

  def findByKey(date: String, caisse: String, groupe: String, agence: String, pdv: String, metric: String): Option[SuspectRow] = DB.withConnection { implicit c =>
    SQL("select * from suspect_rows where date={date} and caisse = {caisse} and groupe = {groupe} and agence ={agence} and pdv ={pdv} and metric = {metric}").on(
      'date -> date,
      'caisse -> caisse,
      'groupe -> groupe,
      'agence -> agence,
      'pdv -> pdv,
      'metric -> metric
    ).as(suspectRow.singleOpt)
  }

  def findById(id:Long): Option[SuspectRow] = DB.withConnection { implicit c =>
    SQL("select * from suspect_rows where id={id} ").on(
      'id -> id
    ).as(suspectRow.singleOpt)
  }

  def contains(suspectRow: SuspectRow): Boolean = {
       findById(suspectRow.id) match{
         case None => false
         case Some(suspectrow)=>true
       }
      }
}

