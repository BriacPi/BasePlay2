package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads, _}


case class Labels(segments: Map[String, SegmentContent], eds: Map[String, EdsContent]) {
  def getCodesToNamesMap:Map[String,String]={
    eds.mapValues(_.default)
  }
}

case class SegmentContent(default: String, ordre: Int)

case class EdsContent(default: String, court: Option[String])

object EdsContent {
  implicit val edsContentReader: Reads[EdsContent] = (
    (JsPath \ "DEFAULT").read[String] and
      (JsPath \ "COURT").readNullable[String]
    )(EdsContent.apply _)
}

object SegmentContent {
  implicit val segmentContentReader: Reads[SegmentContent] = (
    (JsPath \ "DEFAULT").read[String] and
      (JsPath \ "Ordre").read[Int]
    )(SegmentContent.apply _)
}

object Labels {
  implicit val edsContentReader: Reads[Labels] = Json.reads[Labels]
}