package library

import library.utils.Math
import models.{Abnormality, AbnormalityList, Row}

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

  def operationByDimensions(grouped: Map[List[String], List[Row]], operation: List[Row] => Double, row: Row): Double = {
    operation(grouped(row.dimensions))
  }


  // Ways to Detect Abnormalities
  def getAbnormalitiesFromDistanceToMean(rows: List[Row], numberOfStdDev: Int): List[Abnormality] = {
    val groupedByDate = groupByDate(rows)
    val abnormalitiesBecauseOfDistanceToMeanByDate = rows.flatMap { row =>
      val averageByDate = operationByDate(groupedByDate, average, row)
      val standardDeviationByDate = operationByDate(groupedByDate, standardDeviation, row)
      if (row.metric > averageByDate + numberOfStdDev * standardDeviationByDate ||
        row.metric < averageByDate - numberOfStdDev * standardDeviationByDate)
        List(Abnormality(row, List("TooFarFromMeanByDate")))
      else List.empty[Abnormality]
    }

    val groupedByDimensions = groupByDimensions(rows)
    val abnormalitiesBecauseOfDistanceToMeanByDimensions = rows.flatMap { row =>
      val averageByDimensions = operationByDimensions(groupedByDimensions, average, row)
      val standardDeviationByDimensions = operationByDimensions(groupedByDimensions, standardDeviation, row)
      if (row.metric > averageByDimensions + numberOfStdDev * standardDeviationByDimensions ||
        row.metric < averageByDimensions - numberOfStdDev * standardDeviationByDimensions)
        List(Abnormality(row, List("TooFarFromMeanByDimensions")))
      else List.empty[Abnormality]
    }
    abnormalitiesBecauseOfDistanceToMeanByDate ::: abnormalitiesBecauseOfDistanceToMeanByDimensions
  }

  def mergeAbnormalities(listOfAbnormality: List[Abnormality]): List[Abnormality] = {
    def mergeAbnormalitiesWithSameRow(listWithSameRow: List[Abnormality]): Abnormality = {
      Abnormality(listWithSameRow.head.row, listWithSameRow.flatMap(_.reasons))
    }

    val hashMapByRow: Map[Row, List[Abnormality]] = listOfAbnormality.groupBy(abnormality => abnormality.row)
    hashMapByRow.foldLeft(List.empty[Abnormality]) { (acc: List[Abnormality], kv: (Row, List[Abnormality])) =>
      mergeAbnormalitiesWithSameRow(kv._2) :: acc
    }
  }

  def getAllAbnormalities(rows: List[Row], mapCodesToNames: Map[String, String], metric: String): AbnormalityList = {
    val abnormalitiesFromDistanceToMean = getAbnormalitiesFromDistanceToMean(rows, 6)
    val mergedAbnormalities: List[Abnormality] = mergeAbnormalities(abnormalitiesFromDistanceToMean ::: Nil)


    val mergedAbnormalitiesWithNames: List[Abnormality] = mergedAbnormalities.map { abnormality =>
      val row = abnormality.row
      val dimensionsWithNames = row.dimensions.map(dimension =>
        if (mapCodesToNames.contains(dimension)) mapCodesToNames(dimension)
        else dimension)
      Abnormality(Row(row.date, dimensionsWithNames, row.metric), abnormality.reasons)
    }
    AbnormalityList(metric, mergedAbnormalitiesWithNames)

  }
}
