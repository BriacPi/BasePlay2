//package library
//
//import anorm.SqlParser._
//import anorm._
//
//import library.utils.dateParser._
//import models.ErrorBPCE
//
//import play.api.Play.current
//import play.api.db._
//
//
//
//object AbnormalityHandling {
//
//  def getAll(): List[ErrorBPCE] = {
//    DB.withConnection {
//      implicit c =>
//        SQL("select * from testing").as(error1 *)
//    }
//  }
//
//  def filter(status: String): List[ErrorBPCE] = {
//    DB.withConnection {
//      implicit c =>
//        SQL("select * from testing where status = {status}").on('status -> status).as(error1 *)
//    }
//  }
//
//  def findErrorById(error: ErrorBPCE): Option[ErrorBPCE] = {
//    DB.withConnection {
//      implicit c =>
//        SQL("select * from testing where date={date} and caisse = {caisse} and groupe = {groupe} and agence ={agence} and pdv ={pdv} and AbnormalMetric = {AbnormalMetric}").on(
//          'date -> error.date,
//          'caisse -> error.caisse,
//          'groupe -> error.groupe,
//          'agence -> error.agence,
//          'pdv -> error.pdv,
//        'AbnormalMetric -> error.metric
//        ).as(error1.singleOpt)
//    }
//  }
//
//  def contains(error: ErrorBPCE): Boolean = {
//   findErrorById(error) match{
//     case None => false
//     case Some(errorBPCE)=>true
//   }
//  }
//
//  def editStatus(error: ErrorBPCE, newStatus: String) = {
//    DB.withConnection {
//      implicit c =>
//        SQL("update testing set  status = {status} where date={date} and caisse = {caisse} and groupe = {groupe} and agence ={agence} and pdv ={pdv} and AbnormalMetric = {AbnormalMetric}").on(
//          'date -> error.date,
//          'caisse -> error.caisse,
//          'groupe -> error.groupe,
//          'agence -> error.agence,
//          'pdv -> error.pdv,
//          'AbnormalMetric -> error.metric,
//          'status -> newStatus
//        ).executeUpdate()
//    }
//
//  }
//
//  def editType(error: ErrorBPCE, newErrorType: String) = {
//    DB.withConnection {
//      implicit c =>
//        SQL("update testing set  type = {type} where date={date} and caisse = {caisse} and groupe = {groupe} and agence ={agence} and pdv ={pdv} and AbnormalMetric = {AbnormalMetric}").on(
//          'date -> error.date,
//          'caisse -> error.caisse,
//          'groupe -> error.groupe,
//          'agence -> error.agence,
//          'pdv -> error.pdv,
//          'AbnormalMetric -> error.metric,
//          'type -> newErrorType
//        ).executeUpdate()
//    }
//
//  }
//
//  def editComment(error: ErrorBPCE, newComment: String) = {
//    DB.withConnection {
//      implicit c =>
//        SQL("update testing set  comment = {comment} where date={date} and caisse = {caisse} and groupe = {groupe} and agence ={agence} and pdv ={pdv}  and AbnormalMetric = {AbnormalMetric}").on(
//          'date -> error.date,
//          'caisse -> error.caisse,
//          'groupe -> error.groupe,
//          'agence -> error.agence,
//          'pdv -> error.pdv,
//          'AbnormalMetric -> error.metric,
//          'comment -> newComment
//        ).executeUpdate()
//    }
//
//  }
//
//  def editAdmin(error: ErrorBPCE, newAdmin: String) = {
//    DB.withConnection {
//      implicit c =>
//        SQL("update testing set  admin = {admin} where date={date} and caisse = {caisse} and groupe = {groupe} and agence ={agence} and pdv ={pdv} and AbnormalMetric = {AbnormalMetric}").on(
//          'date -> error.date,
//          'caisse -> error.caisse,
//          'groupe -> error.groupe,
//          'agence -> error.agence,
//          'pdv -> error.pdv,
//          'AbnormalMetric -> error.metric,
//          'admin -> newAdmin
//        ).executeUpdate()
//    }
//
//  }
//
//  def add(error: ErrorBPCE) = {
//    DB.withConnection {
//      implicit c =>
//        SQL(
//          "insert into testing (date,caisse,groupe,agence,pdv,AbnormalMetric,type,firstDateDetection,status,comment, admin,reasonForDetection) values ({date},{caisse},{groupe},{agence},{pdv},{AbnormalMetric},{type},{firstDateDetection},{status},{comment},{admin},{reasonForDetection})").on(
//            'date -> error.date,
//            'caisse -> error.caisse,
//            'groupe -> error.groupe,
//            'agence -> error.agence,
//            'pdv -> error.pdv,
//            'AbnormalMetric -> error.metric,
//            'type -> error.errorType,
//            'firstDateDetection -> error.firstDate,
//            'status -> error.status,
//            'comment -> error.comment,
//            'admin -> error.admin,
//            'reasonForDetection -> error.reasonForDetection
//
//          ).executeUpdate()
//    }
//  }
//
//  def getStatus(error:ErrorBPCE):String=findErrorById(error).head.status
//
//  def getErrorType(error:ErrorBPCE):String=findErrorById(error).head.errorType
//
//
//  def addProtected(error: ErrorBPCE) = {
//   if (contains(error)) {
//     if (getStatus(error)=="Treated" && getErrorType(error)=="Anomaly") {editStatus(error,"Not treated")}
//
//   }
//    else add(error)
//  }
//
//
//
//
//  val error1 = {
//    get[java.sql.Date]("date") ~
//      get[String]("caisse") ~
//      get[String]("groupe") ~
//      get[String]("agence") ~
//      get[String]("pdv") ~
//      get[String]("abnormalMetric") ~
//      get[String]("type") ~
//      get[java.sql.Date]("firstDateDetection") ~
//      get[String]("status") ~
//      get[String]("comment") ~
//      get[String]("admin") ~
//      get[String]("reasonForDetection") map {
//      case date ~ caisse ~ groupe ~ agence ~ pdv ~ abnormalMetric ~ errorType ~ firstDateDetection ~ status ~ comment ~ admin ~ reasonForDetection =>
//        ErrorBPCE(date.toString, caisse, groupe, agence, pdv, abnormalMetric, errorType, firstDateDetection.toString, status, comment, admin, reasonForDetection)
//    }
//  }
//
//}
