package library

import breeze.numerics.sqrt
import org.apache.spark._
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionModel, LinearRegressionWithSGD}
import org.apache.spark.mllib.tree.RandomForest
import org.apache.spark.mllib.tree.model.RandomForestModel
import org.apache.spark.rdd.RDD

object MachineLearning {

  def scaleFeatures(data: RDD[LabeledPoint]): RDD[LabeledPoint] = {
    val scaler = {
      new StandardScaler(withMean = true, withStd = true).fit(data.map(x => x.features))
    }
    data.map(x => LabeledPoint(x.label, scaler.transform(x.features)))
    //data.map(x => LabeledPoint(x.label, x.features))
  }

  def runLinearRegression(data: RDD[LabeledPoint], sc: SparkContext): String = {
    val splits = data.randomSplit(Array(0.8, 0.2))
    val training: RDD[LabeledPoint] = splits(0)
    val test: RDD[LabeledPoint] = splits(1)
    val model: LinearRegressionModel = new LinearRegressionWithSGD().
      setIntercept(true).
      run(training)
    val valuesAndPreds = test.map { point =>
      val prediction = model.predict(point.features)
      println((point.label, prediction))
      (point.label, prediction)
    }
    val MSE = valuesAndPreds.map { case (v, p) => math.pow(v - p, 2) }.mean()
    sc.stop()
    print(MSE)
    MSE.toString
  }

  def runRandomForestRegression(data: RDD[LabeledPoint], sc: SparkContext): String = {
    val splits = data.randomSplit(Array(0.8, 0.2))
    val (training, test) = (splits(0), splits(1))

    val categoricalFeaturesInfo = Map[Int, Int]()
    val numTrees = 100 // Use more in practice.
    val featureSubsetStrategy = "sqrt" // Let the algorithm choose.
    val impurity = "variance"
    val maxDepth = 20
    val maxBins = 4

    val model: RandomForestModel = RandomForest.trainRegressor(training, categoricalFeaturesInfo, numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)


    val labelsAndPredictionsOnTest: RDD[(Double, Double)] = test.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }
    val labelsAndPredictionsOnTraining: RDD[(Double, Double)] = training.map { point =>

      val prediction = model.predict(point.features)
      (point.label, prediction)
    }

    val mseOnTraining = sqrt(labelsAndPredictionsOnTraining.map {  case (v, p) => math.pow(v - p, 2) }.mean())
    val mseOnTest = sqrt(labelsAndPredictionsOnTest.map { case (v, p) => math.pow(v - p, 2) }.mean())

    val labelsAndPredictionsOnTrainingString = labelsAndPredictionsOnTraining.map(tuple=> tuple._1.toString+" ,"+tuple._2.toString+'\n').reduce(_+_)
    val labelsAndPredictionsOnTestString = labelsAndPredictionsOnTest.map(tuple=> tuple._1.toString+" ,"+tuple._2.toString+'\n').reduce(_+_)
    //println("Learned regression forest model:\n" + model.toDebugString)

    labelsAndPredictionsOnTestString+mseOnTraining.toString +'\n' +mseOnTest

  }

}
