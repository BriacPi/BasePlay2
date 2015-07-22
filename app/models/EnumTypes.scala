package models

import enumeratum._

sealed trait ReasonForDetection extends EnumEntry

case object ReasonForDetection extends Enum[ReasonForDetection] {

  case object TooFarFromMeanByDate extends ReasonForDetection

  case object TooFarFromMeanByDimensions extends ReasonForDetection

  val values: Seq[ReasonForDetection] = findValues
}


sealed trait Status extends EnumEntry

case object Status extends enumeratum.Enum[Status] {

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