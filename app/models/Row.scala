package models

case class Row(date: java.time.LocalDate, dimensions: List[String], metric: Double) {

  def hasSameDate(that: Row): Boolean = {
    this.date == that.date
  }

  def hasSameDimensions(that: Row): Boolean = {
    this.dimensions == that.dimensions
  }
}







