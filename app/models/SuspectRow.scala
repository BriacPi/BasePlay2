package models

import anorm.SqlParser._
import anorm._
import models.Nature._
import play.api.Play.current
import play.api.db.DB

import scala.language.postfixOps


case class SuspectRow(id: Long, date: java.time.LocalDate, caisse: String, groupe: String, agence: String, pdv: String,
                      metric: String,value:Double, status: Status, nature: Nature, firstDate: java.time.LocalDate, admin: String, comment: String) {

  // Constructeurs
  def this(date: java.time.LocalDate, caisse: String, groupe: String, agence: String, pdv: String, metric: String,value:Double,
           status: Status, nature: Nature, firstDate: java.time.LocalDate, admin: String, comment: String) =
    this(0, date, caisse, groupe, agence, pdv, metric,value, status, nature, firstDate, admin, comment)

  def this(id: Long, date: java.time.LocalDate, caisse: String, groupe: String, agence: String, metric: String,value:Double,
           status: Status, nature: Nature, firstDate: java.time.LocalDate, admin: String, comment: String) =
    this(id: Long, date, caisse, groupe, agence, "Aggregated", metric,value, status, nature, firstDate, admin, comment)

  def this(id: Long, date: java.time.LocalDate, caisse: String, groupe: String, metric: String,value:Double,
           status: Status, nature: Nature, firstDate: java.time.LocalDate, admin: String, comment: String) =
    this(id, date, caisse, groupe, "Aggregated", "Aggregated", metric,value, status, nature, firstDate, admin, comment)

  def this(id: Long, date: java.time.LocalDate, caisse: String, metric: String,value:Double,
           status: Status, nature: Nature, firstDate: java.time.LocalDate, admin: String, comment: String) =
    this(id, date, caisse, "Aggregated", "Aggregated", "Aggregated", metric,value, status, nature, firstDate, admin, comment)

  def this(row: Row, metric: String) =
    this(0, row.date, row.dimensions.head, row.dimensions(1), row.dimensions(2), row.dimensions(3), metric,row.metric, Status.DetectedOnly, Nature.NotSpecified, java.time.LocalDate.now, "", "")

  //Methodes
  def isAbnormal: Boolean = this.nature match {
    case Abnormality => true
    case NotAbnormality => false
    case NotSpecified => false
  }

  def reasonsForDetection: List[ReasonForDetection] = {
    ReasonForDetection.getReasonsForDetection(id)
  }
}

// Data Base
object SuspectRow {
  val suspectRows = {
    get[Long]("id") ~
      get[String]("caisse") ~
      get[String]("groupe") ~
      get[String]("agence") ~
      get[String]("pdv") ~
      get[String]("metric") ~
      get[Double]("value") ~
      get[String]("status") ~
      get[String]("nature") ~
      get[String]("first_date") ~
      get[String]("admin") ~
      get[String]("comment") ~
      get[String]("date") map {
      case id ~ caisse ~ groupe ~ agence ~ pdv ~ metric~ value ~ status ~ nature ~ firstDate ~ admin ~ comment ~ date =>
        SuspectRow(id, java.time.LocalDate.parse(date), caisse, groupe, agence, pdv, metric,value, Status.withName(status),
          Nature.withName(nature), java.time.LocalDate.parse(firstDate), admin, comment)
    }
  }

  def all(): List[SuspectRow] = DB.withConnection { implicit c =>
    SQL("select * from suspect_rows order by date").as(suspectRows *)
  }


