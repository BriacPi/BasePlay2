package library.utils

import breeze.numerics.sqrt


object Math {
  def square(x: Double) = x * x

  def average(list: List[Double]): Double = {
    list.sum / list.length
  }

  def variance(list: List[Double]): Double = {
    val averageOfList = average(list)
    list.foldLeft(0.0)((acc: Double, element: Double) => square(element - averageOfList) + acc) / list.length
  }

  def standardDeviation(list: List[Double]): Double = {
    sqrt(variance(list))
  }
}
