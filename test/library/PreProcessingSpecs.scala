package library


import models.{Row, RowDouble}
import org.scalatest.FunSuite

class PreProcessingSpecs extends FunSuite {

  test("Test filter uncomplete") {
    val listWithAverage: List[RowDouble] = PreProcessing.filterUncompleteRows(rowDoubleList3)
    assert(listWithAverage.size == 3)
  }

  test("Test average") {
    val listWithAverage: List[RowDouble] = PreProcessing.average(RowDoubleList, 2)
    val listWithAverage2 = PreProcessing.average(RowDoubleList2, 6)
    assert(listWithAverage.head.metrics.size == 1 && listWithAverage.tail.tail.forall(row => row.metrics.size == 2))
    assert(math.abs(listWithAverage2.last.metrics.head - 27.16666) <= 0.01)
  }


  test("Test grad") {
    val listWithAverage: List[RowDouble] = PreProcessing.gradient(RowDoubleList, 1)
    val listWithAverage2 = PreProcessing.gradient(RowDoubleList2, 6)
    assert(listWithAverage.head.metrics.size == 1 && listWithAverage.tail.tail.forall(row => row.metrics.size == 2))
    assert(listWithAverage2.size == 14 && math.abs(listWithAverage2.last.metrics.head * 6.0 - 60.0) <= 0.01)

  }
  test("Delay By N Months") {
    val l1 = PreProcessing.delayByNMonths(RowDoubleList, 37)
    val l2 = PreProcessing.delayByNMonths(RowDoubleList2, 6)
    assert(l1 == List())
    assert(l2.size == 2 && l2.head.target == 62.0)
  }


  test("normalization of target ") {
    val normalized = PreProcessing.normalizationOfTheTarget(rowList)
    val normalizedWithZeros = PreProcessing.normalizationOfTheTarget(rowListWithZeros)
    assert(normalized.forall(row => row.target <= 1.0 && row.target >= 0.0))
    assert(normalizedWithZeros.forall(row => row.target == 0.0))
  }

  test("Row to Double") {
    val rowDouble = PreProcessing.applyDoubleTransformationToAll(rowList)
    assert(rowDouble.forall(row => row.dimensions.forall(x => x.isInstanceOf[Double])))
  }


  test("find by id ") {
    val rowDouble = RowDouble(java.time.LocalDate.of(2012, 1, 1), List(1.0), List(2.0), 2.0)
    val rowWithSameId = PreProcessing.filterById(rowDouble, RowDoubleList)
    assert(rowWithSameId.forall(row => row.dimensions.equals(rowDouble.dimensions)))
  }

  test("Ancestor") {
    assert(PreProcessing.ancestor(RowDouble(java.time.LocalDate.of(2014, 5, 1), List(1.0), List(2.0), 98.0), RowDoubleList, 4) >= 0)
    assert(PreProcessing.ancestor(RowDouble(java.time.LocalDate.of(2012, 1, 1), List(1.0), List(2.0), 98.0), RowDoubleList, 4) == (-1))
  }
  test("data base") {

    assert(!CacheMemory.all().isEmpty)

  }

  val rowDouble1 = RowDouble(java.time.LocalDate.of(2012, 1, 1), List(1.0), List(2.0), 2.0)
  val rowDouble2 = RowDouble(java.time.LocalDate.of(2014, 3, 1), List(2.0), List(3.0), 1.0)