  def create(suspectRow: SuspectRow, reason: ReasonForDetection): Unit = {
    DB.withConnection { implicit c =>
      getId(suspectRow) match {
        case None =>
          val id: Option[Long] =
            SQL("insert into suspect_rows (date,caisse,groupe,agence,pdv,metric,value,status,nature,first_date,admin, comment) values " +
              "({date},{caisse},{groupe},{agence},{pdv},{metric},{value},{status},{nature},{firstDate},{admin},{comment})").on(
                'date -> suspectRow.date.toString,
                'caisse -> suspectRow.caisse,
                'groupe -> suspectRow.groupe,
                'agence -> suspectRow.agence,
                'pdv -> suspectRow.pdv,
                'metric -> suspectRow.metric,
                'value -> suspectRow.value,
                'status -> suspectRow.status.toString,
                'nature -> suspectRow.nature.toString,
                'firstDate -> suspectRow.firstDate.toString,
                'admin -> suspectRow.admin,
                'comment -> suspectRow.comment
              ).executeInsert()
          id match {
            case None =>
            case Some(i) => ReasonForDetection.addReasonForDetection(i, reason)
          }
        case Some(id) => ReasonForDetection.addReasonForDetection(id, reason)
      }
    }
  }


  def filterByStatus(status: Status): List[SuspectRow] = {
    DB.withConnection {
      implicit c =>
        SQL("select * from suspect_rows where status = {status}").on('status -> status.toString).as(suspectRows *)
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
    ).as(suspectRows.singleOpt)
  }

  def findById(id: Long): Option[SuspectRow] = DB.withConnection { implicit c =>
    SQL("select * from suspect_rows where id={id} ").on(
      'id -> id
    ).as(suspectRows.singleOpt)
  }

  def getId(suspect: SuspectRow): Option[Long] = DB.withConnection { implicit c =>
    val optionOfRow: Option[SuspectRow] = SQL("select * from suspect_rows where date={date} and caisse = {caisse} and groupe = {groupe} and agence ={agence} and pdv ={pdv} and metric = {metric}").on(
      'date -> suspect.date.toString,
      'caisse -> suspect.caisse,
      'groupe -> suspect.groupe,
      'agence -> suspect.agence,
      'pdv -> suspect.pdv,
      'metric -> suspect.metric
    ).as(suspectRows.singleOpt)
    optionOfRow match {
      case None => None
      case Some(row) => Some(row.id)
    }
  }

  def contains(suspectRow: SuspectRow): Boolean = {
    findByKey(suspectRow.date.toString, suspectRow.caisse, suspectRow.groupe, suspectRow.agence, suspectRow.pdv, suspectRow.metric) match {
      case None => false
      case Some(suspectrow) => true
    }
  }

  def edit(id:Long,newAdmin: String, newComment: String,newNature: Nature, newStatus: Status) = {
    DB.withConnection {
      implicit c =>
        SQL("update suspect_rows set   admin = {admin}, comment = {comment}, nature = {nature}, status = {status} where id = {id}").on(
          'id -> id,
          'admin -> newAdmin,
          'comment -> newComment,
          'nature -> newNature.toString,
          'status -> newStatus.toString
        ).executeUpdate()

    }
  }


  def editNature(id:Long, newNature: Nature) = {
    DB.withConnection {
      implicit c =>
        SQL("update suspect_rows set  nature = {nature} where id = {id}").on(
          'id -> id,
          'nature -> newNature.toString
        ).executeUpdate()
    }
  }

  def editAdmin(id:Long, newAdmin: String) = {
    DB.withConnection {
      implicit c =>
        SQL("update suspect_rows set  admin = {admin} where id = {id}").on(
          'id -> id,
          'admin -> newAdmin
        ).executeUpdate()
    }
  }

  def editComment(id:Long, newComment: String) = {
    DB.withConnection {
      implicit c =>
        SQL("update suspect_rows set  comment = {comment} where id = {id}").on(
          'id -> id,
          'comment -> newComment
        ).executeUpdate()
    }
  }

  def editStatus(id:Long, newStatus: Status) = {
    DB.withConnection {
      implicit c =>
        SQL("update suspect_rows set  status = {status} where id = {id}").on(
          'id ->id,
          'status -> newStatus.toString
        ).executeUpdate()
    }
  }

}

