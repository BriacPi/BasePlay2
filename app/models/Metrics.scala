package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads, _}


case class Metrics(metrics: Map[String, Metric]) {
  def getMetricsToNamesMap:Map[String,String]={
    metrics.mapValues(_.label)
  }
}
case class Metric(label:String,formatString:String, growth:String, invertGrowth:Boolean, description:String, formatGrowth:String)



object Metric {
  implicit val metricReader: Reads[Metric] = (
    (JsPath \ "label").read[String] and
      (JsPath \ "format_string").read[String] and
      (JsPath \ "growth").read[String] and
      (JsPath \ "invert_growth").read[Boolean] and
      (JsPath \ "description").read[String] and
      (JsPath \ "format_growth").read[String]
    )(Metric.apply _)
}



object Metrics {
  implicit val metricsReader: Reads[Metrics] = Json.reads[Metrics]
}