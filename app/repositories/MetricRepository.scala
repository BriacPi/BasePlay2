package repositories

import anorm.SqlParser._
import anorm._
import play.api.Play.current
import play.api.db.DB

import scala.language.postfixOps

case class CodeMetric(id: Long, code: String, metricName: String,format:String)

case class CodeMetricWithoutId( code: String, metricName: String,format:String) {
  def this(codeMetric:CodeMetric)= this(codeMetric.code,codeMetric.metricName,codeMetric.format)
}


trait MetricRepository {

  private[repositories] val recordMapper = {
    long("metrics.id") ~
      str("metrics.metric") ~
      str("metrics.metric_name")~
      str("metrics.format") map {
      case id ~ code ~ metricName~format => {
        CodeMetric(id, code, metricName,format)
      }
    }
  }

  def create(metric: CodeMetricWithoutId): Unit = {
    if (exist(metric)) {}
    else {
    DB.withConnection { implicit c =>
      SQL("insert into metrics (metric,metric_name,format) values " +
        "({metric},{metric_name},{format})").on(
          'metric -> metric.code,
          'metric_name -> metric.metricName,
          'format -> metric.format
        ).executeInsert()
    }
  }
  }

  def delete(code:String): Unit = {
    DB.withConnection { implicit c =>
      SQL("delete from metrics  where metric = " +
        "{metric}").on(
          'metric -> code
        ).executeUpdate()
    }
  }

  def exist(metric: CodeMetricWithoutId):Boolean ={
    val metricOption = {
      DB.withConnection { implicit current =>
        SQL(
          """
          SELECT * FROM metrics
          where metric = {metric}
          """
        ).on(
            'metric -> metric.code
          ).as(recordMapper.singleOpt)
      }
    }
    metricOption match{
      case None =>false
      case Some(m)=>true}
  }

  def list(): List[CodeMetric] = {
    DB.withConnection { implicit current =>
      SQL(
        """
          SELECT * FROM metrics
          ORDER BY metric
        """
      )
        .on("enabled" -> true)
        .as(recordMapper *)
        .toList
    }
  }

  def listCodes(): List[String] = {
    list().map(_.code)
  }

  def findByCode(code: String): Option[CodeMetric] = {
    DB.withConnection { implicit current =>
      SQL(
        """
          SELECT *
          FROM metrics
          WHERE metric = {metric}
        """
      )
        .on("metric" -> code)
        .as(recordMapper.singleOpt)
    }
  }

  def findByName(name: String): Option[CodeMetric] = {
    DB.withConnection { implicit current =>
      SQL(
        """
          SELECT *
          FROM metrics
          WHERE metric_name = {metric_name}
        """
      )
        .on("metric_name" -> name)
        .as(recordMapper.singleOpt)
    }
  }

}

object MetricRepository extends MetricRepository
