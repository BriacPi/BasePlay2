package library

import org.scalatest.FunSuite
import models.Row
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.concurrent.ScalaFutures


class EngineSpecs  extends FunSuite with ScalaFutures{

    test("add Metric to row i.e. uncomplete rows are deleted correctly") {

      val row = new Row(java.time.LocalDate.of(2015,1,2),List("11425", "11425_007_7120", "11425_005_5126", "11425_003_14298"),List(1.0),3.0)
      val rowsForNewMetric1=Future(List(
        Row(java.time.LocalDate.of(2015,1,2),List("11425", "11425_007_7120", "11425_005_5126", "11425_003_14298"),List(),2.0),
        Row(java.time.LocalDate.of(2015,1,3),List("11425", "11425_007_7120", "11425_005_5126", "11425_003_14298"),List(),2.0),
        Row(java.time.LocalDate.of(2015,1,4),List("11425", "11425_007_7120", "11425_005_5126", "11425_003_14298"),List(),2.0)))

      val rowsForNewMetric2=Future(List(
        Row(java.time.LocalDate.of(2015,1,2),List("11425", "11425_007_7120", "11425_005_5127", "11425_003_14298"),List(),2.0),
        Row(java.time.LocalDate.of(2015,1,3),List("11425", "11425_007_7120", "11425_005_5127", "11425_003_14298"),List(),2.0),
        Row(java.time.LocalDate.of(2015,1,4),List("11425", "11425_007_7120", "11425_005_5127", "11425_003_14298"),List(),2.0)))


      whenReady (Engine.addMetricToRow(row,rowsForNewMetric1)) { result =>
        assert(result.contains(Row(java.time.LocalDate.of(2015,1,2), List("11425", "11425_007_7120", "11425_005_5126", "11425_003_14298"), List(2.0, 1.0), 3.0)))
      }

      whenReady (Engine.addMetricToRow(row,rowsForNewMetric2)) { result =>
        assert(result.isEmpty)
      }
    }

}