  val rowList = List(
    Row(java.time.LocalDate.of(2012, 1, 1), List("aa"), List(2.0), 2.0),
    Row(java.time.LocalDate.of(2012, 3, 1), List("bb"), List(3.0), 1.0),
    Row(java.time.LocalDate.of(2013, 4, 1), List("aa"), List(2.0), 2.0),
    Row(java.time.LocalDate.of(2013, 6, 1), List("bb"), List(3.0), 1.0),
    Row(java.time.LocalDate.of(2013, 12, 1), List("aa"), List(3.0), 1.0),
    Row(java.time.LocalDate.of(2014, 1, 1), List("bb"), List(2.0), 2.0)
  )
  val rowListWithZeros = List(
    Row(java.time.LocalDate.of(2012, 1, 1), List("aa"), List(2.0), 0.0),
    Row(java.time.LocalDate.of(2012, 3, 1), List("bb"), List(3.0), 0.0),
    Row(java.time.LocalDate.of(2013, 4, 1), List("aa"), List(2.0), 0.0),
    Row(java.time.LocalDate.of(2013, 6, 1), List("bb"), List(3.0), 0.0),
    Row(java.time.LocalDate.of(2013, 12, 1), List("aa"), List(3.0), 0.0),
    Row(java.time.LocalDate.of(2014, 1, 1), List("bb"), List(2.0), 0.0)
  )
  val rowDoubleList = List(
    RowDouble(java.time.LocalDate.of(2012, 1, 1), List(1.0), List(2.0), 2.0),
    RowDouble(java.time.LocalDate.of(2012, 3, 1), List(2.0), List(3.0), 1.0),
    RowDouble(java.time.LocalDate.of(2013, 4, 1), List(1.0), List(2.0), 2.0),
    RowDouble(java.time.LocalDate.of(2013, 6, 1), List(2.0), List(3.0), 1.0),
    RowDouble(java.time.LocalDate.of(2013, 12, 1), List(1.0), List(3.0), 1.0),
    RowDouble(java.time.LocalDate.of(2014, 1, 1), List(2.0), List(2.0), 2.0)
  )


