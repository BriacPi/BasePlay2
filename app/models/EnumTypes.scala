package models

import anorm.SqlParser._
import anorm._
import enumeratum._
import play.api.db.DB
import play.api.Play.current

import scala.language.postfixOps

sealed trait ReasonForDetection extends EnumEntry

case object ReasonForDetection extends Enum[ReasonForDetection] {

  val reasonForDetection = {
    get[Long]("id") ~
      get[String]("reason") map {
      case id ~ reason =>
        ReasonForDetection.withName(reason)
    }
  }

  def addReasonForDetection(id: Long, reason: ReasonForDetection): Unit = {
    if (!coupleIdReasonExists(id, reason)) {
      DB.withConnection { implicit c =>
        SQL("insert into reasons_for_detection (id,reason) values " +
          "({id},{reason})").on(
            'id -> id,
            'reason -> reason.toString
          ).executeInsert()
      }
    }
  }

  def coupleIdReasonExists(id: Long, reason: ReasonForDetection): Boolean = {
    val reason2 = DB.withConnection { implicit c =>
      SQL("select * from reasons_for_detection where id={id} and reason={reason} ").on(
        'id -> id,
        'reason -> reason.toString
      ).as(reasonForDetection.singleOpt)
    }
    reason2 match {
      case None => false
      case Some(r) => true
    }
  }

  def getReasonsForDetection(id: Long): List[ReasonForDetection] = {
    DB.withConnection { implicit c =>
      SQL("select * from reasons_for_detection where id={id} ").on(
        'id -> id
      ).as(reasonForDetection *)
    }
  }

  case object TooFarFromMeanByDate extends ReasonForDetection

  case object TooFarFromMeanByDimensions extends ReasonForDetection

  case object NotSpecified extends ReasonForDetection

  val values: Seq[ReasonForDetection] = findValues
}


sealed trait Status extends EnumEntry

case object Status extends Enum[Status] {

  case object Solved extends Status

  case object BeingProcessed extends Status

  case object DetectedOnly extends Status

  val values: Seq[Status] = findValues
}

sealed trait Nature extends EnumEntry

case object Nature extends Enum[Nature] {

  case object NotSpecified extends Nature

  case object Abnormality extends Nature

  case object NotAbnormality extends Nature

  val values: Seq[Nature] = findValues

}
