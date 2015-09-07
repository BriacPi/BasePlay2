package library

import library.utils.Math
import models.ReasonForDetection.{TooFarFromMeanByDimensions, TooFarFromMeanByDate}
import models.{Metrics, Row, SuspectRow}

object AbnormalityDetection {
  // List of operations
  def average(rows: List[Row]): Double = {
    Math.average(rows.map(_.metric))
  }

  def variance(rows: List[Row]): Double = {
    Math.variance(rows.map(_.metric))
  }

  def standardDeviation(rows: List[Row]): Double = {
    Math.standardDeviation(rows.map(_.metric))
  }

  // Apply operations
  def groupByDate(rows: List[Row]): Map[java.time.LocalDate, List[Row]] = {
    rows.groupBy((row: Row) => row.date)
  }

  def groupByDimensions(rows: List[Row]): Map[List[String], List[Row]] = {
    rows.groupBy((row: Row) => row.dimensions)
  }

  def operationByDate(grouped: Map[java.time.LocalDate, List[Row]], operation: List[Row] => Double, row: Row): Double = {
    operation(grouped(row.date))
  }

  def operationByDimensions(grouped: Map[List[String], List[Row]], operation: List[Row] => Double, row: Row,filter: Row => Boolean): Double = {
    operation(grouped(row.dimensions).filter(filter))
  }
  def testCompleteness(grouped: Map[List[String], List[Row]], row: Row,filter: Row => Boolean,numberOfMonthsToLookBefore:Int):Boolean={
    grouped(row.dimensions).count(filter) >=numberOfMonthsToLookBefore*4
  }

  // Ways to Detect Abnormalities
  def filterAbnormalitiesFromDistanceToMean(rows: List[Row], numberOfStdDev: Int, metric: String,map:Metrics): Unit = {
//    val groupedByDate = groupByDate(rows)
//    rows.foreach { row =>
//      val averageByDate = operationByDate(groupedByDate, average, row)
//      val standardDeviationByDate = operationByDate(groupedByDate, standardDeviation, row)
//      if (row.metric > averageByDate + numberOfStdDev * standardDeviationByDate ||
//        row.metric < averageByDate - numberOfStdDev * standardDeviationByDate) {
//        SuspectRow.create(new SuspectRow(row, metric,map),TooFarFromMeanByDimensions)
//      }
//    }
    val numberOfMonthsToLookBefore = 8
    val groupedByDimensions = groupByDimensions(rows)
    def  filter(row:Row)(row2:Row):Boolean = {
      row.date.minusMonths(numberOfMonthsToLookBefore).isBefore(row2.date) && row.date.isAfter(row2.date)
    }
    rows.foreach { row =>
      if(row.date.isAfter(java.time.LocalDate.of(2014,6,1))){
      val averageByDimensions = operationByDimensions(groupedByDimensions, average, row, filter(row))
      val standardDeviationByDimensions = operationByDimensions(groupedByDimensions, standardDeviation, row,filter(row))
      if (testCompleteness(groupedByDimensions,row,filter(row),numberOfMonthsToLookBefore) &&
        (row.metric > averageByDimensions + numberOfStdDev * averageByDimensions ||
        row.metric < averageByDimensions - numberOfStdDev * averageByDimensions) &&
        (row.metric > averageByDimensions + numberOfStdDev * standardDeviationByDimensions ||
        row.metric < averageByDimensions - numberOfStdDev * standardDeviationByDimensions)) {
        val criticityScore = (row.metric-averageByDimensions)/(standardDeviationByDimensions+math.Pi-3.14)
        println(criticityScore)
        SuspectRow.create(new SuspectRow(row, metric,criticityScore,map),TooFarFromMeanByDate)}
    }
    }

  }


  def filterAllAbnormalities(rows: List[Row], mapCodesToNames: Map[String, String], metric: String,map:Metrics): Unit = {
    val rowsWithNames = rows.map(row => {
      val dimensionsWithNames = row.dimensions.map(dimension =>
        if (mapCodesToNames.contains(dimension)) mapCodesToNames(dimension)
        else dimension)
      Row(row.date,dimensionsWithNames,row.metric)
    }
    )
    filterAbnormalitiesFromDistanceToMean(rowsWithNames, 10, metric,map)

  }
}