  val RowDoubleList = List(
    RowDouble(java.time.LocalDate.of(2012, 1, 1), List(1.0), List(2.0), 2.0),
    RowDouble(java.time.LocalDate.of(2012, 1, 1), List(2.0), List(3.0), 1.0),
    RowDouble(java.time.LocalDate.of(2012, 2, 1), List(1.0), List(2.0), 2.0),
    RowDouble(java.time.LocalDate.of(2012, 2, 1), List(2.0), List(3.0), 3.0),
    RowDouble(java.time.LocalDate.of(2012, 3, 1), List(2.0), List(3.0), 4.0),
    RowDouble(java.time.LocalDate.of(2012, 3, 1), List(1.0), List(2.0), 4.0),
    RowDouble(java.time.LocalDate.of(2012, 4, 1), List(2.0), List(3.0), 5.0),
    RowDouble(java.time.LocalDate.of(2012, 4, 1), List(1.0), List(2.0), 9.0),
    RowDouble(java.time.LocalDate.of(2012, 5, 1), List(2.0), List(3.0), 78.0),
    RowDouble(java.time.LocalDate.of(2012, 5, 1), List(1.0), List(2.0), 20),
    RowDouble(java.time.LocalDate.of(2012, 6, 1), List(2.0), List(3.0), 12.0),
    RowDouble(java.time.LocalDate.of(2012, 6, 1), List(1.0), List(2.0), 344.0),
    RowDouble(java.time.LocalDate.of(2012, 7, 1), List(2.0), List(3.0), 343.0),
    RowDouble(java.time.LocalDate.of(2012, 7, 1), List(1.0), List(2.0), 983.0),
    RowDouble(java.time.LocalDate.of(2012, 8, 1), List(2.0), List(3.0), 322.0),
    RowDouble(java.time.LocalDate.of(2012, 8, 1), List(1.0), List(2.0), 232.0),
    RowDouble(java.time.LocalDate.of(2012, 9, 1), List(2.0), List(3.0), 1221.0),
    RowDouble(java.time.LocalDate.of(2012, 9, 1), List(1.0), List(2.0), 21.0),
    RowDouble(java.time.LocalDate.of(2012, 10, 1), List(2.0), List(3.0), 198.0),
    RowDouble(java.time.LocalDate.of(2012, 10, 1), List(1.0), List(2.0), 226.0),
    RowDouble(java.time.LocalDate.of(2012, 11, 1), List(2.0), List(3.0), 26.0),
    RowDouble(java.time.LocalDate.of(2012, 11, 1), List(1.0), List(2.0), 73.0),
    RowDouble(java.time.LocalDate.of(2012, 12, 1), List(2.0), List(3.0), 17.0),
    RowDouble(java.time.LocalDate.of(2012, 12, 1), List(1.0), List(2.0), 2.0),
    RowDouble(java.time.LocalDate.of(2013, 1, 1), List(2.0), List(3.0), 632.0),
    RowDouble(java.time.LocalDate.of(2013, 1, 1), List(1.0), List(2.0), 221.0),
    RowDouble(java.time.LocalDate.of(2013, 2, 1), List(2.0), List(3.0), 32.0),
    RowDouble(java.time.LocalDate.of(2013, 2, 1), List(1.0), List(2.0), 46.0),
    RowDouble(java.time.LocalDate.of(2013, 3, 1), List(2.0), List(3.0), 48.0),
    RowDouble(java.time.LocalDate.of(2013, 3, 1), List(1.0), List(2.0), 98.0),
    RowDouble(java.time.LocalDate.of(2013, 4, 1), List(2.0), List(3.0), 37.0),
    RowDouble(java.time.LocalDate.of(2013, 4, 1), List(1.0), List(2.0), 38.0),
    RowDouble(java.time.LocalDate.of(2013, 5, 1), List(2.0), List(3.0), 376.0),
    RowDouble(java.time.LocalDate.of(2013, 5, 1), List(1.0), List(2.0), 392.0),
    RowDouble(java.time.LocalDate.of(2013, 6, 1), List(2.0), List(3.0), 86.0),
    RowDouble(java.time.LocalDate.of(2013, 6, 1), List(1.0), List(2.0), 987.0),
    RowDouble(java.time.LocalDate.of(2013, 7, 1), List(2.0), List(3.0), 65.0),
    RowDouble(java.time.LocalDate.of(2013, 7, 1), List(1.0), List(2.0), 35.0),
    RowDouble(java.time.LocalDate.of(2013, 8, 1), List(2.0), List(3.0), 362.0),
    RowDouble(java.time.LocalDate.of(2013, 8, 1), List(1.0), List(2.0), 32.0),
    RowDouble(java.time.LocalDate.of(2013, 9, 1), List(2.0), List(3.0), 17.0),
    RowDouble(java.time.LocalDate.of(2013, 9, 1), List(1.0), List(2.0), 23.0),
    RowDouble(java.time.LocalDate.of(2013, 10, 1), List(2.0), List(3.0), 93.0),
    RowDouble(java.time.LocalDate.of(2013, 10, 1), List(1.0), List(2.0), 323.0),
    RowDouble(java.time.LocalDate.of(2013, 11, 1), List(2.0), List(3.0), 645.0),
    RowDouble(java.time.LocalDate.of(2013, 11, 1), List(1.0), List(2.0), 666.0),
    RowDouble(java.time.LocalDate.of(2013, 12, 1), List(2.0), List(3.0), 999.0),
    RowDouble(java.time.LocalDate.of(2013, 12, 1), List(1.0), List(2.0), 327.0),
    RowDouble(java.time.LocalDate.of(2014, 1, 1), List(2.0), List(3.0), 83.0),
    RowDouble(java.time.LocalDate.of(2014, 1, 1), List(1.0), List(2.0), 92.0),
    RowDouble(java.time.LocalDate.of(2014, 2, 1), List(2.0), List(3.0), 123.0),
    RowDouble(java.time.LocalDate.of(2014, 2, 1), List(1.0), List(2.0), 32.0),
    RowDouble(java.time.LocalDate.of(2014, 3, 1), List(2.0), List(3.0), 32.0),
    RowDouble(java.time.LocalDate.of(2014, 3, 1), List(1.0), List(2.0), 23.0),
    RowDouble(java.time.LocalDate.of(2014, 4, 1), List(2.0), List(3.0), 983.0),
    RowDouble(java.time.LocalDate.of(2014, 4, 1), List(1.0), List(2.0), 32.0),
    RowDouble(java.time.LocalDate.of(2014, 5, 1), List(2.0), List(3.0), 45.0),
    RowDouble(java.time.LocalDate.of(2014, 5, 1), List(1.0), List(2.0), 98.0),
    RowDouble(java.time.LocalDate.of(2014, 6, 1), List(2.0), List(3.0), 645.0),
    RowDouble(java.time.LocalDate.of(2014, 6, 1), List(1.0), List(2.0), 8.0),
    RowDouble(java.time.LocalDate.of(2014, 7, 1), List(2.0), List(3.0), 2.0),
    RowDouble(java.time.LocalDate.of(2014, 7, 1), List(1.0), List(2.0), 3.0),
    RowDouble(java.time.LocalDate.of(2014, 8, 1), List(2.0), List(3.0), 4.0),
    RowDouble(java.time.LocalDate.of(2014, 8, 1), List(1.0), List(2.0), 41.0),
    RowDouble(java.time.LocalDate.of(2014, 9, 1), List(2.0), List(3.0), 4.0),
    RowDouble(java.time.LocalDate.of(2014, 9, 1), List(1.0), List(2.0), 9.0),
    RowDouble(java.time.LocalDate.of(2014, 10, 1), List(2.0), List(3.0), 93.0),
    RowDouble(java.time.LocalDate.of(2014, 10, 1), List(1.0), List(2.0), 32.0),
    RowDouble(java.time.LocalDate.of(2014, 11, 1), List(2.0), List(3.0), 32.0),
    RowDouble(java.time.LocalDate.of(2014, 11, 1), List(1.0), List(2.0), 45.0),
    RowDouble(java.time.LocalDate.of(2014, 12, 1), List(2.0), List(3.0), 32.0),
    RowDouble(java.time.LocalDate.of(2014, 12, 1), List(1.0), List(2.0), 1.0)

  )
  val RowDoubleList2 = List(
    RowDouble(java.time.LocalDate.of(2012, 1, 1), List(1.0), List(2.0), 2.0),
    RowDouble(java.time.LocalDate.of(2012, 1, 1), List(2.0), List(3.0), 1.0),
    RowDouble(java.time.LocalDate.of(2012, 2, 1), List(1.0), List(2.0), 2.0),
    RowDouble(java.time.LocalDate.of(2012, 2, 1), List(2.0), List(3.0), 3.0),
    RowDouble(java.time.LocalDate.of(2012, 3, 1), List(1.0), List(3.0), 4.0),
    RowDouble(java.time.LocalDate.of(2012, 3, 1), List(2.0), List(2.0), 4.0),
    RowDouble(java.time.LocalDate.of(2012, 4, 1), List(1.0), List(3.0), 5.0),
    RowDouble(java.time.LocalDate.of(2012, 4, 1), List(2.0), List(2.0), 9.0),
    RowDouble(java.time.LocalDate.of(2012, 5, 1), List(1.0), List(3.0), 78.0),
    RowDouble(java.time.LocalDate.of(2012, 5, 1), List(2.0), List(2.0), 20),
    RowDouble(java.time.LocalDate.of(2012, 6, 1), List(1.0), List(3.0), 12.0),
    RowDouble(java.time.LocalDate.of(2012, 6, 1), List(2.0), List(2.0), 6.0),
    RowDouble(java.time.LocalDate.of(2012, 7, 1), List(2.0), List(3.0), 343.0),
    RowDouble(java.time.LocalDate.of(2012, 7, 1), List(1.0), List(2.0), 62.0))

  val rowDoubleList3 = List(
    RowDouble(java.time.LocalDate.of(2012, 1, 1), List(1.0), List(2.0, 1.0), 2.0),
    RowDouble(java.time.LocalDate.of(2012, 3, 1), List(2.0), List(3.0, 3.0), 1.0),
    RowDouble(java.time.LocalDate.of(2013, 4, 1), List(1.0), List(2.0, 4.0), 2.0),
    RowDouble(java.time.LocalDate.of(2013, 6, 1), List(2.0), List(3.0), 1.0),
    RowDouble(java.time.LocalDate.of(2013, 12, 1), List(1.0), List(3.0), 1.0),
    RowDouble(java.time.LocalDate.of(2014, 1, 1), List(2.0), List(2.0), 2.0)
  )
}

