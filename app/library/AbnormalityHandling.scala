package library

import anorm.SqlParser._
import anorm._
import models.ErrorBPCE
import play.api.Play.current
import play.api.db._
import utils.dateParser._

object AbnormalityHandling {

  def getAll(): List[ErrorBPCE] = {
    DB.withConnection{
      implicit c =>
        SQL("select * from testing").as(error1 *)
    }
  }

  def filter (status :String): List[ErrorBPCE] ={
    DB.withConnection{
      implicit c =>
        SQL("select * from testing where status = {status}").on('status->status).as(error1 *)
    }
  }

  def findErrorById(error :ErrorBPCE) :Option[ErrorBPCE] = {
    DB.withConnection{
      implicit c =>
        SQL("select * from testing where date={date} and caisse = {caisse} and groupe = {groupe} and agence ={agence} and pdv ={pdv}").on(
        'date -> error.date,
        'caisse -> error.caisse,
        'groupe -> error.groupe,
        'agence -> error.agence,
        'pdv -> error.pdv
        ).as(error1.singleOpt)
    }
  }

  def add(error: ErrorBPCE) = {
    DB.withConnection {
      implicit c =>
        SQL(
          "insert into testing (date,caisse,groupe,agence,pdv,AbnormalMetric,type,firstDateDetection,status,comment, admin) values ({date},{caisse},{groupe},{agence},{pdv},{AbnormalMetric},{type},{firstDateDetection},{status},{comment},{admin})").on(
            'date -> error.date,
            'caisse -> error.caisse,
            'groupe -> error.groupe,
            'agence -> error.agence,
            'pdv -> error.pdv,
            'AbnormalMetric -> error.metric,
            'type -> error.errorType,
            'firstDateDetection -> error.firstDate,
            'status -> error.status,
            'comment -> error.comment,
            'admin -> error.admin

          ).executeUpdate()
    }
  }

  val error1 = {
      get[java.sql.Date]("date") ~
      get[String]("caisse") ~
      get[String]("groupe") ~
      get[String]("agence") ~
      get[String]("pdv") ~
      get[String]("abnormalMetric") ~
      get[String]("type") ~
      get[java.sql.Date]("firstDateDetection") ~
      get[String]("status") ~
      get[String]("comment") ~
      get[String]("admin") map {
      case date ~ caisse ~ groupe ~ agence ~ pdv ~ abnormalMetric ~ errorType ~ firstDateDetection ~ status ~ comment ~ admin =>
        ErrorBPCE(date.toString, caisse, groupe, agence, pdv, abnormalMetric, errorType, firstDateDetection.toString, status, comment, admin)
    }
  }

}
