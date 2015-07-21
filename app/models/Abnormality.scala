package models

case class Abnormality(row: Row, reasons: List[String])

case class AbnormalityList(metric:String,abnormalities:List[Abnormality])
