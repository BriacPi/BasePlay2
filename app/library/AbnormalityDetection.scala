package library

import library.utils.Math
import models.{Abnormality, Row}

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
  def operationByDate(rows: List[Row], operation: List[Row] => Double): Map[java.time.LocalDate, Double] = {
    rows.groupBy((row: Row) => row.date).mapValues(operation)
  }

  def operationByDimensions(rows: List[Row], operation: List[Row] => Double): Map[List[String], Double] = {
    rows.groupBy((row: Row) => row.dimensions).mapValues(operation)
  }

  // Ways to Detect Abnormalities
  def getAbnormalitiesFromDistanceToMean(rows: List[Row],numberOfStdDev:Int): List[Abnormality] = {
    val averageByDate = operationByDate(rows, average)
    val averageByDimensions = operationByDimensions(rows, average)
    val standardDeviationByDate = operationByDate(rows, standardDeviation)
    val standardDeviationByDimensions = operationByDimensions(rows, standardDeviation)

    val abnormalitiesBecauseOfDistanceToMeanByDate = rows.flatMap { row =>
      if (row.metric > averageByDate(row.date) + numberOfStdDev * standardDeviationByDate(row.date) ||
        row.metric < averageByDate(row.date) - numberOfStdDev * standardDeviationByDate(row.date))
        List(Abnormality(row, List("TooFarFromMeanByDate")))
      else List.empty[Abnormality]
    }
    val abnormalitiesBecauseOfDistanceToMeanByDimensions = rows.flatMap { row =>
      if (row.metric > averageByDimensions(row.dimensions) + numberOfStdDev * standardDeviationByDimensions(row.dimensions) ||
        row.metric < averageByDimensions(row.dimensions) - numberOfStdDev * standardDeviationByDimensions(row.dimensions))
        List(Abnormality(row, List("TooFarFromMeanByDimensions")))
      else List.empty[Abnormality]
    }
    abnormalitiesBecauseOfDistanceToMeanByDate ::: abnormalitiesBecauseOfDistanceToMeanByDimensions
  }

  def mergeAbnormalities(listOfAbnormality: List[Abnormality]): List[Abnormality] = {
    def mergeAbnormalitiesWithSameRow(listWithSameRow: List[Abnormality]): Abnormality = {Abnormality(listWithSameRow.head.row, listWithSameRow.flatMap(_.reasons))}

    val hashMapByRow: Map[Row, List[Abnormality]] = listOfAbnormality.groupBy(abnormality => abnormality.row)
    hashMapByRow.foldLeft(List.empty[Abnormality]) { (acc: List[Abnormality], kv: (Row, List[Abnormality])) =>
      mergeAbnormalitiesWithSameRow(kv._2) :: acc
    }
  }

  def getAllAbnormalities(rows: List[Row]): List[Abnormality] = {
    val abnormalitiesFromDistanceToMean = getAbnormalitiesFromDistanceToMean(rows,6)
    mergeAbnormalities(abnormalitiesFromDistanceToMean)
  }
}
